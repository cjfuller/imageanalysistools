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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;


/**
 * A thresholding method based upon finding the full-width half-max of the histogram of the gradient of the Image.
 * 
 * @author Colin J. Fuller
 *
 */
public class GradientHistogramThresholdingFilter extends Filter {

	@Override
	public void apply(WritableImage im) {

		GaussianFilter GF = new GaussianFilter();
		GF.setWidth(21);
		GF.apply(im);
		
		WritableImage gradient = ImageFactory.createWritable(im);
		
		GradientFilter GRF = new GradientFilter();
		GRF.apply(gradient);
		
		WritableImage smoothedThresholded = ImageFactory.createWritable(im);
		
		this.absoluteThreshold(im, 10);
		
		MaskFilter MF = new MaskFilter();
		
		MF.setReferenceImage(smoothedThresholded);
		
		MF.apply(gradient);
		MF.setReferenceImage(gradient);
		MF.apply(im);
		
		this.histogramThreshold(im, 45);
		
	}
	
	private void absoluteThreshold(WritableImage im, int level) {
		for (ImageCoordinate c : im) {
			if (im.getValue(c) < level) {
				im.setValue(c, 0);
				
			}
		}
	}
	
	private void histogramThreshold(WritableImage im, double stddev) {
		
		Histogram im_hist = new Histogram(im);
		
		int mode = im_hist.getMode();
		int modeVal = im_hist.getCountsAtMode();
		
		double halfModeVal = modeVal / 2.0;
		
		int hw_first = 0;
		
		for (int i = 1; i < im_hist.getMaxValue(); i++) {
			if (im_hist.getCounts(i) > halfModeVal) {
				hw_first = i;
				break;

			}
		}
		
		int hwhm = mode - hw_first;
		
		int threshLevel = (int) (mode + stddev *hwhm);
		
		this.absoluteThreshold(im, threshLevel + 1);
		
	}
	
	

}
