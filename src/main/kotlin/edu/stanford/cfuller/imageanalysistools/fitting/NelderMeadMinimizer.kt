package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

/**
 * A function minimizer that implements the Nelder-Mead or Downhill Simplex method.

 * @author Colin J. Fuller
 */
class NelderMeadMinimizer {
    internal var a: Double = 0.toDouble()
    internal var g: Double = 0.toDouble()
    internal var r: Double = 0.toDouble()
    internal var s: Double = 0.toDouble()
    internal var tol: Double = 0.toDouble()

    /**
     * Constructs a new minimizer with the specified minimization parameters.
     * @param a     Reflection factor (default: 1.0).
     * @param g     Expansion factor (default: 2.0).
     * @param r     Contraction factor (default: 0.5).
     * @param s     Reduction factor (default: 0.5).
     * @param tol   Function value relative tolerance for optimization termination.
     */
    constructor(a: Double, g: Double, r: Double, s: Double, tol: Double) {
        this.a = a
        this.g = g
        this.r = r
        this.s = s
        this.tol = tol
    }

    private fun initDefaults() {
        this.a = 1.0
        this.g = 2.0
        this.r = 0.5
        this.s = 0.5
        this.tol = 1.0e-6
    }

    /**
     * Constructs a new minimizer with the default minimization parameters except for tolerance, which is specified.
     * @param tol   Function value relative tolerance for optimization termination.
     */
    constructor(tol: Double) {
        initDefaults()
        this.tol = tol
    }

    /**
     * Constructs a new minimizer with the default minimization parameters.
     */
    constructor() {
        initDefaults()
    }

    /**
     * Constructs the initial simplex that is the starting point of the optimization given an initial guess at the minimum and a size scale for each parameter.
     * @param initialPoint      The initial guess at the minimum, one component per parameter.
     * @param componentScales   A size scale for each parameter that is used to set how large the initial simplex is.
     * @return                  A matrix containing p + 1 rows, each of which is one set of p parameters, which specify the simplex.
     */
    fun generateInitialSimplex(initialPoint: RealVector, componentScales: RealVector): RealMatrix {
        val initialSimplex = Array2DRowRealMatrix(initialPoint.dimension + 1, initialPoint.dimension)
        initialSimplex.setRowVector(0, initialPoint)

        for (i in 1..initialSimplex.rowDimension - 1) {
            val newVector = initialPoint.copy()
            newVector.setEntry(i - 1, newVector.getEntry(i - 1) + componentScales.getEntry(i - 1))
            initialSimplex.setRowVector(i, newVector)
        }
        return initialSimplex
    }

    /**
     * Constructs the initial simplex that is the starting point of the optimization given an initial guess at the minimum.
     * This method will attempt to guess the scale of each parameters, but it is preferable to specify this manually using the other form of
     * generateInitialSimplex if any information about these scales is known.
     * @param initialPoint      The initial guess at the minimum, one component per parameter.
     * @return                  A matrix containing p + 1 rows, each of which is one set of p parameters, which specify the simplex.
     */
    fun generateInitialSimplex(initialPoint: RealVector): RealMatrix {
        val constantScale = 0.1
        val componentScales = initialPoint.mapMultiply(constantScale)
        //if the initial point has zeros in it, those entries will not be optimized
        //perturb slightly to allow optimization
        (0..componentScales.dimension - 1)
                .filter { componentScales.getEntry(it) == 0.0 }
                .forEach { componentScales.setEntry(it, constantScale) }
        return generateInitialSimplex(initialPoint, componentScales)
    }

    /**
     * Runs the minimization of the specified function starting from an initial guess.
     * @param f                 The ObjectiveFunction to be minimized.
     * @param initialPoint      The initial guess, one component per parameter.
     * @return                  The parameters at the function minimum in the same order as specified in the guess.
     */
    fun optimize(f: ObjectiveFunction, initialPoint: RealVector): RealVector {
        return this.optimize(f, generateInitialSimplex(initialPoint))
    }

    /**
     * Runs the minimization of the specified function starting from an initial simplex.
     * @param f                 The ObjectiveFunction to be minimized.
     * @param initialSimplex    The initial simplex to use to start optimization, as might be returned from [.generateInitialSimplex]
     * @return                  The parameters at the function minimum in the same order as specified for each point on the simplex.
     */
    fun optimize(f: ObjectiveFunction, initialSimplex: RealMatrix): RealVector {
        val currentSimplex = initialSimplex.copy()
        var currTolVal = 1.0e6
        val values = ArrayRealVector(initialSimplex.rowDimension, 0.0)
        var centerOfMass: RealVector = ArrayRealVector(initialSimplex.columnDimension, 0.0)
        var shouldEvaluate = false
        var iterCounter: Long = 0

        while (Math.abs(currTolVal) > this.tol) {
            var maxIndex = 0
            var minIndex = 0
            var maxValue = -1.0 * java.lang.Double.MAX_VALUE
            var minValue = java.lang.Double.MAX_VALUE
            var secondMaxValue = -1.0 * java.lang.Double.MAX_VALUE
            centerOfMass.mapMultiplyToSelf(0.0)

            if (shouldEvaluate) {
                for (i in 0..currentSimplex.rowDimension - 1) {
                    val currRow = currentSimplex.getRowVector(i)
                    values.setEntry(i, f.evaluate(currRow))
                }
            }

            for (i in 0..currentSimplex.rowDimension - 1) {
                val currValue = values.getEntry(i)
                if (currValue < minValue) {
                    minValue = currValue
                    minIndex = i
                }
                if (currValue > maxValue) {
                    secondMaxValue = maxValue
                    maxValue = currValue
                    maxIndex = i
                } else if (currValue > secondMaxValue) {
                    secondMaxValue = currValue
                }
            }

            (0..currentSimplex.rowDimension - 1)
                    .asSequence()
                    .filter { it != maxIndex }
                    .forEach { centerOfMass = centerOfMass.add(currentSimplex.getRowVector(it)) }

            centerOfMass.mapDivideToSelf((currentSimplex.rowDimension - 1).toDouble())

            val oldPoint = currentSimplex.getRowVector(maxIndex)
            val newPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(a).add(centerOfMass) // newpoint = COM + a*(COM-oldpoint)
            val newValue = f.evaluate(newPoint)

            if (newValue < secondMaxValue) { // success
                if (newValue < minValue) { // best found so far
                    //expansion
                    val expPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(g).add(centerOfMass)
                    val expValue = f.evaluate(expPoint)

                    if (expValue < newValue) {
                        currentSimplex.setRowVector(maxIndex, expPoint)
                        currTolVal = 2.0 * (expValue - maxValue) / (1.0e-20 + expValue + maxValue)
                        values.setEntry(maxIndex, expValue)
                        shouldEvaluate = false
                        continue
                    }
                }

                //reflection
                currentSimplex.setRowVector(maxIndex, newPoint)
                currTolVal = 2.0 * (newValue - maxValue) / (1.0e-20 + newValue + maxValue)
                values.setEntry(maxIndex, newValue)
                shouldEvaluate = false
                continue
            }

            //contraction
            val conPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(r).add(centerOfMass)
            val conValue = f.evaluate(conPoint)

            if (conValue < maxValue) {
                currentSimplex.setRowVector(maxIndex, conPoint)
                currTolVal = 2.0 * (conValue - maxValue) / (1.0e-20 + conValue + maxValue)
                values.setEntry(maxIndex, conValue)
                shouldEvaluate = false
                continue
            }

            //reduction
            (0..currentSimplex.rowDimension - 1)
                    .asSequence()
                    .filter { it != minIndex }
                    .forEach {
                        currentSimplex.setRowVector(it,
                                currentSimplex.getRowVector(it)
                                        .subtract(currentSimplex.getRowVector(minIndex))
                                        .mapMultiplyToSelf(s)
                                        .add(currentSimplex.getRowVector(minIndex)))
                    }

            val redValue = f.evaluate(currentSimplex.getRowVector(maxIndex))
            currTolVal = 2.0 * (redValue - maxValue) / (1.0e-20 + redValue + maxValue)
            shouldEvaluate = true

            if (iterCounter++ > 100000) {
                println("stalled?  tol: $currTolVal  minValue: $minValue")
            }
        }

        var minValue = java.lang.Double.MAX_VALUE
        var minVector: RealVector = ArrayRealVector()

        for (i in 0..currentSimplex.rowDimension - 1) {
            values.setEntry(i, f.evaluate(currentSimplex.getRowVector(i)))
            if (values.getEntry(i) < minValue) {
                minValue = values.getEntry(i)
                minVector = currentSimplex.getRowVector(i)
            }
        }
        return minVector
    }
}
