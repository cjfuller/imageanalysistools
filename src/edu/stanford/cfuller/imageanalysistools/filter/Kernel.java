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

import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * @author Colin J. Fuller
 *
 */
public class Kernel {
	
	//TODO: implementation of nonzero boundary conditions
	
	ImageCoordinate halfKernelDimensions;
	
	double[] weights;
	
	Complex[][] transform = null;
	
	int boundaryType;
	
	public final static int BOUNDARY_ZERO = 0;
	public final static int BOUNDARY_REPEAT = 1;
	public final static int BOUNDARY_CIRCULAR = 2;
	
	protected Kernel() {}
	
	/**
	 * Creates a new Kernel object initialized with the specified weights and the specified dimension sizes.
	 * <p>
	 * The weights should be ordered in a 1-d array such that the ith dimension 
	 * of the ImageCoordinate is minor compared to the (i+1)th dimension.  (For example, for a 5d ImageCoordinate, with
	 * a zeroth dimension of size n, the zeroth entry is (0,0,0,0,0), the first (1,0,0,0,0), the nth (0,1,0,0,0), etc.
	 * <p>
	 * 
	 * @param weights			a 1-D matrix of the weights in the kernel for each pixel surrounding a given pixel, whose self-weight is at the midpoint of each dimension.
	 * @param dimensionSizes	and ImageCoordinate containing the full size of each dimension in the kernel.  All entries must be odd.
	 * @throws IllegalArgumentException 	if the dimensionSizes are not odd, or are negative.
	 */
	public Kernel(double[] weights, ImageCoordinate dimensionSizes) {
				
		for (Integer size : dimensionSizes) {
			if (size % 2 == 0 || size < 0) {
				throw new IllegalArgumentException("Kernel size must be odd and positive in all dimensions.");
			}
		}
		
		
		this.weights = weights;
		
		this.halfKernelDimensions = ImageCoordinate.createCoord(0,0,0,0,0);
		
		
		for (int i =0; i < this.halfKernelDimensions.getDimension(); i++) {
			this.halfKernelDimensions.set(i, (dimensionSizes.get(i)-1 )/2);
		}
		
		
		this.boundaryType = BOUNDARY_ZERO;
		
		
	}
	
	public void formatTransformFrom1DInput(int size0, int size1) {
		
        int ydimPowOfTwo = size0;
        int xdimPowOfTwo = size1;

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size1) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size0)/Math.log(2)));
        }
		
        Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];

        int counter = 0;
        
        for (int i = 0; i < colMajorImage.length; i++) {
        	for (int j = 0; j < colMajorImage[0].length; j++) {
        	
        		colMajorImage[i][j] = new Complex(this.weights[counter++], this.weights[counter++]);
        		
        	}
        		
        }
        
        this.transform = colMajorImage;
	}
	
	/**
	 * Gets the weight of a pixel relative to another pixel, given the weights spcified on creation of the kernel.
	 * @param currentPixel		The central pixel, relative to which the weight is being computed.
	 * @param relativePixel		The pixel whose weight relative to the central pixel is being computed.  (Note that
	 * 							this should be an absolute coordinate, and not a coordinate relative to the currentPixel.)
	 * @return					The weight of the relativePixel, relative to the currentPixel.
	 */
	public double getWeight(ImageCoordinate currentPixel, ImageCoordinate relativePixel) {
		
		int index = 0;
		int accumulatedOffset = 1;
						
		for (int i =0; i < currentPixel.getDimension(); i++) {
			int temp = relativePixel.get(i) - currentPixel.get(i);
			if (temp < -1* halfKernelDimensions.get(i) || temp > halfKernelDimensions.get(i)) {
				return 0;
			}
			int tempOffset =  temp + halfKernelDimensions.get(i);
			index += tempOffset*accumulatedOffset;
			accumulatedOffset*=(halfKernelDimensions.get(i)*2 + 1);
		}
				
		if (index >= weights.length || index < 0) return 0;
		return weights[index];
	}
	
	
	/**
	 * Gets the method by which boundary conditions should be dealt with at the edge of images.
	 * @return	The boundary type, which will be one of the public boundary type constants of the Kernel class.
	 */
	public int getBoundaryType() {
		return this.boundaryType;
	}
	
	/**
	 * Gets an ImageCoordinate containing the half size of each dimension of the kernel (that is, each dimension is 2*n+1 pixels, if n is the half size).
	 * @return		an ImageCoordinate containing the half sizes; a reference, not a copy-- do not modify or recycle.
	 */
	public ImageCoordinate getHalfSize() {
		return this.halfKernelDimensions;
	}
	
	protected void finalize() throws Throwable {
		this.halfKernelDimensions.recycle();
	}

	public static Complex[][] getRandomSinglePlaneKernelMatrix(int size0, int size1) {
		
		Complex[][] toReturn = new Complex[size0][size1];
		for (int i = 0; i < size0; i++) {
			for (int j = 0; j < size1; j++) {
				double angle = Math.random()*2-1;
				angle = Math.acos(angle);
				//toReturn[i][j] = new Complex(Math.cos(angle), Math.sin(angle));
				toReturn[i][j]= new Complex(Math.random()*2-1, Math.random()*2-1);
				//toReturn[i][j] = new Complex(1,0);
			}
		}
		
		
		return toReturn;
	}
	
	public Complex[][] getTransformed2DKernel(int size0, int size1) {
		
		if (this.transform != null) {
			return this.transform;
		}
				
		FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();

        int ydimPowOfTwo = size0;
        int xdimPowOfTwo = size1;

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size1) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size0)/Math.log(2)));
        }
		
		double[][] preTransform = new double[ydimPowOfTwo][xdimPowOfTwo];
		
		int counter= 0;
		
		for (int i = 0; i < this.halfKernelDimensions.getY()+1; i++) {
			for (int j =0; j < this.halfKernelDimensions.getX()+1; j++) {
				preTransform[i][j] = this.weights[counter++];
			}
		}
		
		for (int i = this.halfKernelDimensions.getY()+1; i < ydimPowOfTwo - this.halfKernelDimensions.getY(); i++) {
			for (int j = this.halfKernelDimensions.getX()+1; j < xdimPowOfTwo - this.halfKernelDimensions.getX(); j++) {
				preTransform[i][j] = 0;
			}
		}
		
		
		for (int i = ydimPowOfTwo - this.halfKernelDimensions.getY(); i < ydimPowOfTwo; i++) {
			for (int j = xdimPowOfTwo - this.halfKernelDimensions.getX(); j < xdimPowOfTwo; j++) {
				preTransform[i][j] = this.weights[counter++];
			}
		}
		
		double[][] rowImage = preTransform;
		
        
            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];


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
            
            this.transform = colMajorImage;
            
            return colMajorImage;
				
		
	}
	
	public static Complex[][] getTransformedRandomSinglePlaneKernelMatrix(int size0, int size1, int sizeNonzero) {
		
		int halfSize = (sizeNonzero - 1)/2;
		
		FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();


        int ydimPowOfTwo = size0;
        int xdimPowOfTwo = size1;

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size1) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(size0)/Math.log(2)));
        }
		
		double[][] preTransform = new double[ydimPowOfTwo][xdimPowOfTwo];
		
		for (int i = 0; i < halfSize+1; i++) {
			for (int j =0; j < halfSize+1; j++) {
				preTransform[i][j] = Math.random()*2-1;
			}
		}
		
		for (int i = halfSize+1; i < ydimPowOfTwo - halfSize; i++) {
			for (int j = halfSize+1; j < xdimPowOfTwo - halfSize; j++) {
				preTransform[i][j] = 0;
			}
		}
		
		for (int i = ydimPowOfTwo - halfSize; i < ydimPowOfTwo; i++) {
			for (int j = xdimPowOfTwo - halfSize; j < xdimPowOfTwo; j++) {
				preTransform[i][j] = Math.random()*2-1;
			}
		}
		
		double[][] rowImage = preTransform;
		
        
            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];


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
				
		
	}
	
}
