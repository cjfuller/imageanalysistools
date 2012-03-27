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
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math3.linear.ArrayRealVector;
/**
 * A Filter that thresholds an Image using Otsu's method.  (Otsu, 1979, DOI: 10.1109/TSMC.1979.4310076).
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image to be thresholded.  After filtering, this Image will have its original values
 * anywhere where it is above the threshold, and zero values overwritten anywhere where the original values were below the threshold.
 *
 * @author Colin J. Fuller
 *
 *
 */
public class MaximumSeparabilityThresholdingFilter extends Filter {

	final int defaultIncrement = 1;
	final boolean defaultAdaptive = false;


    /**
     * Applies the filter to an Image.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     */
	@Override
	public void apply(Image im) {
		apply_ext(im, defaultAdaptive, defaultIncrement);
	}

    /**
     * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold.
     * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
     * for images with a large dynamic range.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
     */
	public void apply_ext(Image im, boolean adaptiveincrement) {
		apply_ext(im, adaptiveincrement, defaultIncrement);
	}

    /**
     * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold, and specifying a size for the threshold determination increment.
     * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
     * for images with a large dynamic range.
     * <p>
     * The increment size specified (in greylevels) will be used to determine the threshold only if adaptive determination is off; otherwise this parameter will be ignored.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
     * @param increment             the increment size (in greylevels) to use for determining the threshold; must be positive.
     */
	public void apply_ext(Image im, boolean adaptiveincrement, int increment) {
		
		Histogram h = new Histogram(im);
				
		int thresholdValue = 0;
		
		final int numSteps = 1000;
		
		double best_eta = 0;
		int best_index = Integer.MAX_VALUE;
		
		int nonzerocounts = h.getTotalCounts() - h.getCounts(0);
		
		double meannonzero = h.getMeanNonzero();
				
		ArrayRealVector omega_v = new ArrayRealVector(h.getMaxValue());
		ArrayRealVector mu_v = new ArrayRealVector(h.getMaxValue());
		
		int c = 0;
		
		if (adaptiveincrement) {
			increment = (int) ((h.getMaxValue() - h.getMinValue() + 1)*1.0/numSteps);
			if (increment < 1) increment = 1;
		}
		

		for (int k= h.getMinValue(); k < h.getMaxValue() + 1; k+= increment) {
			
			if (k==0) continue;
			
			omega_v.setEntry(c, h.getCumulativeCounts(k) * 1.0/nonzerocounts);
			
			if (c == 0) {
				mu_v.setEntry(c, k*omega_v.getEntry(c));
			} else {
				
				mu_v.setEntry(c, mu_v.getEntry(c-1) + k*h.getCounts(k) *1.0/nonzerocounts);
				for (int i = k-increment + 1; i < k; i++) {
					mu_v.setEntry(c, mu_v.getEntry(c)+h.getCounts(i)*i*1.0/nonzerocounts);
				}
								
			}
			
			double omega = omega_v.getEntry(c);
			double mu = mu_v.getEntry(c);
			
			if (omega > 1e-8 && 1-omega > 1e-8) {
				
				double eta = omega*(1-omega) * Math.pow((meannonzero - mu)/(1-omega) - mu/omega, 2);
				
				if (eta >= best_eta) {
					best_eta = eta;
					best_index = k;
				}
				
				//System.out.printf("%d, %f\n", k, eta);
				
			}
			
			c++;
			
		}
				
		thresholdValue = best_index;
				
		if (thresholdValue == Integer.MAX_VALUE) {
			thresholdValue = 0;
		}
		for (ImageCoordinate coord : im) {
			if (im.getValue(coord) < thresholdValue) im.setValue(coord, 0);
		}
		
	}
	

}
