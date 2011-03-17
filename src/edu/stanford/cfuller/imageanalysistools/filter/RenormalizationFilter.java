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
import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimationFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GradientFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;


/**
 * A Filter that normalizes an Image by local intensity so that features that occur in a dim region of an image will be
 * approximately the same brightness as features in a bright part of an Image.
 * <p>
 * This may be useful, for instance, if multiple cells in an Image both have fluorescence signal that needs to be segmented
 * but are expressing a protein being localized to vastly different levels, which might otherwise cause the lower-expressing cell
 * to be removed from the Image by the segmentation routine.  This is also useful for ensuring that objects of differing brightness
 * are not segmented to be radically different sizes by a segmentation method.
 * <p>
 * The general approach uses median filtering to estimate the local intensity in an Image, and then uses a gradient filter
 * to remove artificial edges due to the size of the median filter.
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image that is to be intensity normalized.  This Image will be overwritten
 * by the normalized version.
 *
 * @author Colin J. Fuller
 */
public class RenormalizationFilter extends Filter {

    /**
     * Applies the filter to an Image, normalizing its intensity.
     * @param im    The Image to be normalized; will be overwritten.
     */
	@Override
	public void apply(Image im) {

		this.referenceImage = new Image(im);
		
		Image input = this.referenceImage;
		Image output = im;

		LocalBackgroundEstimationFilter LBEF = new LocalBackgroundEstimationFilter();
		
		LBEF.setParameters(this.params);
		LBEF.setReferenceImage(input);
		LBEF.setBoxSize((int) Math.ceil(0.5*Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size")))));
		LBEF.apply(output);
		
		Image gradient = new Image(output);

		GradientFilter GF = new GradientFilter();
		
		GF.apply(gradient);
		GF.apply(gradient);
		
		GaussianFilter GAUF = new GaussianFilter();
		
		GAUF.setWidth((int) (2*Math.ceil(Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size"))))));

		GAUF.apply(gradient);
		
		Image tempStorage = new Image(input.getDimensionSizes(), 0.0);
		
		double maxValue = 0.0;
		double minValue = Double.MAX_VALUE;
		
		Histogram h = new Histogram(input);
		
		for (ImageCoordinate i : output) {
			
			double denom = output.getValue(i) + gradient.getValue(i);
			denom = (denom < 1) ? 1 : denom;
			
			if (input.getValue(i)/denom > 0) {
				tempStorage.setValue(i, Math.log(input.getValue(i)/denom));
			} 
			
			if (tempStorage.getValue(i) > maxValue) maxValue = tempStorage.getValue(i);
			if (tempStorage.getValue(i) < minValue) minValue = tempStorage.getValue(i);
			
		}
		
		double sumValue = 0;
		
		for (ImageCoordinate i : output) {
			
			double tempValue = tempStorage.getValue(i);
			
			tempValue = (tempValue - minValue)/(maxValue - minValue) * h.getMaxValue();
			
			if (maxValue == minValue || tempValue < 0) tempValue = 0;
			
			sumValue += tempValue;
			
			output.setValue(i, Math.floor(tempValue));
			
		}
		
		sumValue /= (output.getDimensionSizes().getX() * output.getDimensionSizes().getY());
		
		for (ImageCoordinate i : output) {
			double tempValue = output.getValue(i) - sumValue;
			if (tempValue < 0) tempValue = 0;
			output.setValue(i, tempValue);
		}
		
	}

}
