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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter implementing the operation of binary morphological erosion in an arbitrary (i.e. up to 5) number of dimensions.
 *
 *
 * This filter does not take a reference image.
 *
 *
 * The argument to the apply method should be the image to be eroded.  This should have foreground pixels labeled > 0 and
 * background pixels labeled <= 0.  After erosion, all foreground pixels will be set to 1 and all background pixels to 0.

 * @author Colin J. Fuller
 */
class ErosionFilter : MorphologicalFilter {

    constructor() : super()
    /**
     * Constructs a new ErosionFilter, copying the structuring element and settings from another
     * MorphologicalFilter.
     * @param mf        The MorphologicalFilter whose settings will be copied.
     */
    constructor(mf: MorphologicalFilter) : super(mf) {
    }

    /* (non-Javadoc)
     * @see edu.stanford.cfuller.imageanalysistools.filter.morph.MorphologicalFilter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
     */
    override fun apply(im: WritableImage) {
        if (this.strel == null) return
        val origCopy = ImageFactory.create(im)

        for (ic in im) {
            if (im.getValue(ic) <= 0.0f) {
                im.setValue(ic, 0.0f)
                continue
            }
            this.strel!!.boxImageToElement(ic, origCopy)
            val included = origCopy.none { this.strel!![ic, it] <= 0.0f || origCopy.getValue(it) <= 0.0f }

            if (included) {
                im.setValue(ic, 1.0f)
            } else {
                im.setValue(ic, 0.0f)
            }
        }
    }

    companion object {
        private val defaultSize = 3

        /**
         * Creates a default structuring element for processing with this filter.
         * Currently this is an n by n by...n square structuring element set to all ones in the specified dimensions, where
         * n is a default size, currently 3.
         * @param dimList    A list of dimensions (corresponding to their integer indices in ImageCoordinate) over which the structuring element extends.
         * *
         * @return            A StructuringElement suitable for processing an image over the supplied dimensions.
         */
        fun getDefaultElement(dimList: IntArray): StructuringElement {
            val strelSize = ImageCoordinate.createCoordXYZCT(1, 1, 1, 1, 1)
            for (i in dimList) {
                strelSize[i] = defaultSize
            }
            val toReturn = StructuringElement(strelSize)
            toReturn.setAll(1.0f)
            strelSize.recycle()
            return toReturn
        }
    }
}
