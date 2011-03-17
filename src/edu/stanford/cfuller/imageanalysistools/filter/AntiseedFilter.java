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
 * This class is a Filter that removes regions in one mask that overlap with regions in a second mask.
 * <p>
 * This may be particularly useful for seeded segmentation methods, where this Filter can be used to remove regions from the segmented Image
 * that overlap with regions in the seed mask.
 * <p>
 * The reference image for this filter should be set to the mask that will not be modified.
 * <p>
 * The argument to the apply method
 * should be the mask that will be modified.  Any regions in the Image argument to the apply method that overlap with regions in the reference image
 * will be removed from the Image that is the argument to the apply method.
 *
 * @author Colin J. Fuller
 *
 */

public class AntiseedFilter extends Filter {

    /**
     * Applies the filter, removing any regions from the supplies Image argument that overlap with regions in the reference Image.
     * @param im    The Image to process; regions will be removed from this Image that have any overlap with regions in the reference Image.
     */
	@Override
	public void apply(Image im) {
		java.util.HashSet<Integer> hasSeedSet = new java.util.HashSet<Integer>();
				
		for (ImageCoordinate i : im) {
			int currValue = (int) im.getValue(i);
			int seedValue = (int) this.referenceImage.getValue(i);
			
			if (seedValue > 0) {
				hasSeedSet.add(currValue);
			}
		}
		
		for (ImageCoordinate i : im) {
			int currValue = (int) im.getValue(i);
				
			if (!hasSeedSet.contains(currValue)) {
				im.setValue(i,0);
			}
		}
		
	}

}
