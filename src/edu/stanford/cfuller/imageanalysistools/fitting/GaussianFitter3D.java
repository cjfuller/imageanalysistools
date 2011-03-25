package edu.stanford.cfuller.imageanalysistools.fitting;


import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;import java.lang.Math;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 1/4/11
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaussianFitter3D {

    static RealVector logFactorialLookups;

    static int maxLogFactorialPrecalculated;

    static {
        synchronized (GaussianFitter3D.class) {

            logFactorialLookups = new ArrayRealVector(65536, 0.0);
            maxLogFactorialPrecalculated = -1;

        }
    }


    public RealVector fit(ImageObject toFit, RealVector initialGuess, double ppg) {

        //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background


        RealVector parameterLowerBounds = initialGuess.mapMultiply(0.2);
        RealVector parameterUpperBounds = initialGuess.mapMultiply(5);



        int populationSize = 14;
        double tol = 1.0e-6;
        double scaleFactor = 0.9;
        double crFrq = 0.05;
        int maxIterations = 10;

        NelderMeadMinimizer nmm = new NelderMeadMinimizer(tol);

        RealVector result = nmm.optimize(new MLObjectiveFunction(toFit, ppg), initialGuess);

        return result;

    }

    private double gaussian(double x, double y, double z, double[] parameters) {

        double xmx0 = x-parameters[3];
        double ymy0 = y-parameters[4];
        double zmz0 = z-parameters[5];
        double vxy = parameters[1];
        double vz = parameters[2];
        double A = parameters[0];
        double b = parameters[6];



        return ( A * Math.exp(-(xmx0*xmx0 + ymy0*ymy0)/(2*vxy) - zmz0*zmz0/(2*vz)) + b);

    }

    private double poissonProb(double x, double y, double z, double[] parameters, double n) {
        double mean = gaussian(x,y,z,parameters);

        return (n*Math.log(mean) - mean - logFactorial((int) Math.round(n)+1));
            
    }

    private double logLikelihood(ImageObject toFit, double[] parameters, double ppg) {
        double logLikelihood = 0;

        double[] x = toFit.getxValues();
        double[] y = toFit.getyValues();
        double[] z = toFit.getzValues();
        double[] f = toFit.getFunctionValues();

        for (int i = 0; i < x.length; i++) {

            logLikelihood -= poissonProb(x[i], y[i], z[i], parameters, f[i]);

        }

        return logLikelihood;
    }

    private class MLObjectiveFunction implements ObjectiveFunction, MultivariateRealFunction {


        private ImageObject toFit;
        private double ppg;
        private RealVector params_temp;

        public MLObjectiveFunction(ImageObject toFit, double ppg) {
            this.toFit = toFit;
            this.ppg = ppg;
            this.params_temp = null;
        }

        public double evaluate(RealVector parameters) {

            return value(parameters.getData());

        }

        public double value(double[] parameters) {
            return logLikelihood(this.toFit, parameters, this.ppg);
        }

    }

    public double fitResidual(double value, double x, double y, double z, RealVector parameters) {

        return value - gaussian(x,y,z,parameters.getData());
    }

    private double logFactorial(int n) {

        if (n < 0) return 0;

        if (n <= maxLogFactorialPrecalculated) {
            return logFactorialLookups.getEntry(n);
        }

        synchronized (GaussianFitter3D.class) {

            if (n > maxLogFactorialPrecalculated) {

                if (n >= logFactorialLookups.getDimension()) {

                    int sizeDiff = n+1 - logFactorialLookups.getDimension();

                    RealVector toAppend = new ArrayRealVector(sizeDiff, 0.0);

                    RealVector newLookups = logFactorialLookups.append(toAppend);

                    logFactorialLookups = newLookups;

                }

                for (int i = maxLogFactorialPrecalculated + 1; i <= n; i++) {

                    if (i == 0) {logFactorialLookups.setEntry(i, 0);} else {
                        logFactorialLookups.setEntry(i, logFactorialLookups.getEntry(i-1) + Math.log(i));
                    }
                }

                maxLogFactorialPrecalculated = n;


            }

            


        }

        return logFactorialLookups.getEntry(n);

    }


}
