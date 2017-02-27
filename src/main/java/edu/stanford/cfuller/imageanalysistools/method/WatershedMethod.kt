package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * Implements the commonly used watershed method on the gradient of an Image.

 * @author Colin J. Fuller
 */
class WatershedMethod : Method() {
    override fun go() {
        LoggingUtilities.logDebugMessages()
        val filters = java.util.Vector<Filter>()
        val gf = GaussianFilter()

        if (this.parameters.hasKey("min_size")) {
            gf.setWidth(Math.sqrt(this.parameters.getIntValueForKey("min_size").toDouble()).toInt())
        } else {
            gf.setWidth(5)
        }
        with(filters) {
            add(gf)
            add(GradientFilter())
            add(WatershedFilter())
            add(RelabelFilter())
            add(SimpleThresholdingFilter(0.1))
            add(RelabelFilter())
            add(FillFilter())
        }

        for (f in filters) {
            f.referenceImage = this.images[0]
            f.setParameters(this.parameters)
        }
        val toProcess = ImageFactory.createWritable(this.images[0])
        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())
    }
}
