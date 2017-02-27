package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate


/**
 * A Filter that recursively applies a [MaximumSeparabilityThresholdingFilter] to each region in an Image mask (and the
 * regions resulting from that thresholding, and so on), until every region in the Image falls withing a specified range of sizes
 * or has been removed due to being smaller than the smallest acceptable size.
 *
 * Part of the implementation of the recursive Otsu thresholding method described in Xiong et al. (DOI: 10.1109/ICIP.2006.312365).
 *
 * The reference Image for the Filter should be set to the original Image (not a mask); its values will be used to set
 * the threshold in the MaximumSeparabilityThresholdingFilter for each region.  This Image will not be changed.
 *
 * The argument to the apply method should be the mask whose regions are to be individually thresholded.  This mask will contain
 * the mask with thresholded regions after this Filter has been implies; no particular labeling of regions is guaranteed.
 *
 * @author Colin J. Fuller
 */
class RecursiveMaximumSeparabilityFilter : Filter() {
    //Required parameters
    internal val MAX_SIZE_P = "max_size"
    internal val MIN_SIZE_P = "min_size"

    //Optional parameters
    internal val MAX_REC_P = "max_thresh_recursions"
    internal val DEFAULT_MAX_RECURSIONS = 1
    internal var numRecs: Int = 0
    internal var maxRecursions: Int = DEFAULT_MAX_RECURSIONS

    /**
     * Applies the Filter to an Image mask, replacing its values by the mask that is the result of thresholding each
     * region in the mask.
     * @param im    The Image mask to process; will be overwritten by the result.
     */
    override fun apply(im: WritableImage) {
        val originalImageReference = this.referenceImage
        this.referenceImage = ImageFactory.createWritable(this.referenceImage ?: throw ReferenceImageRequiredException("MaskFilter requires a reference image."))
        var doRecursion = true
        val maskBuffer = ImageFactory.createWritable(im.dimensionSizes, 0.0f)
        var imageBuffer: WritableImage? = null
        val mstf = MaximumSeparabilityThresholdingFilter()
        val MF = MaskFilter()
        mstf.referenceImage = imageBuffer
        mstf.params = this.params
        val lf = LabelFilter()
        var areaMin = -1
        var areaMax = -1

        this.params?.let {
            areaMin = Integer.parseInt(it.getValueForKey(MIN_SIZE_P))
            areaMax = Integer.parseInt(it.getValueForKey(MAX_SIZE_P))

            if (it.hasKey(MAX_REC_P)) {
                this.maxRecursions = it.getIntValueForKey(MAX_REC_P)
            }
        }

        if (areaMin < 0) {
            areaMin = 25 //orig 25, 5000 for HeLa cells, 5 for centromeres
            areaMax = 1000 //orig 1000, 100000 for HeLa cells, 50 for centromeres
        }

        while (doRecursion && this.numRecs < this.maxRecursions) {
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

            //set up points lists
            val xList = java.util.Hashtable<Int, MutableList<Int>>()
            val yList = java.util.Hashtable<Int, MutableList<Int>>()

            for (c in im) {
                val value = im.getValue(c).toInt()
                if (value == 0) continue

                if (!xList.containsKey(value)) {
                    xList.put(value, java.util.Vector<Int>())
                    yList.put(value, java.util.Vector<Int>())
                }
                xList[value]!!.add(c[ImageCoordinate.X])
                yList[value]!!.add(c[ImageCoordinate.Y])
            }

            var lastK = 0
            val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

            for (k in 0..h.maxValue + 1 - 1) {
                if (!flags[k]) continue
                if (lastK > 0) {
                    // TODO(colin): figure out proper null handling
                    val xKList = xList[lastK]!!
                    val yKList = yList[lastK]!!
                    for (i in xKList.indices) {
                        ic[ImageCoordinate.X] = xKList[i]
                        ic[ImageCoordinate.Y] = yKList[i]
                        maskBuffer.setValue(ic, 0f)
                    }
                }
                divided = true

                var lower_width = im.dimensionSizes[ImageCoordinate.X]
                var lower_height = im.dimensionSizes[ImageCoordinate.Y]
                var upper_width = 0
                var upper_height = 0

                // TODO(colin): figure out proper null handling
                val xKList = xList[k]!!
                val yKList = yList[k]!!

                for (i in 0..xKList.size - 1) {
                    ic[ImageCoordinate.X] = xKList[i]
                    ic[ImageCoordinate.Y] = yKList[i]
                    maskBuffer.setValue(ic, k.toFloat())

                    if (ic[ImageCoordinate.X] < lower_width) lower_width = ic[ImageCoordinate.X]
                    if (ic[ImageCoordinate.X] > upper_width - 1) upper_width = ic[ImageCoordinate.X]
                    if (ic[ImageCoordinate.Y] < lower_height) lower_height = ic[ImageCoordinate.Y]
                    if (ic[ImageCoordinate.Y] > upper_height - 1) upper_height = ic[ImageCoordinate.Y]
                }

                val lowerBound = ImageCoordinate.createCoordXYZCT(lower_width, lower_height, 0, 0, 0)
                val upperBound = ImageCoordinate.createCoordXYZCT(upper_width + 1, upper_height + 1, 1, 1, 1)

                maskBuffer.setBoxOfInterest(lowerBound, upperBound)
                imageBuffer = this.referenceImage!!.writableInstance // This is set to be a writable copy above, so not clobbering the reference image.
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

                    if (maskBuffer.getValue(ic) == 0f) {
                        im.setValue(ic, 0f)
                    }
                }

                maskBuffer.clearBoxOfInterest()
                for (ic_restore in imageBuffer) {
                    imageBuffer.setValue(ic_restore, originalImageReference!!.getValue(ic_restore))
                }
                imageBuffer.clearBoxOfInterest()
                lowerBound.recycle()
                upperBound.recycle()
            }
            ic.recycle()
            this.numRecs += 1

            if (divided && this.numRecs < this.maxRecursions) {
                doRecursion = true
            }
            lf.apply(im)
        }
        this.numRecs = 0
        this.referenceImage = originalImageReference
    }
}
