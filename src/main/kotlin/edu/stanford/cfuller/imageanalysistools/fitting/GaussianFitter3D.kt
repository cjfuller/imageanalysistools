package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

import org.apache.commons.math3.optimization.direct.SimplexOptimizer
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex
import org.apache.commons.math3.optimization.PointValuePair

/**
 * Performs a three-dimensional gaussian fit to an object in an image using a maximum likelihood method assuming Poisson
 * distributed pixel intensities (once converted to units of photons).
 * @author Colin J. Fuller
 */
class GaussianFitter3D {
    /**
     * Fits a 3D Gaussian to a supplied object, starting from an initial guess of the parameters of that Gaussian.
     * The Gaussian is contrained to be symmetric in the x and y dimensions (that is, it will have equal variance in both dimensions).
     * @param toFit         The [ImageObject] to be fit to a Gaussian.
     * @param initialGuess  The initial guess at the parameters of the Gaussian.  These must be supplied in the order: amplitude, x-y stddev, z stddev, x position, y position, z position, background.  Positions should be supplied in absolute coordinates from the original image, not relative to the box around the object being fit.
     * @param ppg           The number of photons corresponding to one greylevel in the original image.
     * @return              The best fit Gaussian parameters, in the same order as the initial guess had to be supplied.
     */
    fun fit(toFit: ImageObject, initialGuess: RealVector, ppg: Double): RealVector {
        //parameter ordering: amplitude, stddev x-y, stddev z, x/y/z coords, background
        val tol = 1.0e-6
        val nmm = SimplexOptimizer(tol, tol)
        val nms = NelderMeadSimplex(initialGuess.dimension)
        nmm.setSimplex(nms)
        val pvp = nmm.optimize(10000000, MLObjectiveFunction(toFit, ppg), org.apache.commons.math3.optimization.GoalType.MINIMIZE, initialGuess.toArray())
        val result = ArrayRealVector(pvp.point)
        return result
    }

    private inner class MLObjectiveFunction(private val toFit: ImageObject, private val ppg: Double) : ObjectiveFunction, MultivariateFunction {
        override fun evaluate(parameters: RealVector): Double {
            return value(parameters.toArray())
        }

        override fun value(parameters: DoubleArray): Double {
            return logLikelihood(this.toFit, parameters, this.ppg)
        }
    }

    companion object {
        private fun gaussian(x: Double, y: Double, z: Double, parameters: DoubleArray): Double {
            val xmx0 = x - parameters[3]
            val ymy0 = y - parameters[4]
            val zmz0 = z - parameters[5]
            val vxy = parameters[1]
            val vz = parameters[2]
            val A = parameters[0]
            val b = parameters[6]
            return A * Math.exp(-(xmx0 * xmx0 + ymy0 * ymy0) / (2.0 * vxy * vxy) - zmz0 * zmz0 / (2.0 * vz * vz)) + b
        }

        private fun poissonProb(x: Double, y: Double, z: Double, parameters: DoubleArray, n: Double): Double {
            val mean = gaussian(x, y, z, parameters)
            return n * Math.log(mean) - mean - logFactorial(Math.round(n).toInt() + 1)
        }

        private fun logLikelihood(toFit: ImageObject, parameters: DoubleArray, ppg: Double): Double {
            var logLikelihood = 0.0
            val x = toFit.getxValues()
            val y = toFit.getyValues()
            val z = toFit.getzValues()
            val f = toFit.functionValues

            for (i in x.indices) {
                logLikelihood -= poissonProb(x[i], y[i], z[i], parameters, f!![i])
            }
            return logLikelihood
        }

        /**
         * Calculates the fit residual between a set of parameters and a value at a supplied point.
         * @param value         The observed value at the point.
         * @param x             The x-coordinate of the point (in absolute original image coordinates)
         * @param y             The y-coordinate of the point (in absolute original image coordinates)
         * @param z             The z-coordinate of the point (in absolute original image coordinates.
         * @param parameters    The gaussian parameters in the same order as would be required for or returned from [.fit]
         * @return              The fit residual at the specified point.
         */
        fun fitResidual(value: Double, x: Double, y: Double, z: Double, parameters: RealVector): Double {
            return value - gaussian(x, y, z, parameters.toArray())
        }

        private fun logFactorial(n: Int): Double {
            if (n < 0) return 0.0
            return org.apache.commons.math3.special.Gamma.logGamma((n + 1).toDouble())
        }
    }
}
