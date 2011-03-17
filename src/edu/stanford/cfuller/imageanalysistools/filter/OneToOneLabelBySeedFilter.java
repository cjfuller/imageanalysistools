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
 * A Filter that labels regions in a mask according to the labels in a second (seed) mask.
 * <p>
 * Each distinct region in the input mask will be assigned
 * the label of any seed region that has any pixel overlap with the region in the input mask.  If multiple seed regions
 * overlap with a single region in the input mask, all the pixels the region in the input mask will be assigned to the same one of those seed values
 * (but it is unspecified which one), except for any pixels overlapping directly with a different seed region, which will always
 * be assigned the same label as the seed region.
 * <p>
 * Any regions in the input mask that do not overlap with a seed region will not be changed.  This could potentially lead to
 * duplicate labeling, so it is a good idea to either use a segmentation method that guarantees that every region has a seed,
 * or to first apply a {@link SeedFilter}.
 * <p>
 * The reference Image should be set to the seed mask (this will not be modified by the Filter).
 * <p>
 * The argument to the apply method should be set to the mask that is to be labeled according to the labels in the seed Image.
 *
 * @author Colin J. Fuller
 *
 *
 */

public class OneToOneLabelBySeedFilter extends Filter {


    /**
     * Applies the Filter to the specified Image mask, relabeling it according to the seed regions in the reference Image.
     * @param im    The Image mask to process, whose regions will be relabeled.
     */
	@Override
	public void apply(Image im) {
		java.util.HashMap<Integer, Integer> hasSeedSet = new java.util.HashMap<Integer, Integer>();
		java.util.HashSet<Integer> seedIsMapped = new java.util.HashSet<Integer>();
		
		
		for (ImageCoordinate c : im) {
			
			int currValue = (int) im.getValue(c);
			int seedValue = (int) this.referenceImage.getValue(c);
			
			if (seedValue > 0 && currValue > 0) {
				hasSeedSet.put(currValue, seedValue);
			}
			
		}
		
		for (Integer i : hasSeedSet.values()) {
			seedIsMapped.add(i);
		}
		
		for (ImageCoordinate c : im) {
			
			int currValue = (int) im.getValue(c);
			
			if (hasSeedSet.containsKey(currValue) && currValue > 0) {
				
				im.setValue(c, hasSeedSet.get(currValue));
			}
			
			if (this.referenceImage.getValue(c) > 0 && seedIsMapped.contains((int) this.referenceImage.getValue(c))) {
				
				im.setValue(c, this.referenceImage.getValue(c));
				
			}
			
		}
		

	}

}
