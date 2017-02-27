package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.Histogram


/**
 * A thresholding method based upon finding the full-width half-max of the histogram of the gradient of the Image.

 * @author Colin J. Fuller
 */
class GradientHistogramThresholdingFilter : Filter() {

    override fun apply(im: WritableImage) {
        val GF = GaussianFilter()
        GF.setWidth(21)
        GF.apply(im)

        val gradient = ImageFactory.createWritable(im)
        val GRF = GradientFilter()
        GRF.apply(gradient)

        val smoothedThresholded = ImageFactory.createWritable(im)
        this.absoluteThreshold(im, 10)
        val MF = MaskFilter()
        MF.referenceImage = smoothedThresholded
        MF.apply(gradient)
        MF.referenceImage = gradient
        MF.apply(im)
        this.histogramThreshold(im, 45.0)
    }

    private fun absoluteThreshold(im: WritableImage, level: Int) {
        im
                .asSequence()
                .filter { im.getValue(it) < level }
                .forEach { im.setValue(it, 0f) }
    }

    private fun histogramThreshold(im: WritableImage, stddev: Double) {
        val im_hist = Histogram(im)
        val mode = im_hist.mode
        val modeVal = im_hist.countsAtMode
        val halfModeVal = modeVal / 2.0
        val hw_first = (1..im_hist.maxValue - 1).firstOrNull { im_hist.getCounts(it) > halfModeVal } ?: 0
        val hwhm = mode - hw_first
        val threshLevel = (mode + stddev * hwhm).toInt()
        this.absoluteThreshold(im, threshLevel + 1)
    }
}
