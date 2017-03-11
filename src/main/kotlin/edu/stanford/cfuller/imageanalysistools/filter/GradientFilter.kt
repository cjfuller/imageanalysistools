package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.util.FastMath

/**
 * A filter that takes the gradient of a 2D Image.
 *
 * This filter applies a horizontal and vertical (3x3 pixel) Prewitt gradient filter to an Image separately, and then replaces the
 * original Image with the sum of the two directional components in quadrature.
 *
 * This filter does not use a reference image.
 *
 * The argument to the apply function should be the Image that will be replaced by its gradient.

 * @author Colin J. Fuller
 */
class GradientFilter : Filter() {
    // TODO(colin): deal with more dimensions, or refactor to call GradientFilter2D or something similar.

    /**
     * Applies the GradientFilter to the specified Image.
     * @param im    The Image that will be replaced by its gradient.
     */
    override fun apply(im: WritableImage) {
        val kernelSize = 3
        val halfKernelSize = (kernelSize - 1) / 2

        val kernel1 = Array2DRowRealMatrix(kernelSize, kernelSize)
        kernel1.setEntry(0, 0, 1.0)
        kernel1.setEntry(1, 0, 0.0)
        kernel1.setEntry(2, 0, -1.0)
        kernel1.setEntry(0, 1, 1.0)
        kernel1.setEntry(1, 1, 0.0)
        kernel1.setEntry(2, 1, -1.0)
        kernel1.setEntry(0, 2, 1.0)
        kernel1.setEntry(1, 2, 0.0)
        kernel1.setEntry(2, 2, -1.0)

        val kernel2 = Array2DRowRealMatrix(kernelSize, kernelSize)
        kernel2.setEntry(0, 0, -1.0)
        kernel2.setEntry(1, 0, -1.0)
        kernel2.setEntry(2, 0, -1.0)
        kernel2.setEntry(0, 1, 0.0)
        kernel2.setEntry(1, 1, 0.0)
        kernel2.setEntry(2, 1, 0.0)
        kernel2.setEntry(0, 2, 1.0)
        kernel2.setEntry(1, 2, 1.0)
        kernel2.setEntry(2, 2, 1.0)

        val copy = ImageFactory.create(im)
        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in im) {
            var outputVal = 0.0
            var output1 = 0.0
            var output2 = 0.0

            if (i[ImageCoordinate.X] == 0 || i[ImageCoordinate.Y] == 0 || i[ImageCoordinate.X] == copy.dimensionSizes[ImageCoordinate.X] - 1 || i[ImageCoordinate.Y] == copy.dimensionSizes[ImageCoordinate.Y] - 1) {
                outputVal = 0.0
            } else {
                for (p in -1 * halfKernelSize..halfKernelSize + 1 - 1) {
                    for (q in -1 * halfKernelSize..halfKernelSize + 1 - 1) {
                        ic[ImageCoordinate.X] = i[ImageCoordinate.X] + p
                        ic[ImageCoordinate.Y] = i[ImageCoordinate.Y] + q
                        output1 += kernel1.getEntry(p + halfKernelSize, q + halfKernelSize) * copy.getValue(ic)
                        output2 += kernel2.getEntry(p + halfKernelSize, q + halfKernelSize) * copy.getValue(ic)
                    }
                }
                outputVal = FastMath.hypot(output1, output2)
            }
            im.setValue(i, Math.floor(outputVal).toFloat())
        }
        ic.recycle()
    }
}
