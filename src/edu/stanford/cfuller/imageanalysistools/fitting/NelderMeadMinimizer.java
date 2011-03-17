package edu.stanford.cfuller.imageanalysistools.fitting;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 1/6/11
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class NelderMeadMinimizer {

    double a;
    double g;
    double r;
    double s;
    double tol;

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

    public NelderMeadMinimizer(double tol) {
        initDefaults();
        this.tol = tol;
    }

    public NelderMeadMinimizer() {
        initDefaults();
    }

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

    public RealMatrix generateInitialSimplex(RealVector initialPoint) {

        final double constantScale = 0.1;

        RealVector componentScales = initialPoint.mapMultiply(constantScale);

        return generateInitialSimplex(initialPoint, componentScales);

    }


    public RealVector optimize(ObjectiveFunction f, RealVector initialPoint) {
        return this.optimize(f, generateInitialSimplex(initialPoint));
    }

    public RealVector optimize(ObjectiveFunction f, RealMatrix initialSimplex) {

        RealMatrix currentSimplex = initialSimplex.copy();

        double currTolVal = 1.0e12;
        
        RealVector values = new ArrayRealVector(initialSimplex.getRowDimension(), 0.0);

        RealVector centerOfMass = new ArrayRealVector(initialSimplex.getColumnDimension(), 0.0);

        boolean shouldEvaluate = false;

        while (Math.abs(currTolVal) > this.tol) {

            int maxIndex = 0;
            int minIndex = 0;
            int secondMaxIndex = 0;

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
                    secondMaxIndex = maxIndex;
                    maxValue = currValue;
                    maxIndex = i;
                } else if (currValue > secondMaxValue) {
                    secondMaxValue = currValue;
                    secondMaxIndex = i;
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
