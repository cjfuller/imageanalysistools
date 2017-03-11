package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image

/**
 * A filter that wraps a [DimensionFlipper] and swaps two of the dimensions in an image.
 *
 *
 * The two dimensions to be swapped can be specified using the [.setDimensionsToSwap] method,
 * or by setting the parameters first_dimension_to_swap and second_dimension_to_swap as integer parameters.  In either case,
 * the dimensions to be swapped should be specified according to the dimension constants in [edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate].  Specifying
 * the dimensions in the parameter file will override the setting using [.setDimensionsToSwap].
 *
 *
 * The argument to the apply method should be the Image whose dimensions will be swapped.
 * (In contrast to the DimensionFlipper, this Image will be modified in place.)
 *
 *
 * This filter does not use a reference Image.

 * @author Colin J. Fuller
 */
class DimensionSwappingFilter : Filter() {
    internal var dim0: Int = 0
    internal var dim1: Int = 0
    internal var dimsManuallySet: Boolean = false

    init {
        this.params = null
        this.dimsManuallySet = false
        this.dim0 = 0
        this.dim1 = 0
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        if (!this.dimsManuallySet && this.params != null && this.params!!.hasKey(dim0_param) && this.params!!.hasKey(dim1_param)) {
            this.dim0 = this.params!!.getIntValueForKey(dim0_param)
            this.dim1 = this.params!!.getIntValueForKey(dim1_param)
        }

        val out = DimensionFlipper.flip(im, this.dim0, this.dim1)
        im.resize(out.dimensionSizes)
        im.copy(out)
    }

    /**
     * Sets the two dimensions to swap in the image.  These should correspond to the
     * dimension constants defined in [edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate] (or, rarely, to a user-defined dimension).
     *
     *
     * These will override any dimensions specified in the parameter file.

     * @param dim0    The first dimension to swap.
     * *
     * @param dim1    The second dimension to swap.
     */
    fun setDimensionsToSwap(dim0: Int, dim1: Int) {
        this.dim0 = dim0
        this.dim1 = dim1
        this.dimsManuallySet = true
    }

    companion object {
        internal val dim0_param = "first_dimension_to_swap"
        internal val dim1_param = "second_dimension_to_swap"
    }
}
