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

package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory

import java.util.Arrays


/**
 * A Filter that thresholds an Image based on a single value for each region in a mask.
 *
 *
 * Takes a mask and and Image, calculates a mean value over all pixels in each region, and then uses a thresholding filter to threshold the Image masked
 * with the mask.  Any region whose mean value is below the threshold found by the thresholding filter is removed.
 *
 *
 * The reference Image should be set to an Image that will be used to compute the mean value of each region.  This Image will not be changed.
 *
 *
 * The argument to the apply method should be set to the mask whose regions will be averaged over and removed if below the threshold.

 * @author Colin J. Fuller
 */
class RegionThresholdingFilter : Filter() {

    private var thresholdingFilter: Filter? = null

    fun setThresholdingFilter(f: Filter) {
        this.thresholdingFilter = f
    }

    /**
     * Applies the Filter to an Image mask
     * @param im    The Image mask to process; its regions will be retained or removed based upon each region's average value
     * * compared to a threshold.
     */
    override fun apply(im: WritableImage) {

        val LF = LabelFilter()

        LF.apply(im)

        val h = Histogram(im)

        val n = h.maxValue

        val means = DoubleArray(n + 1)

        Arrays.fill(means, 0.0)

        for (i in im) {

            means[im.getValue(i).toInt()] += this.referenceImage.getValue(i).toDouble()

        }

        for (k in 0..n + 1 - 1) {
            if (h.getCounts(k) > 0) {
                means[k] /= h.getCounts(k).toDouble()
            } else {
                means[k] = 0.0
            }
        }

        val refCopy = ImageFactory.createWritable(this.referenceImage)

        val mf = MaskFilter()

        mf.setReferenceImage(im)
        mf.apply(refCopy)

        this.thresholdingFilter!!.setReferenceImage(refCopy)
        this.thresholdingFilter!!.apply(refCopy)

        val h_thresh = Histogram(refCopy)

        val threshold = h_thresh.minValueNonzero

        for (i in im) {
            if (im.getValue(i) == 0f) continue

            val mean = means[im.getValue(i).toInt()]

            if (mean < threshold) {
                im.setValue(i, 0f)
            }

        }


    }

}
