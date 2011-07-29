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

package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.TimeAveragingFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A method that time averages images, and stores the time-averaged result in place of the usual mask output.
 * 
 * @author Colin J. Fuller
 */
public class TimeAveragingMethod extends Method {
 
	//TODO: handle time averaging (or arbitrary dimension averaging?) for images other than 5D.
	
    public void go() {

        TimeAveragingFilter taf = new TimeAveragingFilter();

        taf.setParameters(this.parameters);

        //first create the reference Image

        ImageCoordinate dimSizes = ImageCoordinate.cloneCoord(this.images.get(0).getDimensionSizes());

        dimSizes.set(ImageCoordinate.C,this.images.size());

        Image reference = new Image(dimSizes, 0.0);

        for (ImageCoordinate ic : reference) {
            ImageCoordinate ic_c = ImageCoordinate.cloneCoord(ic);
            ic_c.set(ImageCoordinate.C,0);
            reference.setValue(ic, this.images.get(ic.get(ImageCoordinate.C)).getValue(ic_c));
            ic_c.recycle();
        }

        taf.setReferenceImage(reference);

        //now create the output image

        dimSizes.set(ImageCoordinate.T,1);

        Image timeAveraged = new Image(dimSizes, 0.0);

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

        filters.add(taf);

        iterateOnFiltersAndStoreResult(filters, timeAveraged, null);


        dimSizes.recycle();


    }


}
