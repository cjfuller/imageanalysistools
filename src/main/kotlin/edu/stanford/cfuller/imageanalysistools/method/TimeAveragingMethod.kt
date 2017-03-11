package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.TimeAveragingFilter
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A method that time averages images, and stores the time-averaged result in place of the usual mask output.
 * @author Colin J. Fuller
 */
class TimeAveragingMethod : Method() {
    //TODO: handle time averaging (or arbitrary dimension averaging?) for images other than 5D.
    override fun go() {
        val taf = TimeAveragingFilter()
        taf.setParameters(this.parameters)

        //first create the reference Image
        val dimSizes = ImageCoordinate.cloneCoord(this.images[0].dimensionSizes)
        dimSizes[ImageCoordinate.C] = this.images.size
        val reference = ImageFactory.createWritable(dimSizes, 0.0f)

        for (ic in reference) {
            val ic_c = ImageCoordinate.cloneCoord(ic)
            ic_c[ImageCoordinate.C] = 0
            reference.setValue(ic, this.imageSet.getImageForIndex(ic[ImageCoordinate.C])!!.getValue(ic_c))
            ic_c.recycle()
        }
        taf.referenceImage = reference

        //now create the output image
        dimSizes[ImageCoordinate.T] = 1
        val timeAveraged = ImageFactory.createWritable(dimSizes, 0.0f)
        val filters = java.util.Vector<Filter>()
        filters.add(taf)
        iterateOnFiltersAndStoreResult(filters, timeAveraged, null)
        dimSizes.recycle()
    }
}
