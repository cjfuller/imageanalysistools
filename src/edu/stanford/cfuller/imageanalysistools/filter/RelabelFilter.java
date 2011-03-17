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
 * A Filter that relabels an Image mask to use a consecutive numbering.
 * <p>
 * Each region in the original mask (regions defined solely upon the labeling in the original mask)
 * will be mapped to a unique value, but these values are not guaranteed to be in the same numerical
 * order as in the original mask.  Region labels in the output mask will begin at 1 and end at the number of regions.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the mask to be relabeled.
 *
 * @author Colin J. Fuller
 */
public class RelabelFilter extends Filter {

	@Override
	public void apply(Image im) {

		int maxVal = edu.stanford.cfuller.imageanalysistools.image.Histogram.findMaxVal(im);
		
		int[] newlabels = new int[maxVal+1];
		
		java.util.Arrays.fill(newlabels, -1);
		
		int labelctr = 1;
		
		for (ImageCoordinate i : im) {
			
			if (im.getValue(i) > 0) {
				if (newlabels[(int) im.getValue(i)] == -1) {
					newlabels[(int) im.getValue(i)] = labelctr;
					im.setValue(i, labelctr++);
					
				} else {
					im.setValue(i, newlabels[(int) im.getValue(i)]);
				}
			}
			
		}
		
		
	}

}
