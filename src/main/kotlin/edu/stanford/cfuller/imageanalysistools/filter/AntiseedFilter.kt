package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * This class is a Filter that removes regions in one mask that overlap with regions in a second mask.
 *
 *
 * This may be particularly useful for seeded segmentation methods, where this Filter can be used to remove regions from the segmented Image
 * that overlap with regions in the seed mask.
 *
 *
 * The reference image for this filter should be set to the mask that will not be modified.
 *
 *
 * The argument to the apply method
 * should be the mask that will be modified.  Any regions in the Image argument to the apply method that overlap with regions in the reference image
 * will be removed from the Image that is the argument to the apply method.

 * @author Colin J. Fuller
 */

class AntiseedFilter : Filter() {
    /**
     * Applies the filter, removing any regions from the supplied Image argument that overlap with regions in the reference Image.
     * @param im    The Image to process; regions will be removed from this Image that have any overlap with regions in the reference Image.
     */
    override fun apply(im: WritableImage) {
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
            if (hasSeedSet.contains(currValue)) {
                im.setValue(i, 0f)
            }
        }
    }
}
