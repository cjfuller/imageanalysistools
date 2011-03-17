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
