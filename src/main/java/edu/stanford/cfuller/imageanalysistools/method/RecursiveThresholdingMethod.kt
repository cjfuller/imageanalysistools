package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparabilityFilter
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter

/**
 * Implements the recursive Otsu thresholding method described in Xiong et al. (DOI: 10.1109/ICIP.2006.312365).
 *
 * The quantification for each resulting regions uses an [edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric].
 * @author Colin J. Fuller
 */


class RecursiveThresholdingMethod : Method() {
    /**
     * Runs the method on the stored images and parameters.
     * As per the specification in the [Method] class, this applies the segmentation method
     * to the first in the set of images, and quantifies the remainder of them.
     */
    override fun go() {
        val filters = java.util.Vector<Filter>()
        filters.add(MaximumSeparabilityThresholdingFilter())
        filters.add(LabelFilter())
        filters.add(RecursiveMaximumSeparabilityFilter())
        filters.add(RelabelFilter())
        for (f in filters) {
            f.setParameters(this.parameters)
            f.referenceImage = this.images[0]
        }

        val toProcess = ImageFactory.createWritable(this.images[0])
        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())
    }
}
