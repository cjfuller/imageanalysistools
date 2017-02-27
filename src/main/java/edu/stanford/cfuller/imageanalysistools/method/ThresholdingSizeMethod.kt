package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * A method to find objects based on a gradient histogram thresholding step and a size-exclusion step.
 * @author Colin J. Fuller
 */
class ThresholdingSizeMethod : Method() {
    override fun go() {
        val filters = java.util.Vector<Filter>()
        with(filters) {
            add(GradientHistogramThresholdingFilter())
            add(LabelFilter())
            add(SizeAbsoluteFilter())
            add(RelabelFilter())
            add(FillFilter())
        }

        for (f in filters) {
            f.setParameters(this.parameters)
            f.referenceImage = this.images[0]
        }
        val toProcess = ImageFactory.createWritable(this.images[0])
        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())
    }
}
