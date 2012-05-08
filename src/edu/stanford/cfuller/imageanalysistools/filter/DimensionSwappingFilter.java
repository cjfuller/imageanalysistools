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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper;
import edu.stanford.cfuller.imageanalysistools.image.Image;

/**
 * A filter that wraps a {@link DimensionFlipper} and swaps two of the dimensions in an image.
 * <p>
 * The two dimensions to be swapped can be specified using the {@link #setDimensionsToSwap(int, int)} method,
 * or by setting the parameters first_dimension_to_swap and second_dimension_to_swap as integer parameters.  In either case,
 * the dimensions to be swapped should be specified according to the dimension constants in {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate}.  Specifying
 * the dimensions in the parameter file will override the setting using {@link #setDimensionsToSwap}.
 * <p>
 * The argument to the apply method should be the Image whose dimensions will be swapped.
 * (In contrast to the DimensionFlipper, this Image will be modified in place.)
 * <p>
 * This filter does not use a reference Image.
 * 
 * @author Colin J. Fuller
 *
 */
public class DimensionSwappingFilter extends Filter {

	int dim0;
	int dim1;
	
	final static String dim0_param = "first_dimension_to_swap";
	final static String dim1_param = "second_dimension_to_swap";
	
	public DimensionSwappingFilter() {
		super();
		this.params = null;
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {
		
		if (this.params != null && this.params.hasKey(dim0_param) && this.params.hasKey(dim1_param)) {
			this.dim0 = this.params.getIntValueForKey(dim0_param);
			this.dim1 = this.params.getIntValueForKey(dim1_param);
		}
		
		Image out = DimensionFlipper.flip(im, this.dim0, this.dim1);
		
		im.resize(out.getDimensionSizes());
		
		im.copy(out);

	}
	
	/**
	 * Sets the two dimensions to swap in the image.  These should correspond to the 
	 * dimension constants defined in {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate} (or, rarely, to a user-defined dimension).
	 * <p>
	 * These will be overriden if dimensions are specified in the parameter file.
	 * 
	 * @param dim0	The first dimension to swap.
	 * @param dim1	The second dimension to swap.
	 */
	public void setDimensionsToSwap(int dim0, int dim1) {
		
		this.dim0 = dim0;
		this.dim1 = dim1;
		
	}

}
