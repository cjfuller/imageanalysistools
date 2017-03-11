package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * @author cfuller
 */
class LaplacianFilterND : Filter() {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        val copy = ImageFactory.create(im)
        val icTemp = ImageCoordinate.createCoord()

        for (ic in im) {
            var count = 0
            var total = 0f
            icTemp.setCoord(ic)

            for (index in 0..ImageCoordinate.C - 1) { // a 3D hack for now until a better implementation is possible.  TODO: fix
                icTemp[index] = ic[index] + 1
                if (im.inBounds(icTemp)) {
                    count++
                    total += copy.getValue(icTemp)
                }

                icTemp[index] = ic[index] - 1
                if (im.inBounds(icTemp)) {
                    count++
                    total += copy.getValue(icTemp)
                }

                icTemp[index] = ic[index]

            }
            val laplacian = count * copy.getValue(ic) - total
            im.setValue(ic, laplacian)
        }
    }
}
