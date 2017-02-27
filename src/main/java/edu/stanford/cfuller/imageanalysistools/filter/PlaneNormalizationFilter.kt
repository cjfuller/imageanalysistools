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
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * Normalizes an image such that each z-plane is divided by its mean value over all other dimensions.
 * The entire image is then rescaled to its original min/max values.
 *
 *
 * This filter does not use a reference image.

 * @author Colin J. Fuller
 */
class PlaneNormalizationFilter : Filter() {

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
    override fun apply(im: WritableImage) {

        var origMin = java.lang.Float.MAX_VALUE
        var origMax = -1.0f * java.lang.Float.MAX_VALUE

        for (ic in im) {
            if (im.getValue(ic) < origMin) {
                origMin = im.getValue(ic)
            }
            if (im.getValue(ic) > origMax) {
                origMax = im.getValue(ic)
            }
        }

        val sums = DoubleArray(im.dimensionSizes[ImageCoordinate.Z])
        val counts = IntArray(sums.size)

        java.util.Arrays.fill(sums, 0.0)
        java.util.Arrays.fill(counts, 0)

        for (ic in im) {

            val z = ic.get(ImageCoordinate.Z)

            sums[z] += im.getValue(ic).toDouble()

        }

        for (ic in im) {

            val z = ic.get(ImageCoordinate.Z)

            sums[z] += im.getValue(ic).toDouble()
            counts[z]++

        }

        for (i in sums.indices) {
            sums[i] /= counts[i].toDouble()
        }

        for (ic in im) {

            val z = ic.get(ImageCoordinate.Z)

            im.setValue(ic, (im.getValue(ic) / sums[z]).toFloat())

        }

        var newMin = java.lang.Float.MAX_VALUE
        var newMax = -1.0f * java.lang.Float.MAX_VALUE

        for (ic in im) {
            if (im.getValue(ic) < newMin) {
                newMin = im.getValue(ic)
            }
            if (im.getValue(ic) > newMax) {
                newMax = im.getValue(ic)
            }
        }

        for (ic in im) {
            im.setValue(ic, (im.getValue(ic) - newMin) / (newMax - newMin) * (origMax - origMin) + origMin)
        }

    }

}
