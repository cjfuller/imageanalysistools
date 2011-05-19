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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that applies a gaussian blur to an Image.
 * <p>
 * This Filter does not use a reference Image.
 *<p>
 * The argument passed to the apply method is the Image to be blurred.
 * 
 *@author Colin J. Fuller
 *
 */


public class GaussianFilter extends Filter {

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
		
//		if (stdDev < 12) {
//			kernelSize = 3*stdDev + 1- (stdDev%2);
//		} else {
//			kernelSize = 2*stdDev - 1;
//		}
//
//        if (kernelSize % 2 == 0) {
//            kernelSize += 1;
//        }

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
/*
        FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();

        if (!this.precalculatedFFT) {
            this.kernelFFT = fft.transform(kernelCoeffs);
        }

        //X-pass filtering

        double[][] rowImage = new double[im.getDimensionSizes().getY()][im.getDimensionSizes().getX()];
        Complex[][] colMajorImage = new Complex[im.getDimensionSizes().getX()][im.getDimensionSizes().getY()];

        for (ImageCoordinate ic : im) {
            rowImage[ic.getY()][ic.getX()] = im.getValue(ic);
        }

        ImageCoordinate boxMin = ImageCoordinate.createCoord(0,0,0,0,0);
        ImageCoordinate boxMax = ImageCoordinate.createCoord(im.getDimensionSizes().getX(), 1,1,1,1);

        for (int r = 0; r < rowImage.length; r++) {
            double[] row = rowImage[r];
            Complex[] transformedRow = fft.transform()
        }
*/
        /*
        Kernel k = new Kernel(kernelSize, kernelSize, kernelCoeffs);

        BufferedImage bufferedImage = im.toBufferedImage();

        ConvolveOp co = new ConvolveOp(k);

        BufferedImage out=  co.createCompatibleDestImage(bufferedImage, null);

        co.filter(bufferedImage, out);

        WritableRaster outRaster = out.getRaster();

        for (ImageCoordinate ic : im) {
            im.setValue(ic, outRaster.getSample(ic.getX(), ic.getY(), 0));
        }
        */

		Image intermediate = new Image(im);
		
		ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
		
		for (ImageCoordinate i : intermediate) {
			double sum = 0;
            double partialCoeffSum = 0;

			for (int offset = -1*halfKernelSize; offset <= halfKernelSize; offset++) {
				double imValue = 0;
				ic.setX(i.getX());
				ic.setY(i.getY() + offset);
				
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
				ic.setX(i.getX()+offset);
				ic.setY(i.getY());
				
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
        //if (this.width != width) {
            //this.precalculatedFFT = false;
            //this.kernelFFT = null;
        //}
		this.width = width;
	}

	
//	private double gaussPDF(double r, double mean, double stddev) {
//		return (1.0/(stddev*Math.sqrt(2*Math.PI))*Math.exp(-Math.pow(r - mean,2.0)/(2.0*stddev*stddev)));
//	}
//	
	
	
}
