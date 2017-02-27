package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.metric.Measurement
import edu.stanford.cfuller.imageanalysistools.metric.Metric
import edu.stanford.cfuller.imageanalysistools.metric.Quantification

/**
 * A method to find isolated chromosomes using DNA stain (designate using parameter marker_channel_index) as well as
 * centromeres using a centromere marker (designate using parameter secondary_marker_channel_index).
 *
 * These will be matched up, and for each chromososome, the centromeric as well as noncentromeric intensities in each channel
 * will be quantified.
 *
 * @author Colin J. Fuller
 */
class ChromosomeCentromereFindingMethod : Method() {
    internal var metric: Metric = edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric()

    override fun go() {
        val ch0_method = CentromereFindingMethod()
        val ch1_method = CentromereFindingMethod()
        val originalChannelNumber = this.parameters.getIntValueForKey("marker_channel_index")
        var secondChannelNumber = this.parameters.getIntValueForKey("secondary_marker_channel_index")
        if (originalChannelNumber >= secondChannelNumber) secondChannelNumber++

        ch0_method.setImages(this.imageSet)
        ch0_method.parameters = this.parameters

        val reordered = ImageSet(this.imageSet)
        reordered.setMarkerImage(secondChannelNumber)

        ch1_method.setImages(reordered)
        ch1_method.parameters = this.parameters
        ch0_method.go()

        val oldMax = this.parameters.getIntValueForKey("max_size")
        val oldMin = this.parameters.getIntValueForKey("min_size")

        if (this.parameters.hasKey("cen_max_size") && this.parameters.hasKey("cen_min_size")) {
            this.parameters.setValueForKey("max_size", this.parameters.getValueForKey("cen_max_size"))
            this.parameters.setValueForKey("min_size", this.parameters.getValueForKey("cen_min_size"))
        }

        ch1_method.go()

        this.parameters.setValueForKey("max_size", Integer.toString(oldMax))
        this.parameters.setValueForKey("min_size", Integer.toString(oldMin))

        val mf = MaskFilter()
        mf.referenceImage = ch1_method.storedImage
        val chromosomeCentromereMask = ImageFactory.createWritable(ch0_method.storedImage!!)
        mf.apply(chromosomeCentromereMask)

        //now remove all these regions from the chromosome mask
        val chromosomeNonCentromereMask = ImageFactory.createWritable(ch0_method.storedImage!!)

        chromosomeNonCentromereMask
                .asSequence()
                .filter { chromosomeCentromereMask.getValue(it) > 0 }
                .forEach { chromosomeNonCentromereMask.setValue(it, 0f) }

        val fullResult = metric.quantify(chromosomeCentromereMask, this.imageSet)
        val chromosomeResult = metric.quantify(chromosomeNonCentromereMask, this.imageSet)

        for (m in chromosomeResult.allMeasurements) {
            fullResult.addMeasurement(
                    Measurement(
                            hasFeature = m.hasAssociatedFeature(),
                            id = m.featureID,
                            measurement = m.measurement,
                            name = m.measurementName + "_chromosome",
                            type = m.measurementType!!,
                            image = m.imageID!!))
        }

        this.storedDataOutput = fullResult
        this.storeImageOutput(chromosomeCentromereMask)
        this.storeImageOutput(chromosomeNonCentromereMask)
    }
}

