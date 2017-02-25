/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2013 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

import edu.stanford.cfuller.imageanalysistools.filter.morph.ErosionFilter

/**
 * A metric that takes each region in a mask and calculates its area and perimeter.

 * For regions with holes, the perimeter will be the total boundary size both to the
 * outside of the region and to the holes.

 * Area and perimeter are both calculated in units of pixels (# of pixels in the
 * region and # of pixels on the border, respectively)

 * @author Colin J. Fuller
 */


class AreaAndPerimeterMetric : Metric() {


    /**
     * Quantifies the area and perimeter for each region in a 2D image.

     * If no regions of interest are present in the supplied mask, this will return null.

     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1, as might be produced by a [edu.stanford.cfuller.imageanalysistools.filter.LabelFilter].
     * *
     * @param images    An ImageSet of Images to be quantified; this will be ignored except for using the name of the marker image.
     * *
     * @return          A Quantification containing measurements for area and perimeter.
     */
    override fun quantify(mask: Image, images: ImageSet): Quantification? {

        val h = Histogram(mask)

        if (h.maxValue == 0) return null

        val q = Quantification()

        for (i in 0..h.maxValue - 1) {
            val m = Measurement(true, (i + 1).toLong(), h.getCounts(i + 1).toDouble(), "area", Measurement.TYPE_SIZE, images.markerImageName)
            q.addMeasurement(m)
        }

        val eroded = ImageFactory.createWritable(mask)

        val dims = intArrayOf(ImageCoordinate.X, ImageCoordinate.Y)
        val ef = ErosionFilter()
        ef.setStructuringElement(ErosionFilter.getDefaultElement(dims))

        ef.apply(eroded)

        for (ic in mask) {
            if (mask.getValue(ic) > 0 && eroded.getValue(ic) == 0f) {
                eroded.setValue(ic, mask.getValue(ic))
            } else {
                eroded.setValue(ic, 0f)
            }
        }

        val hPerim = Histogram(eroded)

        for (i in 0..hPerim.maxValue - 1) {
            val m = Measurement(true, (i + 1).toLong(), hPerim.getCounts(i + 1).toDouble(), "perimeter", Measurement.TYPE_SIZE, images.markerImageName)
            q.addMeasurement(m)
        }

        return q

    }

}


