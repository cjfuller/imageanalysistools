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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * This class is a Filter that removes regions in one mask that do not overlap with regions in a second mask.
 * <p>
 * The reference image for this filter should be set to the mask that will not be modified.
 * <p>
 * The argument to the apply method
 * should be the mask that will be modified.  Any regions in the Image argument to the apply method that do not overlap with regions in the reference image
 * will be removed from this Image.
 *
 * @author Colin J. Fuller
 *
 */
public class SeedFilter extends Filter {

	/**
     * Applies the filter, keeping only the regions from the supplied Image argument that overlap with regions in the reference Image.
     * @param im    The Image to process; regions will be removed from this Image that have no overlap with regions in the reference Image.
     */
	@Override
	public void apply(WritableImage im) {
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
