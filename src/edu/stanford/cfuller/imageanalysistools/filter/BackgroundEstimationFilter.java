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

/**
 * A Filter that estimates the background within each region of an Image, and removes any pixels in each region that are above the
 * estimated background for that region.  This can be used to filter out any residual foreground pixels before
 * using some metric for quantification of the background within each region.
 * <p>
 * The background estimation is done by for each region, computing the mode (integer-valued) intensity within that region, and computing the
 * half-width at half-maximum of the histogram of that region.  Then, any values that are greater than two half-widths above the mode value of the region are discarded.
 * <p>
 * The reference Image for this filter should be set to a regular Image whose background is to be estimated.
 *<p>
 * The Image argument to the apply method should be a mask of regions to be filtered based upon the intensities within each region.  Each region must be labeled individually.
 *
 * @author Colin J. Fuller
 */

public class BackgroundEstimationFilter extends Filter {

	private int backgroundLevel;
	
	public int getBackground() {return backgroundLevel;}


    /**
     * Runs the filter, calculating a background level for each region in the mask passed as the Image parameter and removing any
     * pixels from mask that are above the background level.
     * 
     * @param im    The Image containing the mask of regions to process.  Each region must be labeled individually.
     */
	@Override
	public void apply(Image im) {
		
		// TODO This recapitulates the c++ code, but there's something wrong here (with the c++ as well)
		
		Image maskCopy = new Image(im);
		
		MaskFilter mf = new MaskFilter();
		
		Histogram hMask = new Histogram(im);
				
		for (int k = 1; k < hMask.getMaxValue() + 1; k++) {
			for (ImageCoordinate i : im) {
				if (im.getValue(i) == k) {
					maskCopy.setValue(i, k);
				} else {
					maskCopy.setValue(i,0);
				}
			}
			
			Image imCopy = new Image(this.referenceImage);
			
			mf.setReferenceImage(maskCopy);
			mf.apply(imCopy);
			
			Histogram h = new Histogram(imCopy);
			
			int mode = h.getMode();
			int stddev = 2;
			
			this.backgroundLevel = mode;
			int modeVal = h.getCountsAtMode();
			
			double halfModeVal = modeVal/2.0;
			
			int hw_first = 0;
            int hw_second = 0;

            boolean firstFound = false;
			
			
			for (int i = 1; i < h.getMaxValue(); i++) {

                if (firstFound && h.getCounts(i) < halfModeVal && i > mode) {
                    hw_second = i;
                    break;
                }

				if ((!firstFound) && h.getCounts(i) > halfModeVal) {
					
					hw_first = i;
                    firstFound = true;
				
				}
                
			}
			
			int hwhm = (hw_second - hw_first)/2;

            if (hwhm < 1) hwhm = 1;

			this.backgroundLevel = mode + stddev * hwhm;
			
			//System.out.println("mode: " + mode + " stddev: " + stddev + " threshold: " + this.backgroundLevel);
			
			for (ImageCoordinate i : im) {
				if (imCopy.getValue(i) > this.backgroundLevel){
					im.setValue(i, 0);
				}
			}
			
		}

	}

}
