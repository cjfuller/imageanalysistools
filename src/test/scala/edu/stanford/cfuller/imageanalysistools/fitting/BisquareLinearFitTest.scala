package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.linear.{ArrayRealVector, RealVector}
import org.scalatest._
import org.scalatest.matchers._

case class VectorApproxMatcher(rhs: RealVector)(implicit eps: Double) extends Matcher[RealVector] {
  def apply(lhs: RealVector) = {
    val diffNorm = lhs.subtract(rhs).getNorm
    MatchResult(
      diffNorm < eps,
      diffNorm + " was not < " + eps,
      diffNorm + " was not >= " + eps
    )
  }
}

class BisquareLinearFitTest extends FlatSpec with Matchers {
  implicit val eps = 1e-3

  def beApproximately(rhs: RealVector) = VectorApproxMatcher(rhs)

  def makeVector(n: Int, eltGenerator: Int => Double): ArrayRealVector = {
    val vec = new ArrayRealVector(n, 0.0)
    (0 until n).foreach(i => vec.setEntry(i, eltGenerator(i)))
    vec
  }

  "The Bisquare Linear Fitter" should "fit a straight line with slope 1" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i)
    val w = makeVector(5, i => 1.0)
    val expected = makeVector(2, Vector(1, 0))
    val result = new BisquareLinearFit().wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().fit(x, y)
    bisquareWeightedResult should beApproximately (expected)
  }

  it should "fit a straight line with slope 1 with y-intercept disabled" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i)
    val w = makeVector(5, i => 1.0)
    val expected = makeVector(2, Vector(1, 0))
    val result = new BisquareLinearFit().disableIntercept.wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().fit(x, y)
    bisquareWeightedResult should beApproximately (expected)
  }

  it should "fit a straight line with slope 1 with y offset" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i + 1)
    val w = makeVector(5, i => 1.0)
    val expected = makeVector(2, Vector(1, 1))
    val result = new BisquareLinearFit().wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().fit(x, y)
    bisquareWeightedResult should beApproximately (expected)
  }

  it should "fit a straight line with slope 1, y offset, and y-intercept disabled" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i + 1)
    val w = makeVector(5, i => 1.0)
    val expected = makeVector(2, Vector(1.333333, 0))
    val result = new BisquareLinearFit().disableIntercept.wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().disableIntercept.fit(x, y)
    bisquareWeightedResult should beApproximately (expected)
  }

  it should "fit a straight line with slope 1, y offset, and uneven weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i + 1)
    val w = makeVector(5, i => i)
    val expected = makeVector(2, Vector(1, 1))
    val result = new BisquareLinearFit().wlsFit(x, y, w)
    result should beApproximately (expected)
  }

  it should "fit a straight line with slope 1, y offset, y-intercept disabled, and uneven weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, i => i + 1)
    val w = makeVector(5, i => i)
    val expected = makeVector(2, Vector(1.3, 0))
    val result = new BisquareLinearFit().disableIntercept.wlsFit(x, y, w)
    result should beApproximately (expected)
  }

  it should "fit scattered points, with y offset and even weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, Vector(5, 17, 8, 2, 1))
    val w = makeVector(5, i => 1)
    val expected = makeVector(2, Vector(-2.3, 11.2))
    val result = new BisquareLinearFit().wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().fit(x, y)
    val bisquareWeightedExpected = makeVector(2, Vector(-2.349, 11.2717))
    bisquareWeightedResult should beApproximately (bisquareWeightedExpected)
  }

  it should "fit scattered points, with y offset, y-intercept disabled and even weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, Vector(5, 17, 8, 2, 1))
    val w = makeVector(5, i => 1)
    val expected = makeVector(2, Vector(1.433, 0))
    val result = new BisquareLinearFit().disableIntercept.wlsFit(x, y, w)
    result should beApproximately (expected)

    val bisquareWeightedResult = new BisquareLinearFit().disableIntercept.fit(x, y)
    val bisquareWeightedExpected = makeVector(2, Vector(1.2939, 0))
    bisquareWeightedResult should beApproximately (bisquareWeightedExpected)
  }

  it should "fit scattered points with y offset and uneven weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, Vector(5, 17, 8, 2, 1))
    val w = makeVector(5, i => i)
    val expected = makeVector(2, Vector(-4.6, 18.1))
    val result = new BisquareLinearFit().wlsFit(x, y, w)
    result should beApproximately (expected)
  }

  it should "fit scattered points with y offset, y-intercept disabled and uneven weights" in {
    val x = makeVector(5, i => i)
    val y = makeVector(5, Vector(5, 17, 8, 2, 1))
    val w = makeVector(5, i => i)
    val expected = makeVector(2, Vector(0.83, 0))
    val result = new BisquareLinearFit().disableIntercept.wlsFit(x, y, w)
    result should beApproximately (expected)
  }

  it should "fit some linear points with an outlier, with y-offset" in {
    val x = makeVector(18, i => i)
    val y = makeVector(18, i => i + 1)
    x.setEntry(17, 92)
    y.setEntry(17, 343)

    val expected = makeVector(2, Vector(1, 1))
    val result = new BisquareLinearFit().fit(x, y)

    result should beApproximately (expected)
  }

  it should "fit some linear points with an outlier, with y-offset, y-intercept disabled" in {
    val x = makeVector(18, i => i)
    val y = makeVector(18, i => i + 1)
    x.setEntry(17, 92)
    y.setEntry(17, 343)

    val expected = makeVector(2, Vector(1.0903, 0))
    val result = new BisquareLinearFit().disableIntercept.fit(x, y)

    result should beApproximately (expected)
  }
}
