package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Image
/**
 * Describes filters that operate using a seed Image.
 *
 * Conceptually this should be distinct from a reference image in that the seed Image should be a mask identifying regions using some other means
 * that will guide the SeededFilter.
 *
 * @author Colin J. Fuller
 */
interface SeededFilter {
    /**
     * Sets the seed Image to a given Image.  This Image should not be modified by the Filter.
     * @param im    The seed Image.
     */
    fun setSeed(im: Image)
}
