package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * @author cfuller
 */
class TimepointSplittingMethod : Method() {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.method.Method#go()
	 */
    override fun go() {
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

        //now create the output images
        val split = reference.split(ImageCoordinate.T)
        for (singleT in split) {
            this.storeImageOutput(singleT)
        }
        dimSizes.recycle()
    }
}
