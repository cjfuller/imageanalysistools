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

import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.TimeAveragingFilter
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A method that time averages images, and stores the time-averaged result in place of the usual mask output.

 * @author Colin J. Fuller
 */
class TimeAveragingMethod : Method() {

    //TODO: handle time averaging (or arbitrary dimension averaging?) for images other than 5D.

    override fun go() {

        val taf = TimeAveragingFilter()

        taf.setParameters(this.parameters)

        //first create the reference Image

        val dimSizes = ImageCoordinate.cloneCoord(this.images[0].dimensionSizes)

        dimSizes[ImageCoordinate.C] = this.images.size

        val reference = ImageFactory.createWritable(dimSizes, 0.0f)

        for (ic in reference) {
            val ic_c = ImageCoordinate.cloneCoord(ic)
            ic_c[ImageCoordinate.C] = 0
            reference.setValue(ic, this.imageSet.getImageForIndex(ic[ImageCoordinate.C])!!.getValue(ic_c))
            ic_c.recycle()
        }

        taf.setReferenceImage(reference)

        //now create the output image

        dimSizes[ImageCoordinate.T] = 1

        val timeAveraged = ImageFactory.createWritable(dimSizes, 0.0f)

        val filters = java.util.Vector<Filter>()

        filters.add(taf)

        iterateOnFiltersAndStoreResult(filters, timeAveraged, null)


        dimSizes.recycle()


    }


}
