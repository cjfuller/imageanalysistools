package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.method.Method
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.clustering.ObjectClustering
import edu.stanford.cfuller.imageanalysistools.metric.Measurement
import edu.stanford.cfuller.imageanalysistools.metric.Metric
import edu.stanford.cfuller.imageanalysistools.metric.Quantification
import edu.stanford.cfuller.imageanalysistools.metric.ZeroMetric
import java.util.*

/**
 * Method to find centromeres in immunofluorescence images.
 *
 * This method operates in two stages: first, a normalization stage, and second, a segmentation stage.
 *
 * In the normalization stage, the image is locally background corrected
 * so that varying brightness of image objects has a reduced effect upon the size of the object in the segmentation, and
 * so that a relatively simple intensity thresholding method can be used to segment objects of different brightness.
 *
 * In the segmentation stage, the objects are segmented based on normalized brightness, and size filtering is
 * applied according to user-specified sizes.
 *
 * Optionally, centromere objects can be clustered into possible cells, and then these cells can be used as the basis
 * for a further (non-normalized) thresholding step in which any dim objects in each cell are removed (as are commonly
 * found for antibodies with high punctate background).
 *
 * As per the specification in the [Method] class, the first image in the stored images will be used as the reference
 * image for segmentation and the remaining channels quantified.

 * @author Colin J. Fuller
 */
open class CentromereFindingMethod : Method() {
    internal val BKG_NUM_PT_PARAM = "bkg_num_points"
    protected var metric: Metric = edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric()

    protected fun centromereFinding(input: Image): Image {
        val filters = java.util.Vector<Filter>()
        with(filters) {
            add(LocalMaximumSeparabilityThresholdingFilter())
            add(LabelFilter())
            add(RecursiveMaximumSeparabilityFilter())
            add(RelabelFilter())
            add(SizeAbsoluteFilter())
            add(RelabelFilter())
        }

        for (i in filters) {
            i.setParameters(this.parameters)
            i.referenceImage = this.images[0]
        }

        val toProcess = ImageFactory.createWritable(input)
        iterateOnFiltersAndStoreResult(filters, toProcess, metric)
        return this.storedImage!!
    }

    protected open fun normalizeInputImage(input: WritableImage) {
        val rnf = RenormalizationFilter()
        rnf.setParameters(this.parameters)
        rnf.apply(input)
    }

    protected fun doBandpassFilter(input: WritableImage) {
        val bf = BandpassFilter()
        bf.setParameters(this.parameters)
        val band_lower = 3.0f
        val band_upper = 4.0f
        bf.setBand(band_lower.toDouble(), band_upper.toDouble())
        bf.apply(input)
    }

    protected fun doPostFiltering(input: WritableImage, reference: Image) {
        val stf = SimpleThresholdingFilter()
        val saf = SizeAbsoluteFilter()
        val lf = LabelFilter()
        stf.setParameters(this.parameters)
        saf.setParameters(this.parameters)
        this.clearImageOutput()
        stf.referenceImage = reference
        saf.apply(input)
        stf.apply(input)
        lf.apply(input)
    }

    protected fun clusterByDNA(groupMask: WritableImage, allCentromeres: Image) {
        var groupMask = groupMask
        var dnaChannel = 0
        if (this.parameters.hasKey("DNA_channel")) {
            dnaChannel = this.parameters.getIntValueForKey("DNA_channel")
        }
        val dnaImage = this.imageSet.getImageForIndex(dnaChannel)

        val mstf = MaximumSeparabilityThresholdingFilter()
        val lf = LabelFilter()
        val mf = MaskFilter()
        lf.setParameters(this.parameters)
        mstf.setParameters(this.parameters)
        groupMask = ImageFactory.createWritable(dnaImage!!)
        mstf.apply(groupMask)
        lf.apply(groupMask)
        mf.referenceImage = allCentromeres
        mf.apply(groupMask)
    }

    protected fun clusterByCentromeres(groupMask: WritableImage, allCentromeres: WritableImage, reference: Image) {
        var gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask)
        val MF = MaskFilter()
        val LF = LabelFilter()
        val RLF = RelabelFilter()
        val h = Histogram(groupMask)
        var decreaseBackground = java.lang.Boolean.parseBoolean(this.parameters.getValueForKey("decrease_speckle_background"))
        decreaseBackground = decreaseBackground || this.parameters.hasKey("maximum_number_of_centromeres") && this.parameters.getIntValueForKey("maximum_number_of_centromeres") < h.maxValue

        if (!decreaseBackground) {
            if (this.parameters.hasKeyAndTrue("use_basic_clustering")) {
                groupMask.copy(ObjectClustering.doBasicClustering(groupMask, reference, gaussianFilteredMask))
            } else {
                ObjectClustering.doComplexClustering(groupMask, reference, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask)
            }
        } else {
            val output = ImageFactory.createWritable(ObjectClustering.doBasicClustering(groupMask, reference, ImageFactory.create(gaussianFilteredMask)))
            val rtf = RegionThresholdingFilter()
            val mstf_clustering = MaximumSeparabilityThresholdingFilter()
            rtf.setThresholdingFilter(mstf_clustering)
            rtf.setParameters(this.parameters)
            val ch0_copy = ImageFactory.createWritable(this.images[0])
            val h_clustered = Histogram(output)
            val singleClusterTemp = ImageFactory.createWritable(output)

            for (c in 1..h_clustered.maxValue) {
                for (i in singleClusterTemp) {
                    if (output.getValue(i) == c.toFloat()) {
                        singleClusterTemp.setValue(i, c.toFloat())
                        ch0_copy.setValue(i, this.images[0].getValue(i))
                    } else {
                        singleClusterTemp.setValue(i, 0f)
                        ch0_copy.setValue(i, 0f)
                    }
                }
                rtf.referenceImage = ch0_copy
                LF.apply(singleClusterTemp)
                rtf.apply(singleClusterTemp)

                singleClusterTemp
                        .asSequence()
                        .filter { output.getValue(it) == c.toFloat() && singleClusterTemp.getValue(it) == 0f }
                        .forEach { output.setValue(it, 0f) }
            }
            LF.apply(output)
            MF.referenceImage = output
            MF.apply(groupMask)
            gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask)

            if (this.parameters.hasKey("use_basic_clustering") && this.parameters.getBooleanValueForKey("use_basic_clustering")) {
                groupMask.copy(ObjectClustering.doBasicClustering(groupMask, reference, gaussianFilteredMask))
            } else {
                ObjectClustering.doComplexClustering(groupMask, reference, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask)
            }
        }
        MF.referenceImage = groupMask
        MF.apply(allCentromeres)
        RLF.apply(allCentromeres)

        if (parameters.hasKey("minimum_cluster_size") && Integer.parseInt(parameters.getValueForKey("minimum_cluster_size")) > 0) {
            val h_clustered = Histogram(groupMask)
            val clusterContents = java.util.Vector<java.util.HashSet<Int>>()
            for (i in 0..h_clustered.maxValue + 1 - 1) {
                clusterContents.add(java.util.HashSet<Int>())
            }
            groupMask
                    .asSequence()
                    .filter { groupMask.getValue(it) > 0 && allCentromeres.getValue(it) > 0 }
                    .forEach { clusterContents[groupMask.getValue(it).toInt()].add(allCentromeres.getValue(it).toInt()) }

            groupMask
                    .asSequence()
                    .filter { groupMask.getValue(it) > 0 && clusterContents[groupMask.getValue(it).toInt()].size < Integer.parseInt(this.parameters.getValueForKey("minimum_cluster_size")) }
                    .forEach { groupMask.setValue(it, 0f) }

            RLF.apply(groupMask)
        }

        MF.referenceImage = groupMask
        MF.apply(allCentromeres)
        RLF.apply(allCentromeres)
    }

    protected fun doClustering(groupMask: WritableImage, allCentromeres: WritableImage, reference: WritableImage) {
        if (this.parameters.hasKeyAndTrue("cluster_by_DNA")) {
            this.clusterByDNA(groupMask, allCentromeres)
        } else {
            this.clusterByCentromeres(groupMask, allCentromeres, reference)
        }
    }

    protected fun calculateRegionCentroids(mask: Image): HashMap<Float, FloatArray> {
        val centroids = HashMap<Float, FloatArray>()
        val h = Histogram(mask)
        for (ic in mask) {
            val value = mask.getValue(ic)
            if (value <= 0.0) {
                continue
            }

            if (!centroids.containsKey(value)) {
                val zeros = FloatArray(2)
                zeros[0] = 0.0f
                zeros[1] = 0.0f
                centroids.put(value, zeros)
            }
            val currCen = centroids[value]
            currCen!![0] += (ic[ImageCoordinate.X] / h.getCounts(value.toInt())).toFloat()
            currCen!![1] += (ic[ImageCoordinate.Y] / h.getCounts(value.toInt())).toFloat()
        }
        return centroids
    }


    protected fun doCentromereBasedBackgroundSubtraction(backgroundMask: WritableImage, groupMask: WritableImage) {
        var n_points = 3
        if (this.parameters.hasKey(BKG_NUM_PT_PARAM)) {
            n_points = this.parameters.getIntValueForKey(BKG_NUM_PT_PARAM)
        }

        val centroids = this.calculateRegionCentroids(backgroundMask)
        var maxValue = 0.0f
        var minValue = java.lang.Float.MAX_VALUE
        val DESIRED_RANGE = 4095.0f

        for (ic in backgroundMask) {
            var value = 0.0f

            val pq = PriorityQueue(n_points + 1,
                    java.util.Comparator<Float> { o1, o2 ->
                        if (o1 < o2) return@Comparator 1
                        if (o1 == o2) return@Comparator 0
                        -1
                    }
            )

            for (k in centroids.keys) {
                val v = centroids[k]!!
                val dist = Math.hypot((v[0] - ic[ImageCoordinate.X]).toDouble(), (v[1] - ic[ImageCoordinate.Y]).toDouble()).toFloat()

                if (pq.peek() == null || dist < pq.peek() || pq.size < n_points) {
                    pq.add(dist)
                }

                if (pq.size > n_points) {
                    pq.poll()
                }
            }

            val firstN = pq.toTypedArray()
            val sum = firstN.sum()
            value = n_points / sum
            if (value < minValue) { minValue = value }
            if (value > maxValue) { maxValue = value }
            backgroundMask.setValue(ic, value)
        }

        for (ic in backgroundMask) {
            val newValue = (backgroundMask.getValue(ic) - minValue) / (maxValue - minValue) * DESIRED_RANGE
            backgroundMask.setValue(ic, newValue)
        }
        val mstf = MaximumSeparabilityThresholdingFilter()
        mstf.apply(backgroundMask)
        backgroundMask
                .asSequence()
                .filter { backgroundMask.getValue(it) > 0 }
                .forEach { backgroundMask.setValue(it, 1.0f) }
    }

    protected fun doBackgroundSubtraction(groupMask: WritableImage, allCentromeres: Image): WritableImage {
        val backgroundMask = ImageFactory.createWritable(groupMask)
        if (this.parameters.hasKeyAndTrue("use_clustering")) {
            val chblf = ConvexHullByLabelFilter()
            chblf.referenceImage = allCentromeres
            chblf.apply(backgroundMask)
        } else {
            doCentromereBasedBackgroundSubtraction(backgroundMask, groupMask)
        }
        backgroundMask
                .asSequence()
                .filter { allCentromeres.getValue(it) > 0 }
                .forEach { backgroundMask.setValue(it, 0f) }

        return backgroundMask
    }

    protected fun generateOutput(groupMask: Image, allCentromeres: Image, backgroundMask: Image) {
        val fullResult = metric.quantify(allCentromeres, this.imageSet)
        var backgroundResult: Quantification? = metric.quantify(backgroundMask, this.imageSet)
        if (backgroundResult == null) { // either not using clustering or the quantification failed due to no ROIs
            backgroundResult = ZeroMetric().quantify(backgroundMask, this.imageSet)
        }
        this.parameters.setValueForKey("background_calculated", "true")
        val resultMap = IntArray(Histogram.findMaxVal(allCentromeres))
        for (i in allCentromeres) {
            val value = allCentromeres.getValue(i).toInt()
            if (value > 0) {
                resultMap[value - 1] = groupMask.getValue(i).toInt()
            }
        }

        for (i in resultMap.indices) {
            fullResult.addMeasurement(Measurement(
                    hasFeature = true,
                    id = (i + 1).toLong(),
                    measurement = resultMap[i].toDouble(),
                    name = "cell_id",
                    type = Measurement.TYPE_GROUPING,
                    image = this.imageSet.markerImageName!!))
            var regionToLookup = resultMap[i]
            if (backgroundResult.allRegions.size == 1) { // only a single background value applied per image
                regionToLookup = 1
            }

            backgroundResult
                    .getAllMeasurementsForRegion(regionToLookup.toLong())
                    .asSequence()
                    .filter { it.measurementType === Measurement.TYPE_INTENSITY }
                    .forEach { fullResult.addMeasurement(Measurement(
                            hasFeature = true,
                            id = (i + 1).toLong(),
                            measurement = it.measurement,
                            name = it.measurementName!!,
                            type = Measurement.TYPE_BACKGROUND,
                            image = this.imageSet.markerImageName!!))
                    }
        }
        this.storedDataOutput = fullResult
    }

    /**
     * Runs the centromere finding method using the stored images and parameters.
     */
    override fun go() {
        this.parameters.setValueForKey("DEBUG", "false")
        val SELF = SizeAbsoluteFilter()
        val ImThF = SimpleThresholdingFilter()
        val LF = LabelFilter()
        val RLF = RelabelFilter()
        val MF = MaskFilter()
        SELF.setParameters(this.parameters)
        ImThF.setParameters(this.parameters)
        LF.setParameters(this.parameters)
        RLF.setParameters(this.parameters)
        val normalized = ImageFactory.createWritable(this.images[0])

        this.doBandpassFilter(normalized)
        this.normalizeInputImage(normalized)
        val groupMask = ImageFactory.createWritable(centromereFinding(normalized))
        this.doPostFiltering(groupMask, normalized)
        RLF.apply(groupMask)
        val allCentromeres = ImageFactory.createWritable(groupMask)
        val allCentromeresCopy = ImageFactory.create(allCentromeres)
        this.storeImageOutput(allCentromeresCopy)

        if (this.parameters.hasKeyAndTrue("use_clustering")) {
            this.doClustering(groupMask, allCentromeres, normalized)
            RLF.apply(groupMask)
            this.storeImageOutput(groupMask)
        }

        val backgroundMask = this.doBackgroundSubtraction(groupMask, allCentromeres)
        this.storeImageOutput(backgroundMask)
        this.generateOutput(groupMask, allCentromeres, backgroundMask)
    }
}
