/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2013 Colin J. Fuller
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

import java.util.ArrayList
import java.util.HashMap

import edu.stanford.cfuller.imageanalysistools.fitting.NelderMeadMinimizer
import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

/**
 * A Filter that thresholds an Image using a modified Otsu's method.  (Otsu, 1979, DOI: 10.1109/TSMC.1979.4310076).  This attempts to fit the
 * eta vs. greylevel histogram to a double gaussian, and takes the higher value for the threshold.
 *
 *
 * This filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image to be thresholded.  After filtering, this Image will have its original values
 * anywhere where it is above the threshold, and zero values overwritten anywhere where the original values were below the threshold.

 * @author Colin J. Fuller
 */
class LocalMaximumSeparabilityThresholdingFilter : Filter() {

    internal val defaultIncrement = 1
    internal val defaultAdaptive = false


    /**
     * Applies the filter to an Image.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     */
    override fun apply(im: WritableImage) {
        apply_ext(im, defaultAdaptive, defaultIncrement)
    }

    /**
     * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold, and specifying a size for the threshold determination increment.
     * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
     * for images with a large dynamic range.
     *
     *
     * The increment size specified (in greylevels) will be used to determine the threshold only if adaptive determination is off; otherwise this parameter will be ignored.
     * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
     * *
     * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
     * *
     * @param increment             the increment size (in greylevels) to use for determining the threshold; must be positive.
     */
    @JvmOverloads fun apply_ext(im: WritableImage, adaptiveincrement: Boolean, increment: Int = defaultIncrement) {
        var increment = increment

        val h = Histogram(im)

        var thresholdValue = 0

        val numSteps = 1000

        var best_eta = 0.0
        var best_index = Integer.MAX_VALUE

        val nonzerocounts = h.totalCounts - h.getCounts(0)

        val meannonzero = h.meanNonzero

        val omega_v = ArrayRealVector(h.maxValue)
        val mu_v = ArrayRealVector(h.maxValue)
        val eta_v = ArrayRealVector(h.maxValue)

        var c = 0

        if (adaptiveincrement) {
            increment = ((h.maxValue - h.minValue + 1) * 1.0 / numSteps).toInt()
            if (increment < 1) increment = 1
        }

        run {
            var k = h.minValue
            while (k < h.maxValue + 1) {

                if (k == 0) {
                    k += increment
                    continue
                }

                omega_v.setEntry(c, h.getCumulativeCounts(k) * 1.0 / nonzerocounts)

                if (c == 0) {
                    mu_v.setEntry(c, k * omega_v.getEntry(c))
                } else {

                    mu_v.setEntry(c, mu_v.getEntry(c - 1) + k.toDouble() * h.getCounts(k).toDouble() * 1.0 / nonzerocounts)
                    for (i in k - increment + 1..k - 1) {
                        mu_v.setEntry(c, mu_v.getEntry(c) + h.getCounts(i).toDouble() * i.toDouble() * 1.0 / nonzerocounts)
                    }

                }

                val omega = omega_v.getEntry(c)
                val mu = mu_v.getEntry(c)

                if (omega > 1e-8 && 1 - omega > 1e-8) {

                    val eta = omega * (1 - omega) * Math.pow((meannonzero - mu) / (1 - omega) - mu / omega, 2.0)

                    eta_v.setEntry(c, eta)

                    if (eta >= best_eta) {
                        best_eta = eta
                        best_index = k
                    }


                } else {
                    eta_v.setEntry(c, 0.0)
                }


                c++
                k += increment

            }
        }

        val orig_method_best_index = best_index

        c = 1

        val maxima = ArrayList<Int>()
        val k_by_c = HashMap<Int, Int>()
        val c_by_k = HashMap<Int, Int>()

        var k = h.minValue + 1
        while (k < h.maxValue) {

            //detect if this is a local maximum

            k_by_c.put(c, k)
            c_by_k.put(k, c)

            var lastEntryNotEqual = c - 1
            var nextEntryNotEqual = c + 1

            while (lastEntryNotEqual > 0 && eta_v.getEntry(lastEntryNotEqual) == eta_v.getEntry(c)) {
                --lastEntryNotEqual
            }
            while (nextEntryNotEqual < eta_v.dimension - 1 && eta_v.getEntry(nextEntryNotEqual) == eta_v.getEntry(c)) {
                ++nextEntryNotEqual
            }



            if (eta_v.getEntry(c) > eta_v.getEntry(lastEntryNotEqual) && eta_v.getEntry(c) > eta_v.getEntry(nextEntryNotEqual)) {


                maxima.add(k)

            }

            c++
            k += increment

        }


        //now that we have maxima, try doing a gaussian fit to find the positions.  If there's only one, we need to guess at a second

        val parameters = ArrayRealVector(6, 0.0)

        var position0 = 0
        var position1 = h.maxValue

        if (maxima.size > 1) {

            var best_max = 0.0
            var second_best_max = 0.0

            var best_pos = 0
            var second_best_pos = 0

            for (k in maxima) {

                val ck = c_by_k[k]

                val eta_k = eta_v.getEntry(ck)

                if (eta_k > best_max) {

                    second_best_max = best_max
                    second_best_pos = best_pos

                    best_max = eta_k
                    best_pos = ck

                } else if (eta_k > second_best_max) {

                    second_best_max = eta_k
                    second_best_pos = ck

                }


            }


            position0 = best_pos

            position1 = second_best_pos


        } else {

            position0 = c_by_k[maxima[0]]
            position1 = (eta_v.dimension - position0) / 2 + position0

        }

        //make sure that position 1 is larger than position 0

        if (position1 < position0) {

            val temp = position0
            position0 = position1
            position1 = temp

        }

        val s = (position1 - position0) / 4.0

        parameters.setEntry(0, eta_v.getEntry(position0))//*Math.sqrt(2*Math.PI)*s);
        parameters.setEntry(1, position0.toDouble())
        parameters.setEntry(2, s)
        parameters.setEntry(3, eta_v.getEntry(position1))//*Math.sqrt(2*Math.PI)*s);
        parameters.setEntry(4, position1.toDouble())
        parameters.setEntry(5, s)

        val dgof = DoubleGaussianObjectiveFunction()

        dgof.setEta(eta_v)

        val nmm = NelderMeadMinimizer()


        val result = nmm.optimize(dgof, parameters)

        best_index = result.getEntry(4).toInt()

        if (k_by_c.containsKey(best_index)) {
            best_index = k_by_c[best_index]
        } else {
            //fall back to the normal global maximum if the fitting seems to have found an invalid value.
            best_index = orig_method_best_index

        }

        thresholdValue = best_index

        if (thresholdValue == Integer.MAX_VALUE) {
            thresholdValue = 0
        }
        for (coord in im) {
            if (im.getValue(coord) < thresholdValue) im.setValue(coord, 0f)
        }

    }

    protected inner class DoubleGaussianObjectiveFunction : ObjectiveFunction {

        //format of the point = A0, mean0, stddev0, A1, mean1, stddev1

        internal var etaValues: RealVector

        fun setEta(eta: RealVector) {
            this.etaValues = eta
        }


        protected fun doubleGaussProb(x: Double, parameters: RealVector): Double {
            val A0 = parameters.getEntry(0)
            val A1 = parameters.getEntry(3)
            val mean0 = parameters.getEntry(1)
            val mean1 = parameters.getEntry(4)
            val s0 = parameters.getEntry(2)
            val s1 = parameters.getEntry(5)

            return A0 * Math.exp(-Math.pow(x - mean0, 2.0) / (2.0 * s0 * s0)) + A1 * Math.exp(-Math.pow(x - mean1, 2.0) / (2.0 * s1 * s1))

        }

        /**
         * Calculates the sse between the gaussians specified by the parameters and the data.
         */
        override fun evaluate(point: RealVector): Double {

            var sse = 0.0

            for (i in 0..this.etaValues.dimension - 1) {

                val err = this.etaValues.getEntry(i) - doubleGaussProb(i.toDouble(), point)

                sse += err * err

            }

            return sse

        }


    }


}
/**
 * Applies the filter to an Image, optionally turning on adaptive determination of the increment size used to find the threshold.
 * Turning on adaptive determination of the increment will generally make the threshold slightly less optimal, but can sometimes speed up the filtering, especially
 * for images with a large dynamic range.
 * @param im    The Image to be thresholded; this will be overwritten by the thresholded Image.
 * *
 * @param adaptiveincrement     true to turn on adaptive determination of the threshold increment; false to turn it off and use the default value
 */
