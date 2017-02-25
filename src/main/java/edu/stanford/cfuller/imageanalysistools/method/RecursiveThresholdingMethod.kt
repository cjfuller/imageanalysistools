/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.method

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparabilityFilter
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter

/**
 * Implements the recursive Otsu thresholding method described in Xiong et al. (DOI: 10.1109/ICIP.2006.312365).
 *
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
            f.setReferenceImage(this.images[0])
        }

        val toProcess = ImageFactory.createWritable(this.images[0])

        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())

    }

}
