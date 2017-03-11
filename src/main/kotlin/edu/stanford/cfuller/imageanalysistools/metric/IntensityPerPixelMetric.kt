package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

/**
 * A simple Metric that quantifies the total intensity in a region of interest divided by the size of that region of interest.
 *
 * A side note on using intensity per pixel as a metric rather than integrated intensity:
 *
 * When observing objects much smaller than the diffraction limit of light, an increase in brightness of a point source will appear
 * almost the same as multiple point sources of equivalent total brightness close together in that same diffraction-limited volume.  For this reason,
 * the apparent size of objects below the diffraction limit is often solely dependent on the brightess of objects, and not their
 * actual size.  Thus, for an Image segmentation method that may not attempt to compensate for objects of differing brightness, which
 * then quantified on integrated intensity would effectively be multiplying differences in intensity: brighter objects would
 * contain more pixels (as they would have larger apparent size), and these pixels would each be brighter.  Measuring intensity per
 * pixel counters this effect (though in many cases where the sample permits using a constant region size, this would be a better choice).
 * This is even more effective when combined with something like the [edu.stanford.cfuller.imageanalysistools.filter.RenormalizationFilter],
 * which tries to locally intensity normalize an image before segmentation, so that differences in brightness will not by themselves
 * lead to changes in apparent object size.
 *
 * @author Colin J. Fuller
 */

class IntensityPerPixelMetric : Metric {
    /**
     * Quantifies the (area) average intensity for each region of interest in an image.
     * If no regions of interest are present in the supplied mask, this will return null.
     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1,
     *                  as might be produced by a [edu.stanford.cfuller.imageanalysistools.filter.LabelFilter].
     * @param images    An ImageSet of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * @return          A RealMatrix containing the average intensity value for each region of interest in each input Image.  A single column of the region labels preceeds a sub-matrix, the (i,j)th
     *                  entry of which will contain the quantification of ROI (i+1) in Image j.  This will then be followed by a single column containing the pixel count for that region.
     */
    override fun quantify(mask: Image, images: ImageSet): Quantification {
        val h = edu.stanford.cfuller.imageanalysistools.image.Histogram(mask)
        if (h.maxValue == 0) throw Error("Unable to quantify image; no regions found.")
        val channelIntensities = org.apache.commons.math3.linear.Array2DRowRealMatrix(images.imageCount, h.maxValue).scalarMultiply(0.0)

        for (i in mask) {
            val regionNum = mask.getValue(i).toInt()
            if (regionNum > 0) {
                for (c in 0..images.imageCount - 1) {
                    channelIntensities.addToEntry(c, regionNum - 1, images.getImageForIndex(c)!!.getValue(i).toDouble())
                }
            }
        }

        for (i in 0..h.maxValue - 1) {
            for (c in 0..images.imageCount - 1) {
                channelIntensities.setEntry(c, i, channelIntensities.getEntry(c, i) / h.getCounts(i + 1))
            }
        }

        val q = Quantification()
        for (i in 0..h.maxValue - 1) {
            (0..images.imageCount - 1)
                    .asSequence()
                    .map {
                        Measurement(
                                hasFeature = true,
                                id = (i + 1).toLong(),
                                measurement = channelIntensities.getEntry(it, i),
                                name = "channel_" + it,
                                type = Measurement.TYPE_INTENSITY,
                                image = images.markerImageName!!) }
                    .forEach { q.addMeasurement(it) }
            val m = Measurement(
                    hasFeature = true,
                    id = (i + 1).toLong(),
                    measurement = h.getCounts(i + 1).toDouble(),
                    name = "pixel_count",
                    type = Measurement.TYPE_SIZE,
                    image = images.markerImageName!!)
            q.addMeasurement(m)
        }
        return q
    }
}
