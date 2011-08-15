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

package edu.stanford.cfuller.imageanalysistools.fitting;

import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.Array2DRowRealMatrix;

import edu.stanford.cfuller.imageanalysistools.random.RandomGenerator;

/**
 * This class implements the differential evolution algorithm for function minimization.
 *
 * @author Colin J. Fuller
 */

public class DifferentialEvolutionMinimizer {

    /**
     * Updates the function values for each parameter set in the population only if that parameter set has changed.
     * @param f             The function being minimized
     * @param population    The current population of parameters, one parameter set per row.
     * @param values        The vector of the last computed values for each entry in the population; entries that have changed will be overwritten.
     * @param valuesChanged An array indicating whether each entry in the population has changed and requires computation of the value.
     */
    private void computeValues(ObjectiveFunction f, RealMatrix population, RealVector values, boolean[] valuesChanged) {

        for (int i =0; i < population.getRowDimension(); i++) {
            if (valuesChanged[i]) {
                values.setEntry(i, f.evaluate(population.getRowVector(i)));
            }
        }

    }


    /**
     * Performs a minimization of a function starting with a given population.
     * 
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
    public RealVector minimizeWithPopulation(RealMatrix population, ObjectiveFunction f, RealVector parameterLowerBounds, RealVector parameterUpperBounds, int populationSize, double scaleFactor, int maxIterations, double crossoverFrequency, double tol) {


        int numberOfParameters = parameterLowerBounds.getDimension();
        
        double currMinValue = Double.MAX_VALUE;
        double currMaxValue = -1.0*Double.MAX_VALUE;
        int iterationCounter = maxIterations;
        
        double mutationProb = 0.01;

        int totalIterations =0;

        RealVector values = new ArrayRealVector(populationSize);

        boolean[] valuesChanged = new boolean[populationSize];

        java.util.Arrays.fill(valuesChanged, true);

        computeValues(f, population, values, valuesChanged);

        RealVector newVec = new ArrayRealVector(numberOfParameters);

        RealMatrix newPopulation = new Array2DRowRealMatrix(populationSize, numberOfParameters);

        while(iterationCounter > 0) {

            
            for (int i =0; i < populationSize; i++) {

                int i1 = RandomGenerator.getGenerator().randInt(populationSize);
                int i2 = RandomGenerator.getGenerator().randInt(populationSize);
                int i3 = RandomGenerator.getGenerator().randInt(populationSize);

                newVec.mapMultiplyToSelf(0.0);

                boolean inBounds = true;

                boolean isCrossingOver = false;

                for (int j =0; j < numberOfParameters; j++) {

                    if ((RandomGenerator.rand() < crossoverFrequency) ^ isCrossingOver) {

                        if (!isCrossingOver) {isCrossingOver = true;}

                        newVec.setEntry(j, scaleFactor* (population.getEntry(i2, j) -population.getEntry(i1, j) ) + population.getEntry(i3, j) );

                        

                    } else {

                        if (isCrossingOver) {isCrossingOver = false;}

                        newVec.setEntry(j, population.getEntry(i,j));


                    }
                    
                    //random 10% range +/- mutation
                    
                    if ((RandomGenerator.rand() < mutationProb)) {
                    	
                    	double magnitude = 0.2*(parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j));
                    	
                    	newVec.setEntry(j, newVec.getEntry(j) + (RandomGenerator.rand() -0.5) * magnitude);
                    	
                    }
                    
                    if (newVec.getEntry(j) < parameterLowerBounds.getEntry(j) || newVec.getEntry(j) > parameterUpperBounds.getEntry(j)) {

                        inBounds = false;

                    }


                }

                double functionValue = Double.MAX_VALUE;

                if (inBounds) functionValue = f.evaluate(newVec);

                //if (inBounds) System.out.printf("in bounds candidate value: %1.2f  old value: %1.2f \n", functionValue, values.getEntry(i));
                
                if (functionValue < values.getEntry(i)) {

                    newPopulation.setRowVector(i, newVec);
                    valuesChanged[i] = true;
                    values.setEntry(i, functionValue);

                } else {

                    newPopulation.setRowVector(i, population.getRowVector(i));
                    valuesChanged[i] = false;
                }

            }

            population = newPopulation;

            double tempMinValue = Double.MAX_VALUE;
            double tempMaxValue = -1.0*Double.MAX_VALUE;
            
            double averageValue = 0;

            for (int i =0; i < values.getDimension(); i++) {
                double value = values.getEntry(i);
                averageValue += value;
                if (value < tempMinValue) {
                    tempMinValue = value;
                }
                if (value > tempMaxValue) {
                    tempMaxValue = value;
                }

            }
            
            averageValue /= values.getDimension();

            currMinValue = tempMinValue;
            currMaxValue = tempMaxValue;
            
            //LoggingUtilities.getLogger().info("Iteration counter: " + Integer.toString(totalIterations) + "  best score: " + currMinValue + "  worst score: " + currMaxValue + " average score: " + averageValue);

            if (Math.abs(currMaxValue - currMinValue) < Math.abs(tol*currMaxValue) + Math.abs(tol*currMinValue)) {
                iterationCounter--;
            } else {
                iterationCounter = 1;
            }

            totalIterations++;

        }

        for (int i = 0; i < populationSize; i++) {
            valuesChanged[i] = true;
        }

        computeValues(f, population, values, valuesChanged);

        double tempMinValue = Double.MAX_VALUE;
        int tempMinIndex = 0;

        for (int i =0; i < populationSize; i++) {

            if (values.getEntry(i) < tempMinValue) {
                tempMinValue = values.getEntry(i);
                tempMinIndex = i;
            }
        }

        RealVector output = new ArrayRealVector(numberOfParameters);

        for (int i =0; i < numberOfParameters; i++) {

            output.setEntry(i, population.getEntry(tempMinIndex, i));

        }


        return output;

    }


    /**
     * Performs a minimization of a function starting with an single initial guess; a full population is generated based on this guess and the parameter bounds.
     *
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
    public RealVector minimizeWithInitial(ObjectiveFunction f, RealVector parameterLowerBounds, RealVector parameterUpperBounds, int populationSize, double scaleFactor, int maxIterations, double crossoverFrequency, double tol, RealVector initialGuess) {

        int numberOfParameters = parameterLowerBounds.getDimension();

        //generate the initial population

        RealMatrix population = new Array2DRowRealMatrix(populationSize, numberOfParameters);

        for (int i =1; i < populationSize; i++) {

            for (int j = 0; j < numberOfParameters; j++) {

                population.setEntry(i,j,RandomGenerator.rand()*(parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j)) + parameterLowerBounds.getEntry(j));

            }
        }

        population.setRowVector(0, initialGuess);

        return minimizeWithPopulation(population, f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crossoverFrequency, tol);

    }


    /**
     * Performs a minimization of a function starting with only parameter bounds; a full population is generated based on these bounds.
     *
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
    public RealVector minimize(ObjectiveFunction f, RealVector parameterLowerBounds, RealVector parameterUpperBounds, int populationSize, double scaleFactor, int maxIterations, double crossoverFrequency, double tol) {

        int numberOfParameters = parameterLowerBounds.getDimension();

        //generate the initial population

        RealMatrix population = new Array2DRowRealMatrix(populationSize, numberOfParameters);

        for (int i =0; i < populationSize; i++) {

            for (int j = 0; j < numberOfParameters; j++) {

                population.setEntry(i,j,RandomGenerator.rand()*(parameterUpperBounds.getEntry(j) - parameterLowerBounds.getEntry(j)) + parameterLowerBounds.getEntry(j));

            }
        }

        return minimizeWithPopulation(population, f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crossoverFrequency, tol);



    }



}
