package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.Array2DRowRealMatrix

import edu.stanford.cfuller.imageanalysistools.random.RandomGenerator

/**
 * This class implements the differential evolution algorithm for function minimization.

 * @author Colin J. Fuller
 */

class DifferentialEvolutionMinimizer {
    /**
     * Updates the function values for each parameter set in the population only if that parameter set has changed.
     * @param f             The function being minimized
     * @param population    The current population of parameters, one parameter set per row.
     * @param values        The vector of the last computed values for each entry in the population; entries that have changed will be overwritten.
     * @param valuesChanged An array indicating whether each entry in the population has changed and requires computation of the value.
     */
    private fun computeValues(f: ObjectiveFunction, population: RealMatrix, values: RealVector, valuesChanged: BooleanArray) {
        (0..population.rowDimension - 1)
                .asSequence()
                .filter { valuesChanged[it] }
                .forEach { values.setEntry(it, f.evaluate(population.getRowVector(it))) }
    }

    /**
     * Performs a minimization of a function starting with a given population.
     * @param population            The population of parameters to start from, one population entry per row, one parameter per column.
     * @param f                     The function to be minimized.
     * @param parameterLowerBounds  The lower bounds of each parameter.  This must have the same size as the column dimension of the population.  Generated parameter values less than these values will be discarded.
     * @param parameterUpperBounds  The upper bounds of each paraemter.  This must have the same size as the column dimension of the population.  Generated parameter values greater than these values will be discarded.
     * @param populationSize        The size of the population of parameters sets.  This must be equal to the row dimension of the population.
     * @param scaleFactor           Factor controlling the scale of crossed over entries during crossover events.
     * @param maxIterations         The maximum number of iterations to allow before returning a result.
     * @param crossoverFrequency    The frequency of crossover from 0 to 1.  At any given parameter, this is the probability of initiating a crossover as well as the probability of ending one after it has started.
     * @param tol                   Relative function value tolerance controlling termination; if the maximal and minimal population values differ by less than this factor times the maximal value, optimization will terminate.
     * @return                      The parameter values at the minimal function value found.
     */
    fun minimizeWithPopulation(population: RealMatrix, f: ObjectiveFunction, parameterLowerBounds: RealVector, parameterUpperBounds: RealVector, populationSize: Int, scaleFactor: Double, maxIterations: Int, crossoverFrequency: Double, tol: Double): RealVector {
        var population = population
        val numberOfParameters = parameterLowerBounds.dimension
        var currMinValue = java.lang.Double.MAX_VALUE
        var currMaxValue = -1.0 * java.lang.Double.MAX_VALUE
        var iterationCounter = maxIterations
        val mutationProb = 0.01
        val values = ArrayRealVector(populationSize)
        val valuesChanged = BooleanArray(populationSize, { true })
        computeValues(f, population, values, valuesChanged)
        val newVec = ArrayRealVector(numberOfParameters)
        val newPopulation = Array2DRowRealMatrix(populationSize, numberOfParameters)
        while (iterationCounter > 0) {
            for (i in 0..populationSize - 1) {
                val i1 = RandomGenerator.generator!!.randInt(populationSize)
                val i2 = RandomGenerator.generator!!.randInt(populationSize)
                val i3 = RandomGenerator.generator!!.randInt(populationSize)
                newVec.mapMultiplyToSelf(0.0)
                var inBounds = true
                var isCrossingOver = false
                for (j in 0..numberOfParameters - 1) {
                    if ((RandomGenerator.rand() < crossoverFrequency) xor isCrossingOver) {
                        if (!isCrossingOver) {
                            isCrossingOver = true
                        }
                        newVec.setEntry(j, scaleFactor * (population.getEntry(i2, j) - population.getEntry(i1, j)) + population.getEntry(i3, j))
                    } else {
                        if (isCrossingOver) {
                            isCrossingOver = false
                        }
                        newVec.setEntry(j, population.getEntry(i, j))
                    }
                    //random 10% range +/- mutation
                    if (RandomGenerator.rand() < mutationProb) {
                        val magnitude = 0.2 * (parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j))
                        newVec.setEntry(j, newVec.getEntry(j) + (RandomGenerator.rand() - 0.5) * magnitude)
                    }

                    if (newVec.getEntry(j) < parameterLowerBounds.getEntry(j) || newVec.getEntry(j) > parameterUpperBounds.getEntry(j)) {
                        inBounds = false
                    }
                }

                var functionValue = java.lang.Double.MAX_VALUE
                if (inBounds) functionValue = f.evaluate(newVec)
                if (functionValue < values.getEntry(i)) {
                    newPopulation.setRowVector(i, newVec)
                    valuesChanged[i] = true
                    values.setEntry(i, functionValue)
                } else {
                    newPopulation.setRowVector(i, population.getRowVector(i))
                    valuesChanged[i] = false
                }
            }
            population = newPopulation
            var tempMinValue = java.lang.Double.MAX_VALUE
            var tempMaxValue = -1.0 * java.lang.Double.MAX_VALUE

            for (i in 0..values.dimension - 1) {
                val value = values.getEntry(i)
                if (value < tempMinValue) {
                    tempMinValue = value
                }
                if (value > tempMaxValue) {
                    tempMaxValue = value
                }
            }

            currMinValue = tempMinValue
            currMaxValue = tempMaxValue

            if (Math.abs(currMaxValue - currMinValue) < Math.abs(tol * currMaxValue) + Math.abs(tol * currMinValue)) {
                iterationCounter--
            } else {
                iterationCounter = 1
            }
        }

        for (i in 0..populationSize - 1) {
            valuesChanged[i] = true
        }
        computeValues(f, population, values, valuesChanged)
        var tempMinValue = java.lang.Double.MAX_VALUE
        var tempMinIndex = 0
        for (i in 0..populationSize - 1) {
            if (values.getEntry(i) < tempMinValue) {
                tempMinValue = values.getEntry(i)
                tempMinIndex = i
            }
        }
        val output = ArrayRealVector(numberOfParameters)
        for (i in 0..numberOfParameters - 1) {
            output.setEntry(i, population.getEntry(tempMinIndex, i))
        }
        return output
    }


    /**
     * Performs a minimization of a function starting with an single initial guess; a full population is generated based on this guess and the parameter bounds.
     * @param f                     The function to be minimized.
     * @param parameterLowerBounds  The lower bounds of each parameter.  Generated parameter values less than these values will be discarded.
     * @param parameterUpperBounds  The upper bounds of each parameter.  Generated parameter values greater than these values will be discarded.
     * @param populationSize        The size of the population of parameters sets to use for optimization.
     * @param scaleFactor           Factor controlling the scale of crossed over entries during crossover events.
     * @param maxIterations         The maximum number of iterations to allow before returning a result.
     * @param crossoverFrequency    The frequency of crossover from 0 to 1.  At any given parameter, this is the probability of initiating a crossover as well as the probability of ending one after it has started.
     * @param tol                   Relative function value tolerance controlling termination; if the maximal and minimal population values differ by less than this factor times the maximal value, optimization will terminate.
     * @param initialGuess          A guess at the value of each parameter at the optimum.
     * @return                      The parameter values at the minimal function value found.
     */
    fun minimizeWithInitial(f: ObjectiveFunction, parameterLowerBounds: RealVector, parameterUpperBounds: RealVector, populationSize: Int, scaleFactor: Double, maxIterations: Int, crossoverFrequency: Double, tol: Double, initialGuess: RealVector): RealVector {
        val numberOfParameters = parameterLowerBounds.dimension

        //generate the initial population
        val population = Array2DRowRealMatrix(populationSize, numberOfParameters)
        for (i in 1..populationSize - 1) {
            for (j in 0..numberOfParameters - 1) {
                population.setEntry(i, j, RandomGenerator.rand() * (parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j)) + parameterLowerBounds.getEntry(j))
            }
        }
        population.setRowVector(0, initialGuess)
        return minimizeWithPopulation(population, f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crossoverFrequency, tol)
    }


    /**
     * Performs a minimization of a function starting with only parameter bounds; a full population is generated based on these bounds.
     * @param f                     The function to be minimized.
     * @param parameterLowerBounds  The lower bounds of each parameter.  Generated parameter values less than these values will be discarded.
     * @param parameterUpperBounds  The upper bounds of each parameter.  Generated parameter values greater than these values will be discarded.
     * @param populationSize        The size of the population of parameters sets to use for optimization.
     * @param scaleFactor           Factor controlling the scale of crossed over entries during crossover events.
     * @param maxIterations         The maximum number of iterations to allow before returning a result.
     * @param crossoverFrequency    The frequency of crossover from 0 to 1.  At any given parameter, this is the probability of initiating a crossover as well as the probability of ending one after it has started.
     * @param tol                   Relative function value tolerance controlling termination; if the maximal and minimal population values differ by less than this factor times the maximal value, optimization will terminate.
     * @return                      The parameter values at the minimal function value found.
     */
    fun minimize(f: ObjectiveFunction, parameterLowerBounds: RealVector, parameterUpperBounds: RealVector, populationSize: Int, scaleFactor: Double, maxIterations: Int, crossoverFrequency: Double, tol: Double): RealVector {
        val numberOfParameters = parameterLowerBounds.dimension

        //generate the initial population
        val population = Array2DRowRealMatrix(populationSize, numberOfParameters)
        for (i in 0..populationSize - 1) {
            for (j in 0..numberOfParameters - 1) {
                population.setEntry(i, j, RandomGenerator.rand() * (parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j)) + parameterLowerBounds.getEntry(j))
            }
        }
        return minimizeWithPopulation(population, f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crossoverFrequency, tol)
    }
}
