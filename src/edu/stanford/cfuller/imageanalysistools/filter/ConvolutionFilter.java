/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * Convolves a 2D x-y Image with a 2D kernel.
 * <p>
 * Before calling apply, the kernel must be specified using the {@link #setKernel(Kernel)} method.
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * After calling the apply method, the image argument to apply will be overwritten by the convolved Image.
 * 
 * @author Colin J. Fuller
 */
public class ConvolutionFilter extends Filter {

	//TODO: deal with boundary conditions of types other than zero.
	
	//TODO: deal with something other than single image plane transforms.
	
	Kernel k;
	
	Complex[][] transformStorage;
	
	@Override
	public void apply(Image im) {
		
		Complex[][] transformed = null;
		
		if (this.transformStorage != null) {
			transformed = new Complex[this.transformStorage.length][this.transformStorage[0].length];
			for (int i =0; i < transformed.length; i++) {
				for (int j = 0; j < transformed.length; j++) {
					transformed[i][j] = this.transformStorage[i][j];
				}
			}
			
		} else {
			transformed = ConvolutionFilter.transform(im, 0);

		}
				
		Complex[][] kernelTransform = this.k.getTransformed2DKernel(transformed[0].length, transformed.length);
				
		for (int i = 0; i < transformed.length; i++) {
			for (int j = 0; j < transformed.length; j++) {
				transformed[i][j] = transformed[i][j].multiply(kernelTransform[i][j]);
			}
		}
		
		inverseTransform(im,0,transformed);
		
	}
	
	public void setTransform(Complex[][] transformed) {
		this.transformStorage = transformed;
	}
	
	public void setKernel(Kernel k) {
		this.k = k;
	}
	
	
	public static Complex[][] transform(Image im, int z) {

        FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();

        int ydimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.Y);
        int xdimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.X);

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.X)) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.Y))/Math.log(2)));
        }

        //for (int p =0; p < im.getPlaneCount(); p++) {

        int p = z;
        
            im.selectPlane(p);

            double[][] rowImage = new double[ydimPowOfTwo][xdimPowOfTwo];
            for (int i =0; i < ydimPowOfTwo; i++) {
                java.util.Arrays.fill(rowImage[i], 0); // ensures zero-padding
            }
            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];

            for (ImageCoordinate ic : im) {
                rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] = im.getValue(ic);
            }

            for (int r = 0; r < rowImage.length; r++) {
                double[] row = rowImage[r];
                Complex[] transformedRow = fft.transform(row);

                for (int c = 0; c < colMajorImage.length; c++) {
                    colMajorImage[c][r] = transformedRow[c];
                }
            }

            for (int c = 0; c < colMajorImage.length; c++) {
                colMajorImage[c] = fft.transform(colMajorImage[c]);
            }
            return colMajorImage;
        //}
	}
	
	public void inverseTransform(Image orig, int z, Complex[][] transformed) {
		
		Complex[][] colMajorImage = transformed;
		
		double[][] rowImage = new double[colMajorImage[0].length][colMajorImage.length];
		
        FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();


        for (int c = 0; c < colMajorImage.length; c++) {
            colMajorImage[c] = fft.inversetransform(colMajorImage[c]);
        }

        Complex[] tempRow = new Complex[rowImage.length];

        //also calculate min/max values
        double newMin = Double.MAX_VALUE;
        double newMax = 0;

        for (int r = 0; r < rowImage.length; r++) {

            for (int c = 0; c < colMajorImage.length; c++) {
                tempRow[c] = colMajorImage[c][r];
            }

            Complex[] transformedRow = fft.inversetransform(tempRow);

            for (int c = 0; c < colMajorImage.length; c++) {
                rowImage[r][c] = transformedRow[c].abs();
                if (rowImage[r][c] < newMin) newMin = rowImage[r][c];
                if (rowImage[r][c] > newMax) newMax = rowImage[r][c];
            }
        }

        //rescale values to same min/max as before

        Histogram h = new Histogram(orig);

        double oldMin = h.getMinValue();
        double oldMax = h.getMaxValue();

        double scaleFactor = (oldMax - oldMin)/(newMax - newMin);

        ImageCoordinate minCoord = ImageCoordinate.createCoordXYZCT(0, 0, z, 0, 0);
        ImageCoordinate maxCoord = ImageCoordinate.cloneCoord(orig.getDimensionSizes());
        maxCoord.set(ImageCoordinate.Z,z+1);
        
        orig.setBoxOfInterest(minCoord, maxCoord);
        
        scaleFactor = 1;
        oldMin  = 0;

        for (ImageCoordinate ic : orig) {
            orig.setValue(ic, (float) ((rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] - newMin)*scaleFactor + oldMin));
        }
        
        orig.clearBoxOfInterest();
        
        minCoord.recycle();
        maxCoord.recycle();


    
	}

}
