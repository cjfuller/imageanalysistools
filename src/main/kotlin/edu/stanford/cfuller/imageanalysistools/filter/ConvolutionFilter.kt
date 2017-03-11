package edu.stanford.cfuller.imageanalysistools.filter

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.FastFourierTransformer

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * Convolves a 2D x-y Image with a 2D kernel.
 *
 *
 * Before calling apply, the kernel must be specified using the [.setKernel] method.
 *
 *
 * This filter does not use a reference Image.
 *
 *
 * After calling the apply method, the image argument to apply will be overwritten by the convolved Image.

 * @author Colin J. Fuller
 */
class ConvolutionFilter : Filter() {
    // TODO: deal with boundary conditions of types other than zero.
    // TODO: deal with something other than single image plane transforms.

    internal var k: Kernel = Kernel.noOp()
    internal var transformStorage: Array<Array<Complex>>? = null

    override fun apply(im: WritableImage) {
        val tfStorage = this.transformStorage
        val transformed: Array<Array<Complex>> = if (tfStorage != null) {
            val tf = Array<Array<Complex>>(tfStorage.size) { i ->
                Array<Complex>(tfStorage[0].size) { j ->
                    tfStorage[i][j]
                }
            }
            tf
        } else {
            ConvolutionFilter.transform(im, 0)
        }

        val kernelTransform = this.k.getTransformed2DKernel(transformed[0].size, transformed.size)

        for (i in transformed.indices) {
            for (j in transformed.indices) {
                transformed[i][j] = transformed[i][j].multiply(kernelTransform[i][j])
            }
        }

        inverseTransform(im, 0, transformed)
    }

    fun setTransform(transformed: Array<Array<Complex>>) {
        this.transformStorage = transformed
    }

    fun setKernel(k: Kernel) {
        this.k = k
    }

    fun inverseTransform(orig: WritableImage, z: Int, transformed: Array<Array<Complex>>) {
        val colMajorImage = transformed
        val rowImage = Array(colMajorImage[0].size) { DoubleArray(colMajorImage.size) }
        val fft = org.apache.commons.math3.transform.FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD)

        for (c in colMajorImage.indices) {
            colMajorImage[c] = fft.transform(colMajorImage[c], org.apache.commons.math3.transform.TransformType.INVERSE)
        }

        val tempRow = arrayOfNulls<Complex>(rowImage.size)

        //also calculate min/max values
        var newMin = java.lang.Double.MAX_VALUE
        var newMax = 0.0

        for (r in rowImage.indices) {

            for (c in colMajorImage.indices) {
                tempRow[c] = colMajorImage[c][r]
            }

            val transformedRow = fft.transform(tempRow, org.apache.commons.math3.transform.TransformType.INVERSE)

            for (c in colMajorImage.indices) {
                rowImage[r][c] = transformedRow[c].abs()
                if (rowImage[r][c] < newMin) newMin = rowImage[r][c]
                if (rowImage[r][c] > newMax) newMax = rowImage[r][c]
            }
        }

        //rescale values to same min/max as before

        val h = Histogram(orig)

        var oldMin = h.minValue.toDouble()
        val oldMax = h.maxValue.toDouble()

        var scaleFactor = (oldMax - oldMin) / (newMax - newMin)

        val minCoord = ImageCoordinate.createCoordXYZCT(0, 0, z, 0, 0)
        val maxCoord = ImageCoordinate.cloneCoord(orig.dimensionSizes)
        maxCoord[ImageCoordinate.Z] = z + 1

        orig.setBoxOfInterest(minCoord, maxCoord)

        scaleFactor = 1.0
        oldMin = 0.0

        for (ic in orig) {
            orig.setValue(ic, ((rowImage[ic[ImageCoordinate.Y]][ic[ImageCoordinate.X]] - newMin) * scaleFactor + oldMin).toFloat())
        }

        orig.clearBoxOfInterest()
        minCoord.recycle()
        maxCoord.recycle()
    }

    companion object {
        fun transform(im: Image, z: Int): Array<Array<Complex>> {
            val fft = org.apache.commons.math3.transform.FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD)
            var ydimPowOfTwo = im.dimensionSizes[ImageCoordinate.Y]
            var xdimPowOfTwo = im.dimensionSizes[ImageCoordinate.X]

            if (!org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(ydimPowOfTwo.toLong()) || !org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(xdimPowOfTwo.toLong())) {
                xdimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(im.dimensionSizes[ImageCoordinate.X].toDouble()) / Math.log(2.0))).toInt()
                ydimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(im.dimensionSizes[ImageCoordinate.Y].toDouble()) / Math.log(2.0))).toInt()
            }

            val p = z
            im.selectPlane(p)

            val rowImage = Array(ydimPowOfTwo) { DoubleArray(xdimPowOfTwo) }
            for (i in 0..ydimPowOfTwo - 1) {
                java.util.Arrays.fill(rowImage[i], 0.0) // ensures zero-padding
            }
            for (ic in im) {
                rowImage[ic[ImageCoordinate.Y]][ic[ImageCoordinate.X]] = im.getValue(ic).toDouble()
            }
            val colMajorImage = Array<Array<Complex>>(xdimPowOfTwo) { Array<Complex>(ydimPowOfTwo, { Complex(0.0, 0.0) }) }

            for (r in rowImage.indices) {
                val row = rowImage[r]
                val transformedRow = fft.transform(row, org.apache.commons.math3.transform.TransformType.FORWARD)

                for (c in colMajorImage.indices) {
                    colMajorImage[c][r] = transformedRow[c]
                }
            }

            for (c in colMajorImage.indices) {
                colMajorImage[c] = fft.transform(colMajorImage[c], org.apache.commons.math3.transform.TransformType.FORWARD)
            }
            return colMajorImage
        }
    }
}
