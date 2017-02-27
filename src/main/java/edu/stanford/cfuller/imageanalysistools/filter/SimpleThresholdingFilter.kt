package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that thresholds an Image at a fractional level between the minimum and the maximum value of the Image.
 *
 *
 * The reference Image should be set to the Image to be thresholded.  This Image will not be modified.
 *
 *
 * The argument to the apply method should be set to an Image that will get the output of the thresholding.  This should be the
 * same dimensions as the reference Image.  This Image will be unchanged except that any pixel below the threshold in the reference Image
 * will be set to zero in this Image.

 * @author Colin J. Fuller
 */

class SimpleThresholdingFilter() : Filter() {
    var fractionalLevel: Double = 0.0

    /**
     * Constructs a SimpleThresholdingFilter that will threshold an Image at some fractional level of the difference between the minimum
     * and maximum value of the Image.
     *
     *
     * For example, if fractionalLevel is set to 0.1, the threshold will be set at min + 0.1*(max-min).
     * @param fractionalLevel   The fractional level at which to threshold the Image.
     */
    constructor(fractionalLevel: Double) : this() {
        this.fractionalLevel = fractionalLevel
    }

    /**
     * Applies the SimpleThresholdingFilter to an Image.
     * @param im    The Image whose pixels wil be set to zero where the reference Image is below the fractional threshold.
     */
    override fun apply(im: WritableImage) {
        val referenceImage = this.referenceImage ?: throw ReferenceImageRequiredException("Filter requires a reference image.")
        val h = Histogram(referenceImage)
        val cutoff = (h.maxValue - h.minValue) * this.fractionalLevel + h.minValue
        im.asSequence()
                .filter { referenceImage.getValue(it) < cutoff }
                .forEach { im.setValue(it, 0f) }
    }
}
