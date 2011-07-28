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

import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import org.apache.commons.math.linear.*;

/**
 * A function that calculates the likelihood of observing a set of points given a set of Gaussian sources generating them.
 *
 * This is used for clustering to determine the likelihood of having observed the ClusterObjects at their locations given the
 * set of Clusters currently under consideration.
 *
 * @author Colin J. Fuller
 *
 */
public class GaussianLikelihoodObjectiveFunction implements ObjectiveFunction {


    final double negLog2PI = -1.0*Math.log(Math.PI*2.0);
    final int nParametersEach = 5;
    final int numDim = 2;
    final double detCutoff = 1.0e-30;
    RealVector mean;
    RealVector x;
    RealVector pk;
    RealMatrix clusterProbs;
    java.util.Vector<RealMatrix> abdMatrices;
    RealVector det;
    java.util.Vector<ClusterObject> objects;

    /**
     * Constructs a new GaussianLikelihoodObjectiveFunction that will determine the likelihood of having observed the
     * supplied ClusterObjects at their locations.
     *
     * @param objects   A Vector containing the observed ClusterObjects (with locations already determined and assigned).
     */
    public GaussianLikelihoodObjectiveFunction(java.util.Vector<ClusterObject> objects) {
        mean = new ArrayRealVector(numDim);
        x = new ArrayRealVector(numDim);
        pk = new ArrayRealVector();
        clusterProbs = new Array2DRowRealMatrix();
        abdMatrices = new java.util.Vector<RealMatrix>();
        det = new ArrayRealVector();
        this.objects = objects;

    }

    /**
     * Evaluates the function with the specified parameters.
     *
     * The parameters describe a set of gaussian generators (which are the Clusters).
     *
     * @param parameters    A RealVector containing the values of all the parameters of each Gaussian, ordered so that all the parameters of a single gaussian are together, then the next gaussian, etc.
     * @return              The negative log likelihood of having observed the ClusterObjects at their locations, given the parameters describing the Gaussian clusters.
     */
    public double evaluate(RealVector parameters) {

        int nClusters = parameters.getDimension()/nParametersEach;

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("nClusters: " + nClusters + "  abdMatrices_size: " + abdMatrices.size() + "  det_dim: " + det.getDimension());


        if (det.getDimension() != nClusters) {
            
            clusterProbs = new Array2DRowRealMatrix(this.objects.size(), nClusters);
            det = new ArrayRealVector(nClusters);
            pk = new ArrayRealVector(nClusters);

            if (abdMatrices.size() < nClusters) {
                int originalSize = abdMatrices.size();
                for (int i =0; i < nClusters - originalSize; i++) {
                    abdMatrices.add(new Array2DRowRealMatrix(numDim, numDim));
                }
            } else {
                abdMatrices.setSize(nClusters);
            }

        }

        pk.mapMultiplyToSelf(0.0);
        

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("nClusters: " + nClusters + "  abdMatrices_size: " + abdMatrices.size() + "  det_dim: " + det.getDimension());

        for (int i =0; i < nClusters; i++) {
            /*
            double ct = Math.cos(parameters.getEntry(nParametersEach*i+3));
            double st = Math.sin(parameters.getEntry(nParametersEach*i+3));
            double sin2t = Math.sin(2*parameters.getEntry(nParametersEach*i+3));
            double a = (ct*ct/(2*parameters.getEntry(nParametersEach*i+2)) + st*st/(2*parameters.getEntry(nParametersEach*i+4)));
            double b = (sin2t/(4*parameters.getEntry(nParametersEach*i+4)) - sin2t/(4*parameters.getEntry(nParametersEach*i+2)));
            double d = (st*st/(2*parameters.getEntry(nParametersEach*i+2)) + ct*ct/(2*parameters.getEntry(nParametersEach*i+4)));
            */

            double a = parameters.getEntry(nParametersEach*i+2);
            double d = parameters.getEntry(nParametersEach*i+4);
            double b = Math.sqrt(a*d)*parameters.getEntry(nParametersEach*i+3);
                    
            abdMatrices.get(i).setEntry(0, 0, a);
            abdMatrices.get(i).setEntry(1, 0, b);
            abdMatrices.get(i).setEntry(0, 1, b);
            abdMatrices.get(i).setEntry(1, 1, d);

            LUDecomposition abdLU = (new LUDecompositionImpl(abdMatrices.get(i)));

            det.setEntry(i, (abdLU).getDeterminant());
            //det.setEntry(i, a*d-b*b);
            try {
                abdMatrices.set(i, abdLU.getSolver().getInverse());
            } catch (org.apache.commons.math.linear.SingularMatrixException e) {
                return Double.MAX_VALUE;
            }

        }


        for (int n =0; n < this.objects.size(); n++) {
            
            ClusterObject c = this.objects.get(n);

            double max = -1.0*Double.MAX_VALUE;
            int maxIndex = 0;

            for (int k = 0; k < nClusters; k++) {

                mean.setEntry(0, c.getCentroid().getX() - parameters.getEntry(nParametersEach*k));
                mean.setEntry(1, c.getCentroid().getY() - parameters.getEntry(nParametersEach*k+1));

                x = abdMatrices.get(k).operate(mean);

                double dot = x.dotProduct(mean);

//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("k, n: " + k + ", " + this.objects.size());
//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("parameters: " + parameters.toString());
//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("abd matrix: " + abdMatrices.get(k).toString());
//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("det: " + det.getEntry(k));
//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("mean: " + mean.toString());
//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("dot: " + dot);



                double logN = negLog2PI - 0.5*Math.log(det.getEntry(k)) -0.5*dot;

//                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("logN: " + logN);


                clusterProbs.setEntry(n, k, logN);
                if (logN > max) {
                    max = logN;
                    maxIndex = k;
                }

                if (Double.isInfinite(logN) || Double.isNaN(logN)) {
                    return Double.MAX_VALUE;
                }

            }

            c.setMostProbableCluster(maxIndex);


        }

        for (int k = 0; k < nClusters; k++) {

            double tempMax = -1.0*Double.MAX_VALUE;

            for (int n = 0; n < this.objects.size(); n++) {
                if (clusterProbs.getEntry(n, k) > tempMax) tempMax = clusterProbs.getEntry(n,k);
            }

            pk.setEntry(k, tempMax + Math.log(clusterProbs.getColumnVector(k).mapSubtract(tempMax).mapExpToSelf().getL1Norm()) - Math.log(this.objects.size()));
            
        }


        double pkMax = -1.0*Double.MAX_VALUE;

        for (int k = 0; k < nClusters; k++) {
            if (pk.getEntry(k) > pkMax) pkMax = pk.getEntry(k);
        }

        double logSumPk = pkMax + Math.log(pk.mapSubtract(pkMax).mapExpToSelf().getL1Norm());

        pk.mapSubtractToSelf(logSumPk);

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("pk: " + pk.toString());

        double L = 0;

        for (int n = 0; n < this.objects.size(); n++) {

            RealVector toSum = clusterProbs.getRowVector(n).add(pk);

            double tempMax = -1.0*Double.MAX_VALUE;

            for (int k = 0; k < nClusters; k++) {
                if (toSum.getEntry(k) > tempMax) tempMax = toSum.getEntry(k);
            }

            double pn = tempMax + Math.log(toSum.mapSubtract(tempMax).mapExpToSelf().getL1Norm());

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("pn: " + pn);

            L += pn;

        }
        

        return -1.0*L;
    }

}
