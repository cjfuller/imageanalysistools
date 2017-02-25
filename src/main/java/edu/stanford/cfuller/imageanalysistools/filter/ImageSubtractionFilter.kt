/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate


/**
 * A Filter that subtracts one image from another.
 *
 *
 * A planar image can also be subtracted from a stack of images by calling the setSubtractPlanarImage method.
 *
 *
 * The reference Image is the image that will be subtracted.
 *
 *
 * The Image argument to the apply method will have the reference image subtracted from it in place.

 * @author Colin J. Fuller
 */
class ImageSubtractionFilter : Filter() {

    internal var subtractPlanarImage: Boolean = false

    /**
     * Constructs a new ImageSubtractionFilter.  Defaults to not subtracting a planar image from each plane.
     */
    init {
        this.subtractPlanarImage = false
    }

    /**
     * Sets whether the reference image is a planar image that should be subtracted from each plane of the image argument to the apply method.
     * @param subPlanar whether the reference image should be subtracted from every plane.
     */
    fun setSubtractPlanarImage(subPlanar: Boolean) {
        this.subtractPlanarImage = subPlanar
    }

    /**
     * Appies the filter, subtracting the reference image from the argument to this method.
     * @param im    The Image to be filtered; this will be filtered in place.
     */
    override fun apply(im: WritableImage) {

        if (this.referenceImage == null) {
            throw ReferenceImageRequiredException("ImageSubtractionFilter requires a reference image.")
        }

        if (this.subtractPlanarImage) {
            val ic2 = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

            for (ic in im) {
                ic2.set(ImageCoordinate.X, ic.get(ImageCoordinate.X))
                ic2.set(ImageCoordinate.Y, ic.get(ImageCoordinate.Y))
                im.setValue(ic, im.getValue(ic) - this.referenceImage!!.getValue(ic2))

            }

            ic2.recycle()

        } else {

            for (ic in im) {
                im.setValue(ic, im.getValue(ic) - this.referenceImage!!.getValue(ic))
            }

        }

    }

}