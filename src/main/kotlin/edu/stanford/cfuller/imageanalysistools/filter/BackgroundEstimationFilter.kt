package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that estimates the background within each region of an Image, and removes any pixels in each region that are above the
 * estimated background for that region.  This can be used to filter out any residual foreground pixels before
 * using some metric for quantification of the background within each region.
 *
 *
 * The background estimation is done by for each region, computing the mode (integer-valued) intensity within that region, and computing the
 * half-width at half-maximum of the histogram of that region.  Then, any values that are greater than two half-widths above the mode value of the region are discarded.
 *
 *
 * The reference Image for this filter should be set to a regular Image whose background is to be estimated.
 *
 *
 * The Image argument to the apply method should be a mask of regions to be filtered based upon the intensities within each region.  Each region must be labeled individually.

 * @author Colin J. Fuller
 */

class BackgroundEstimationFilter : Filter() {
    var background: Int = 0
        private set


    /**
     * Runs the filter, calculating a background level for each region in the mask passed as the Image parameter and removing any
     * pixels from mask that are above the background level.

     * @param im    The Image containing the mask of regions to process.  Each region must be labeled individually.
     */
    override fun apply(im: WritableImage) {
        // TODO This recapitulates the c++ code, but there's something wrong here (with the c++ as well)
        val maskCopy = ImageFactory.createWritable(im)
        val mf = MaskFilter()
        val hMask = Histogram(im)

        for (k in 1..hMask.maxValue + 1 - 1) {
            for (i in im) {
                if (im.getValue(i) == k.toFloat()) {
                    maskCopy.setValue(i, k.toFloat())
                } else {
                    maskCopy.setValue(i, 0f)
                }
            }

            // TODO(colin): figure out correct null handling with reference images.
            val imCopy = ImageFactory.createWritable(this.referenceImage!!)
            mf.referenceImage = maskCopy
            mf.apply(imCopy)

            val h = Histogram(imCopy)
            val mode = h.mode
            this.background = mode
            val stddev = 2
            val modeVal = h.countsAtMode
            val halfModeVal = modeVal / 2.0
            var hw_first = 0
            var hw_second = 0
            var firstFound = false

            for (i in 1..h.maxValue - 1) {
                if (firstFound && h.getCounts(i) < halfModeVal && i > mode) {
                    hw_second = i
                    break
                }
                if (!firstFound && h.getCounts(i) > halfModeVal) {
                    hw_first = i
                    firstFound = true
                }
            }

            var hwhm = (hw_second - hw_first) / 2
            if (hwhm < 1) hwhm = 1
            this.background = mode + stddev * hwhm

            im
                    .asSequence()
                    .filter { imCopy.getValue(it) > this.background }
                    .forEach { im.setValue(it, 0f) }
        }
    }
}
