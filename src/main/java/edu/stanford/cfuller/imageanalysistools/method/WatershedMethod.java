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

import edu.stanford.cfuller.imageanalysistools.filter.*;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;

/**
 * Implements the commonly used watershed method on the gradient of an Image.
 * 
 * @author Colin J. Fuller
 */
public class WatershedMethod extends Method {


    public void go() {

        LoggingUtilities.logDebugMessages();

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

        GaussianFilter gf = new GaussianFilter();

        if (this.parameters.hasKey("min_size")) {

            gf.setWidth((int) Math.sqrt(this.parameters.getIntValueForKey("min_size")));

        } else {
            gf.setWidth(5);
        }

        filters.add(gf);

        filters.add(new GradientFilter());

        filters.add(new WatershedFilter());

        filters.add(new RelabelFilter());

        filters.add(new SimpleThresholdingFilter(0.1));

        filters.add(new RelabelFilter());
        filters.add(new FillFilter());

        for (Filter f : filters) {
            f.setReferenceImage(this.images.get(0));
            f.setParameters(this.parameters);
        }

        WritableImage toProcess = ImageFactory.createWritable(this.images.get(0));

        iterateOnFiltersAndStoreResult(filters, toProcess, new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric());

    }

}
