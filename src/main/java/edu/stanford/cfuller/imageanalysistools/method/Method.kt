package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.metric.Metric
import edu.stanford.cfuller.imageanalysistools.metric.Quantification
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.frontend.StatusUpdater

/**
 * An analysis method that can be run in an independent thread of execution.

 * @author Colin J. Fuller
 */
abstract class Method : Runnable {
    var parameters: ParameterDictionary = ParameterDictionary.emptyDictionary()
    protected var metadata: AnalysisMetadata? = null
    protected var storedImages: MutableList<Image> = mutableListOf()
    protected var images: MutableList<Image> = mutableListOf()
    protected var imageSet: ImageSet = ImageSet(this.parameters)
    var storedDataOutput: Quantification? = null
        protected set
    protected var updater: StatusUpdater? = null
    var displayName: String = "Method"

    /**
     * Runs through a sequence of Filters and applies them successively to an input image; quantifies images according to the
     * supplied metric and the mask that results from application of the filters.
     * @param filters       The List of Filters to apply to the image, in order.
     * @param toProcess     The Image that will be processed by the Filters; may be overwritten during the filtering process.
     * @param m             The Metric that will be used to quantify the Images.
     */
    protected fun iterateOnFiltersAndStoreResult(filters: List<Filter>, toProcess: WritableImage, m: Metric?) {
        var c = 0

        if (java.lang.Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
            LoggingUtilities.logger.info("starting filters")
            val name = Integer.toString(c++)

            if (ij.IJ.getInstance() == null) {
                ij.ImageJ()
            }
            val ip = ImageFactory.create(toProcess).toImagePlus()
            ip.title = name
            ip.show()
        }
        for (f in filters) {
            f.apply(toProcess)
            if (java.lang.Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
                LoggingUtilities.logger.info("completed filter #" + c)
                val name = Integer.toString(c)
                if (this.updater == null) c++

                val ip = ImageFactory.create(toProcess).toImagePlus()
                ip.title = name
                ip.show()
            }

            if (this.updater != null) {
                updater!!.update(++c, filters.size, "")
            }
        }
        if (m != null) {
            this.storeDataOutput(m.quantify(toProcess, this.imageSet))
        }
        this.storeImageOutput(ImageFactory.create(toProcess))
        this.parameters.addIfNotSet("background_calculated", "false")
    }

    /**
     * Stores an Image to an internal list of Images that can later be written to disk.
     * @param im    The Image to add to the list of output Images.
     */
    protected fun storeImageOutput(im: Image) {
        this.storedImages.add(im)
    }

    /**
     * Stores a Quantification to be used as the data for ouptut.
     * @param q The quantification object that will be used as output.
     */
    protected fun storeDataOutput(q: Quantification) {
        this.storedDataOutput = q
    }

    /**
     * Clears all Images that have been stored for output.
     */
    protected fun clearImageOutput() {
        this.storedImages.clear()
    }

    /**
     * Runs the analysis method.
     */
    abstract fun go()

    /**
     * Gets the first in the list of stored output Images.
     * Will not remove the image from the list, so successive calls
     * can return the same Image.
     * @return  The first Image in the list of stored output Images, or null if no Images have been stored.
     */
    val storedImage: Image?
        get() = if (this.storedImages.size > 0) this.storedImages[0] else null

    /**
     * Sets the Images to be quantified/processed for this method.
     * If there is one Image that is to receive special treatment (for example, one color channel to be segmented, and this
     * used to quantify all the channels), then that Image should be specified in the ImageSet using its setMarkerImage method.
     * This method will not overwrite any Images already passed using prior calls this method, but rather append the supplied
     * Images to the list of those already passed in.  However, if a marker image has already been passed in in a previous ImageSet, it
     * will not be changed by calling this method.
     * @param images    The List of images to be processed/quantified.
     */
    fun setImages(images: ImageSet) {
        val marker = images.markerIndex

        if (marker != null) {
            this.images.add(images.markerImage!!)

            images.indices
                    .filter { it != marker }
                    .forEach { this.images.add(images.getImageForIndex(it)!!) }
        } else {
            this.images.addAll(images)
        }
        this.imageSet = images
    }

    /**
     * Gets the stored AnalysisMetadata used for this method.
     * This will get a reference, not a copy, so modifications will be reflected in the stored AnalysisMetadata.
     * @return      A reference to the AnalysisMetadata used for this method.
     */
    /**
     * Sets the analysis metadata for this method to those found in a supplied [ParameterDictionary].

     * This retains a reference to the AnalysisMetadata and does not copy it, so external changes to the AnalysisMetadata
     * will be reflected in the stored one as well.

     * Also sets the parameters and images associated with this method to be those contained in the AnalysisMetadata object.

     * @param am    The AnalysisMetadata containing the metadata for running this method.
     */
    var analysisMetadata: AnalysisMetadata
        get() = this.metadata!!
        set(am) {
            this.metadata = am
            this.parameters = am.outputParameters!!
            this.setImages(am.inputImages)
        }

    fun setStatusUpdater(up: StatusUpdater) {
        this.updater = up
    }

    /**
     * Method called when the thread is started.
     */
    override fun run() {
        this.go()
    }

    companion object {
        internal val defaultMethodPackageName = "edu.stanford.cfuller.imageanalysistools.method"
        /**
         * Retrieves the method to run from its name.
         * @param methodName    The name of the method to run, either fully qualified or relative to the default method package.
         * @return                A Method object of the type specified in the parameter dictionary.
         */
        fun loadMethod(methodName: String): Method {
            var methodName = methodName
            var method: Method? = null
            //methodName might be fully qualified, in which case we want to use
            //that; otherwise, we should fall back on a default value
            if (!methodName.contains(".")) {
                methodName = defaultMethodPackageName + "." + methodName
            }

            try {
                method = Class.forName(methodName).newInstance() as Method
            } catch (e: ClassNotFoundException) {
                LoggingUtilities.logger.severe("Could not find method: " + methodName)
                e.printStackTrace()
            } catch (e: InstantiationException) {
                LoggingUtilities.logger.severe("Could not instantiate method: " + methodName)
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                LoggingUtilities.logger.severe("Could not access method constructor for: " + methodName)
                e.printStackTrace()
            }
            return method!!
        }
    }
}
