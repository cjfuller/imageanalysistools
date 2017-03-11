package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

import edu.stanford.cfuller.imageanalysistools.filter.morph.ErosionFilter

/**
 * A metric that takes each region in a mask and calculates its area and perimeter.
 *
 * For regions with holes, the perimeter will be the total boundary size both to the
 * outside of the region and to the holes.
 *
 * Area and perimeter are both calculated in units of pixels (# of pixels in the
 * region and # of pixels on the border, respectively)
 *
 * TODO(colin): # of pixels on the border is very odd for small regions (e.g. a 2x2 box has a
 * perimeter of 4...). Consider fixing.
 * @author Colin J. Fuller
 */
class AreaAndPerimeterMetric : Metric {
    /**
     * Quantifies the area and perimeter for each region in a 2D image.
     * If no regions of interest are present in the supplied mask, this will return null.
     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1, as might be produced by a [edu.stanford.cfuller.imageanalysistools.filter.LabelFilter].
     * @param images    An ImageSet of Images to be quantified; this will be ignored except for using the name of the marker image.
     * @return          A Quantification containing measurements for area and perimeter.
     */
    override fun quantify(mask: Image, images: ImageSet): Quantification {
        val h = Histogram(mask)
        if (h.maxValue == 0) throw Error("Unable to quantify image; no regions found.")

        val q = Quantification()

        (0..h.maxValue - 1)
                .asSequence()
                .map {
                    Measurement(
                            hasFeature = true,
                            id = (it + 1).toLong(),
                            measurement = h.getCounts(it + 1).toDouble(),
                            name = "area",
                            type = Measurement.TYPE_SIZE,
                            image = images.markerImageName ?: "Marker image")
                }
                .forEach { q.addMeasurement(it) }

        val eroded = ImageFactory.createWritable(mask)
        val dims = intArrayOf(ImageCoordinate.X, ImageCoordinate.Y)
        val ef = ErosionFilter()
        ef.strel = ErosionFilter.getDefaultElement(dims)
        ef.apply(eroded)

        for (ic in mask) {
            if (mask.getValue(ic) > 0 && eroded.getValue(ic) == 0f) {
                eroded.setValue(ic, mask.getValue(ic))
            } else {
                eroded.setValue(ic, 0f)
            }
        }

        val hPerim = Histogram(eroded)
        (0..hPerim.maxValue - 1)
                .asSequence()
                .map {
                    Measurement(
                            hasFeature = true,
                            id = (it + 1).toLong(),
                            measurement = hPerim.getCounts(it + 1).toDouble(),
                            name = "perimeter",
                            type = Measurement.TYPE_SIZE,
                            image = images.markerImageName ?: "Marker image") }
                .forEach { q.addMeasurement(it) }
        return q
    }
}
