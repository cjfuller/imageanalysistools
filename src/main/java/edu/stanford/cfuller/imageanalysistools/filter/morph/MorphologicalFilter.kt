package edu.stanford.cfuller.imageanalysistools.filter.morph

import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * A Filter representing a binary morphological filter in an arbitrary (i.e. up to 5) number of dimensions.
 *
 *
 * A MorphologicalFilter processes an [Image][edu.stanford.cfuller.imageanalysistools.image.Image] using a [StructuringElement] for the morphological operation.
 *
 *
 * Eventually MorphologicalFilters will implement processing greyscale images, but only binary filters are currently implemented.

 */
abstract class MorphologicalFilter() : Filter() {
    var strel: StructuringElement? = null
    var processAsBinary: Boolean = true

    /**
     * Constructs a new MorphologicalFilter, copying the structuring element and settings from another
     * MorphologicalFilter.
     * @param copySettings        The MorphologicalFilter whose settings will be copied.
     */
    constructor(copySettings: MorphologicalFilter) : this() {
        this.strel = copySettings.strel
        this.processAsBinary = copySettings.processAsBinary
    }

    /* (non-Javadoc)
     * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
     */
    abstract override fun apply(im: WritableImage)
}
