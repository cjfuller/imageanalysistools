package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

/**
 * @author cfuller
 */
class ZeroMetric : Metric {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.metric.Metric#quantify(edu.stanford.cfuller.imageanalysistools.image.Image, java.util.Vector)
	 */
    override fun quantify(mask: Image, images: ImageSet): Quantification {
        val maxRegion = Histogram.findMaxVal(mask)
        val q = Quantification()

        (1..maxRegion - 1)
                .asSequence()
                .map { Measurement(true, it.toLong(), 0.0, "zero", "zero", images.markerImageName!!) }
                .forEach { q.addMeasurement(it) }
        return q
    }
}
