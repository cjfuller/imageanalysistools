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

package edu.stanford.cfuller.imageanalysistools.clustering

import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction

import org.apache.commons.math3.analysis.function.Exp
import org.apache.commons.math3.linear.*

/**
 * A function that calculates the likelihood of observing a set of points given a set of Gaussian sources generating them.

 * This is used for clustering to determine the likelihood of having observed the ClusterObjects at their locations given the
 * set of Clusters currently under consideration.

 * @author Colin J. Fuller
 */
class GaussianLikelihoodObjectiveFunction
/**
 * Constructs a new GaussianLikelihoodObjectiveFunction that will determine the likelihood of having observed the
 * supplied ClusterObjects at their locations.

 * @param objects   A Vector containing the observed ClusterObjects (with locations already determined and assigned).
 */
(internal var objects: java.util.Vector<ClusterObject>) : ObjectiveFunction {


    internal val negLog2PI = -1.0 * Math.log(Math.PI * 2.0)
    internal val nParametersEach = 5
    internal val numDim = 2
    internal val detCutoff = 1.0e-30
    internal var mean: RealVector
    internal var x: RealVector
    internal var pk: RealVector
    internal var clusterProbs: RealMatrix
    internal var abdMatrices: java.util.Vector<RealMatrix>
    internal var det: RealVector

    init {
        mean = ArrayRealVector(numDim)
        x = ArrayRealVector(numDim)
        pk = ArrayRealVector()
        clusterProbs = Array2DRowRealMatrix()
        abdMatrices = java.util.Vector<RealMatrix>()
        det = ArrayRealVector()

    }

    /**
     * Evaluates the function with the specified parameters.

     * The parameters describe a set of gaussian generators (which are the Clusters).

     * @param parameters    A RealVector containing the values of all the parameters of each Gaussian, ordered so that all the parameters of a single gaussian are together, then the next gaussian, etc.
     * *
     * @return              The negative log likelihood of having observed the ClusterObjects at their locations, given the parameters describing the Gaussian clusters.
     */
    override fun evaluate(parameters: RealVector): Double {

        val nClusters = parameters.dimension / nParametersEach

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("nClusters: " + nClusters + "  abdMatrices_size: " + abdMatrices.size() + "  det_dim: " + det.getDimension());


        if (det.dimension != nClusters) {

            clusterProbs = Array2DRowRealMatrix(this.objects.size, nClusters)
            det = ArrayRealVector(nClusters)
            pk = ArrayRealVector(nClusters)

            if (abdMatrices.size < nClusters) {
                val originalSize = abdMatrices.size
                for (i in 0..nClusters - originalSize - 1) {
                    abdMatrices.add(Array2DRowRealMatrix(numDim, numDim))
                }
            } else {
                abdMatrices.setSize(nClusters)
            }

        }

        pk.mapMultiplyToSelf(0.0)


        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("nClusters: " + nClusters + "  abdMatrices_size: " + abdMatrices.size() + "  det_dim: " + det.getDimension());

        for (i in 0..nClusters - 1) {
            /*
            double ct = Math.cos(parameters.getEntry(nParametersEach*i+3));
            double st = Math.sin(parameters.getEntry(nParametersEach*i+3));
            double sin2t = Math.sin(2*parameters.getEntry(nParametersEach*i+3));
            double a = (ct*ct/(2*parameters.getEntry(nParametersEach*i+2)) + st*st/(2*parameters.getEntry(nParametersEach*i+4)));
            double b = (sin2t/(4*parameters.getEntry(nParametersEach*i+4)) - sin2t/(4*parameters.getEntry(nParametersEach*i+2)));
            double d = (st*st/(2*parameters.getEntry(nParametersEach*i+2)) + ct*ct/(2*parameters.getEntry(nParametersEach*i+4)));
            */

            val a = parameters.getEntry(nParametersEach * i + 2)
            val d = parameters.getEntry(nParametersEach * i + 4)
            val b = Math.sqrt(a * d) * parameters.getEntry(nParametersEach * i + 3)

            abdMatrices[i].setEntry(0, 0, a)
            abdMatrices[i].setEntry(1, 0, b)
            abdMatrices[i].setEntry(0, 1, b)
            abdMatrices[i].setEntry(1, 1, d)

            val abdLU = LUDecomposition(abdMatrices[i])

            det.setEntry(i, abdLU.determinant)
            //det.setEntry(i, a*d-b*b);
            try {
                abdMatrices[i] = abdLU.solver.inverse
            } catch (e: org.apache.commons.math3.linear.SingularMatrixException) {
                return java.lang.Double.MAX_VALUE
            }

        }


        for (n in this.objects.indices) {

            val c = this.objects[n]

            var max = -1.0 * java.lang.Double.MAX_VALUE
            var maxIndex = 0

            for (k in 0..nClusters - 1) {

                mean.setEntry(0, c.centroid.x - parameters.getEntry(nParametersEach * k))
                mean.setEntry(1, c.centroid.y - parameters.getEntry(nParametersEach * k + 1))

                x = abdMatrices[k].operate(mean)

                val dot = x.dotProduct(mean)

                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("k, n: " + k + ", " + this.objects.size());
                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("parameters: " + parameters.toString());
                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("abd matrix: " + abdMatrices.get(k).toString());
                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("det: " + det.getEntry(k));
                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("mean: " + mean.toString());
                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("dot: " + dot);


                val logN = negLog2PI - 0.5 * Math.log(det.getEntry(k)) - 0.5 * dot

                //                java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("logN: " + logN);


                clusterProbs.setEntry(n, k, logN)
                if (logN > max) {
                    max = logN
                    maxIndex = k
                }

                if (java.lang.Double.isInfinite(logN) || java.lang.Double.isNaN(logN)) {
                    return java.lang.Double.MAX_VALUE
                }

            }

            c.mostProbableCluster = maxIndex


        }

        for (k in 0..nClusters - 1) {

            var tempMax = -1.0 * java.lang.Double.MAX_VALUE

            for (n in this.objects.indices) {
                if (clusterProbs.getEntry(n, k) > tempMax) tempMax = clusterProbs.getEntry(n, k)
            }

            pk.setEntry(k, tempMax + Math.log(clusterProbs.getColumnVector(k).mapSubtract(tempMax).mapToSelf(Exp()).l1Norm) - Math.log(this.objects.size.toDouble()))

        }


        var pkMax = -1.0 * java.lang.Double.MAX_VALUE

        for (k in 0..nClusters - 1) {
            if (pk.getEntry(k) > pkMax) pkMax = pk.getEntry(k)
        }

        val logSumPk = pkMax + Math.log(pk.mapSubtract(pkMax).mapToSelf(Exp()).l1Norm)

        pk.mapSubtractToSelf(logSumPk)

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("pk: " + pk.toString());

        var L = 0.0

        for (n in this.objects.indices) {

            val toSum = clusterProbs.getRowVector(n).add(pk)

            var tempMax = -1.0 * java.lang.Double.MAX_VALUE

            for (k in 0..nClusters - 1) {
                if (toSum.getEntry(k) > tempMax) tempMax = toSum.getEntry(k)
            }

            val pn = tempMax + Math.log(toSum.mapSubtract(tempMax).mapToSelf(Exp()).l1Norm)

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("pn: " + pn);

            L += pn

        }


        return -1.0 * L
    }

}
