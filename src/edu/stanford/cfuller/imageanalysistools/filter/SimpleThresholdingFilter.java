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

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that thresholds an Image at a fractional level between the minimum and the maximum value of the Image.
 * <p>
 * The reference Image should be set to the Image to be thresholded.  This Image will not be modified.
 * <p>
 * The argument to the apply method should be set to an Image that will get the output of the thresholding.  This should be the
 * same dimensions as the reference Image.  This Image will be unchanged except that any pixel below the threshold in the reference Image
 * will be set to zero in this Image.
 */

public class SimpleThresholdingFilter extends Filter {

    private double fractionalLevel;

    /**
     * Constructs a SimpleThresholdingFilter that defaults to not thresholding the Image.
     */
    public SimpleThresholdingFilter() {

        this.fractionalLevel = 0;
    }

    /**
     * Constructs a SimpleThresholdingFilter that will threshold an Image at some fractional level of the difference between the minimum
     * and maximum value of the Image.
     * <p>
     * For example, if fractionalLevel is set to 0.1, the threshold will be set at min + 0.1*(max-min).
     * @param fractionalLevel   The fractional level at which to threshold the Image.
     */
    public SimpleThresholdingFilter(double fractionalLevel) {
        this.fractionalLevel = fractionalLevel;
    }


    /**
     * Applies the SimpleThresholdingFilter to an Image.
     * @param im    The Image whose pixels wil be set to zero where the reference Image is below the fractional threshold.
     */
	@Override
	public void apply(Image im) {

        Histogram h = new Histogram(this.referenceImage);

        double cutoff = (h.getMaxValue()-h.getMinValue())*this.fractionalLevel + h.getMinValue();

        for (ImageCoordinate ic : im) {

            if (this.referenceImage.getValue(ic) < cutoff) {
                im.setValue(ic, 0);
            }

        }

	}

    /**
     * Sets a new fractional level for the thresholding of the Image.
     * <p>
     * For example, if fractionalLevel is set to 0.1, the threshold will be set at min + 0.1*(max-min).  Where min and max are the minimum
     * and maximum values of the reference Image.
     * @param level     The fractional level at which to threshold the Image.
     */
    public void setFractionalLevel(double level) {
        this.fractionalLevel = level;
    }

}
