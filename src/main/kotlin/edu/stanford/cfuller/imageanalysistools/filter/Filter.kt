package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image

/**
 * This class represents an Image operation.
 *
 *
 * Classes that extend filter implement the apply method, which takes an Image and modifies it in place according to the
 * operation that that particular filter represents.  Filters may also use a second Image, called the reference image, for additional information
 * during the processing, but should not modify the reference image.  For instance, a filter might take a mask for the Image parameter
 * to the apply function, and operate on this mask, but use the intensity information in a reference image to guide the operation on the mask.
 *
 *
 * Classes that extend filter should document exactly what they expect for the reference image and the Image parameter to the apply function.

 * @author Colin J. Fuller
 */

abstract class Filter {
    var referenceImage: Image? = null
    var params: ParameterDictionary? = null

    /**
     * Applies the Filter to the supplied Image.
     * @param im    The Image to process.
     */
    abstract fun apply(im: WritableImage)

    /**
     * Sets the parameters for this filter to the specified ParameterDictionary.
     * @param params    The ParameterDictionary used for the analysis.
     */
    fun setParameters(params: ParameterDictionary) {
        this.params = params
    }
}
