package edu.stanford.cfuller.imageanalysistools.filter

import java.util.ArrayList
import java.util.HashMap

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * Filters an Image in an arbitrary number of dimensions using a specified kernel.
 *
 *
 * Currently only supports independent kernels for each dimension, such that at
 * any point, the filtering due to the combination of kernels is the product of
 * the filtering by the kernel in each dimension.

 * @author Colin J. Fuller
 */
class KernelFilterND : Filter() {
    internal var dimensionsToFilter: MutableList<Int> = ArrayList<Int>()
    // TODO: reimplement Kernel class to be more ND-friendly and use this instead.
    internal var kernelByDimension: MutableMap<Int, DoubleArray> = HashMap<Int, DoubleArray>()
    internal var halfDimensionSizes: MutableMap<Int, Int> = HashMap<Int, Int>()

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        for (i in this.dimensionsToFilter.indices) {
            val original = ImageFactory.create(im)
            val dim = this.dimensionsToFilter[i]
            val size = halfDimensionSizes[dim]
            val currDimKernel = kernelByDimension[dim]

            size ?: throw AssertionError("Unexpectedly found null size in apply.")
            currDimKernel ?: throw AssertionError("Unexpectedly found null kernel in apply.")

            for (ic in im) {
                val ic2 = ImageCoordinate.cloneCoord(ic)
                var kernelTotal = 0.0
                var filterTotal = 0.0
                val currPosOffset = size - ic[dim]

                val min = ic[dim] - size
                val max = ic[dim] + size + 1

                for (dimValue in min..max - 1) {
                    ic2[dim] = dimValue
                    val kernelOffset = dimValue + currPosOffset
                    val currKernelValue = currDimKernel[kernelOffset]
                    kernelTotal += currKernelValue
                    var imageValue = 0f

                    if (original.inBounds(ic2)) {
                        imageValue = original.getValue(ic2)
                    }
                    filterTotal += currKernelValue * imageValue
                }
                im.setValue(ic, (filterTotal / kernelTotal).toFloat())
            }
        }
    }

    /**
     * Adds a dimension to be filtered.  The integer dimension must be specified
     * by the same integer used to specify it in an ImageCoordinate.  The kernel
     * can have any size >= 1, and it must have an odd number of elements.

     * @param dimension        The dimension to filter.
     * *
     * @param kernel        The kernel that will be applied to the specified dimension.
     */
    fun addDimensionWithKernel(dimension: Int, kernel: DoubleArray) {
        this.dimensionsToFilter.add(dimension)
        this.kernelByDimension.put(dimension, kernel)
        this.halfDimensionSizes.put(dimension, (kernel.size - 1) / 2)
    }
}
