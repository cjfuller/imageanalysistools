package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.stat.regression.SimpleRegression

import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimationFilter

class BisquareLinearFit {
    private var noIntercept: Boolean = false
    private val tuningConstant: Double = DEFAULT_TUNING_CONST

    /**
     * Disables the fitting of the y-intercept for this fit.
     */
    fun disableIntercept(): BisquareLinearFit {
        this.noIntercept = true
        return this
    }

    /**
     * Performs a robust least squares fit with bisquare weights to the supplied data.

     * @param indVarValues A RealVector containing the values of the independent variable.
     * *
     * @param depVarValues A RealVector containing the values of the dependent variable.
     * *
     * @return a RealVector containing two elements: the slope of the fit and the y-intercept of the fit.
     */
    fun fit(indVarValues: RealVector, depVarValues: RealVector): RealVector {
        val uniformWeights = ArrayRealVector(indVarValues.dimension, 1.0)
        var lastParams: RealVector = ArrayRealVector(2, java.lang.Double.MAX_VALUE)
        var currParams = wlsFit(indVarValues, depVarValues, uniformWeights)
        var weights: RealVector = uniformWeights
        val leverages = this.calculateLeverages(indVarValues)
        var c = 0
        var norm_mult = 1.0

        if (!this.noIntercept) {
            norm_mult = 2.0
        }
        val maxiter = 10000

        while (lastParams.subtract(currParams).norm > CONV_NORM * norm_mult && c++ < maxiter) {
            lastParams = currParams
            val stdAdjR = this.calculateStandardizedAdjustedResiduals(indVarValues, depVarValues, leverages, currParams)
            weights = calculateBisquareWeights(stdAdjR)
            currParams = wlsFit(indVarValues, depVarValues, weights)
        }
        return currParams
    }

    /**
     * Performs a weighted least squares fit with supplied weights to the supplied data.

     * @param indVarValues A RealVector containing the values of the independent variable.
     * *
     * @param depVarValues A RealVector containing the values of the dependent variable.
     * *
     * @param weights A RealVector containing the weights for the data points.
     * *
     * @return a RealVector containing two elements: the slope of the fit and the y-intercept of the fit.
     */
    fun wlsFit(indVarValues: RealVector, depVarValues: RealVector, weights: RealVector): RealVector {
        //initial guess for the fit: unweighted regression.
        val unweighted = SimpleRegression(!this.noIntercept)

        for (i in 0..indVarValues.dimension - 1) {
            unweighted.addData(indVarValues.getEntry(i), depVarValues.getEntry(i))
        }
        var parameters: RealVector? = null

        if (this.noIntercept) {
            parameters = ArrayRealVector(1, 0.0)
        } else {
            parameters = ArrayRealVector(2, 0.0)
        }
        parameters.setEntry(0, unweighted.slope)

        if (!this.noIntercept) {
            parameters.setEntry(1, unweighted.intercept)
        }
        val nmm = NelderMeadMinimizer(1e-12)
        val wof = WlsObjectiveFunction()
        wof.setIndVar(indVarValues)
        wof.setDepVar(depVarValues)
        wof.setWeights(weights)
        wof.setShouldFitIntercept(!this.noIntercept)
        parameters = nmm.optimize(wof, parameters)

        if (this.noIntercept) {
            val output = ArrayRealVector(2, 0.0)
            output.setEntry(0, parameters!!.getEntry(0))
            return output
        }
        return parameters
    }

    /**
     * Calculates the leverages of data points for least squares fitting (assuming equal variances).

     * @param indVarValues The values of the independent variable used for the fitting.
     * *
     * @return a RealVector containing a leverage value for each independent variable value.
     */
    private fun calculateLeverages(indVarValues: RealVector): RealVector {
        var indVarMatrix: RealMatrix? = null

        if (this.noIntercept) {
            indVarMatrix = Array2DRowRealMatrix(indVarValues.dimension, 1)
        } else {
            indVarMatrix = Array2DRowRealMatrix(indVarValues.dimension, 2)
        }

        indVarMatrix.setColumnVector(0, indVarValues)

        if (!this.noIntercept) {
            indVarMatrix.setColumnVector(1, indVarValues.mapMultiply(0.0).mapAdd(1.0))
        }
        val leverages = ArrayRealVector(indVarValues.dimension)
        val xQR = QRDecomposition(indVarMatrix)
        val xR = xQR.r
        val smallerDim = if (xR.rowDimension < xR.columnDimension) xR.rowDimension else xR.columnDimension
        val xRSq = xR.getSubMatrix(0, smallerDim - 1, 0, smallerDim - 1)
        val xRQR = QRDecomposition(xRSq)
        val xRInv = xRQR.solver.inverse
        val xxRInv = indVarMatrix.multiply(xRInv)

        for (i in 0..indVarValues.dimension - 1) {
            val sum = (0..xxRInv.columnDimension - 1).sumByDouble { Math.pow(xxRInv.getEntry(i, it), 2.0) }
            leverages.setEntry(i, sum)
        }
        return leverages
    }

    /**
     * Calculates the standardized adjusted residuals (according to the same definition used by MATLAB) of the data points for fitting.
     *
     * @param indVarValues The values of the independent variable used for the fitting.
     * @param depVarValues The values of the dependent variable used for the fitting.
     * @param leverages the leverages of the independent variables, as compted by [.calculateLeverages]
     * @param fitParams the results of a (possibly weighted) least squares fit to the data, containing one or two components: a slope and an optional y-intercept.
     * @return a RealVector containing an adjusted residual value for each data point
     */
    private fun calculateStandardizedAdjustedResiduals(indVarValues: RealVector, depVarValues: RealVector, leverages: RealVector, fitParams: RealVector): RealVector {
        var predictedValues = indVarValues.mapMultiply(fitParams.getEntry(0))
        val denom = leverages.mapMultiply(-1.0)
                .mapAddToSelf(1 + CLOSE_TO_ZERO)
                .mapToSelf(org.apache.commons.math3.analysis.function.Sqrt())

        if (!this.noIntercept) {
            predictedValues = predictedValues.mapAdd(fitParams.getEntry(1))
        }

        val mean = (0..depVarValues.dimension - 1).sumByDouble { depVarValues.getEntry(it) } / depVarValues.dimension.toDouble()
        val stddev = depVarValues.mapSubtract(mean).norm * (depVarValues.dimension * 1.0 / (depVarValues.dimension - 1))
        val residuals = depVarValues.subtract(predictedValues).ebeDivide(denom)
        val absDev = residuals.map(org.apache.commons.math3.analysis.function.Abs())
        var smallerDim = 2
        if (this.noIntercept) {
            smallerDim = 1
        }
        val resArray = residuals.map(org.apache.commons.math3.analysis.function.Abs()).toArray()
        resArray.sort()
        val partialRes = ArrayRealVector(absDev.dimension - smallerDim + 1, 0.0)
        for (i in smallerDim - 1..resArray.size - 1) {
            partialRes.setEntry(i - smallerDim + 1, resArray[i])
        }

        var resMAD = 0.0
        if (partialRes.dimension % 2 == 0) {
            resMAD = LocalBackgroundEstimationFilter.quickFindKth(partialRes.dimension / 2, partialRes) + LocalBackgroundEstimationFilter.quickFindKth(partialRes.dimension / 2 - 1, partialRes)
            resMAD /= 2.0
        } else {
            resMAD = LocalBackgroundEstimationFilter.quickFindKth((partialRes.dimension - 1) / 2, partialRes)
        }
        resMAD /= 0.6745
        if (resMAD < stddev * CLOSE_TO_ZERO) {
            resMAD = stddev * CLOSE_TO_ZERO
        }
        return residuals.mapDivide(DEFAULT_TUNING_CONST * resMAD)
    }

    /**
     * Calculates the weight for the next weighted least squares iteration using the bisquare weighting function.
     * @param stdAdjR the standardized adjusted residuals, as computed by [.calculateStandardizedAdjustedResiduals]
     * *
     * @return a RealVector containing weights for each data point suitable for weighted least squares fitting.
     */
    private fun calculateBisquareWeights(stdAdjR: RealVector): RealVector {
        val bisquareWeights = ArrayRealVector(stdAdjR.dimension, 0.0)

        (0..bisquareWeights.dimension - 1)
                .asSequence()
                .filter { Math.abs(stdAdjR.getEntry(it)) < 1 }
                .forEach { bisquareWeights.setEntry(it, Math.pow(1 - Math.pow(stdAdjR.getEntry(it), 2.0), 2.0)) }
        return bisquareWeights
    }

    /**
     * A class implementing weighted sum of squares on a linear model.
     * Negative weights will be made positive.
     */
    private class WlsObjectiveFunction : ObjectiveFunction {
        private var ind: RealVector? = null
        private var dep: RealVector? = null
        private var weights: RealVector? = null
        private var shouldFitIntercept: Boolean = true

        fun setWeights(weights: RealVector) {
            this.weights = weights
        }

        fun setDepVar(dep: RealVector) {
            this.dep = dep
        }

        fun setIndVar(ind: RealVector) {
            this.ind = ind
        }

        fun setShouldFitIntercept(fitInt: Boolean) {
            this.shouldFitIntercept = fitInt
        }

        override fun evaluate(parameters: RealVector): Double {
            val predictedResponse = this.ind!!.mapMultiply(parameters.getEntry(0))
            if (this.shouldFitIntercept) {
                predictedResponse.mapAddToSelf(parameters.getEntry(1))
            }
            val sqrRes = this.dep!!.subtract(predictedResponse).mapToSelf(org.apache.commons.math3.analysis.function.Power(2.0))
            return sqrRes.ebeMultiply(this.weights).l1Norm
        }
    }

    companion object {
        internal val DEFAULT_TUNING_CONST = 4.685
        internal val CONV_NORM = 1e-5
        internal val CLOSE_TO_ZERO = 1e-6
    }
}
