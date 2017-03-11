package edu.stanford.cfuller.imageanalysistools.frontend

import ij.ImagePlus
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.method.Method
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * Interface to run analysis routines from an ImageJ plugin.

 * @author Colin J. Fuller
 */
class IJAnalysis {
    internal var toProcess: ImagePlus? = null

    /**
     * Sets the ImagePlus to analyze.
     * @param input        The ImagePlus to be analyzed.
     */
    fun setImagePlus(input: ImagePlus) {
        this.toProcess = input
    }

    /**
     * Runs the analysis, using the specified parameters and the stored ImagePlus.
     * @param params    A ParameterDictionary containing the information for the method to run and any parameters needed by that method.
     * *
     * @return            An ImagePlus containing the output image from the method being run.
     */
    fun run(params: ParameterDictionary): ImagePlus? {
        val m = Method.loadMethod(params.getValueForKey("method_name")!!)
        m.parameters = params
        val im_nonsplit = ImageFactory.create(this.toProcess!!)
        val split = im_nonsplit.splitChannels()
        params.addIfNotSet("number_of_channels", Integer.toString(split.size))
        val imSet = ImageSet(params)

        for (i in split) {
            imSet.addImageWithImage(i)
        }

        if (params.hasKey("marker_channel_index")) {
            val markerIndex = params.getIntValueForKey("marker_channel_index")
            imSet.setMarkerImage(markerIndex)
        }

        m.setImages(imSet)
        val su = ImageJStatusUpdater()
        su.update(-1, 1, "Processing...")
        m.setStatusUpdater(su)
        m.run()

        val result = m.storedImage
        if (result != null) {
            return result.toImagePlus()
        } else {
            return null
        }
    }
}
