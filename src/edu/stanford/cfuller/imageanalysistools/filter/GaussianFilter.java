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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that applies a gaussian blur to a 2D Image.
 * <p>
 * This Filter does not use a reference Image.
 *<p>
 * The argument passed to the apply method is the Image to be blurred.
 * 
 *@author Colin J. Fuller
 *
 */


public class GaussianFilter extends Filter {

	//TODO: reimplement using ConvolutionFilter to make use of FFT implementation.
	
	//TODO: deal with more than 2 dimensional blur. (Or make that be the job of other filters and rename this one -2D?)
	
	int width;
   // boolean precalculatedFFT;
    //Complex[] kernelFFT;

    /**
     * Constructs a GaussianFilter with a default size blur.
     */
	public GaussianFilter() {
		this.width = 5;
       // this.precalculatedFFT = false;
        //this.kernelFFT = null;
	}

    /**
     * Applies the GaussianFilter to the specified Image, blurring it by convolution with a Gaussian function.
     * @param im    The Image to process, which will be blurred.
     */
	@Override
	public void apply(Image im) {

		int kernelSize = this.width;


        final int halfKernelSizeCutoff = 8;
        int halfKernelSize = (kernelSize - 1)/2;


        if (halfKernelSize > halfKernelSizeCutoff) {
            GaussianFilter gf2 = new GaussianFilter();
            gf2.setWidth(halfKernelSize);
            gf2.apply(im);
            gf2.apply(im);
            return;
        }


        double powOfTwo = Math.pow(2.0, kernelSize - 1);
		
		double[] coeffs = new double[kernelSize];

        double coeffsSum = 0;

		for (int i = 0; i < coeffs.length; i++) {
			coeffs[i] = org.apache.commons.math.util.MathUtils.binomialCoefficientDouble(kernelSize-1, i)/powOfTwo;
            coeffsSum+=coeffs[i];
        }


        double[] kernelCoeffs = new double[kernelSize*kernelSize];

        int kernelCounter = 0;

        for (int i =0; i < coeffs.length; i++) {
            for (int j =0; j < coeffs.length; j++) {
                kernelCoeffs[kernelCounter++] = (float) (coeffs[i]*coeffs[j]);
            }
        }


		Image intermediate = new Image(im);
		
		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		
		for (ImageCoordinate i : intermediate) {
			double sum = 0;
            double partialCoeffSum = 0;

			for (int offset = -1*halfKernelSize; offset <= halfKernelSize; offset++) {
				double imValue = 0;
				ic.set(ImageCoordinate.X,i.get(ImageCoordinate.X));
				ic.set(ImageCoordinate.Y,i.get(ImageCoordinate.Y) + offset);
				
				if (im.inBounds(ic)) {
                    partialCoeffSum += coeffs[halfKernelSize+offset];
					imValue = im.getValue(ic);
				}
				
				
				sum+= imValue * coeffs[halfKernelSize + offset];
				
			}
			intermediate.setValue(i, sum/(partialCoeffSum/coeffsSum));
		}
		
		for (ImageCoordinate i : intermediate) {
			double sum = 0;
            double partialCoeffSum = 0;

			for (int offset = -1*halfKernelSize; offset <= halfKernelSize; offset++) {
				double imValue = 0;
				ic.set(ImageCoordinate.X,i.get(ImageCoordinate.X)+offset);
				ic.set(ImageCoordinate.Y,i.get(ImageCoordinate.Y));
				
				if (intermediate.inBounds(ic)) {
                    partialCoeffSum += coeffs[halfKernelSize+offset];
					imValue = intermediate.getValue(ic);
				}
				
				
				sum+= imValue * coeffs[halfKernelSize + offset];
				
			}
			im.setValue(i, sum/(partialCoeffSum/coeffsSum));
		}
		
		ic.recycle();
	
	}

    /**
     * Sets the width of the Gaussian filter.  This is the standard deviation of the Gaussian function in units of pixels.
     * @param width     The width of the Gaussian to be used for filtering, in pixels.
     */
	public void setWidth(int width) {
       
		this.width = width;
	}

	
//	private double gaussPDF(double r, double mean, double stddev) {
//		return (1.0/(stddev*Math.sqrt(2*Math.PI))*Math.exp(-Math.pow(r - mean,2.0)/(2.0*stddev*stddev)));
//	}
//	
	
	
}
