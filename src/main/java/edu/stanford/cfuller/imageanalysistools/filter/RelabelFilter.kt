package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that relabels an Image mask to use a consecutive numbering.
 *
 *
 * Each region in the original mask (regions defined solely upon the labeling in the original mask)
 * will be mapped to a unique value, but these values are not guaranteed to be in the same numerical
 * order as in the original mask.  Region labels in the output mask will begin at 1 and end at the number of regions.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the mask to be relabeled.

 * @author Colin J. Fuller
 */
class RelabelFilter : Filter() {
    override fun apply(im: WritableImage) {
        val maxVal = edu.stanford.cfuller.imageanalysistools.image.Histogram.findMaxVal(im)
        val newlabels = IntArray(maxVal + 1, { -1 })
        var labelctr = 1

        im.asSequence()
                .filter { im.getValue(it) > 0 }
                .forEach {
                    if (newlabels[im.getValue(it).toInt()] == -1) {
                        newlabels[im.getValue(it).toInt()] = labelctr
                        im.setValue(it, labelctr++.toFloat())

                    } else {
                        im.setValue(it, newlabels[im.getValue(it).toInt()].toFloat())
                    }
                }
    }
}
