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

import edu.stanford.cfuller.imageanalysistools.filter.*
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage

/**
 * Implements the a version of the watershed method that is seeded off of supplied regions.

 * The eventual watershed division of the Image will have as many regions as seed regions.

 * @author Colin J. Fuller
 */
class SeededWatershedMethod : Method() {


    override fun go() {

        val filters = java.util.Vector<Filter>()

        val gf = GaussianFilter()

        if (this.parameters.hasKey("min_size")) {

            gf.setWidth(Math.sqrt(this.parameters.getIntValueForKey("min_size").toDouble()).toInt())

        } else {
            gf.setWidth(5)
        }

        filters.add(gf)

        filters.add(GradientFilter())

        val swf = SeededWatershedFilter()

        val rtf = CentromereFindingMethod()

        parameters.addIfNotSet("use_clustering", "false")

        rtf.setImages(this.imageSet)
        rtf.parameters = this.parameters
        rtf.go()

        swf.setSeed(rtf.storedImage!!)

        filters.add(swf)
        filters.add(MergeFilter())

        filters.add(RelabelFilter())

        filters.add(SimpleThresholdingFilter(0.1))

        filters.add(RelabelFilter())
        filters.add(FillFilter())

        for (f in filters) {
            f.setReferenceImage(this.images[0])
            f.setParameters(this.parameters)
        }

        val toProcess = ImageFactory.createWritable(this.images[0])

        iterateOnFiltersAndStoreResult(filters, toProcess, edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric())

    }
}
