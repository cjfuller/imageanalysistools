/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

/**
 * Performs a three-dimensional gaussian fit to an object in an image using a maximum likelihood method assuming Poisson
 * distributed pixel intensities (once converted to units of photons).  Allows nonzero covariances and unequal variances for the three dimensions.

 * @author Colin J. Fuller
 */
class GaussianFitter3DWithCovariance {


    /**
     * Fits a 3D Gaussian to a supplied object, starting from an initial guess of the parameters of that Gaussian.


     * @param toFit         The [ImageObject] to be fit to a Gaussian.
     * *
     * @param initialGuess  The initial guess at the parameters of the Gaussian.  These must be supplied in the order: amplitude, x stddev, y stddev, z stddev, x-y corr, x-z corr, y-z corr, x position, y position, z position, background.  Positions should be supplied in absolute coordinates from the original image, not relative to the box around the object being fit.
     * *
     * @param ppg           The number of photons corresponding to one greylevel in the original image.
     * *
     * @return              The best fit Gaussian parameters, in the same order as the initial guess had to be supplied.
     */
    fun fit(toFit: ImageObject, initialGuess: RealVector, ppg: Double): RealVector {

        //parameter ordering: amplitude, stddev x-y, stddev z, x/y/z coords, background

        //System.out.println("for object " + toFit.getLabel() + " in image: " + toFit.getImageID() + " initial guess is: " + initialGuess.toString());


        val tol = 1.0e-6
        val nmm = NelderMeadMinimizer(tol)

        var result = nmm.optimize(MLObjectiveFunction(toFit, ppg), initialGuess)
        //System.out.println("for object " + toFit.getLabel() + " in image: " + toFit.getImageID() + " result iter 1 is: " + result.toString());

        result = nmm.optimize(MLObjectiveFunction(toFit, ppg), result)
        //System.out.println("for object " + toFit.getLabel() + " in image: " + toFit.getImageID() + " result iter 2 is: " + result.toString());

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

        internal var logFactorialLookups: RealVector

        internal var maxLogFactorialPrecalculated: Int = 0

        init {

            synchronized(GaussianFitter3DWithCovariance::class.java) {

                logFactorialLookups = ArrayRealVector(65536, 0.0)
                maxLogFactorialPrecalculated = -1

            }
        }

        private fun gaussian(x: Double, y: Double, z: Double, parameters: DoubleArray): Double {

            val xmx0 = x - parameters[7]
            val ymy0 = y - parameters[8]
            val zmz0 = z - parameters[9]
            val v1 = parameters[1]
            val v2 = parameters[2]
            val v3 = parameters[3]
            val r12 = parameters[4]
            val r13 = parameters[5]
            val r23 = parameters[6]
            val A = parameters[0]
            val b = parameters[10]

            val denom = 2.0 * r12 * r13 * r23 - r23 * r23 * v1 - r13 * r13 * v2 - r12 * r12 * v3 + v1 * v2 * v3

            val term0 = -0.5 * zmz0 * (ymy0 * (r12 * r13 - r23 * v1) / denom + xmx0 * (r12 * r23 - r13 * v2) / denom + zmz0 * (-r12 * r12 + v1 * v2) / denom)
            val term1 = -0.5 * ymy0 * (zmz0 * (r12 * r13 - r23 * v1) / denom + xmx0 * (r13 * r23 - r12 * v3) / denom + ymy0 * (-r13 * r13 + v1 * v3) / denom)
            val term2 = -0.5 * xmx0 * (zmz0 * (r12 * r23 - r13 * v2) / denom + ymy0 * (r13 * r23 - r12 * v3) / denom + xmx0 * (-r23 * r23 + v2 * v3) / denom)

            return A * Math.exp(term0 + term1 + term2) + b

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

                logLikelihood -= poissonProb(x[i], y[i], z[i], parameters, f[i])

            }

            return logLikelihood
        }


        /**
         * Calculates the fit residual between a set of parameters and a value at a supplied point.
         * @param value         The observed value at the point.
         * *
         * @param x             The x-coordinate of the point (in absolute original image coordinates)
         * *
         * @param y             The y-coordinate of the point (in absolute original image coordinates)
         * *
         * @param z             The z-coordinate of the point (in absolute original image coordinates.
         * *
         * @param parameters    The gaussian parameters in the same order as would be required for or returned from [.fit]
         * *
         * @return              The fit residual at the specified point.
         */
        fun fitResidual(value: Double, x: Double, y: Double, z: Double, parameters: RealVector): Double {

            return value - gaussian(x, y, z, parameters.toArray())
        }

        private fun logFactorial(n: Int): Double {

            if (n < 0) return 0.0

            if (n <= maxLogFactorialPrecalculated) {
                return logFactorialLookups.getEntry(n)
            }

            synchronized(GaussianFitter3D::class.java) {

                if (n > maxLogFactorialPrecalculated) {

                    if (n >= logFactorialLookups.dimension) {

                        val sizeDiff = n + 1 - logFactorialLookups.dimension

                        val toAppend = ArrayRealVector(sizeDiff, 0.0)

                        val newLookups = logFactorialLookups.append(toAppend)

                        logFactorialLookups = newLookups

                    }

                    for (i in maxLogFactorialPrecalculated + 1..n) {

                        if (i == 0) {
                            logFactorialLookups.setEntry(i, 0.0)
                        } else {
                            logFactorialLookups.setEntry(i, logFactorialLookups.getEntry(i - 1) + Math.log(i.toDouble()))
                        }
                    }

                    maxLogFactorialPrecalculated = n


                }


            }

            return logFactorialLookups.getEntry(n)

        }
    }


}
