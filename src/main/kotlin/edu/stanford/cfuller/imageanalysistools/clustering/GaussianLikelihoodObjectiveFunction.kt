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
    internal var mean: RealVector = ArrayRealVector(numDim)
    internal var x: RealVector = ArrayRealVector(numDim)
    internal var pk: RealVector = ArrayRealVector()
    internal var clusterProbs: RealMatrix = Array2DRowRealMatrix()
    internal var abdMatrices: java.util.Vector<RealMatrix> = java.util.Vector<RealMatrix>()
    internal var det: RealVector = ArrayRealVector()

    /**
     * Evaluates the function with the specified parameters.

     * The parameters describe a set of gaussian generators (which are the Clusters).

     * @param parameters    A RealVector containing the values of all the parameters of each Gaussian, ordered so that all the parameters of a single gaussian are together, then the next gaussian, etc.
     * *
     * @return              The negative log likelihood of having observed the ClusterObjects at their locations, given the parameters describing the Gaussian clusters.
     */
    override fun evaluate(parameters: RealVector): Double {
        val nClusters = parameters.dimension / nParametersEach

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

        for (i in 0..nClusters - 1) {
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

        this.objects.indices.forEach { n ->
            val c = this.objects[n]
            var max = -1.0 * java.lang.Double.MAX_VALUE
            var maxIndex = 0

            for (k in 0..nClusters - 1) {
                mean.setEntry(0, c.centroid.x - parameters.getEntry(nParametersEach * k))
                mean.setEntry(1, c.centroid.y - parameters.getEntry(nParametersEach * k + 1))
                x = abdMatrices[k].operate(mean)
                val dot = x.dotProduct(mean)
                val logN = negLog2PI - 0.5 * Math.log(det.getEntry(k)) - 0.5 * dot

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

            this.objects.indices
                    .asSequence()
                    .filter { clusterProbs.getEntry(it, k) > tempMax }
                    .forEach { tempMax = clusterProbs.getEntry(it, k) }

            pk.setEntry(k, tempMax + Math.log(clusterProbs.getColumnVector(k).mapSubtract(tempMax).mapToSelf(Exp()).l1Norm) - Math.log(this.objects.size.toDouble()))
        }

        var pkMax = -1.0 * java.lang.Double.MAX_VALUE

        (0..nClusters - 1)
                .asSequence()
                .filter { pk.getEntry(it) > pkMax }
                .forEach { pkMax = pk.getEntry(it) }

        val logSumPk = pkMax + Math.log(pk.mapSubtract(pkMax).mapToSelf(Exp()).l1Norm)
        pk.mapSubtractToSelf(logSumPk)
        var L = 0.0

        for (n in this.objects.indices) {
            val toSum = clusterProbs.getRowVector(n).add(pk)
            var tempMax = -1.0 * java.lang.Double.MAX_VALUE

            (0..nClusters - 1)
                    .asSequence()
                    .filter { toSum.getEntry(it) > tempMax }
                    .forEach { tempMax = toSum.getEntry(it) }

            val pn = tempMax + Math.log(toSum.mapSubtract(tempMax).mapToSelf(Exp()).l1Norm)
            L += pn
        }

        return -1.0 * L
    }
}
