package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import org.apache.commons.math3.linear.ArrayRealVector

/**
 * A Filter that thresholds an Image using Otsu's method.  (Otsu, 1979, DOI: 10.1109/TSMC.1979.4310076).
 *
 *
 * This filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image to be thresholded.  After filtering, this Image will have its original values
 * anywhere where it is above the threshold, and zero values overwritten anywhere where the original values were below the threshold.

 * @author Colin J. Fuller
 */
class MaximumSeparabilityThresholdingFilter : Filter() {
    internal val defaultIncrement = 1
    internal val defaultAdaptive = false

    /**
     * Applies the filter to an Image.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     */
    override fun apply(im: WritableImage) {
        apply_ext(im, defaultAdaptive, defaultIncrement)
    }

    /**
     * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold, and specifying a size for the threshold determination increment.
     * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
     * for images with a large dynamic range.
     *
     *
     * The increment size specified (in greylevels) will be used to determine the threshold only if adaptive determination is off; otherwise this parameter will be ignored.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     * *
     * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
     * *
     * @param increment             the increment size (in greylevels) to use for determining the threshold; must be positive.
     */
    @JvmOverloads fun apply_ext(im: WritableImage, adaptiveincrement: Boolean, increment: Int = defaultIncrement) {
        var increment = increment
        val h = Histogram(im)
        var thresholdValue = 0
        val numSteps = 1000
        var best_eta = 0.0
        var best_index = Integer.MAX_VALUE
        val nonzerocounts = h.totalCounts - h.getCounts(0)
        val meannonzero = h.meanNonzero
        val omega_v = ArrayRealVector(h.maxValue)
        val mu_v = ArrayRealVector(h.maxValue)
        var c = 0

        if (adaptiveincrement) {
            increment = ((h.maxValue - h.minValue + 1) * 1.0 / numSteps).toInt()
            if (increment < 1) increment = 1
        }

        var k = h.minValue
        while (k < h.maxValue + 1) {
            if (k == 0) {
                k += increment
                continue
            }
            omega_v.setEntry(c, h.getCumulativeCounts(k) * 1.0 / nonzerocounts)

            if (c == 0) {
                mu_v.setEntry(c, k * omega_v.getEntry(c))
            } else {
                mu_v.setEntry(c, mu_v.getEntry(c - 1) + k.toDouble() * h.getCounts(k).toDouble() * 1.0 / nonzerocounts)
                for (i in k - increment + 1..k - 1) {
                    mu_v.setEntry(c, mu_v.getEntry(c) + h.getCounts(i).toDouble() * i.toDouble() * 1.0 / nonzerocounts)
                }
            }

            val omega = omega_v.getEntry(c)
            val mu = mu_v.getEntry(c)

            if (omega > 1e-8 && 1 - omega > 1e-8) {
                val eta = omega * (1 - omega) * Math.pow((meannonzero - mu) / (1 - omega) - mu / omega, 2.0)
                if (eta >= best_eta) {
                    best_eta = eta
                    best_index = k
                }
            }
            c++
            k += increment
        }

        thresholdValue = best_index

        if (thresholdValue == Integer.MAX_VALUE) {
            thresholdValue = 0
        }
        im.asSequence()
                .filter { im.getValue(it) < thresholdValue }
                .forEach { im.setValue(it, 0f) }
    }
}
