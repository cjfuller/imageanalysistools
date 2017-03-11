package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.BandpassFilter
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.Label3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.LocalMaximumSeparabilityThresholdingFilter
import edu.stanford.cfuller.imageanalysistools.filter.PlaneNormalizationFilter
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparability3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.Renormalization3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter
import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.metric.Metric

/**
 * A method analogous to the [CentromereFindingMethod], but intended to be applied to 3D images.
 *
 * Identifies volumes, rather than planar regions, and outputs a 3D mask rather than a single plane mask.
 *
 * @author Colin J. Fuller
 */
class CentromereFinding3DMethod : Method() {
    internal var metric: Metric = edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric()

    /**
     * Runs the centromere finding method using the stored images and parameters.
     */
    override fun go() {
        var referenceChannel = 0
        this.parameters.addIfNotSet("marker_channel_index", Integer.toString(referenceChannel))
        referenceChannel = this.parameters.getIntValueForKey("marker_channel_index")
        this.centromereFinding(this.images[0])
    }

    private fun centromereFinding(input: Image): Image {
        var input = input
        val filters = java.util.Vector<Filter>()
        val LBE3F = Renormalization3DFilter()
        val band_lower = 4.0f
        val band_upper = 5.0f
        val bf = BandpassFilter()
        bf.setBand(band_lower.toDouble(), band_upper.toDouble())
        bf.setShouldRescale(true)
        if (this.parameters.hasKeyAndTrue("swap_z_t")) {
            input = DimensionFlipper.flipZT(input)
        }

        with(filters) {
            add(bf)
            add(LBE3F)
            add(LocalMaximumSeparabilityThresholdingFilter())
            add(Label3DFilter())
            add(RecursiveMaximumSeparability3DFilter())
            add(RelabelFilter())
            add(SizeAbsoluteFilter())
            add(RelabelFilter())
        }
        for (i in filters) {
            i.setParameters(this.parameters)
            i.referenceImage = this.images[0]
        }

        val toProcess = ImageFactory.createWritable(input)
        iterateOnFiltersAndStoreResult(filters, toProcess, metric)
        return this.storedImage!!
    }
}
