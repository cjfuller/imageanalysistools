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

package edu.stanford.cfuller.imageanalysistools.filter.morph

import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * A Filter representing a binary morphological filter in an arbitrary (i.e. up to 5) number of dimensions.
 *
 *
 * A MorphologicalFilter processes an [Image][edu.stanford.cfuller.imageanalysistools.image.Image] using a [StructuringElement] for the morphological operation.
 *
 *
 * Eventually MorphologicalFilters will implement processing greyscale images, but only binary filters are currently implemented.

 */
abstract class MorphologicalFilter : Filter {

    internal var strel: StructuringElement? = null
    internal var processAsBinary: Boolean = false

    /**
     * Constructs a new MorphologicalFilter.
     */
    constructor() {
        this.strel = null
        this.processAsBinary = true
    }

    /**
     * Constructs a new MorphologicalFilter, copying the structuring element and settings from another
     * MorphologicalFilter.
     * @param copySettings        The MorphologicalFilter whose settings will be copied.
     */
    constructor(copySettings: MorphologicalFilter) {
        this.strel = copySettings.strel
        this.processAsBinary = copySettings.processAsBinary
    }

    /* (non-Javadoc)
     * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
     */
    abstract override fun apply(im: WritableImage)

    /**
     * Sets the StructuringElement to be used for the filtering.
     * @param strel        The StructuringElement to be used.
     */
    fun setStructuringElement(strel: StructuringElement) {
        this.strel = strel
    }

    /**
     * Sets whether this filter processes images as binary or greyscale.
     *
     *
     * Currently does not affect anything; all images are processed as binary.

     * @param isBinary        Whether the filter should act as if all images are binary.
     */
    fun setProcessAsBinary(isBinary: Boolean) {
        this.processAsBinary = isBinary
    }

}
