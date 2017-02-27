package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that inverts an Image.  Each value in the Image is replaced by the maximum value in the Image minus the value.
 *
 *
 * This Filter does not use a reference image.
 *
 *
 * The argument to the apply method should be the Image that will be inverted.

 * @author Colin J. Fuller
 */

class InversionFilter : Filter() {
    /**
     * Applies the inversion Filter to the Image.
     * @param im    The Image to invert.
     */
    override fun apply(im: WritableImage) {
        val h = Histogram(im)
        val max = h.maxValue
        for (c in im) {
            im.setValue(c, max - im.getValue(c))
        }
    }
}
