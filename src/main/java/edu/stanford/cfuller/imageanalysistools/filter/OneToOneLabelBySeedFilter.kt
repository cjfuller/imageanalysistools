package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that labels regions in a mask according to the labels in a second (seed) mask.
 *
 * Each distinct region in the input mask will be assigned
 * the label of any seed region that has any pixel overlap with the region in the input mask.  If multiple seed regions
 * overlap with a single region in the input mask, all the pixels the region in the input mask will be assigned to the same one of those seed values
 * (but it is unspecified which one), except for any pixels overlapping directly with a different seed region, which will always
 * be assigned the same label as the seed region.
 *
 * Any regions in the input mask that do not overlap with a seed region will not be changed.  This could potentially lead to
 * duplicate labeling, so it is a good idea to either use a segmentation method that guarantees that every region has a seed,
 * or to first apply a [SeedFilter].
 *
 * The reference Image should be set to the seed mask (this will not be modified by the Filter).
 *
 * The argument to the apply method should be set to the mask that is to be labeled according to the labels in the seed Image.
 *
 * @author Colin J. Fuller
 */

class OneToOneLabelBySeedFilter : Filter() {
    /**
     * Applies the Filter to the specified Image mask, relabeling it according to the seed regions in the reference Image.
     * @param im    The Image mask to process, whose regions will be relabeled.
     */
    override fun apply(im: WritableImage) {
        val referenceImage = this.referenceImage ?: throw ReferenceImageRequiredException("MaskFilter requires a reference image.")
        val hasSeedSet = java.util.HashMap<Int, Int>()
        val seedIsMapped = java.util.HashSet<Int>()
        for (c in im) {
            val currValue = im.getValue(c).toInt()
            val seedValue = referenceImage.getValue(c).toInt()

            if (seedValue > 0 && currValue > 0) {
                hasSeedSet.put(currValue, seedValue)
            }
        }
        seedIsMapped += hasSeedSet.values

        for (c in im) {
            val currValue = im.getValue(c).toInt()
            if (hasSeedSet.containsKey(currValue) && currValue > 0) {
                im.setValue(c, hasSeedSet[currValue]!!.toFloat())
            }
            if (referenceImage.getValue(c) > 0 && seedIsMapped.contains(referenceImage.getValue(c).toInt())) {
                im.setValue(c, referenceImage.getValue(c))
            }
        }
    }
}
