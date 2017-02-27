package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * This class is a Filter that removes regions in one mask that do not overlap with regions in a second mask.
 *
 *
 * The reference image for this filter should be set to the mask that will not be modified.
 *
 *
 * The argument to the apply method
 * should be the mask that will be modified.  Any regions in the Image argument to the apply method that do not overlap with regions in the reference image
 * will be removed from this Image.

 * @author Colin J. Fuller
 */
class SeedFilter : Filter() {
    /**
     * Applies the filter, keeping only the regions from the supplied Image argument that overlap with regions in the reference Image.
     * @param im    The Image to process; regions will be removed from this Image that have no overlap with regions in the reference Image.
     */
    override fun apply(im: WritableImage) {
        // TODO(colin): reference image null checking
        val hasSeedSet = java.util.HashSet<Int>()

        for (i in im) {
            val currValue = im.getValue(i).toInt()
            val seedValue = this.referenceImage!!.getValue(i).toInt()
            if (seedValue > 0) {
                hasSeedSet.add(currValue)
            }
        }

        for (i in im) {
            val currValue = im.getValue(i).toInt()
            if (!hasSeedSet.contains(currValue)) {
                im.setValue(i, 0f)
            }
        }
    }
}
