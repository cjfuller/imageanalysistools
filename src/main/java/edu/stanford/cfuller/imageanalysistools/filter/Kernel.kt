/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.FastFourierTransformer

import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * @author Colin J. Fuller
 */
class Kernel {
    // TODO: implementation of nonzero boundary conditions
    // TODO: complete documentation

    /**
     * Gets an ImageCoordinate containing the half size of each dimension of the kernel (that is, each dimension is 2*n+1 pixels, if n is the half size).
     * @return        an ImageCoordinate containing the half sizes; a reference, not a copy-- do not modify or recycle.
     */
    var halfSize: ImageCoordinate = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        internal set

    internal var weights: DoubleArray = doubleArrayOf()

    internal var transform: Array<Array<Complex>>? = null

    /**
     * Gets the method by which boundary conditions should be dealt with at the edge of images.
     * @return    The boundary type, which will be one of the public boundary type constants of the Kernel class.
     */
    var boundaryType: Int = 0
        internal set

    private constructor() {}

    /**
     * Creates a new Kernel object initialized with the specified weights and the specified dimension sizes.
     *
     *
     * The weights should be ordered in a 1-d array such that the ith dimension
     * of the ImageCoordinate is minor compared to the (i+1)th dimension.  (For example, for a 5d ImageCoordinate, with
     * a zeroth dimension of size n, the zeroth entry is (0,0,0,0,0), the first (1,0,0,0,0), the nth (0,1,0,0,0), etc.
     *
     *
     * @param weights            a 1-D matrix of the weights in the kernel for each pixel surrounding a given pixel, whose self-weight is at the midpoint of each dimension.
     * @param dimensionSizes    and ImageCoordinate containing the full size of each dimension in the kernel.  All entries must be odd.
     * @throws IllegalArgumentException    if the dimensionSizes are not odd, or are negative.
     */
    constructor(weights: DoubleArray, dimensionSizes: ImageCoordinate) {
        dimensionSizes
                .asSequence()
                .map { dimensionSizes[it] }
                .filter { it % 2 == 0 || it < 0 }
                .forEach { throw IllegalArgumentException("Kernel size must be odd and positive in all dimensions.") }

        this.weights = weights
        this.halfSize = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (s in this.halfSize) {
            this.halfSize[s] = (dimensionSizes[s] - 1) / 2
        }
        this.boundaryType = BOUNDARY_ZERO
    }

    fun formatTransformFrom1DInput(size0: Int, size1: Int) {
        var ydimPowOfTwo = size0
        var xdimPowOfTwo = size1

        if (!org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(ydimPowOfTwo.toLong()) || !org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(xdimPowOfTwo.toLong())) {
            xdimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size1.toDouble()) / Math.log(2.0))).toInt()
            ydimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size0.toDouble()) / Math.log(2.0))).toInt()
        }

        val colMajorImage = Array<Array<Complex>>(xdimPowOfTwo) { Array<Complex>(ydimPowOfTwo, { Complex(0.0, 0.0) }) }
        var counter = 0

        for (i in colMajorImage.indices) {
            for (j in 0..colMajorImage[0].size - 1) {
                colMajorImage[i][j] = Complex(this.weights[counter++], this.weights[counter++])
            }
        }
        this.transform = colMajorImage
    }

    /**
     * Gets the weight of a pixel relative to another pixel, given the weights spcified on creation of the kernel.
     * @param currentPixel        The central pixel, relative to which the weight is being computed.
     * *
     * @param relativePixel        The pixel whose weight relative to the central pixel is being computed.  (Note that
     * * 							this should be an absolute coordinate, and not a coordinate relative to the currentPixel.)
     * *
     * @return                    The weight of the relativePixel, relative to the currentPixel.
     */
    fun getWeight(currentPixel: ImageCoordinate, relativePixel: ImageCoordinate): Double {
        var index = 0
        var accumulatedOffset = 1

        for (i in 0..currentPixel.dimension - 1) {
            val temp = relativePixel[i] - currentPixel[i]
            if (temp < -1 * halfSize[i] || temp > halfSize[i]) {
                return 0.0
            }
            val tempOffset = temp + halfSize[i]
            index += tempOffset * accumulatedOffset
            accumulatedOffset *= halfSize[i] * 2 + 1
        }

        if (index >= weights.size || index < 0) return 0.0
        return weights[index]
    }

    @Throws(Throwable::class)
    private fun finalize() {
        this.halfSize.recycle()
    }

    fun getTransformed2DKernel(size0: Int, size1: Int): Array<Array<Complex>> {
        val transform = this.transform
        if (transform != null) {
            return transform
        }

        val fft = org.apache.commons.math3.transform.FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD)
        var ydimPowOfTwo = size0
        var xdimPowOfTwo = size1

        if (!org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(ydimPowOfTwo.toLong()) || !org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(xdimPowOfTwo.toLong())) {
            xdimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size1.toDouble()) / Math.log(2.0))).toInt()
            ydimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size0.toDouble()) / Math.log(2.0))).toInt()
        }

        val preTransform = Array(ydimPowOfTwo) { DoubleArray(xdimPowOfTwo) }
        var counter = 0

        for (i in 0..this.halfSize[ImageCoordinate.Y] + 1 - 1) {
            for (j in 0..this.halfSize[ImageCoordinate.X] + 1 - 1) {
                preTransform[i][j] = this.weights[counter++]
            }
        }

        for (i in this.halfSize[ImageCoordinate.Y] + 1..ydimPowOfTwo - this.halfSize[ImageCoordinate.Y] - 1) {
            for (j in this.halfSize[ImageCoordinate.X] + 1..xdimPowOfTwo - this.halfSize[ImageCoordinate.X] - 1) {
                preTransform[i][j] = 0.0
            }
        }

        for (i in ydimPowOfTwo - this.halfSize[ImageCoordinate.Y]..ydimPowOfTwo - 1) {
            for (j in xdimPowOfTwo - this.halfSize[ImageCoordinate.X]..xdimPowOfTwo - 1) {
                preTransform[i][j] = this.weights[counter++]
            }
        }

        val rowImage = preTransform
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
        this.transform = colMajorImage
        return colMajorImage
    }

    companion object {
        val BOUNDARY_ZERO = 0
        val BOUNDARY_REPEAT = 1
        val BOUNDARY_CIRCULAR = 2

        fun noOp(): Kernel {
            return Kernel(doubleArrayOf(1.0), ImageCoordinate.createCoordXYZCT(1,1,1,1,1))
        }

        fun getRandomSinglePlaneKernelMatrix(size0: Int, size1: Int): Array<Array<Complex>> {
            val toReturn = Array<Array<Complex>>(size0) { Array<Complex>(size1, { Complex(0.0, 0.0) }) }
            for (i in 0..size0 - 1) {
                for (j in 0..size1 - 1) {
                    var angle = Math.random() * 2 - 1
                    angle = Math.acos(angle)
                    toReturn[i][j] = Complex(Math.random() * 2 - 1, Math.random() * 2 - 1)
                }
            }
            return toReturn
        }

        fun getTransformedRandomSinglePlaneKernelMatrix(size0: Int, size1: Int, sizeNonzero: Int): Array<Array<Complex>> {
            val halfSize = (sizeNonzero - 1) / 2
            val fft = org.apache.commons.math3.transform.FastFourierTransformer(org.apache.commons.math3.transform.DftNormalization.STANDARD)
            var ydimPowOfTwo = size0
            var xdimPowOfTwo = size1

            if (!org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(ydimPowOfTwo.toLong()) || !org.apache.commons.math3.util.ArithmeticUtils.isPowerOfTwo(xdimPowOfTwo.toLong())) {
                xdimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size1.toDouble()) / Math.log(2.0))).toInt()
                ydimPowOfTwo = Math.pow(2.0, Math.ceil(Math.log(size0.toDouble()) / Math.log(2.0))).toInt()
            }

            val preTransform = Array(ydimPowOfTwo) { DoubleArray(xdimPowOfTwo) }

            for (i in 0..halfSize + 1 - 1) {
                for (j in 0..halfSize + 1 - 1) {
                    preTransform[i][j] = Math.random() * 2 - 1
                }
            }

            for (i in halfSize + 1..ydimPowOfTwo - halfSize - 1) {
                for (j in halfSize + 1..xdimPowOfTwo - halfSize - 1) {
                    preTransform[i][j] = 0.0
                }
            }

            for (i in ydimPowOfTwo - halfSize..ydimPowOfTwo - 1) {
                for (j in xdimPowOfTwo - halfSize..xdimPowOfTwo - 1) {
                    preTransform[i][j] = Math.random() * 2 - 1
                }
            }

            val rowImage = preTransform
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
