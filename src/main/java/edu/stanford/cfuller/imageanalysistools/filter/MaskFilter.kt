package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that applies a mask to an Image.  A mask is any Image that has nonzero pixel values in regions and zero pixel
 * values elsewhere.
 *
 *
 * The reference Image should be set to the mask that will be applied.  This will not be modified.
 *
 *
 * The argument to the apply method should be the Image that will be masked by the reference Image.  The masking operation will leave
 * the Image unchanged except for setting to zero any pixel that has a zero value in the mask.

 * @author Colin J. Fuller
 */
class MaskFilter : Filter() {
    /**
     * Applies the MaskFilter to a given Image.
     * @param im    The Image that will be masked by the reference Image.
     */
    override fun apply(im: WritableImage) {
        val referenceImage = this.referenceImage ?: throw ReferenceImageRequiredException("MaskFilter requires a reference image.")
        im.asSequence()
                .filter { referenceImage.getValue(it) == 0f }
                .forEach { im.setValue(it, 0f) }
    }
}
