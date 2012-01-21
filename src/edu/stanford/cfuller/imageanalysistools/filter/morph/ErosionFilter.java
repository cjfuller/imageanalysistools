/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.filter.morph;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter implementing the operation of binary morphological erosion in an arbitrary (i.e. up to 5) number of dimensions.
 * <p>
 * This filter does not take a reference image.
 * <p>
 * The argument to the apply method should be the image to be eroded.  This should have foreground pixels labeled > 0 and
 * background pixels labeled <= 0.  After erosion, all foreground pixels will be set to 1 and all background pixels to 0.
 * 
 * @author Colin J. Fuller
 */
public class ErosionFilter extends MorphologicalFilter {

	/**
	 * Constructs a new DilationFilter.
	 */
	public ErosionFilter(MorphologicalFilter mf) {
		super(mf);
	}
	
	/**
	 * Constructs a new ErosionFilter, copying the structuring element and settings from another
	 * MorphologicalFilter.
	 * @param mf		The MorphologicalFilter whose settings will be copied.
	 */
	public ErosionFilter() {
		super();
	}
	
	private static final int defaultSize = 3;
	
	/**
	 * Creates a default structuring element for processing with this filter.
	 * Currently this is an n by n by...n square structuring element set to all ones in the specified dimensions, where 
	 * n is a default size, currently 3.
	 * @param dimList	A list of dimensions (corresponding to their integer indices in ImageCoordinate) over which the structuring element extends.
	 * @return			A StructuringElement suitable for processing an image over the supplied dimensions.
	 */
	public static StructuringElement getDefaultElement(int[] dimList) {
		ImageCoordinate strelSize = ImageCoordinate.createCoordXYZCT(1,1,1,1,1);
		for (int i : dimList) {
			strelSize.set(i, defaultSize);
		}
		
		StructuringElement toReturn = new StructuringElement(strelSize);
		
		toReturn.setAll(1.0f);
		
		strelSize.recycle();
		
		return toReturn;
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.morph.MorphologicalFilter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {
		
		if (this.strel == null) return;
		
		Image origCopy = new Image(im);
		
		for (ImageCoordinate ic : im) {
			
			this.strel.boxImageToElement(ic, origCopy);
			
			boolean included = true;
			
			for (ImageCoordinate boxedCoord : origCopy) {
				if (this.strel.get(ic, boxedCoord) <= 0.0f || origCopy.getValue(boxedCoord) <= 0.0f) {
					included = false;
					break;
				}
			}
			
			if (included) {
				im.setValue(ic, 1.0f);	
			} else {
				im.setValue(ic, 0.0f);
			}
			
		}
		

	}

}
