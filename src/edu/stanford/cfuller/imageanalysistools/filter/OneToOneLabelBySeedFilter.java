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
