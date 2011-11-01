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
 * A Filter that applies a mask to an Image.  A mask is any Image that has nonzero pixel values in regions and zero pixel
 * values elsewhere.
 * <p>
 * The reference Image should be set to the mask that will be applied.  This will not be modified.
 * <p>
 * The argument to the apply method should be the Image that will be masked by the reference Image.  The masking operation will leave
 * the Image unchanged except for setting to zero any pixel that has a zero value in the mask.
 *
 * @author Colin J. Fuller
 */
public class MaskFilter extends Filter {


    /**
     * Applies the MaskFilter to a given Image.
     * @param im    The Image that will be masked by the reference Image.
     */
	@Override
	public void apply(Image im) {
		
		for (ImageCoordinate c : im) {
			if (this.referenceImage.getValue(c) == 0) {
				im.setValue(c, 0);
			}
		}
		
	}

}
