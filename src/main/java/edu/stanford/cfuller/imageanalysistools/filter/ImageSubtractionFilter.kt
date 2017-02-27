package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that subtracts one image from another.
 *
 * A planar image can also be subtracted from a stack of images by calling the setSubtractPlanarImage method.
 *
 * The reference Image is the image that will be subtracted.
 *
 * The Image argument to the apply method will have the reference image subtracted from it in place.
 *
 * @author Colin J. Fuller
 */
class ImageSubtractionFilter() : Filter() {
    internal var subtractPlanarImage: Boolean = false

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
                ic2[ImageCoordinate.X] = ic[ImageCoordinate.X]
                ic2[ImageCoordinate.Y] = ic[ImageCoordinate.Y]
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