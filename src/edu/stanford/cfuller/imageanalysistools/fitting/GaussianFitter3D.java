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


import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;import java.lang.Math;

/**
 * Performs a three-dimensional gaussian fit to an object in an image using a maximum likelihood method assuming Poisson
 * distributed pixel intensities (once converted to units of photons).
 *
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


    /**
     * Fits a 3D Gaussian to a supplied object, starting from an initial guess of the parameters of that Gaussian.
     *
     * The Gaussian is contrained to be symmetric in the x and y dimensions (that is, it will have equal variance in both dimensions).
     *
     * @param toFit         The {@link ImageObject} to be fit to a Gaussian.
     * @param initialGuess  The initial guess at the parameters of the Gaussian.  These must be supplied in the order: amplitude, x-y variance, z variance, x position, y position, z position, background.  Positions should be supplied in absolute coordinates from the original image, not relative to the box around the object being fit.
     * @param ppg           The number of photons corresponding to one greylevel in the original image.
     * @return              The best fit Gaussian parameters, in the same order as the initial guess had to be supplied.
     */
    public RealVector fit(ImageObject toFit, RealVector initialGuess, double ppg) {

        //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background

        //System.out.println("for object " + toFit.getLabel() + " initial guess is: " + initialGuess.toString());


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


    /**
     * Calculates the fit residual between a set of parameters and a value at a supplied point.
     * @param value         The observed value at the point.
     * @param x             The x-coordinate of the point (in absolute original image coordinates)
     * @param y             The y-coordinate of the point (in absolute original image coordinates)
     * @param z             The z-coordinate of the point (in absolute original image coordinates.
     * @param parameters    The gaussian parameters in the same order as would be required for or returned from {@link #fit}
     * @return              The fit residual at the specified point.
     */
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
