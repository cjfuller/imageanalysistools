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


import edu.stanford.cfuller.imageanalysistools.filter.BandpassFilter
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.Label3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.LocalMaximumSeparabilityThresholdingFilter
import edu.stanford.cfuller.imageanalysistools.filter.PlaneNormalizationFilter
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparability3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.Renormalization3DFilter
import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter
import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.metric.Metric

/**
 * A method analogous to the [CentromereFindingMethod], but intended to be applied to 3D images.
 *
 *
 * Identifies volumes, rather than planar regions, and outputs a 3D mask rather than a single plane mask.

 * @author Colin J. Fuller
 */
class CentromereFinding3DMethod : Method() {

    internal var metric: Metric

    /**
     * Sole constructor, which creates a default instance.
     */
    init {
        this.metric = edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric()
    }


    /**

     * Runs the centromere finding method using the stored images and parameters.

     */
    override fun go() {

        var referenceChannel = 0

        this.parameters.addIfNotSet("marker_channel_index", Integer.toString(referenceChannel))

        referenceChannel = this.parameters.getIntValueForKey("marker_channel_index")

        this.centromereFinding(this.images[0])


    }

    protected fun centromereFinding(input: Image): Image {
        var input = input

        val filters = java.util.Vector<Filter>()

        val LBE3F = Renormalization3DFilter()

        val band_lower = 4.0f
        val band_upper = 5.0f

        val bf = BandpassFilter()

        bf.setBand(band_lower.toDouble(), band_upper.toDouble())
        bf.setShouldRescale(true)

        if (this.parameters.hasKeyAndTrue("swap_z_t")) {
            input = DimensionFlipper.flipZT(input)
        }

        filters.add(bf)

        filters.add(LBE3F)
        //filters.add(LBE3F);


        filters.add(LocalMaximumSeparabilityThresholdingFilter())
        filters.add(Label3DFilter())
        filters.add(RecursiveMaximumSeparability3DFilter())
        filters.add(RelabelFilter())
        filters.add(SizeAbsoluteFilter())
        filters.add(RelabelFilter())

        for (i in filters) {
            i.setParameters(this.parameters)
            i.setReferenceImage(this.images[0])
        }

        val toProcess = ImageFactory.createWritable(input)

        iterateOnFiltersAndStoreResult(filters, toProcess, metric)


        return this.storedImage
    }

}
