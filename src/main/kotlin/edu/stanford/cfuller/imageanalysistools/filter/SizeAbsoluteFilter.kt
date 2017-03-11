package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that removes regions from a mask that are larger or smaller than specified size cutoffs.
 *
 *
 * The size cutoffs are retrieved from the ParameterDictionary used for the analysis, in the parameters "max_size" and "min_size", in units of number
 * of pixels. If these are not specified some default values will be used.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument to the apply method is the mask that will have regions of size outside of the specified bounds removed.

 * @author Colin J. Fuller
 */
class SizeAbsoluteFilter : Filter() {
    /**
     * Applies the SizeAbsoluteFilter, removing regions sized outside the specified range.
     * @param im    The mask whose regions of unusual size will be removed.
     */
    override fun apply(im: WritableImage) {
        val h = Histogram(im)
        var absCutoffMin = -1
        var absCutoffMax = -1

        if (this.params != null) {
            absCutoffMax = this.params!!.getIntValueForKey("max_size")
            absCutoffMin = this.params!!.getIntValueForKey("min_size")
        }

        if (absCutoffMin < 0) { // default values
            absCutoffMax = 50
            absCutoffMin = 5
        }

        val multipliers = IntArray(h.maxValue + 1)

        for (i in multipliers.indices) {
            if (h.getCounts(i) < absCutoffMin || h.getCounts(i) > absCutoffMax) {
                multipliers[i] = 0
            } else {
                multipliers[i] = 1
            }
        }

        for (ic in im) {
            val value = im.getValue(ic)
            im.setValue(ic, value * multipliers[value.toInt()])
        }
    }
}
