package edu.stanford.cfuller.imageanalysistools.filter.morph

import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * A Filter implementing the operation of binary morphological closing in an arbitrary (i.e. up to 5) number of dimensions.
 *
 *
 * This filter does not take a reference image.
 *
 *
 * The argument to the apply method should be the image to be processed.  This should have foreground pixels labeled > 0 and
 * background pixels labeled <= 0.  After processing, all foreground pixels will be set to 1 and all background pixels to 0.

 * @author Colin J. Fuller
 */
class ClosingFilter : MorphologicalFilter() {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.morph.MorphologicalFilter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        val df = DilationFilter(this)
        val ef = ErosionFilter(this)
        df.apply(im)
        ef.apply(im)
    }
}
