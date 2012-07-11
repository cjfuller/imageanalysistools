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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A filter that averages an Image over all points in the time dimension.
 * <p>
 * The reference Image for this filter should be set to the time series Image to be averaged.
 * <p>
 * The argument to the apply method should be an Image of all zeros that will be overwritten with the same dimension sizes as the time series,
 * except for only containing a single time point.
 *
 * @author Colin J. Fuller
 *
 */
public class TimeAveragingFilter extends Filter{

    /**
     * Time-averages the reference Image, overwriting the argument to this method with the result of the averaging.
     * @param output    An Image containing all zeros that will be overwritten that has the same dimension sizes as the reference Image, but a singleton time dimension.
     */
    public void apply(WritableImage output) {

        Image im = this.referenceImage;

        int size_t = im.getDimensionSizes().get(ImageCoordinate.T);

        for (ImageCoordinate ic : im) {
            
            ImageCoordinate ic_t = ImageCoordinate.cloneCoord(ic);

            ic_t.set(ImageCoordinate.T,0);

            output.setValue(ic_t, output.getValue(ic_t) + im.getValue(ic)/size_t);

            ic_t.recycle();
        }


    }



}
