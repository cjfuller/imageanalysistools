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

package edu.stanford.cfuller.imageanalysistools.clustering;

import edu.stanford.cfuller.imageanalysistools.fitting.DifferentialEvolutionMinimizer;
import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;

/**
 * Implements Gaussian mixture model clustering for a set of ClusterObjects using a differential evolution algorithm for the likelihood maximization.
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

            parameterLowerBounds.setEntry(numParametersEach*i,    -0.1*singleCluster.getDimensionSizes().getX());
            parameterLowerBounds.setEntry(numParametersEach*i+1,  -0.1*singleCluster.getDimensionSizes().getY());
            parameterLowerBounds.setEntry(numParametersEach*i+2,  tol);
            parameterLowerBounds.setEntry(numParametersEach*i+3,  -1);
            parameterLowerBounds.setEntry(numParametersEach*i+4,  tol);

            parameterUpperBounds.setEntry(numParametersEach*i,    1.1*singleCluster.getDimensionSizes().getX());
            parameterUpperBounds.setEntry(numParametersEach*i+1,  1.1*singleCluster.getDimensionSizes().getY());
            parameterUpperBounds.setEntry(numParametersEach*i+2,  Math.pow(0.05*singleCluster.getDimensionSizes().getX(), 2));
            parameterUpperBounds.setEntry(numParametersEach*i+3,  1);
            parameterUpperBounds.setEntry(numParametersEach*i+4,  Math.pow(0.05*singleCluster.getDimensionSizes().getY(), 2));
            
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
