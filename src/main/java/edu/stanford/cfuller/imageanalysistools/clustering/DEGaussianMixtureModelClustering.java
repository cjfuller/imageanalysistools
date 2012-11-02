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

package edu.stanford.cfuller.imageanalysistools.clustering;

import edu.stanford.cfuller.imageanalysistools.fitting.DifferentialEvolutionMinimizer;
import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Implements Gaussian mixture model clustering for a set of ClusterObjects using a differential evolution algorithm for the likelihood maximization.
 * 
 * @author Colin J. Fuller
 */
public class DEGaussianMixtureModelClustering {

    /**
     * Performs Gaussian mixture model clustering on the given inputs using a differential evolution algorithm for maximization of the likelihood of having observed the data.
     * @param singleCluster     An Image mask with each object to be clustered labeled with a unique greylevel value.  (Note that the name stems from this method's original use to recursively divide existing single clusters; this need not actually correspond to a single cluster).
     * @param clusterObjects    A Vector containing an initialized ClusterObject for each object present in the Image passed as singleCluster.
     * @param clusters          A Vector containing an initialized Cluster (guess) for each of the k clusters that will be determined.
     * @param k                 The number of clusters to end up with.
     * @param n                 The number of cluster objects.
     * @return                  The negative log likelihood of observing the objects at their locations, given the maximally likely clustering scheme found.  On return, clusterObjects and clusters will have been updated to reflect this maximally likely scheme.
     */
    public static double go(Image singleCluster, java.util.Vector<ClusterObject> clusterObjects, java.util.Vector<Cluster> clusters, int k, int n) {

        final int numParametersEach = 5;

        int numParameters = k*numParametersEach;

        int populationSize = numParameters;

        double tol = 1.0e-3;
        double scaleFactor =0.9;
        double crFrq = 0.05;
        int maxIterations = 10;

        RealVector parameterLowerBounds = new ArrayRealVector(numParameters);
        RealVector parameterUpperBounds = new ArrayRealVector(numParameters);

        for (int i =0; i < k; i++) {

            parameterLowerBounds.setEntry(numParametersEach*i,    -0.1*singleCluster.getDimensionSizes().get(ImageCoordinate.X));
            parameterLowerBounds.setEntry(numParametersEach*i+1,  -0.1*singleCluster.getDimensionSizes().get(ImageCoordinate.Y));
            parameterLowerBounds.setEntry(numParametersEach*i+2,  tol);
            parameterLowerBounds.setEntry(numParametersEach*i+3,  -1);
            parameterLowerBounds.setEntry(numParametersEach*i+4,  tol);

            parameterUpperBounds.setEntry(numParametersEach*i,    1.1*singleCluster.getDimensionSizes().get(ImageCoordinate.X));
            parameterUpperBounds.setEntry(numParametersEach*i+1,  1.1*singleCluster.getDimensionSizes().get(ImageCoordinate.Y));
            parameterUpperBounds.setEntry(numParametersEach*i+2,  Math.pow(0.05*singleCluster.getDimensionSizes().get(ImageCoordinate.X), 2));
            parameterUpperBounds.setEntry(numParametersEach*i+3,  1);
            parameterUpperBounds.setEntry(numParametersEach*i+4,  Math.pow(0.05*singleCluster.getDimensionSizes().get(ImageCoordinate.Y), 2));
            
        }

        ObjectiveFunction f = new GaussianLikelihoodObjectiveFunction(clusterObjects);

        DifferentialEvolutionMinimizer dem = new DifferentialEvolutionMinimizer();

        RealVector output = null;

        double L = Double.MAX_VALUE;

        while (L == Double.MAX_VALUE || output == null) {
            output = dem.minimize(f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crFrq, tol);
            L = f.evaluate(output);
        }




        //set up new clusters

        for (int i = 0; i < k; i++) {
            clusters.get(i).setCentroidComponents(output.getEntry(numParametersEach*i), output.getEntry(numParametersEach*i+1), 0);
            clusters.get(i).setID(i+1);
            clusters.get(i).getObjectSet().clear();
        }

        //assign objects to clusters

        for (ClusterObject c : clusterObjects) {
            c.setCurrentCluster(clusters.get(c.getMostProbableCluster()));
            c.getCurrentCluster().getObjectSet().add(c);
        }

        return L;

    }

}
