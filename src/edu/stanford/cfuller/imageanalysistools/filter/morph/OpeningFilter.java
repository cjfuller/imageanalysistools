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

/**
 * A Filter implementing the operation of binary morphological opening in an arbitrary (i.e. up to 5) number of dimensions.
 * <p>
 * This filter does not take a reference image.
 * <p>
 * The argument to the apply method should be the image to be processed.  This should have foreground pixels labeled > 0 and
 * background pixels labeled <= 0.  After processing, all foreground pixels will be set to 1 and all background pixels to 0.
 * 
 * @author Colin J. Fuller
 */
public class OpeningFilter extends MorphologicalFilter {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.morph.MorphologicalFilter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {
		DilationFilter df = new DilationFilter(this);
		ErosionFilter ef = new ErosionFilter(this);

		ef.apply(im);
		df.apply(im);


	}

}
