package edu.stanford.cfuller.imageanalysistools.filter

import java.util.Deque

import org.apache.commons.math3.distribution.FDistribution
import org.apache.commons.math3.exception.MathIllegalArgumentException

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * Applies a QO-tree segmentation to an image and replaces pixel values with the
 * mean over the leaf of the tree containing each pixel.  The size of the leaf
 * is determined by comparing the variance of the noise to the variance of the
 * pixel values.  (See the reference for the exact method.)
 *
 *
 * Reference: Boulanger et al., doi:10.1109/TMI.2009.2033991
 *
 *
 * The argument to the apply method should be the Image to which to apply the
 * QOtree segmentation and mean filtering.
 *
 *
 * This filter does not use a reference Image.

 * @author Colin J. Fuller
 */
class VariableSizeMeanFilter : Filter() {
    private var minBoxSize: Int = 2

    /**
     * Sets the minimum box size of the resulting segmentation.
     *
     *
     * The box size is the linear dimension of the smallest possible volume from
     * the QOTree segmentation.
     *
     * @param size        The linear dimension of the box size.
     */
    fun setBoxSize(size: Int) {
        this.minBoxSize = 2 * size //factor of 2 is because it may subdivide once beyond this
    }

    private inner class OcttreeNode(boxMin: ImageCoordinate, boxMax: ImageCoordinate) {
        // TODO(colin): proper null handling
        var boxMin: ImageCoordinate? = null
            internal set
        var boxMax: ImageCoordinate? = null
            internal set

        internal var children: MutableList<OcttreeNode>

        init {
            this.boxMin = boxMin
            this.boxMax = boxMax
            this.children = java.util.ArrayList<OcttreeNode>(8)
        }

        fun subDivide(): Boolean {
            val dim_mults = IntArray(3)
            // TODO(colin): fix this use of X..Z
            // continue as long as we can subdivide at least one dimension
            val succeeded = (ImageCoordinate.X..ImageCoordinate.Z).any {
                this.boxMin!![it] + minBoxSize < this.boxMax!![it]
            }
            if (!succeeded) return succeeded

            // loop over each dimension, dividing it into two
            for (x in 0..1) {
                for (y in 0..1) {
                    for (z in 0..1) {
                        dim_mults[0] = x
                        dim_mults[1] = y
                        dim_mults[2] = z
                        val boxMin_new = ImageCoordinate.cloneCoord(this.boxMin!!)
                        val boxMax_new = ImageCoordinate.cloneCoord(this.boxMax!!)

                        // continue if we can't divide this dimension and would otherwise put in two children with the same range
                        // TODO(colin): fix this use of X..Z
                        val skip = (ImageCoordinate.X..ImageCoordinate.Z).any {
                            this.boxMin!![it] + minBoxSize >= this.boxMax!![it] && dim_mults[it] == 0
                        }
                        if (skip) continue

                        //otherwise, divide
                        for (dim in ImageCoordinate.X..ImageCoordinate.Z) { //TODO: fix this
                            //if we shouldn't divide this dimension, leave it the same size
                            if (this.boxMin!![dim] + minBoxSize >= this.boxMax!![dim]) {
                                continue
                            }

                            //divide the dimension

                            boxMin_new[dim] = this.boxMin!![dim] + dim_mults[dim] * ((this.boxMax!![dim] - this.boxMin!![dim]) / 2)
                            boxMax_new[dim] = dim_mults[dim] * this.boxMax!![dim] + (1 - dim_mults[dim]) * (this.boxMin!![dim] + (1 - dim_mults[dim]) * ((this.boxMax!![dim] - this.boxMin!![dim]) / 2))
                        }

                        //add new nodes for the divided children
                        children.add(OcttreeNode(boxMin_new, boxMax_new))
                    }
                }
            }
            return true
        }

        fun getChildren(): List<OcttreeNode> {
            return this.children
        }

        protected fun finalize() {
            this.boxMin!!.recycle()
            this.boxMax!!.recycle()
            this.boxMin = null
            this.boxMax = null
            // TODO(colin): why won't it let me call super.finalize()?
            //super.finalize()
        }
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        //calculate Laplacian of Image, calculate pseudo-residual (as in Boulanger, 2010)
        val residual = ImageFactory.createWritable(im)
        val LF = LaplacianFilterND()
        LF.apply(residual)

        //for 3D, residual is Laplacian divided by sqrt(56)
        val norm = Math.sqrt(56.0).toFloat()
        //for 2D, residual is sqrt(30)
        //norm = Math.sqrt(30);

        for (ic in residual) {
            residual.setValue(ic, residual.getValue(ic) / norm)
        }

        //perform an octtree segmentation of the Image, using a criterion based on relative variance of image and noise (as in Boulanger, 2010)
        val root = OcttreeNode(ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0), ImageCoordinate.cloneCoord(im.dimensionSizes))

        if (this.shouldSubDivide(root, im, residual)) {
            root.subDivide()
        }

        val queue = java.util.ArrayDeque<OcttreeNode>()
        queue.addAll(root.getChildren())
        val leaves = java.util.ArrayList<OcttreeNode>()

        while (!queue.isEmpty()) {
            val current = queue.pop()
            if (this.shouldSubDivide(current, im, residual) && current.subDivide()) {
                queue.addAll(current.getChildren())
            } else {
                leaves.add(current)
            }
        }

        for (node in leaves) {
            var count = 0.0
            var mean = 0f
            im.setBoxOfInterest(node.boxMin!!, node.boxMax!!)

            for (ic in im) {
                mean += im.getValue(ic)
                count++
            }
            mean /= count.toFloat()

            for (ic in im) {
                im.setValue(ic, mean)
            }
            im.clearBoxOfInterest()
        }
    }

    private fun shouldSubDivide(node: OcttreeNode, im: Image, laplacianFiltered: Image): Boolean {
        im.setBoxOfInterest(node.boxMin!!, node.boxMax!!)
        laplacianFiltered.setBoxOfInterest(node.boxMin!!, node.boxMax!!)

        var l_sum = 0.0
        var sum = 0.0
        var count = 0.0
        for (ic in im) {
            l_sum += laplacianFiltered.getValue(ic).toDouble()
            sum += im.getValue(ic).toDouble()
            count++
        }
        if (count == 1.0) return false
        l_sum /= count
        sum /= count
        var l_var = 0.0
        var `var` = 0.0
        for (ic in im) {
            l_var += Math.pow(laplacianFiltered.getValue(ic) - l_sum, 2.0)
            `var` += Math.pow(im.getValue(ic) - sum, 2.0)
        }
        l_var /= count - 1
        `var` /= count - 1
        im.clearBoxOfInterest()
        laplacianFiltered.clearBoxOfInterest()

        val cutoff = 0.0001
        val smallerVar = if (`var` < l_var) `var` else l_var
        val largerVar = if (`var` > l_var) `var` else l_var
        try {
            val f = FDistribution(count - 1, count - 1)
            val valueAtLowerCutoff = f.inverseCumulativeProbability(cutoff)
            val valueAtUpperCutoff = f.inverseCumulativeProbability(1 - cutoff)
            val result = smallerVar / largerVar > valueAtUpperCutoff || smallerVar / largerVar < valueAtLowerCutoff
            return result
        } catch (e: MathIllegalArgumentException) {
            LoggingUtilities.logger.severe("Exception while calculating variable size mean QO partition: " + e.message)
            e.printStackTrace()
            return false
        }
    }
}
