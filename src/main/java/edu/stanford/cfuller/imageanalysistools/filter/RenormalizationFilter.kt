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
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimationFilter
import edu.stanford.cfuller.imageanalysistools.filter.GradientFilter
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter
import edu.stanford.cfuller.imageanalysistools.image.Histogram


/**
 * A Filter that normalizes an Image by local intensity so that features that occur in a dim region of an image will be
 * approximately the same brightness as features in a bright part of an Image.
 *
 *
 * This may be useful, for instance, if multiple cells in an Image both have fluorescence signal that needs to be segmented
 * but are expressing a protein being localized to vastly different levels, which might otherwise cause the lower-expressing cell
 * to be removed from the Image by the segmentation routine.  This is also useful for ensuring that objects of differing brightness
 * are not segmented to be radically different sizes by a segmentation method.
 *
 *
 * The general approach uses median filtering to estimate the local intensity in an Image, and then uses a gradient filter
 * to remove artificial edges due to the size of the median filter.
 *
 *
 * This filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image that is to be intensity normalized.  This Image will be overwritten
 * by the normalized version.

 * @author Colin J. Fuller
 */
class RenormalizationFilter : Filter() {

    /**
     * Applies the filter to an Image, normalizing its intensity.
     * @param im    The Image to be normalized; will be overwritten.
     */
    override fun apply(im: WritableImage) {

        this.referenceImage = ImageFactory.create(im)

        val input = this.referenceImage
        val output = im

        val LBEF = LocalBackgroundEstimationFilter()

        LBEF.setParameters(this.params)
        LBEF.setReferenceImage(input)
        LBEF.setBoxSize(Math.ceil(0.5 * Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size")).toDouble())).toInt())
        LBEF.apply(output)

        val gradient = ImageFactory.createWritable(output)

        val GF = GradientFilter()

        GF.apply(gradient)
        GF.apply(gradient)

        val GAUF = GaussianFilter()

        GAUF.setWidth((2 * Math.ceil(Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size")).toDouble()))).toInt())

        GAUF.apply(gradient)

        val tempStorage = ImageFactory.createWritable(input.dimensionSizes, 0.0f)

        var maxValue = 0.0f
        var minValue = java.lang.Float.MAX_VALUE

        val h = Histogram(input)

        for (i in output) {

            var denom = output.getValue(i) + gradient.getValue(i)
            denom = if (denom < 1) 1 else denom

            if (input.getValue(i) / denom > 0) {
                tempStorage.setValue(i, Math.log((input.getValue(i) / denom).toDouble()).toFloat())
            }

            if (tempStorage.getValue(i) > maxValue) maxValue = tempStorage.getValue(i)
            if (tempStorage.getValue(i) < minValue) minValue = tempStorage.getValue(i)

        }

        var sumValue = 0f
        var pixelCount: Long = 0

        for (i in output) {

            var tempValue = tempStorage.getValue(i)

            tempValue = (tempValue - minValue) / (maxValue - minValue) * h.maxValue

            if (maxValue == minValue || tempValue < 0) tempValue = 0f

            sumValue += tempValue

            pixelCount++

            output.setValue(i, Math.floor(tempValue.toDouble()).toFloat())

        }

        sumValue /= pixelCount.toFloat()

        for (i in output) {
            var tempValue = output.getValue(i) - sumValue
            if (tempValue < 0) tempValue = 0f
            output.setValue(i, tempValue)
        }

    }

}
