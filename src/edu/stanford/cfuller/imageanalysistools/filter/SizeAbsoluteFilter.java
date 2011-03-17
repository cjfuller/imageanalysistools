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
 * A Filter that removes regions from a mask that are larger or smaller than specified size cutoffs.
 * <p>
 * The size cutoffs are retrieved from the ParameterDictionary used for the analysis, in the parameters "maxSize" and "minSize", in units of number
 * of pixels. If these are not specified some default values will be used.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method is the mask that will have regions of size outside of the specified bounds removed.
 *
 * @author Colin J. Fuller
 *
 */
public class SizeAbsoluteFilter extends Filter {


    /**
     * Applies the SizeAbsoluteFilter, removing regions sized outside the specified range.
     * @param im    The mask whose regions of unusual size will be removed.
     */
	@Override
	public void apply(Image im) {


        Histogram h = new Histogram(im);


        int absCutoffMin = -1;
        int absCutoffMax = -1;

        if (this.params != null) {

            absCutoffMax = this.params.getIntValueForKey("max_size");
            absCutoffMin = this.params.getIntValueForKey("min_size");

        }

        if (absCutoffMin < 0) { // default values
            absCutoffMax = 50;
            absCutoffMin = 5;
        }

        int[] multipliers = new int[h.getMaxValue() + 1];

        for (int i = 0; i < multipliers.length; i++) {

            if (h.getCounts(i) < absCutoffMin || h.getCounts(i) > absCutoffMax) {
                multipliers[i] = 0;
            } else {
                multipliers[i] = 1;
            }
            
        }

        for (ImageCoordinate ic : im) {

            double value = im.getValue(ic);

            im.setValue(ic, value*multipliers[(int) value]);
            
            
        }

        

	}

}
