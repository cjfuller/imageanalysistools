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
