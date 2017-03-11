package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.metric.Metric

/**
 * Method to find spots in noisy images.
 *
 * The core of this method is the same as the [CentromereFindingMethod], and differs only in that it does more
 * extensive normalization of the input image before segmenting it.  The normalization in this method attempts to reduce
 * noise using a combination of gaussian filtering, bandpass filtering, laplace filtering, and use of a [RenormalizationFilter].
 *
 * @author Colin J. Fuller
 */


class NoisySpotFindingMethod : CentromereFindingMethod() {
    override fun normalizeInputImage(input: WritableImage) {
        val rnf = RenormalizationFilter()
        rnf.setParameters(this.parameters)
        val GF = GaussianFilter()
        GF.setWidth(3)
        val BF = BandpassFilter()
        BF.setBand(0.3, 0.7)
        val LapF = LaplacianFilter()
        GF.apply(input)
        rnf.apply(input)
        GF.apply(input)
        LapF.apply(input)
        BF.apply(input)
        rnf.apply(input)
    }
}
