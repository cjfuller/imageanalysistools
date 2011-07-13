/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * @author Colin J. Fuller
 */
public class ConvolutionFilter extends Filter {

	//TODO: deal with boundary conditions of types other than zero.
	
	Kernel k;
	
	Complex[][] transformStorage;
	
	@Override
	public void apply(Image im) {
//		Image original = new Image(im);
//		Image boxed = new Image(im);
//		
//		ImageCoordinate boxMin = ImageCoordinate.createCoord(0,0,0,0,0);
//		ImageCoordinate boxMax = ImageCoordinate.createCoord(0,0,0,0,0);
//		
//		ImageCoordinate halfKernelSize = k.getHalfSize(); //do not recycle
//				
//		for (ImageCoordinate ic : original) {
//			
//			for (int i = 0; i < boxMin.getDimensionality(); i++) {
//				boxMin.set(i, ic.get(i) - halfKernelSize.get(i));
//				boxMax.set(i, ic.get(i) + halfKernelSize.get(i)+1);
//			}
//			
//			boxed.setBoxOfInterest(boxMin, boxMax);
//			
//			double tempValue = 0;
//			
//			for (ImageCoordinate icBoxed : boxed) {
//				tempValue += k.getWeight(ic, icBoxed)*boxed.getValue(icBoxed);
//			}
//			
//			boxed.clearBoxOfInterest();
//						
//			im.setValue(ic, tempValue);
//			
//		}
//		
//		boxMin.recycle();
//		boxMax.recycle();
		
		
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
		
		
		//Complex[][] kernel = Kernel.getTransformedRandomSinglePlaneKernelMatrix(transformed[0].length, transformed.length, 5);
		
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

        int ydimPowOfTwo = im.getDimensionSizes().getY();
        int xdimPowOfTwo = im.getDimensionSizes().getX();

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().getX()) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().getY())/Math.log(2)));
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
                rowImage[ic.getY()][ic.getX()] = im.getValue(ic);
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

        ImageCoordinate minCoord = ImageCoordinate.createCoord(0, 0, z, 0, 0);
        ImageCoordinate maxCoord = ImageCoordinate.cloneCoord(orig.getDimensionSizes());
        maxCoord.setZ(z+1);
        
        orig.setBoxOfInterest(minCoord, maxCoord);
        
        scaleFactor = 1;
        oldMin  = 0;

        for (ImageCoordinate ic : orig) {
            orig.setValue(ic, (rowImage[ic.getY()][ic.getX()] - newMin)*scaleFactor + oldMin);
        }
        
        orig.clearBoxOfInterest();
        
        minCoord.recycle();
        maxCoord.recycle();


    
	}

}
