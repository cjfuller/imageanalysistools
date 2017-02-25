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

package edu.stanford.cfuller.imageanalysistools.fitting

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.integration.LegendreGaussIntegrator
import org.apache.commons.math3.exception.util.DummyLocalizable
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.exception.ConvergenceException

/**
 * An ImageObject that fits to a three-dimensional gaussian that is symmetric in x-y and has no covariance
 * between any of the dimensions.

 * @author Colin J. Fuller
 */
class GaussianImageObject : ImageObject {

    /**
     * Creates an empty GaussianImageObject.
     */
    constructor() {
        init()
    }

    /**
     * Creates a GaussianImageObject from the specified masked region in an Image.
     * @param label     The greylevel of the object in the Image mask.
     * *
     * @param mask      The mask of objects in the Image, with a unique greylevel assigned to each object.
     * *
     * @param parent    The Image that the object occurs in and that is masked by mask.
     * *
     * @param p         The parameters associated with this analysis.
     */
    constructor(label: Int, mask: Image, parent: Image, p: ParameterDictionary) {
        init(label, mask, parent, p)

    }


    /**
     * Fits this object to a 3-dimensional gaussian, and estimates error and goodness of fit.
     * @param p     The parameters for the current analysis.
     */
    override fun fitPosition(p: ParameterDictionary) {

        if (this.sizeInPixels == 0) {
            this.nullifyImages()
            return
        }

        this.fitParametersByChannel = java.util.ArrayList<FitParameters>()
        this.fitR2ByChannel = java.util.ArrayList<Double>()
        this.fitErrorByChannel = java.util.ArrayList<Double>()
        this.nPhotonsByChannel = java.util.ArrayList<Double>()

        val gf = GaussianFitter3D()

        //System.out.println(this.parent.getDimensionSizes().getZ());

        var numChannels = 0

        if (p.hasKey(NUM_WAVELENGTHS_PARAM)) {
            numChannels = p.getIntValueForKey(NUM_WAVELENGTHS_PARAM)
        } else {
            numChannels = this.parent!!.dimensionSizes.get(ImageCoordinate.C)
        }

        for (channelIndex in 0..numChannels - 1) {

            var fitParameters: RealVector = ArrayRealVector(7, 0.0)

            val ppg = p.getDoubleValueForKey(PHOTONS_PER_LEVEL_PARAM)

            this.parentBoxMin!!.set(ImageCoordinate.C, channelIndex)
            this.parentBoxMax!!.set(ImageCoordinate.C, channelIndex + 1)

            this.boxImages()

            val x = java.util.ArrayList<Double>()
            val y = java.util.ArrayList<Double>()
            val z = java.util.ArrayList<Double>()
            val f = java.util.ArrayList<Double>()


            for (ic in this.parent!!) {
                x.add(ic.get(ImageCoordinate.X).toDouble())
                y.add(ic.get(ImageCoordinate.Y).toDouble())
                z.add(ic.get(ImageCoordinate.Z).toDouble())
                f.add(parent!!.getValue(ic).toDouble())
            }

            xValues = DoubleArray(x.size)
            yValues = DoubleArray(y.size)
            zValues = DoubleArray(z.size)
            functionValues = DoubleArray(f.size)

            var xCentroid = 0.0
            var yCentroid = 0.0
            var zCentroid = 0.0
            var totalCounts = 0.0

            for (i in x.indices) {

                xValues[i] = x[i]
                yValues[i] = y[i]
                zValues[i] = z[i]
                functionValues[i] = f[i] * ppg
                xCentroid += xValues!![i] * functionValues!![i]
                yCentroid += yValues!![i] * functionValues!![i]
                zCentroid += zValues!![i] * functionValues!![i]
                totalCounts += functionValues!![i]
            }


            xCentroid /= totalCounts
            yCentroid /= totalCounts
            zCentroid /= totalCounts

            //z sometimes seems to be a bit off... trying (20110415) to go back to max value pixel at x,y centroid

            val xRound = Math.round(xCentroid).toInt()
            val yRound = Math.round(yCentroid).toInt()

            var maxVal = 0.0
            var maxInd = 0

            var minZ = java.lang.Double.MAX_VALUE
            var maxZ = 0.0

            for (i in x.indices) {

                if (zValues!![i] < minZ) minZ = zValues!![i]
                if (zValues!![i] > maxZ) maxZ = zValues!![i]

                if (xValues!![i] == xRound.toDouble() && yValues!![i] == yRound.toDouble()) {
                    if (functionValues!![i] > maxVal) {
                        maxVal = functionValues!![i]
                        maxInd = zValues!![i].toInt()
                    }
                }
            }

            zCentroid = maxInd.toDouble()


            //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background

            //amplitude: find the max value; background: find the min value


            var maxValue = 0.0

            var minValue = java.lang.Double.MAX_VALUE


            for (ic in this.parent!!) {

                if (parent!!.getValue(ic) > maxValue) maxValue = parent!!.getValue(ic).toDouble()
                if (parent!!.getValue(ic) < minValue) minValue = parent!!.getValue(ic).toDouble()

            }


            fitParameters.setEntry(0, (maxValue - minValue) * 0.95)
            fitParameters.setEntry(6, minValue + 0.05 * (maxValue - minValue))

            //positions

            fitParameters.setEntry(3, xCentroid)
            fitParameters.setEntry(4, yCentroid)
            fitParameters.setEntry(5, zCentroid)

            //variances

            val limitedWidthxy = 200.0
            val limitedWidthz = 500.0

            var sizex = limitedWidthxy / p.getDoubleValueForKey(PIXELSIZE_PARAM)
            var sizez = limitedWidthz / p.getDoubleValueForKey(SECTIONSIZE_PARAM)

            if (p.hasKey(Z_WIDTH_PARAM)) {
                sizez = p.getDoubleValueForKey(Z_WIDTH_PARAM)
            }

            if (p.hasKey(XY_WIDTH_PARAM)) {
                sizex = p.getDoubleValueForKey(XY_WIDTH_PARAM)
            }

            fitParameters.setEntry(1, sizex / 2)
            fitParameters.setEntry(2, sizez / 2)

            //amplitude and background are in arbitrary intensity units; convert to photon counts

            fitParameters.setEntry(0, fitParameters.getEntry(0) * ppg)
            fitParameters.setEntry(6, fitParameters.getEntry(6) * ppg)

            //System.out.println("guess: " + fitParameters);

            //do the fit


            fitParameters = gf.fit(this, fitParameters, ppg)

            //System.out.println("fit: " + fitParameters);


            val fp = FitParameters()

            fp.setPosition(ImageCoordinate.X, fitParameters.getEntry(3))
            fp.setPosition(ImageCoordinate.Y, fitParameters.getEntry(4))
            fp.setPosition(ImageCoordinate.Z, fitParameters.getEntry(5))

            fp.setSize(ImageCoordinate.X, fitParameters.getEntry(1))
            fp.setSize(ImageCoordinate.Y, fitParameters.getEntry(1))
            fp.setSize(ImageCoordinate.Z, fitParameters.getEntry(2))

            fp.amplitude = fitParameters.getEntry(0)
            fp.background = fitParameters.getEntry(6)

            fitParametersByChannel!!.add(fp)

            //calculate R2

            var residualSumSquared = 0.0
            var mean = 0.0
            var variance = 0.0
            var R2 = 0.0

            var n_photons = 0.0

            for (i in this.xValues!!.indices) {

                residualSumSquared += Math.pow(GaussianFitter3D.fitResidual(functionValues!![i], xValues!![i], yValues!![i], zValues!![i], fitParameters), 2.0)

                mean += functionValues!![i]

                n_photons += functionValues!![i] - fitParameters.getEntry(6)

            }

            mean /= functionValues!!.size.toDouble()

            for (i in this.xValues!!.indices) {
                variance += Math.pow(functionValues!![i] - mean, 2.0)
            }

            R2 = 1 - residualSumSquared / variance

            this.fitR2ByChannel!!.add(R2)

            this.unboxImages()

            //calculate fit error

            val s_xy = fitParameters.getEntry(1) * fitParameters.getEntry(1) * Math.pow(p.getDoubleValueForKey(PIXELSIZE_PARAM), 2.0)
            val s_z = fitParameters.getEntry(2) * fitParameters.getEntry(2) * Math.pow(p.getDoubleValueForKey(SECTIONSIZE_PARAM), 2.0)

            //s_z = 0; //remove!!

            var error = (2 * s_xy + s_z) / (n_photons - 1)// + 4*Math.sqrt(Math.PI) * Math.pow(2*s_xy,1.5)*Math.pow(fitParameters.getEntry(6),2)/(p.getDoubleValueForKey("pixelsize_nm")*n_photons*n_photons);

            val b = fitParameters.getEntry(6)
            val a = p.getDoubleValueForKey(PIXELSIZE_PARAM)
            val alpha = p.getDoubleValueForKey(SECTIONSIZE_PARAM)
            val sa_x = s_xy + Math.pow(a, 2.0) / 12
            val sa_z = s_z + Math.pow(alpha, 2.0) / 12

            //System.out.printf("b = %f, a = %f, alpha = %f, s_xy = %f, s_z = %f, n= %f\n", b, a, alpha, s_xy, s_z, n_photons);

            var error_x = sa_x / n_photons * (16.0 / 9.0 + 8.0 * Math.PI * sa_x * b * b / (n_photons * Math.pow(p.getDoubleValueForKey(PIXELSIZE_PARAM), 2.0)))
            var error_z = sa_z / n_photons * (16.0 / 9.0 + 8.0 * Math.PI * sa_z * b * b / (n_photons * Math.pow(p.getDoubleValueForKey(SECTIONSIZE_PARAM), 2.0)))

            val A = 1.0 / (2.0 * Math.sqrt(2.0) * Math.pow(Math.PI, 1.5) * Math.sqrt(sa_z) * sa_x)

            val eif = ErrIntFunc()

            eif.setParams(b, n_photons, A, sa_z, sa_x, a, alpha)

            val lgi = LegendreGaussIntegrator(5, 10, 1000)

            //integrate over 10*width of PSF in z

            val size = 10 * Math.sqrt(sa_z)

            var intpart = 0.0
            try {

                if (b < 0) throw ConvergenceException(DummyLocalizable("negative background!")) // a negative value for b seems to cause the integration to hang, preventing the program from progressing

                intpart = lgi.integrate(10000, eif, -size, size)

                val fullIntPart = intpart + Math.pow(2 * Math.PI, 1.5) * sa_x * A / Math.sqrt(sa_z)

                error_x = Math.sqrt(2 / (n_photons * sa_z / (2 * sa_z + sa_x) * fullIntPart))
                error_z = Math.sqrt(2 / (n_photons * sa_x / (2 * sa_z + sa_x) * fullIntPart))

            } catch (e: ConvergenceException) {
                LoggingUtilities.logger.severe("Integration error: " + e.message)
                error_x = -1.0
                error_z = -1.0
            }


            if (error_x > 0 && error_z > 0) {

                error = Math.sqrt(2.0 * error_x * error_x + error_z * error_z)

            } else {
                error = java.lang.Double.NaN
            }

            this.fitErrorByChannel!!.add(error)

            this.positionsByChannel.add(fitParameters.getSubVector(3, 3))

            this.nPhotonsByChannel!!.add(n_photons)

        }

        this.hadFittingError = false
    }

    protected inner class DI1Func : UnivariateFunction {

        private var z: Double = 0.toDouble()
        private var b: Double = 0.toDouble()
        private var n: Double = 0.toDouble()
        private var A: Double = 0.toDouble()
        private var sa_z: Double = 0.toDouble()
        private var a: Double = 0.toDouble()
        private var alpha: Double = 0.toDouble()

        override fun value(t: Double): Double {

            val tau = b / (n * a * a * alpha * A * Math.exp(-z * z / (2 * sa_z)))

            return -1.0 * t * Math.log(t) / (t + tau)
        }

        fun setZ(z: Double) {
            this.z = z
        }

        fun setParams(b: Double, n: Double, A: Double, sa_z: Double, a: Double, alpha: Double) {
            this.b = b
            this.n = n
            this.A = A
            this.sa_z = sa_z
            this.a = a
            this.alpha = alpha
        }

    }

    protected inner class ErrIntFunc : UnivariateFunction {
        private var b: Double = 0.toDouble()
        private var n: Double = 0.toDouble()
        private var A: Double = 0.toDouble()
        private var sa_z: Double = 0.toDouble()
        private var sa_x: Double = 0.toDouble()
        private var a: Double = 0.toDouble()
        private var alpha: Double = 0.toDouble()

        private val lgi: LegendreGaussIntegrator
        private val di1: DI1Func

        init {
            this.lgi = LegendreGaussIntegrator(5, 10, 1000)
            this.di1 = DI1Func()
        }

        fun setParams(b: Double, n: Double, A: Double, sa_z: Double, sa_x: Double, a: Double, alpha: Double) {
            this.b = b
            this.n = n
            this.A = A
            this.sa_z = sa_z
            this.sa_x = sa_x
            this.a = a
            this.alpha = alpha
            this.di1.setParams(b, n, A, sa_z, a, alpha)
        }

        @Throws(IllegalArgumentException::class)
        override fun value(z: Double): Double {

            this.di1.setZ(z)

            var I1 = 0.0

            try {
                I1 = lgi.integrate(10000, di1, 0.0, 1.0)
            } catch (e: ConvergenceException) {
                throw IllegalArgumentException(e)
            }

            val part1 = 4.0 * Math.PI * A * Math.exp(-z * z / (2 * sa_z)) * I1

            val part2 = 2.0 * Math.PI * sa_x * b / (sa_z * sa_z * n * a * a * alpha) * z * z * Math.log(1 / (1 + n * A * a * a * alpha * Math.exp(-z * z / (2 * sa_z)) / b))

            return part1 + part2

        }


    }

    companion object {

        internal val serialVersionUID = 2L

        /**
         * Required parameters
         */

        internal val PHOTONS_PER_LEVEL_PARAM = "photons_per_greylevel"
        internal val PIXELSIZE_PARAM = "pixelsize_nm"
        internal val SECTIONSIZE_PARAM = "z_sectionsize_nm"

        /**
         * Optional parameters
         */

        internal val NUM_WAVELENGTHS_PARAM = "num_wavelengths"
        internal val Z_WIDTH_PARAM = "z_width"
        internal val XY_WIDTH_PARAM = "xy_width"
    }

}
