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
import edu.stanford.cfuller.imageanalysistools.image.Histogram;


/**
 * A thresholding method based upon finding the full-width half-max of the histogram of the gradient of the Image.
 * 
 * @author Colin J. Fuller
 *
 */
public class GradientHistogramThresholdingFilter extends Filter {

	@Override
	public void apply(Image im) {

		GaussianFilter GF = new GaussianFilter();
		GF.setWidth(21);
		GF.apply(im);
		
		Image gradient = new Image(im);
		
		GradientFilter GRF = new GradientFilter();
		GRF.apply(gradient);
		
		Image smoothedThresholded = new Image(im);
		
		this.absoluteThreshold(im, 10);
		
		MaskFilter MF = new MaskFilter();
		
		MF.setReferenceImage(smoothedThresholded);
		
		MF.apply(gradient);
		MF.setReferenceImage(gradient);
		MF.apply(im);
		
		this.histogramThreshold(im, 45);
		
	}
	
	private void absoluteThreshold(Image im, int level) {
		for (ImageCoordinate c : im) {
			if (im.getValue(c) < level) {
				im.setValue(c, 0);
				
			}
		}
	}
	
	private void histogramThreshold(Image im, double stddev) {
		
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
