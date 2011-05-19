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

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * A function minimizer that implements the Nelder-Mead or Downhill Simplex method.
 * 
 * @author Colin J. Fuller
 */
public class NelderMeadMinimizer {

    double a;
    double g;
    double r;
    double s;
    double tol;

    /**
     * Constructs a new minimizer with the specified minimization parameters.
     * @param a     Reflection factor (default: 1.0).
     * @param g     Expansion factor (default: 2.0).
     * @param r     Contraction factor (default: 0.5).
     * @param s     Reduction factor (default: 0.5).
     * @param tol   Function value relative tolerance for optimization termination.
     */
    public NelderMeadMinimizer(double a, double g, double r, double s, double tol) {

        this.a = a;
        this.g = g;
        this.r = r;
        this.s = s;
        this.tol = tol;
        
    }

    private void initDefaults() {
        this.a = 1.0;
        this.g = 2.0;
        this.r = 0.5;
        this.s = 0.5;
        this.tol = 1.0e-6;
    }

    /**
     * Constructs a new minimizer with the default minimization parameters except for tolerance, which is specified.
     * @param tol   Function value relative tolerance for optimization termination.
     */
    public NelderMeadMinimizer(double tol) {
        initDefaults();
        this.tol = tol;
    }

    /**
     * Constructs a new minimizer with the default minimization parameters.
     */
    public NelderMeadMinimizer() {
        initDefaults();
    }

    /**
     * Constructs the initial simplex that is the starting point of the optimization given an initial guess at the minimum and a size scale for each parameter.
     * @param initialPoint      The initial guess at the minimum, one component per parameter.
     * @param componentScales   A size scale for each parameter that is used to set how large the initial simplex is.
     * @return                  A matrix containing p + 1 rows, each of which is one set of p parameters, which specify the simplex.
     */
    public RealMatrix generateInitialSimplex(RealVector initialPoint, RealVector componentScales) {
        RealMatrix initialSimplex = new Array2DRowRealMatrix(initialPoint.getDimension()+1, initialPoint.getDimension());

        initialSimplex.setRowVector(0, initialPoint);

        for (int i =1; i < initialSimplex.getRowDimension(); i++) {
            RealVector newVector = initialPoint.copy();
            newVector.setEntry(i-1, newVector.getEntry(i-1) + componentScales.getEntry(i-1));
            initialSimplex.setRowVector(i, newVector);
        }

        return initialSimplex;
    }

    /**
     * Constructs the initial simplex that is the starting point of the optimization given an initial guess at the minimum.
     *
     * This method will attempt to guess the scale of each parameters, but it is preferable to specify this manually using the other form of
     * generateInitialSimplex if any information about these scales is known.
     *
     * @param initialPoint      The initial guess at the minimum, one component per parameter.
     * @return                  A matrix containing p + 1 rows, each of which is one set of p parameters, which specify the simplex.
     */
    public RealMatrix generateInitialSimplex(RealVector initialPoint) {

        final double constantScale = 0.1;

        RealVector componentScales = initialPoint.mapMultiply(constantScale);

        return generateInitialSimplex(initialPoint, componentScales);

    }

    /**
     * Runs the minimization of the specified function starting from an initial guess.
     * @param f                 The ObjectiveFunction to be minimized.
     * @param initialPoint      The initial guess, one component per parameter.
     * @return                  The parameters at the function minimum in the same order as specified in the guess.
     */
    public RealVector optimize(ObjectiveFunction f, RealVector initialPoint) {
        return this.optimize(f, generateInitialSimplex(initialPoint));
    }

    /**
     * Runs the minimization of the specified function starting from an initial simplex.
     * @param f                 The ObjectiveFunction to be minimized.
     * @param initialSimplex    The initial simplex to use to start optimization, as might be returned from {@link #generateInitialSimplex}
     * @return                  The parameters at the function minimum in the same order as specified for each point on the simplex.
     */
    public RealVector optimize(ObjectiveFunction f, RealMatrix initialSimplex) {

        RealMatrix currentSimplex = initialSimplex.copy();

        double currTolVal = 1.0e12;
        
        RealVector values = new ArrayRealVector(initialSimplex.getRowDimension(), 0.0);

        RealVector centerOfMass = new ArrayRealVector(initialSimplex.getColumnDimension(), 0.0);

        boolean shouldEvaluate = false;

        while (Math.abs(currTolVal) > this.tol) {

            int maxIndex = 0;
            int minIndex = 0;
            double maxValue = -1.0*Double.MAX_VALUE;
            double minValue = Double.MAX_VALUE;
            double secondMaxValue = -1.0*Double.MAX_VALUE;


            centerOfMass.mapMultiplyToSelf(0.0);

            if (shouldEvaluate) {

                for (int i = 0; i < currentSimplex.getRowDimension(); i++) {
                    RealVector currRow = currentSimplex.getRowVector(i);
                    values.setEntry(i, f.evaluate(currRow));
                }

            }

            for (int i =0; i < currentSimplex.getRowDimension(); i++) {

                double currValue = values.getEntry(i);

                if (currValue < minValue) {minValue = currValue; minIndex = i;}
                if (currValue > maxValue) {
                    secondMaxValue = maxValue;
                    maxValue = currValue;
                    maxIndex = i;
                } else if (currValue > secondMaxValue) {
                    secondMaxValue = currValue;
                }
            }


            for (int i =0; i < currentSimplex.getRowDimension(); i++) {
                if (i == maxIndex) continue;

                centerOfMass = centerOfMass.add(currentSimplex.getRow(i));

            }


            centerOfMass.mapDivideToSelf(currentSimplex.getRowDimension()-1);

            RealVector oldPoint = currentSimplex.getRowVector(maxIndex);

            RealVector newPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(a).add(centerOfMass); // newpoint = COM + a*(COM-oldpoint)

            double newValue = f.evaluate(newPoint);

            if (newValue < secondMaxValue) { // success

                if (newValue < minValue) { // best found so far

                    //expansion

                    RealVector expPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(g).add(centerOfMass);

                    double expValue = f.evaluate(expPoint);

                    if (expValue < newValue) {
                        currentSimplex.setRowVector(maxIndex, expPoint);
                        currTolVal = 2.0*(expValue - maxValue)/(1.0e-20 +expValue + maxValue);

                        values.setEntry(maxIndex, expValue);
                        shouldEvaluate = false;
                        continue;
                    }



                }

                //reflection

                currentSimplex.setRowVector(maxIndex, newPoint);
                currTolVal = 2.0*(newValue - maxValue)/(1.0e-20 + newValue + maxValue);
                values.setEntry(maxIndex, newValue);
                shouldEvaluate = false;
                continue;


                
            }

            //contraction

            RealVector conPoint = centerOfMass.subtract(oldPoint).mapMultiplyToSelf(r).add(centerOfMass);
            double conValue = f.evaluate(conPoint);

            if (conValue < maxValue) {
                currentSimplex.setRowVector(maxIndex, conPoint);
                currTolVal = 2.0*(conValue - maxValue)/(1.0e-20 + conValue + maxValue);
                values.setEntry(maxIndex, conValue);
                shouldEvaluate = false;
                continue;
            }

            //reduction

            for (int i =0; i < currentSimplex.getRowDimension(); i++) {
                if (i == minIndex) continue;

                currentSimplex.setRowVector(i, currentSimplex.getRowVector(i).subtract(currentSimplex.getRowVector(minIndex)).mapMultiplyToSelf(s).add(currentSimplex.getRowVector(minIndex)));

            }

            double redValue = f.evaluate(currentSimplex.getRowVector(maxIndex));

            currTolVal = 2.0*(redValue - maxValue)/(1.0e-20 + redValue + maxValue);

            shouldEvaluate = true;

        }

        double minValue = Double.MAX_VALUE;

        RealVector minVector = null;

        for (int i =0; i < currentSimplex.getRowDimension(); i++) {
            values.setEntry(i, f.evaluate(currentSimplex.getRowVector(i)));
            if (values.getEntry(i) < minValue) {
                minValue = values.getEntry(i);
                minVector = currentSimplex.getRowVector(i);
            }
        }


        return minVector;

    }



}
