package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * Applies a Laplacian filter to an Image.
 *
 *
 * This filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image to be filtered.
 *
 * @author Colin J. Fuller
 */
class LaplacianFilter : Filter() {
    /**
     * Applies a Laplacian filter to an Image.
     * @param im    The Image to be filtered; this will be replaced by the Laplacian-filtered Image
     */
    override fun apply(im: WritableImage) {
        val numEl = kernel[kernelHalfSize][kernelHalfSize].toInt()
        val newIm = ImageFactory.createWritable(im)
        var minValue = java.lang.Float.MAX_VALUE

        for (ic in im) {
            var newValue = 0f
            val icTemp = ImageCoordinate.cloneCoord(ic)
            var count = -1 //subtract one for the center pixel

            for (i in kernel.indices) {
                for (j in 0..kernel[0].size - 1) {
                    icTemp[ImageCoordinate.X] = ic[ImageCoordinate.X] + j - kernelHalfSize
                    icTemp[ImageCoordinate.Y] = ic[ImageCoordinate.Y] + i - kernelHalfSize

                    if (!im.inBounds(icTemp)) {
                        continue
                    }
                    count++
                    newValue += (im.getValue(icTemp) * kernel[i][j]).toFloat()
                }
            }

            if (count < numEl) {
                newValue -= (numEl - count) * im.getValue(ic)
            }
            newIm.setValue(ic, newValue)
            if (newValue < minValue) minValue = newValue
        }

        for (ic in im) {
            im.setValue(ic, newIm.getValue(ic) - minValue)
        }
    }

    companion object {
        internal var kernel: Array<DoubleArray>
        internal val kernelHalfSize = 1

        init {
            kernel = Array(2 * kernelHalfSize + 1) { DoubleArray(2 * kernelHalfSize + 1) }
            kernel[0][0] = -1.0
            kernel[0][1] = -1.0
            kernel[0][2] = -1.0
            kernel[1][0] = -1.0
            kernel[1][1] = 8.0
            kernel[1][2] = -1.0
            kernel[2][0] = -1.0
            kernel[2][1] = -1.0
            kernel[2][2] = -1.0
        }
    }
}
