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
import edu.stanford.cfuller.imageanalysistools.image.Image;


/**
 * Method to find spots in noisy images.
 * <p>
 * The core of this method is the same as the {@link CentromereFindingMethod}, and differs only in that it does more
 * extensive normalization of the input image before segmenting it.  The normalization in this method attempts to reduce
 * noise using a combination of gaussian filtering, bandpass filtering, laplace filtering, and use of a {@link RenormalizationFilter}.
 *
 * @author Colin J. Fuller
 *
 */


public class NoisySpotFindingMethod extends CentromereFindingMethod {


    /**
     * Sole constructor, which creates a default instance.
     */
	public NoisySpotFindingMethod() {
		this.metric = new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric();
	}


    @Override
    protected void normalizeInputImage(Image input) {
        
        RenormalizationFilter rnf = new RenormalizationFilter();
        rnf.setParameters(this.parameters);

        GaussianFilter GF = new GaussianFilter();
        GF.setWidth(3);

        BandpassFilter BF = new BandpassFilter();
        BF.setBand(0.3, 0.7);

        LaplacianFilter LapF = new LaplacianFilter();
        

        GF.apply(input);
        rnf.apply(input);
        GF.apply(input);
        LapF.apply(input);
        BF.apply(input);
        rnf.apply(input);

    }

}
