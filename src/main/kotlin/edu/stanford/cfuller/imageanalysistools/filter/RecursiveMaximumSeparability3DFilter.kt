package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that recursively applies a [MaximumSeparabilityThresholdingFilter] to each region in an Image mask (and the
 * regions resulting from that thresholding, and so on), until every region in the Image falls withing a specified range of sizes
 * or has been removed due to being smaller than the smallest acceptable size.  This filter is intended to operate on 3D images.
 *
 *
 * Part of the implementation of the recursive Otsu thresholding method described in Xiong et al. (DOI: 10.1109/ICIP.2006.312365).
 *
 *
 * The reference Image for the Filter should be set to the original Image (not a mask); its values will be used to set
 * the threshold in the MaximumSeparabilityThresholdingFilter for each region.  This Image will not be changed.
 *
 *
 * The argument to the apply method should be the mask whose regions are to be individually thresholded.  This mask will contain
 * the mask with thresholded regions after this Filter has been implies; no particular labeling of regions is guaranteed.


 * @author Colin J. Fuller
 */
class RecursiveMaximumSeparability3DFilter : Filter() {
    internal val MAX_RECURSIONS = 3
    internal var numRecs: Int = 0

    init {
        this.numRecs = 0
    }

    /**
     * Applies the Filter to an Image mask, replacing its values by the mask that is the result of thresholding each
     * region in the mask.
     * @param im    The Image mask to process; will be overwritten by the result.
     */
    override fun apply(im: WritableImage) {
        var referenceImage = this.referenceImage ?: throw ReferenceImageRequiredException("MaskFilter requires a reference image.")
        val originalImageReference = referenceImage

        referenceImage = ImageFactory.createWritable(referenceImage)
        this.referenceImage = referenceImage
        // to save having to allocate an Image at every step, this
        // overwrites the reference Image, so make a copy
        var doRecursion = true
        val maskBuffer = ImageFactory.createWritable(im.dimensionSizes, 0.0f)
        var imageBuffer: WritableImage? = null
        val mstf = MaximumSeparabilityThresholdingFilter()
        val MF = MaskFilter()

        mstf.referenceImage = imageBuffer
        mstf.params = this.params

        val lf = Label3DFilter()
        var areaMin = -1
        var areaMax = -1

        this.params?.let {
            areaMin = Integer.parseInt(it.getValueForKey("min_size"))
            areaMax = Integer.parseInt(it.getValueForKey("max_size"))
        }

        if (areaMin < 0) {
            areaMin = 25 //orig 25, 5000 for HeLa cells, 5 for centromeres
            areaMax = 1000 //orig 1000, 100000 for HeLa cells, 50 for centromeres
        }

        while (doRecursion && this.numRecs < MAX_RECURSIONS) {
            doRecursion = false
            val h = Histogram(im)
            val flags = BooleanArray(h.maxValue + 1)
            val remove = BooleanArray(h.maxValue + 1)
            flags[0] = false
            remove[0] = false
            for (i in 1..h.maxValue) {
                flags[i] = h.getCounts(i) > areaMax
                remove[i] = h.getCounts(i) < areaMin
            }
            im.asSequence()
                    .filter { remove[im.getValue(it).toInt()] }
                    .forEach { im.setValue(it, 0f) }

            var divided = false
            val xList = java.util.Hashtable<Int, MutableList<Int>>()
            val yList = java.util.Hashtable<Int, MutableList<Int>>()
            val zList = java.util.Hashtable<Int, MutableList<Int>>()

            for (c in im) {
                val value = im.getValue(c).toInt()
                if (value == 0) continue

                if (!xList.containsKey(value)) {
                    xList.put(value, java.util.Vector<Int>())
                    yList.put(value, java.util.Vector<Int>())
                    zList.put(value, java.util.Vector<Int>())
                }
                xList[value]!!.add(c[ImageCoordinate.X])
                yList[value]!!.add(c[ImageCoordinate.Y])
                zList[value]!!.add(c[ImageCoordinate.Z])
            }

            var lastK = 0
            val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

            for (k in 0..h.maxValue + 1 - 1) {
                if (!flags[k]) continue

                if (lastK > 0) {
                    // TODO(colin): figure out proper null handling here.
                    val xKList = xList[lastK]!!
                    val yKList = yList[lastK]!!
                    val zKList = zList[lastK]!!
                    for (i in xKList.indices) {
                        ic[ImageCoordinate.X] = xKList[i]
                        ic[ImageCoordinate.Y] = yKList[i]
                        ic[ImageCoordinate.Z] = zKList[i]
                        maskBuffer.setValue(ic, 0f)
                    }
                }
                divided = true

                var lower_width = im.dimensionSizes[ImageCoordinate.X]
                var lower_height = im.dimensionSizes[ImageCoordinate.Y]
                var lower_z = im.dimensionSizes[ImageCoordinate.Z]
                var upper_width = 0
                var upper_height = 0
                var upper_z = 0

                // TODO(colin): figure out proper null handling here.
                val xKList = xList[k]!!
                val yKList = yList[k]!!
                val zKList = zList[k]!!

                for (i in 0..xKList.size - 1) {
                    ic[ImageCoordinate.X] = xKList[i]
                    ic[ImageCoordinate.Y] = yKList[i]
                    ic[ImageCoordinate.Z] = zKList[i]
                    maskBuffer.setValue(ic, k.toFloat())

                    if (ic[ImageCoordinate.X] < lower_width) lower_width = ic[ImageCoordinate.X]
                    if (ic[ImageCoordinate.X] > upper_width - 1) upper_width = ic[ImageCoordinate.X]
                    if (ic[ImageCoordinate.Y] < lower_height) lower_height = ic[ImageCoordinate.Y]
                    if (ic[ImageCoordinate.Y] > upper_height - 1) upper_height = ic[ImageCoordinate.Y]
                    if (ic[ImageCoordinate.Z] < lower_z) lower_z = ic[ImageCoordinate.Z]
                    if (ic[ImageCoordinate.Z] > upper_z - 1) upper_z = ic[ImageCoordinate.Z]
                }

                val lowerBound = ImageCoordinate.createCoordXYZCT(lower_width, lower_height, lower_z, 0, 0)
                val upperBound = ImageCoordinate.createCoordXYZCT(upper_width + 1, upper_height + 1, upper_z + 1, 1, 1)

                maskBuffer.setBoxOfInterest(lowerBound, upperBound)
                imageBuffer = referenceImage.writableInstance // This is set to be a writable copy above, so not clobbering the reference image.
                imageBuffer.setBoxOfInterest(lowerBound, upperBound)

                lastK = k
                MF.referenceImage = maskBuffer
                MF.apply(imageBuffer)
                mstf.apply(imageBuffer)
                MF.referenceImage = imageBuffer
                MF.apply(maskBuffer)

                for (i in 0..xKList.size - 1) {
                    ic[ImageCoordinate.X] = xKList[i]
                    ic[ImageCoordinate.Y] = yKList[i]
                    ic[ImageCoordinate.Z] = zKList[i]

                    if (maskBuffer.getValue(ic) == 0f) {
                        im.setValue(ic, 0f)
                    }

                }

                maskBuffer.clearBoxOfInterest()
                for (ic_restore in imageBuffer) {
                    imageBuffer.setValue(ic_restore, originalImageReference.getValue(ic_restore))
                }
                imageBuffer.clearBoxOfInterest()
                lowerBound.recycle()
                upperBound.recycle()
            }
            ic.recycle()
            this.numRecs += 1

            if (divided && this.numRecs < MAX_RECURSIONS) {
                lf.apply(im)
                doRecursion = true
            }
        }
        this.numRecs = 0
        this.referenceImage = originalImageReference
        return
    }
}
