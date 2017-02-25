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
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A filter that performs prefiltering on a 3D image to aid in segmentation by normalizing for local variations in image intensity.
 *
 *
 * The argument to the apply method should be the image to be filtered.
 *
 *
 * This filter does not use a reference Image.


 * @author Colin J. Fuller
 */
class Renormalization3DFilter : Filter() {

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {

        val pnf = PlaneNormalizationFilter()

        pnf.apply(im)

        val mean = ImageFactory.createWritable(im)


        val VSMF = VariableSizeMeanFilter()
        VSMF.setBoxSize(5) // was 5

        val kf = KernelFilterND()
        val d = doubleArrayOf(0.1, 0.2, 0.4, 0.2, 0.1)
        kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d)

        val gf = GaussianFilter()

        gf.setWidth(5)

        val lfnd = LaplacianFilterND()
        val zpf = ZeroPointFilter()

        VSMF.apply(mean)

        gf.apply(mean)
        kf.apply(mean)


        kf.setParameters(this.params)
        lfnd.setParameters(this.params)
        zpf.setParameters(this.params)

        val lf = ImageFactory.createWritable(mean)

        lfnd.apply(lf)
        zpf.apply(lf)
        gf.apply(lf)
        kf.apply(lf)

        var min = java.lang.Float.MAX_VALUE
        var max = 0f

        val h = Histogram(im)

        for (ic in im) {
            val value = im.getValue(ic) / (1f + lf.getValue(ic) + mean.getValue(ic))
            if (value < min) {
                min = value
            }
            if (value > max) {
                max = value
            }
            im.setValue(ic, value)
        }

        for (ic in im) {
            im.setValue(ic, (im.getValue(ic) - min) / (max - min) * h.maxValue)
        }

        kf.apply(im)


    }

}
