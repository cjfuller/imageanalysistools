package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction
import edu.stanford.cfuller.imageanalysistools.fitting.NelderMeadMinimizer
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter
import edu.stanford.cfuller.imageanalysistools.filter.GradientFilter
import org.apache.commons.math3.linear.ArrayRealVector

import org.apache.commons.math3.linear.RealVector

/**
 * A Filter that takes an image and segments it using an active contour-based method.
 *
 *
 * Currently, only one contour per image is supported, and an initial guess must be supplied.
 *
 *
 * After applying the filter, the argument to the apply method is replaced by a set of points
 * on the contour.  (These are unlikely to be continuous.)
 *
 *
 * This filter does not take a reference image.
 *
 *
 * The initial contour guess must be supplied as a set of point stored in a RealVector, ordered as:
 * {x0, y0, x1, y1, ..., xn, yn}.  These will be circularized for optimizing the contour such that the
 * nth point will be adjacent to the zeroth point.
 *
 *
 * The optimized contour points (in the same format as the initial contour must be applied) are available by
 * calling the [.getContourPoints] method after the apply method has been called.

 * @author Colin J. Fuller
 */
class ActiveContourFilter : Filter() {
    /**
     * Gets the points on the optimized contour after the apply method has been called
     * in the format specified in the class intro documentation.
     * @return    a RealVector containing the points on the contour.
     */
    var contourPoints: RealVector = ArrayRealVector()
        internal set

    internal var initialContour: RealVector = ArrayRealVector()

    /**
     * This class is the core of the active contour optimization and provides an "energy"
     * value given a set of points on the contour.  Implements ObjectiveFunction for use
     * with optimizers.
     */
    protected inner class ActiveContourObjectiveFunction
    /**
     * Constructs a new ActiveContourObjectiveFunction.
     * @param im  the Image that the contour is being used to segment.
     */
    (internal var image: WritableImage) : ObjectiveFunction {
        internal var gradientImage: WritableImage
        internal var gradient_weight: Double = 0.toDouble()
        internal var im_weight: Double = 0.toDouble()
        internal var elastic_weight: Double = 0.toDouble()
        internal var continuity_weight: Double = 0.toDouble()
        internal val defaultGaussianWidth = 5
        internal val defaultGradientWeight = -2.0
        internal val defaultImWeight = 0.0
        internal val defaultElasticWeight = 0.7
        internal val defaultContinuityWeight = 0.7

        init {
            val gausf = GaussianFilter()
            gausf.setWidth(defaultGaussianWidth)
            val gradf = GradientFilter()
            this.gradientImage = ImageFactory.createWritable(image)
            gausf.apply(this.gradientImage)
            gradf.apply(this.gradientImage)
            this.gradient_weight = defaultGradientWeight
            this.im_weight = defaultImWeight
            this.elastic_weight = defaultElasticWeight
            this.continuity_weight = defaultContinuityWeight
        }

        private fun calculateAverageDistance(parameters: RealVector): Double {
            var averageDistance = 0.0
            var i = 2
            while (i < parameters.dimension - 1) {
                val xm1 = parameters.getEntry(i - 2)
                val ym1 = parameters.getEntry(i - 1)
                val x = parameters.getEntry(i)
                val y = parameters.getEntry(i + 1)
                averageDistance += Math.pow(Math.pow(x - xm1, 2.0) + Math.pow(y - ym1, 2.0), 0.5)
                i += 2
            }

            val xm1 = parameters.getEntry(parameters.dimension - 2)
            val ym1 = parameters.getEntry(parameters.dimension - 1)
            val x = parameters.getEntry(0)
            val y = parameters.getEntry(1)
            averageDistance += Math.pow(Math.pow(x - xm1, 2.0) + Math.pow(y - ym1, 2.0), 0.5)
            averageDistance /= parameters.dimension / 2.0
            return averageDistance
        }

        /**
         * Evaluates the ObjectiveFunction, providing the energy value for
         * the supplied set of points on the contour.
         * @param parameters        A RealVector containing the parameters for the
         * * 						objective function, which in this case are the points
         * * 						on the contour in the format listed in the class documentation.
         * *
         * @return    a double that is the value of the contour energy function
         */
        override fun evaluate(parameters: RealVector): Double {
            return this.internalEnergy(parameters) + this.externalEnergy(parameters)
        }

        private fun continuityTerm(parameters: RealVector): Double {
            var ct = 0.0
            val averageDistance = this.calculateAverageDistance(parameters)
            var i = 2
            while (i < parameters.dimension - 1) {
                val xm1 = parameters.getEntry(i - 2)
                val ym1 = parameters.getEntry(i - 1)
                val x = parameters.getEntry(i)
                val y = parameters.getEntry(i + 1)
                val dist = Math.pow(Math.pow(x - xm1, 2.0) + Math.pow(y - ym1, 2.0), 0.5)
                ct += Math.pow(averageDistance - dist, 2.0)
                i += 2
            }

            //end condition
            val xm1 = parameters.getEntry(parameters.dimension - 2)
            val ym1 = parameters.getEntry(parameters.dimension - 1)
            val x = parameters.getEntry(0)
            val y = parameters.getEntry(1)
            val dist = Math.pow(Math.pow(x - xm1, 2.0) + Math.pow(y - ym1, 2.0), 0.5)
            ct += Math.pow(averageDistance - dist, 2.0)
            ct *= this.continuity_weight
            return ct
        }

        private fun curvatureTerm(parameters: RealVector): Double {
            var energy = 0.0
            var i = 2
            while (i < parameters.dimension - 2) {
                val xm1 = parameters.getEntry(i - 2)
                val ym1 = parameters.getEntry(i - 1)
                val x = parameters.getEntry(i)
                val y = parameters.getEntry(i + 1)
                val xp1 = parameters.getEntry(i + 2)
                val yp1 = parameters.getEntry(i + 3)
                energy += Math.pow(xm1 + xp1 - 2 * x, 2.0) + Math.pow(ym1 + yp1 - 2 * y, 2.0)
                i += 2
            }

            //now do end conditions
            val dim = parameters.dimension

            //first
            var xm1 = parameters.getEntry(dim - 2)
            var ym1 = parameters.getEntry(dim - 1)
            var x = parameters.getEntry(0)
            var y = parameters.getEntry(1)
            var xp1 = parameters.getEntry(2)
            var yp1 = parameters.getEntry(3)
            energy += Math.pow(xm1 + xp1 - 2 * x, 2.0) + Math.pow(ym1 + yp1 - 2 * y, 2.0)

            //last
            xm1 = parameters.getEntry(dim - 4)
            ym1 = parameters.getEntry(dim - 3)
            x = parameters.getEntry(dim - 2)
            y = parameters.getEntry(dim - 1)
            xp1 = parameters.getEntry(0)
            yp1 = parameters.getEntry(1)
            energy += Math.pow(xm1 + xp1 - 2 * x, 2.0) + Math.pow(ym1 + yp1 - 2 * y, 2.0)
            energy *= this.elastic_weight
            return energy
        }

        private fun internalEnergy(parameters: RealVector): Double {
            return continuityTerm(parameters) + curvatureTerm(parameters)
        }

        private fun externalEnergy(parameters: RealVector): Double {
            val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
            var energy = 0.0
            var i = 0
            while (i < parameters.dimension - 1) {
                ic[ImageCoordinate.X] = parameters.getEntry(i).toInt()
                ic[ImageCoordinate.Y] = parameters.getEntry(i + 1).toInt()
                energy += this.gradient_weight * Math.abs(this.gradientImage.getValue(ic))
                energy += this.im_weight * this.image.getValue(ic)
                i += 2
            }
            return energy
        }
    }

    /**
     * Applies the filter, replacing the supplied image by the set of points on the optimized contour.
     * The image will have value 1 at these points and 0 elsewhere.
     * @param im    the Image to be filtered.
     */
    override fun apply(im: WritableImage) {
        val acof = ActiveContourObjectiveFunction(im)
        val nmm = NelderMeadMinimizer()
        var out = nmm.optimize(acof, initialContour)
        out = nmm.optimize(acof, out)
        this.contourPoints = out

        im.forEach { im.setValue(it, 0.0f) }
        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        var i = 0
        while (i < this.contourPoints.dimension - 1) {
            ic[ImageCoordinate.X] = this.contourPoints.getEntry(i).toInt()
            ic[ImageCoordinate.Y] = this.contourPoints.getEntry(i + 1).toInt()
            im.setValue(ic, 1f)
            i += 2
        }
    }

    /**
     * Sets the initial guess for the contour.
     * @param    points    a RealVector containing the initial contour points in the format specified
     * * 					in the class intro documentation.
     */
    fun setInitialContour(points: RealVector) {
        this.initialContour = points
    }
}


