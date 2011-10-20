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

/**
 * A filter that performs prefiltering on a 3D image to aid in segmentation.
 * <p>
 * Currently, this is just a wrapper around a box-size-1 {@link VariableSizeMeanFilter}.
 * <p>
 * The argument to the apply method should be the image to be filtered.
 * <p>
 * This filter does not use a reference Image.
 * 
 * 
 * @author Colin J. Fuller
 *
 */
public class Renormalization3DFilter extends Filter {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {

		VariableSizeMeanFilter VSMF = new VariableSizeMeanFilter();
		VSMF.setBoxSize(1);
		VSMF.apply(im);

		
	}

}
