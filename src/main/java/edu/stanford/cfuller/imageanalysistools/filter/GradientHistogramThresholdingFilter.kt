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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.Histogram


/**
 * A thresholding method based upon finding the full-width half-max of the histogram of the gradient of the Image.

 * @author Colin J. Fuller
 */
class GradientHistogramThresholdingFilter : Filter() {

    override fun apply(im: WritableImage) {

        val GF = GaussianFilter()
        GF.setWidth(21)
        GF.apply(im)

        val gradient = ImageFactory.createWritable(im)

        val GRF = GradientFilter()
        GRF.apply(gradient)

        val smoothedThresholded = ImageFactory.createWritable(im)

        this.absoluteThreshold(im, 10)

        val MF = MaskFilter()

        MF.setReferenceImage(smoothedThresholded)

        MF.apply(gradient)
        MF.setReferenceImage(gradient)
        MF.apply(im)

        this.histogramThreshold(im, 45.0)

    }

    private fun absoluteThreshold(im: WritableImage, level: Int) {
        for (c in im) {
            if (im.getValue(c) < level) {
                im.setValue(c, 0f)

            }
        }
    }

    private fun histogramThreshold(im: WritableImage, stddev: Double) {

        val im_hist = Histogram(im)

        val mode = im_hist.mode
        val modeVal = im_hist.countsAtMode

        val halfModeVal = modeVal / 2.0

        var hw_first = 0

        for (i in 1..im_hist.maxValue - 1) {
            if (im_hist.getCounts(i) > halfModeVal) {
                hw_first = i
                break

            }
        }

        val hwhm = mode - hw_first

        val threshLevel = (mode + stddev * hwhm).toInt()

        this.absoluteThreshold(im, threshLevel + 1)

    }


}
