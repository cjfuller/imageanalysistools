package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A filter that averages an Image over all points in the time dimension.
 *
 *
 * The reference Image for this filter should be set to the time series Image to be averaged.
 *
 *
 * The argument to the apply method should be an Image of all zeros that will be overwritten with the same dimension sizes as the time series,
 * except for only containing a single time point.

 * @author Colin J. Fuller
 */
class TimeAveragingFilter : Filter() {
    /**
     * Time-averages the reference Image, overwriting the argument to this method with the result of the averaging.
     * @param output    An Image containing all zeros that will be overwritten that has the same dimension sizes as the reference Image, but a singleton time dimension.
     */
    override fun apply(output: WritableImage) {
        val im = this.referenceImage
        val size_t = im!!.dimensionSizes[ImageCoordinate.T]

        for (ic in im) {
            val ic_t = ImageCoordinate.cloneCoord(ic)
            ic_t[ImageCoordinate.T] = 0
            output.setValue(ic_t, output.getValue(ic_t) + im.getValue(ic) / size_t)
            ic_t.recycle()
        }
    }
}
