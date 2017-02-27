package edu.stanford.cfuller.imageanalysistools.clustering

import edu.stanford.cfuller.imageanalysistools.fitting.DifferentialEvolutionMinimizer
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

/**
 * Implements Gaussian mixture model clustering for a set of ClusterObjects using a differential evolution algorithm for the likelihood maximization.

 * @author Colin J. Fuller
 */
object DEGaussianMixtureModelClustering {

    /**
     * Performs Gaussian mixture model clustering on the given inputs using a differential evolution algorithm for maximization of the likelihood of having observed the data.
     * @param singleCluster     An Image mask with each object to be clustered labeled with a unique greylevel value.  (Note that the name stems from this method's original use to recursively divide existing single clusters; this need not actually correspond to a single cluster).
     * *
     * @param clusterObjects    A Vector containing an initialized ClusterObject for each object present in the Image passed as singleCluster.
     * *
     * @param clusters          A Vector containing an initialized Cluster (guess) for each of the k clusters that will be determined.
     * *
     * @param k                 The number of clusters to end up with.
     * *
     * @param n                 The number of cluster objects.
     * *
     * @return                  The negative log likelihood of observing the objects at their locations, given the maximally likely clustering scheme found.  On return, clusterObjects and clusters will have been updated to reflect this maximally likely scheme.
     */
    fun go(singleCluster: Image, clusterObjects: java.util.Vector<ClusterObject>, clusters: java.util.Vector<Cluster>, k: Int, n: Int): Double {
        val numParametersEach = 5
        val numParameters = k * numParametersEach
        val populationSize = numParameters
        val tol = 1.0e-3
        val scaleFactor = 0.9
        val crFrq = 0.05
        val maxIterations = 10
        val parameterLowerBounds = ArrayRealVector(numParameters)
        val parameterUpperBounds = ArrayRealVector(numParameters)

        for (i in 0..k - 1) {
            parameterLowerBounds.setEntry(numParametersEach * i, -0.1 * singleCluster.dimensionSizes[ImageCoordinate.X])
            parameterLowerBounds.setEntry(numParametersEach * i + 1, -0.1 * singleCluster.dimensionSizes[ImageCoordinate.Y])
            parameterLowerBounds.setEntry(numParametersEach * i + 2, tol)
            parameterLowerBounds.setEntry(numParametersEach * i + 3, -1.0)
            parameterLowerBounds.setEntry(numParametersEach * i + 4, tol)

            parameterUpperBounds.setEntry(numParametersEach * i, 1.1 * singleCluster.dimensionSizes[ImageCoordinate.X])
            parameterUpperBounds.setEntry(numParametersEach * i + 1, 1.1 * singleCluster.dimensionSizes[ImageCoordinate.Y])
            parameterUpperBounds.setEntry(numParametersEach * i + 2, Math.pow(0.05 * singleCluster.dimensionSizes[ImageCoordinate.X], 2.0))
            parameterUpperBounds.setEntry(numParametersEach * i + 3, 1.0)
            parameterUpperBounds.setEntry(numParametersEach * i + 4, Math.pow(0.05 * singleCluster.dimensionSizes[ImageCoordinate.Y], 2.0))
        }

        val f = GaussianLikelihoodObjectiveFunction(clusterObjects)
        val dem = DifferentialEvolutionMinimizer()
        var output: RealVector? = null
        var L = java.lang.Double.MAX_VALUE

        while (L == java.lang.Double.MAX_VALUE || output == null) {
            output = dem.minimize(f, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crFrq, tol)
            L = f.evaluate(output)
        }

        //set up new clusters
        for (i in 0..k - 1) {
            clusters[i].setCentroidComponents(output.getEntry(numParametersEach * i), output.getEntry(numParametersEach * i + 1), 0.0)
            clusters[i].id = i + 1
            clusters[i].objectSet.clear()
        }

        //assign objects to clusters
        for (c in clusterObjects) {
            val cluster = clusters[c.mostProbableCluster]
            cluster.objectSet.add(c)
            c.currentCluster = cluster
        }
        return L
    }
}
