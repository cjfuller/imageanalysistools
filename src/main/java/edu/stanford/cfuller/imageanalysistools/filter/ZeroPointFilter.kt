package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A filter that adjusts the image values to make the minimum value zero if there
 * are negative values in the image, and otherwise leaves the image unchanged.

 * @author Colin J. Fuller
 */
class ZeroPointFilter : Filter() {

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        var min = 0f
        im.asSequence()
                .filter { im.getValue(it) < min }
                .forEach { min = im.getValue(it) }

        if (min >= 0) return
        for (ic in im) {
            im.setValue(ic, im.getValue(ic) - min)
        }
    }
}
