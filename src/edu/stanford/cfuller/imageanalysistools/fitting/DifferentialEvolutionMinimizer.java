/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.fitting;

import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import edu.stanford.cfuller.imageanalysistools.random.RandomGenerator;



public class DifferentialEvolutionMinimizer {

    private void computeValues(ObjectiveFunction f, RealMatrix population, RealVector values, boolean[] valuesChanged) {

        for (int i =0; i < population.getRowDimension(); i++) {
            if (valuesChanged[i]) {
                values.setEntry(i, f.evaluate(population.getRowVector(i)));
            }
        }

    }


    public RealVector minimizeWithPopulation(RealMatrix population, ObjectiveFunction f, RealVector parameterLowerBounds, RealVector parameterUpperBounds, int populationSize, double scaleFactor, int maxIterations, double crossoverFrequency, double tol) {


        int numberOfParameters = parameterLowerBounds.getDimension();
        
        double currMinValue = Double.MAX_VALUE;
        double lastMinValue = Double.MAX_VALUE;
        double currMaxValue = -1.0*Double.MAX_VALUE;
        int iterationCounter = maxIterations;

        int totalIterations =0;

        RealVector values = new ArrayRealVector(populationSize);

        boolean[] valuesChanged = new boolean[populationSize];

        java.util.Arrays.fill(valuesChanged, true);

        computeValues(f, population, values, valuesChanged);

        RealVector newVec = new ArrayRealVector(numberOfParameters);

        RealMatrix newPopulation = new Array2DRowRealMatrix(populationSize, numberOfParameters);

        while(iterationCounter > 0) {

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Iteration counter: " + Integer.toString(iterationCounter));

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

                        if (newVec.getEntry(j) < parameterLowerBounds.getEntry(j) || newVec.getEntry(j) > parameterUpperBounds.getEntry(j)) {

                            inBounds = false;

                        }

                    } else {

                        if (isCrossingOver) {isCrossingOver = false;}

                        newVec.setEntry(j, population.getEntry(i,j));


                    }


                }

                double functionValue = Double.MAX_VALUE;

                if (inBounds) functionValue = f.evaluate(newVec);

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

            lastMinValue = currMinValue;

            String strValues = org.apache.commons.math.linear.RealVectorFormat.formatRealVector(values);

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Population values: " + strValues);


            double[] sValues = values.toArray();


            double tempMinValue = Double.MAX_VALUE;
            double tempMaxValue = -1.0*Double.MAX_VALUE;

            for (int i =0; i < values.getDimension(); i++) {
                double value = values.getEntry(i);
                if (value < tempMinValue) {
                    tempMinValue = value;
                }
                if (value > tempMaxValue) {
                    tempMaxValue = value;
                }

            }

            currMinValue = tempMinValue;
            currMaxValue = tempMaxValue;

            if (Math.abs(currMaxValue - currMinValue) < Math.abs(tol*currMaxValue)) {
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
