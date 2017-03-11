package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * Implements the a version of the watershed method that is seeded off of supplied regions.
 * The eventual watershed division of the Image will have as many regions as seed regions.
 * @author Colin J. Fuller
 */
class SeededWatershedMethod : Method() {
    override fun go() {
        val filters = java.util.Vector<Filter>()
        val gf = GaussianFilter()

        if (this.parameters.hasKey("min_size")) {
            gf.setWidth(Math.sqrt(this.parameters.getIntValueForKey("min_size").toDouble()).toInt())
        } else {
            gf.setWidth(5)
        }
        filters.add(gf)
        filters.add(GradientFilter())

        val swf = SeededWatershedFilter()
        val rtf = CentromereFindingMethod()
        parameters.addIfNotSet("use_clustering", "false")
        rtf.setImages(this.imageSet)
        rtf.parameters = this.parameters
        rtf.go()
        swf.setSeed(rtf.storedImage!!)
        filters.add(swf)
        filters.add(MergeFilter())
        filters.add(RelabelFilter())
        filters.add(SimpleThresholdingFilter(0.1))
        filters.add(RelabelFilter())
        filters.add(FillFilter())

        for (f in filters) {
            f.referenceImage = this.images[0]
            f.setParameters(this.parameters)
        }
        val toProcess = ImageFactory.createWritable(this.images[0])
        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())
    }
}
