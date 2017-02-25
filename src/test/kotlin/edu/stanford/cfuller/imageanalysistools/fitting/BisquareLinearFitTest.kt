package edu.stanford.cfuller.imageanalysistools.fitting

import io.kotlintest.matchers.Matcher
import io.kotlintest.specs.FlatSpec
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

val eps = 1e-3

fun beApproximately(expected: RealVector): Matcher<RealVector> {
    return VectorApproxMatcher(expected)
}

class VectorApproxMatcher(val expected: RealVector, val tolerance: Double = eps): Matcher<RealVector> {
    override fun test(value: RealVector) {
        val diffNorm = value.subtract(expected).norm
        if (diffNorm > tolerance) {
            throw AssertionError("$value was not $expected to within $tolerance")
        }
    }

}

class BisquareLinearFitTest : FlatSpec() {
    fun makeVector(n: Int, eltGenerator: (Int) -> Double): ArrayRealVector {
        val vec = ArrayRealVector(n, 0.0)
        (0..(n-1)).forEach {
            vec.setEntry(it, eltGenerator(it))
        }
        return vec
    }
    fun makeVector(arr: Array<Double>): ArrayRealVector {
        return makeVector(arr.size, { arr[it] })
    }
    init {
        "The Bisquare Linear Fitter" should "fit a straight line with slope 1" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, Int::toDouble)
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(1.0, 0.0))
            val result = BisquareLinearFit().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().fit(x, y)
            bisquareWeightedResult should beApproximately(expected)
        }

        "The Bisquare Linear Fitter" should "fit a straight line with slope 1 with y-intercept disabled" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, Int::toDouble)
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(1.0, 0.0))
            val result = BisquareLinearFit().disableIntercept().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().fit(x, y)
            bisquareWeightedResult should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit a straight line with slope 1 with y offset" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, { it + 1.0 })
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(1.0, 1.0))
            val result = BisquareLinearFit().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().fit(x, y)
            bisquareWeightedResult should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit a straight line with slope 1, y offset, and y-intercept disabled" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, { it + 1.0 })
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(1.333333, 0.0))
            val result = BisquareLinearFit().disableIntercept().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().disableIntercept().fit(x, y)
            bisquareWeightedResult should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit a straight line with slope 1, y offset, and uneven weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, { it + 1.0 })
            val w = makeVector(5, Int::toDouble)
            val expected = makeVector(arrayOf(1.0, 1.0))
            val result = BisquareLinearFit().wlsFit(x, y, w)
            result should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit a straight line with slope 1, y offset, y-intercept disabled, and uneven weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(5, { it + 1.0 })
            val w = makeVector(5, Int::toDouble)
            val expected = makeVector(arrayOf(1.3, 0.0))
            val result = BisquareLinearFit().disableIntercept().wlsFit(x, y, w)
            result should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit scattered points, with y offset and even weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(arrayOf(5.0, 17.0, 8.0, 2.0, 1.0))
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(-2.3, 11.2))
            val result = BisquareLinearFit().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().fit(x, y)
            val bisquareWeightedExpected = makeVector(arrayOf(-2.349, 11.2717))
            bisquareWeightedResult should beApproximately (bisquareWeightedExpected)
        }

        "The Bisquare Linear Fitter" should "fit scattered points, with y offset, y-intercept disabled and even weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(arrayOf(5.0, 17.0, 8.0, 2.0, 1.0))
            val w = makeVector(5, { 1.0 })
            val expected = makeVector(arrayOf(1.433, 0.0))
            val result = BisquareLinearFit().disableIntercept().wlsFit(x, y, w)
            result should beApproximately (expected)

            val bisquareWeightedResult = BisquareLinearFit().disableIntercept().fit(x, y)
            val bisquareWeightedExpected = makeVector(arrayOf(1.2939, 0.0))
            bisquareWeightedResult should beApproximately (bisquareWeightedExpected)
        }

        "The Bisquare Linear Fitter" should "fit scattered points with y offset and uneven weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(arrayOf(5.0, 17.0, 8.0, 2.0, 1.0))
            val w = makeVector(5, Int::toDouble)
            val expected = makeVector(arrayOf(-4.6, 18.1))
            val result = BisquareLinearFit().wlsFit(x, y, w)
            result should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit scattered points with y offset, y-intercept disabled and uneven weights" {
            val x = makeVector(5, Int::toDouble)
            val y = makeVector(arrayOf(5.0, 17.0, 8.0, 2.0, 1.0))
            val w = makeVector(5, Int::toDouble)
            val expected = makeVector(arrayOf(0.83, 0.0))
            val result = BisquareLinearFit().disableIntercept().wlsFit(x, y, w)
            result should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit some linear points with an outlier, with y-offset" {
            val x = makeVector(18, Int::toDouble)
            val y = makeVector(18, { it + 1.0 })
            x.setEntry(17, 92.0)
            y.setEntry(17, 343.0)

            val expected = makeVector(2, { 1.0 })
            val result = BisquareLinearFit().fit(x, y)

            result should beApproximately (expected)
        }

        "The Bisquare Linear Fitter" should "fit some linear points with an outlier, with y-offset, y-intercept disabled" {
            val x = makeVector(18, Int::toDouble)
            val y = makeVector(18, { it + 1.0 })
            x.setEntry(17, 92.0)
            y.setEntry(17, 343.0)

            val expected = makeVector(arrayOf(1.0903, 0.0))
            val result = BisquareLinearFit().disableIntercept().fit(x, y)

            result should beApproximately (expected)
        }
    }
}