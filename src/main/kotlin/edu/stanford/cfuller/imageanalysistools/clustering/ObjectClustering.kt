package edu.stanford.cfuller.imageanalysistools.clustering

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.ArrayRealVector
import edu.stanford.cfuller.imageanalysistools.random.RandomGenerator

import java.util.Vector

/**
 * Utilites for doing clustering of objects in an Image.
 * @author Colin J. Fuller
 */

object ObjectClustering {
    /**
     * Sets up a set of ClusterObjects and a set of Clusters from two Image masks, one labeled with individual objects, and one labeled with all objects in a single cluster grouped with a single label.
     * @param labeledByObject       An Image mask with all objects in the Image labeled with an unique greylevel value.  These labels must start with 1 and be consecutive.
     * @param labeledByCluster      An Image mask with all the objects in each cluster labeled with the same unique greylevel value.  These labels must start with 1 and be consecutive.
     * @param clusterObjects        A Vector of ClusterObjects that will contain the initialized ClusterObjects on return; this can be empty, and any contents will be erased.
     * @param clusters              A Vector of Clusters that will contain the initialized Clusters on return; this can be empty, and any contents will be erased.
     * @param k                     The number of clusters in the Image.  This must be the same as the number of unique nonzero greylevels in the labeledByCluster Image.
     * @return                      The number of ClusterObjects in the Image.
     */
    fun initializeObjectsAndClustersFromClusterImage(labeledByObject: Image, labeledByCluster: Image, clusterObjects: Vector<ClusterObject>, clusters: Vector<Cluster>, k: Int): Int {
        clusters.clear()

        for (j in 0..k - 1) {

            clusters.add(Cluster())
            clusters[j].id = j + 1

        }

        val h = Histogram(labeledByObject)
        val n = h.maxValue
        clusterObjects.clear()

        for (j in 0..n - 1) {
            clusterObjects.add(ClusterObject())
            clusterObjects[j].setCentroidComponents(0.0, 0.0, 0.0)
            clusterObjects[j].setnPixels(0)
        }

        for (i in labeledByObject) {
            if (labeledByObject.getValue(i) > 0) {
                val value = labeledByObject.getValue(i).toInt()
                clusterObjects[value - 1].incrementnPixels()
                clusterObjects[value - 1].centroid = clusterObjects[value - 1].centroid.add(Vector3D(i.get(ImageCoordinate.X).toDouble(), i.get(ImageCoordinate.Y).toDouble(), i.get(ImageCoordinate.Z).toDouble()))
            }
        }

        (0..n - 1)
                .map { clusterObjects[it] }
                .forEach { it.centroid = it.centroid.scalarMultiply(1.0 / it.getnPixels()) }

        for (i in labeledByObject) {
            val clusterValue = labeledByCluster.getValue(i).toInt()
            val objectValue = labeledByObject.getValue(i).toInt()
            if (clusterValue == 0 || objectValue == 0) {
                continue
            }

            clusters[clusterValue - 1].objectSet.add(clusterObjects[objectValue - 1])
        }

        for (c in clusters) {
            var objectCounter = 0
            c.setCentroidComponents(0.0, 0.0, 0.0)

            for (co in c.objectSet) {
                objectCounter++
                co.currentCluster = c
                c.centroid = c.centroid.add(co.centroid)
            }

            c.centroid = c.centroid.scalarMultiply(1.0 / objectCounter)
        }
        return n
    }

    /**
     * Sets up a set of ClusterObjects and a set of Clusters from an Image mask with each object labeled with a unique greylevel.
     * @param im                The Image mask with each cluster object labeled with a unique greylevel.  These must start at 1 and be consecutive.
     * @param clusterObjects    A Vector of ClusterObjects that will contain the initialized ClusterObjects on return; this may be empty, and any contents will be erased.
     * @param clusters          A Vector of Clusters that will contain the initialized Clusters on return; this may be empty, and any contents will be erased.
     * @param k                 The number of Clusters to generate.
     * @return                  The number of ClusterObjects in the Image.
     */
    fun initializeObjectsAndClustersFromImage(im: Image, clusterObjects: Vector<ClusterObject>, clusters: Vector<Cluster>, k: Int): Int {
        var n = 0
        clusters.clear()

        for (j in 0..k - 1) {
            clusters.add(Cluster())
            clusters[j].id = j + 1
        }

        val h = Histogram(im)
        n = h.maxValue
        clusterObjects.clear()

        for (j in 0..n - 1) {
            clusterObjects.add(ClusterObject())
            clusterObjects[j].setCentroidComponents(0.0, 0.0, 0.0)
            clusterObjects[j].setnPixels(0)
        }

        for (i in im) {
            if (im.getValue(i) > 0) {
                val current = clusterObjects[im.getValue(i).toInt() - 1]
                current.incrementnPixels()
                current.centroid = current.centroid.add(Vector3D(i[ImageCoordinate.X].toDouble(), i[ImageCoordinate.Y].toDouble(), i[ImageCoordinate.Z].toDouble()))
            }
        }

        (0..n - 1)
                .map { clusterObjects[it] }
                .forEach { it.centroid = it.centroid.scalarMultiply(1.0 / it.getnPixels()) }

        //initialize clusters using kmeans++ strategy
        val probs = DoubleArray(n, { 0.0 })
        val cumulativeProbs = DoubleArray(n, { 0.0 })

        //choose the initial cluster
        val initialClusterObject = Math.floor(n * RandomGenerator.rand()).toInt()
        clusters[0].centroid = clusterObjects[initialClusterObject].centroid
        clusters[0].objectSet.add(clusterObjects[initialClusterObject])

        for (j in 0..n - 1) {
            clusterObjects[j].currentCluster = clusters[0]
        }

        //assign the remainder of the clusters
        for (j in 1..k - 1) {
            var probSum = 0.0
            for (m in 0..n - 1) {
                var minDist = java.lang.Double.MAX_VALUE
                var bestCluster: Cluster? = null
                for (p in 0..j - 1) {
                    val tempDist = clusterObjects[m].distanceTo(clusters[p])
                    if (tempDist < minDist) {
                        minDist = tempDist
                        bestCluster = clusters[p]
                    }
                }
                probs[m] = minDist
                probSum += minDist
                clusterObjects[m].currentCluster = bestCluster
            }

            for (m in 0..n - 1) {
                probs[m] = probs[m] / probSum
                if (m == 0) {
                    cumulativeProbs[m] = probs[m]
                } else {
                    cumulativeProbs[m] = cumulativeProbs[m - 1] + probs[m]
                }
            }

            val randNum = RandomGenerator.rand()
            var nextCenter = 0

            for (m in 0..n - 1) {
                if (randNum < cumulativeProbs[m]) {
                    nextCenter = m
                    break
                }
            }

            clusters[j].centroid = clusterObjects[nextCenter].centroid
        }

        for (m in 0..n - 1) {
            var minDist = java.lang.Double.MAX_VALUE
            var bestCluster: Cluster? = null

            for (p in 0..k - 1) {
                val tempDist = clusterObjects[m].distanceTo(clusters[p])
                if (tempDist < minDist) {
                    minDist = tempDist
                    bestCluster = clusters[p]
                }
            }

            clusterObjects[m].currentCluster = bestCluster
            bestCluster!!.objectSet.add(clusterObjects[m])
        }
        return n
    }

    /**
     * Calculates a metric of clustering goodness, which is based upon the ratio of the average distance between objects withing a cluster to the maximal distance between objects in two different clusters.

     * This is used to determine whether it is actually better to subdivide a cluster into multiple clusters or not; in general having more clusters will
     * produce a higher likelihood score from the Gaussian mixture model clustering, so some independent metric is needed to guess at what
     * the best number of clusters is.  Using this metric, clusters are only subdivided if the division can substantially reduce the ratio of
     * the distances between objects withing a cluster relative to distances of object between clusters.

     * @param clusterObjects    A Vector containing the ClusterObjects currently being clustered.
     * *
     * @param clusters          A Vector containing the current cluster assignments on which the metric is to be calculated.
     * *
     * @param k                 The number of clusters.
     * *
     * @param n                 The number of cluster objects.
     * *
     * @return                  The result of applying the metric.  A lower score means the clustering is better.
     */
    fun getInterClusterDistances(clusterObjects: Vector<ClusterObject>, clusters: Vector<Cluster>, k: Int, n: Int): Double {
        val intraClusterDists = DoubleArray(k, { 0.0 })
        val intraCounts = IntArray(k, { 0 })
        val maxIntra = DoubleArray(k, { 0.0 })

        for (i in 0..n - 1) {
            val i_ID = clusterObjects[i].currentCluster?.id
            for (j in i + 1..n - 1) {
                val j_ID = clusterObjects[j].currentCluster?.id

                if (i_ID == j_ID && i_ID != null) {
                    val dist = clusterObjects[i].distanceTo(clusterObjects[j])
                    intraClusterDists[i_ID - 1] += dist
                    intraCounts[i_ID - 1]++

                    if (dist > maxIntra[i_ID - 1]) {
                        maxIntra[i_ID - 1] = dist
                    }
                }
            }
        }

        var ratios = 0.0
        var ratio_counts = 0

        for (i in 0..k - 1) {
            if (clusters[i].objectSet.size == 0) {
                continue
            }

            for (j in i + 1..k - 1) {
                if (clusters[j].objectSet.size == 0) {
                    continue
                }

                var maxDist = 0.0
                for (i_obj in clusters[i].objectSet) {
                    clusters[j].objectSet
                            .asSequence()
                            .map { i_obj.distanceTo(it) }
                            .filter { it > maxDist }
                            .forEach { maxDist = it }
                }

                val zeroCountScalingFactor = 4.0

                if (intraCounts[i] == 0) {
                    intraCounts[i] = 1
                    intraClusterDists[i] = maxDist / zeroCountScalingFactor
                }

                if (intraCounts[j] == 0) {
                    intraCounts[j] = 1
                    intraClusterDists[j] = maxDist / zeroCountScalingFactor
                }

                ratios += 2 * (intraClusterDists[i] / intraCounts[i] + intraClusterDists[j] / intraCounts[j]) / maxDist
                ratio_counts++
            }
        }

        if (ratio_counts == 0) {
            val maxElement = maxIntra.max() ?: 0.0

            ratios = 4 * intraClusterDists[0] / (maxElement + 1e-9)
            ratio_counts = 1
        }

        return ratios / ratio_counts
    }


    /**
     * Relabels an Image mask that is labeled with an individual greylevel for each cluster object to have all objects in a single cluster labeled with the same value.


     * @param output            The Image mask labeled with unique greylevels for each cluster object; this should have regions labeled according to the same scheme used to generate the clusters and objects.  It will be overwritten.
     * *
     * @param clusterObjects    A Vector containing the ClusterObjects corresponding to the labeled objects in the Image mask.
     * *
     * @param clusters          A Vector containing the Clusters comprised of the ClusterObjects that will determine the labels in the output Image.
     * *
     * @param k                 The number of Clusters.
     */
    fun clustersToMask(output: WritableImage, clusterObjects: Vector<ClusterObject>, clusters: Vector<Cluster>, k: Int) {
        for (i in output) {
            val value = output.getValue(i).toInt()
            if (value > 0) {
                output.setValue(i, clusterObjects[value - 1].currentCluster?.id?.toFloat() ?: Float.NaN)
            }
        }
    }

    /**
     * Performs a long-range gaussian filtering on an Image mask labeled by ClusterObject.

     * This is useful to isolate general areas of an Image that contain objects, and is often an excellent approximation or starting point for the clustering.

     * @param input     The Image to be filtered (this will be left unchanged).
     * *
     * @return          The filtered Image.
     */
    fun gaussianFilterMask(input: Image): WritableImage {
        val origCopy = ImageFactory.createWritable(input)
        val gf = GaussianFilter()
        val MAX_VALUE = 4095

        origCopy
                .asSequence()
                .filter { origCopy.getValue(it) > 0 }
                .forEach { origCopy.setValue(it, MAX_VALUE.toFloat()) }

        gf.setWidth(origCopy.dimensionSizes[ImageCoordinate.X] / 5)
        gf.apply(origCopy)
        return origCopy
    }

    /**
     * Applies basic clustering to an Image with objects.

     * This will use the long-range gaussian filtering approach to assign clusters; objects sufficiently near to each other will be smeared into a single object and assigned to the same cluster.

     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * *
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * *
     * @param gaussianFiltered  The mask with a long range Gaussian filter applied (as from [.gaussianFilterMask]).  This is an optional parameter;
     * *                          input null to have this automatically generated by the method.  This parameter is
     * *                          chiefly useful to save computation time when running the clutering multiple times.
     * *                          This will be modified, so if planning to reuse the Gaussian filtered image, pass in a copy.
     */
    fun doBasicClustering(input: WritableImage, original: Image, gaussianFiltered: Image?): Image {
        val rlf = RelabelFilter()
        val lf = LabelFilter()
        val mf = MaskFilter()
        mf.referenceImage = input
        val h_individualCentromeres = Histogram(input)
        val origCopy: WritableImage = gaussianFiltered?.writableInstance ?: gaussianFilterMask(input).writableInstance

        lf.apply(origCopy)

        val mapped = ImageFactory.createWritable(origCopy)
        val h_mapped_0 = Histogram(origCopy)

        //first, find the centroid of each cluster
        val centroids_x = ArrayRealVector(h_mapped_0.maxValue + 1)
        val centroids_y = ArrayRealVector(h_mapped_0.maxValue + 1)
        val counts = ArrayRealVector(h_mapped_0.maxValue + 1)
        centroids_x.mapMultiplyToSelf(0.0)
        centroids_y.mapMultiplyToSelf(0.0)
        counts.mapMultiplyToSelf(0.0)

        for (i in origCopy) {
            if (origCopy.getValue(i) > 0) {
                val value = origCopy.getValue(i).toInt()
                centroids_x.setEntry(value, centroids_x.getEntry(value) + i[ImageCoordinate.X])
                centroids_y.setEntry(value, centroids_y.getEntry(value) + i[ImageCoordinate.Y])
                counts.setEntry(value, counts.getEntry(value) + 1)
            }
        }
        for (i in 0..counts.dimension - 1) {
            if (counts.getEntry(i) == 0.0) {
                counts.setEntry(i, 1.0)
                centroids_x.setEntry(i, (-1 * origCopy.dimensionSizes[ImageCoordinate.X]).toDouble())
                centroids_y.setEntry(i, (-1 * origCopy.dimensionSizes[ImageCoordinate.Y]).toDouble())
            }
            centroids_x.setEntry(i, centroids_x.getEntry(i) / counts.getEntry(i))
            centroids_y.setEntry(i, centroids_y.getEntry(i) / counts.getEntry(i))
        }

        for (i in origCopy) {
            if (mapped.getValue(i) > 0 || input.getValue(i) == 0f) continue
            var minDistance = java.lang.Double.MAX_VALUE
            var minIndex = 0

            for (j in 0..centroids_x.dimension - 1) {
                val dist = Math.hypot(centroids_x.getEntry(j) - i[ImageCoordinate.X], centroids_y.getEntry(j) - i[ImageCoordinate.Y])
                if (dist < minDistance) {
                    minDistance = dist
                    minIndex = j
                }
            }
            mapped.setValue(i, minIndex.toFloat())
        }

        val centromereAssignments = IntArray(h_individualCentromeres.maxValue + 1, { 0 })

        for (i in mapped) {
            if (input.getValue(i) > 0) {
                val value = input.getValue(i).toInt()
                if (centromereAssignments[value] > 0) {
                    mapped.setValue(i, centromereAssignments[value].toFloat())
                } else {
                    centromereAssignments[value] = mapped.getValue(i).toInt()
                }
            }
        }
        mf.apply(mapped)
        origCopy.copy(mapped)
        mf.referenceImage = origCopy
        mf.apply(input)
        rlf.apply(input)
        rlf.apply(origCopy)
        return origCopy
    }

    /**
     * Applies complex clustering to an Image with objects.
     * This will first use the basic clustering to initially assign clusters and then attempt to subdivide those clusters using Gaussian mixture model clustering on each initial cluster.
     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * @param maxClusters       A rough upper bound to the number of clusters expected in the Image.  More clusters than this may be found, but if a clustering iteration finds more clusters than this as the best solution, it will terminate the clustering.
     * @param gaussianFiltered  The mask with a long range Gaussian filter applied (as from [.gaussianFilterMask]).  This is an optional parameter;
     *                          input null to have this automatically generated by the method.  This parameter is
     *                          chiefly useful to save computation time when running the clutering multiple times.
     *                          This will be modified, so if planning to reuse the Gaussian filtered image, pass in a copy.
     */
    fun doComplexClustering(input: WritableImage, original: Image, maxClusters: Int, gaussianFiltered: Image) {
        val output = doBasicClustering(input, original, gaussianFiltered)
        doClusteringWithInitializedClusters(input, original, maxClusters, output)
    }

    /**
     * Applies the complex clustering to an Image with objects that have already been grouped into initial guesses of clusters.
     * This will use the cluster guesses as a starting point and attempt to subdivide these clusters using Gaussian mixture model clustering on each cluster individually.
     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * @param maxClusters       A rough upper bound to the number of clusters expected in the Image.  More clusters than this may be found, but if a clustering iteration finds more clusters than this as the best solution, it will terminate the clustering.
     * @param clusterImage      A version of the Image mask relabeled such that each object in the Image is assigned a greylevel value corresponding to its cluster.  Each cluster should have a unique value, these should start at 1, and they should be consecutive.
     */
    fun doClusteringWithInitializedClusters(input: WritableImage, original: Image, maxClusters: Int, clusterImage: Image) {
        val interdist_cutoff = 0.89
        val clusterObjects = Vector<ClusterObject>()
        val clusters = Vector<Cluster>()
        var bestRatio = 1.0
        var bestK = 0
        val origCopy = ImageFactory.createWritable(clusterImage)
        var bestImage: Image = ImageFactory.create(origCopy)
        var overallMaxL = 1.0 * java.lang.Double.MAX_VALUE
        var repeatThis = 0
        val numRepeats = 3
        val rlf = RelabelFilter()
        val h_ssf = Histogram(origCopy)
        val k_init = h_ssf.maxValue
        var numAttempts = 0
        var lastBestK = 0
        var k = 1

        while (k <= maxClusters) {
            numAttempts++
            val orig_k = k
            var interdist: Double
            var currMaxL = -1.0 * java.lang.Double.MAX_VALUE
            var n = 0
            var L = -1.0 * java.lang.Double.MAX_VALUE

            if (numAttempts == 1) {
                k = k_init
                bestImage = ImageFactory.create(origCopy)
                bestK = k_init
            }

            var candidateNewBestImage: Image = ImageFactory.createShallow(bestImage)
            val h = Histogram(bestImage)
            var currentMaxImageValue = h.maxValue
            var sumL = 0.0
            val singleCluster = ImageFactory.createWritable(input)
            val dividedClusterTemp = ImageFactory.createWritable(singleCluster)

            for (clusterNumber in 1..h.maxValue) {
                singleCluster.copy(input)
                dividedClusterTemp.copy(singleCluster)
                val clusterMin = ImageCoordinate.cloneCoord(singleCluster.dimensionSizes)
                val clusterMax = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
                for (i in singleCluster) {
                    if (bestImage.getValue(i) != clusterNumber.toFloat()) {
                        singleCluster.setValue(i, 0f)
                    } else {
                        //find the min and max bounds of this cluster
                        for (dim in i) {
                            if (i[dim] < clusterMin[dim]) {
                                clusterMin[dim] = i[dim]
                            }
                            if (i[dim] >= clusterMax[dim]) {
                                clusterMax[dim] = i[dim] + 1
                            }
                        }
                    }
                }
                singleCluster.setBoxOfInterest(clusterMin, clusterMax)
                rlf.apply(singleCluster)
                val hSingleCluster = Histogram(singleCluster)
                val nSingleCluster = hSingleCluster.maxValue
                var accepted = false
                var tempBestRatio = java.lang.Double.MAX_VALUE
                var tempBestL = 0.0
                val origCurrentMaxImageValue = currentMaxImageValue
                val tempCandidateNewBestImage = ImageFactory.createWritable(candidateNewBestImage)
                var kMax = if (bestK < 3) 6 else 4

                if (kMax > nSingleCluster) {
                    kMax = nSingleCluster
                }

                for (tempK in 2..kMax - 1) {
                    for (repeatCounter in 0..numRepeats - 1) {
                        var accept = false
                        var tempCurrentMaxImageValue = origCurrentMaxImageValue
                        n = initializeObjectsAndClustersFromImage(singleCluster, clusterObjects, clusters, tempK)
                        L = DEGaussianMixtureModelClustering.go(singleCluster, clusterObjects, clusters, tempK, n)
                        interdist = getInterClusterDistances(clusterObjects, clusters, tempK, n)

                        if (interdist < interdist_cutoff && interdist < tempBestRatio) {
                            accept = true
                            accepted = true
                            tempBestRatio = interdist
                            tempBestL = L
                        }

                        if (accept) {
                            dividedClusterTemp.copy(singleCluster)
                            dividedClusterTemp.setBoxOfInterest(clusterMin, clusterMax)
                            clustersToMask(dividedClusterTemp, clusterObjects, clusters, tempK)

                            val newClusterValue = tempCurrentMaxImageValue
                            tempCandidateNewBestImage.copy(candidateNewBestImage)

                            singleCluster
                                    .asSequence()
                                    .filter { dividedClusterTemp.getValue(it) > 1 }
                                    .forEach { tempCandidateNewBestImage.setValue(it, newClusterValue + dividedClusterTemp.getValue(it) - 1) }

                            tempCurrentMaxImageValue = newClusterValue + tempK - 1
                            currentMaxImageValue = tempCurrentMaxImageValue
                        }

                        clusterObjects.clear()
                        clusters.clear()
                    }
                }

                if (accepted) {
                    sumL += tempBestL
                    candidateNewBestImage = tempCandidateNewBestImage
                } else {
                    if (nSingleCluster > 0) {
                        n = initializeObjectsAndClustersFromImage(singleCluster, clusterObjects, clusters, 1)
                        sumL += DEGaussianMixtureModelClustering.go(singleCluster, clusterObjects, clusters, 1, n)
                    }

                    clusterObjects.clear()
                    clusters.clear()
                }

                dividedClusterTemp.clearBoxOfInterest()
                singleCluster.clearBoxOfInterest()
                clusterMin.recycle()
                clusterMax.recycle()
            }

            k = currentMaxImageValue
            n = initializeObjectsAndClustersFromClusterImage(input, candidateNewBestImage, clusterObjects, clusters, k)
            L = sumL
            val tempL = -1.0 * L
            interdist = getInterClusterDistances(clusterObjects, clusters, clusters.size, clusterObjects.size)

            if (interdist == -1.0) {
                interdist = 1.0
            }
            val ratio = interdist

            if (numAttempts == 1) {
                overallMaxL = tempL
                bestRatio = java.lang.Double.MAX_VALUE
            }

            if (tempL >= overallMaxL && ratio < bestRatio) {
                bestRatio = ratio
                lastBestK = bestK
                bestK = k
                repeatThis = 0
                val newBestImage = ImageFactory.createWritable(input)
                clustersToMask(newBestImage, clusterObjects, clusters, bestK)
                rlf.apply(newBestImage)
                bestImage = newBestImage
                overallMaxL = tempL
            }

            if (tempL > currMaxL) {
                currMaxL = tempL
            }
            clusters.clear()
            clusterObjects.clear()

            if (++repeatThis < numRepeats) {
                k = orig_k
            } else {
                repeatThis = 0
            }

            if (orig_k > k) {
                k = orig_k
            }

            if (k > maxClusters) break
            if (repeatThis == 0 && bestK == lastBestK) break
            if (numAttempts >= maxClusters) break
            k++
        }
        input.copy(bestImage)
    }
}
