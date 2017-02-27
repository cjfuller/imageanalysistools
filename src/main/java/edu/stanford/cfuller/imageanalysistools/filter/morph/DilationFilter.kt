package edu.stanford.cfuller.imageanalysistools.filter.morph

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter implementing the operation of binary morphological dilation in an arbitrary (i.e. up to 5) number of dimensions.
 *
 *
 * This filter does not take a reference image.
 *
 *
 * The argument to the apply method should be the image to be dilated.  This should have foreground pixels labeled > 0 and
 * background pixels labeled <= 0.  After dilation, all foreground pixels will be set to 1 and all background pixels to 0.

 * @author Colin J. Fuller
 */
class DilationFilter() : MorphologicalFilter() {
    /**
     * Constructs a new DilationFilter, copying the structuring element and settings from another
     * MorphologicalFilter.
     * @param mf        The MorphologicalFilter whose settings will be copied.
     */
    constructor(mf: MorphologicalFilter) : super(copySettings = mf)

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.morph.MorphologicalFilter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {
        val strel = this.strel ?: return
        val origCopy = ImageFactory.create(im)
        for (ic in im) {
            strel.boxImageToElement(ic, origCopy)
            val included = origCopy.any { strel[ic, it] > 0 && origCopy.getValue(it) > 0 }
            if (included) {
                im.setValue(ic, 1.0f)
            } else {
                im.setValue(ic, 0.0f)
            }
        }
    }

    companion object {
        private val defaultSize = 3
        /**
         * Creates a default structuring element for processing with this filter.
         * Currently this is an n by n by...n square structuring element set to all ones in the specified dimensions, where
         * n is a default size, currently 3.
         * @param dimList    A list of dimensions (corresponding to their integer indices in ImageCoordinate) over which the structuring element extends.
         * *
         * @return            A StructuringElement suitable for processing an image over the supplied dimensions.
         */
        fun getDefaultElement(dimList: IntArray): StructuringElement {
            val strelSize = ImageCoordinate.createCoordXYZCT(1, 1, 1, 1, 1)
            for (i in dimList) {
                strelSize[i] = defaultSize
            }
            val toReturn = StructuringElement(strelSize)
            toReturn.setAll(1.0f)
            strelSize.recycle()
            return toReturn
        }
    }
}
