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

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;


/**
 * A Filter that removes regions from a mask that are larger or smaller than specified size cutoffs.
 * <p>
 * The size cutoffs are retrieved from the ParameterDictionary used for the analysis, in the parameters "maxSize" and "minSize", in units of number
 * of pixels. If these are not specified some default values will be used.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method is the mask that will have regions of size outside of the specified bounds removed.
 *
 * @author Colin J. Fuller
 *
 */
public class SizeAbsoluteFilter extends Filter {


    /**
     * Applies the SizeAbsoluteFilter, removing regions sized outside the specified range.
     * @param im    The mask whose regions of unusual size will be removed.
     */
	@Override
	public void apply(Image im) {


        Histogram h = new Histogram(im);


        int absCutoffMin = -1;
        int absCutoffMax = -1;

        if (this.params != null) {

            absCutoffMax = this.params.getIntValueForKey("max_size");
            absCutoffMin = this.params.getIntValueForKey("min_size");

        }

        if (absCutoffMin < 0) { // default values
            absCutoffMax = 50;
            absCutoffMin = 5;
        }

        int[] multipliers = new int[h.getMaxValue() + 1];

        for (int i = 0; i < multipliers.length; i++) {

            if (h.getCounts(i) < absCutoffMin || h.getCounts(i) > absCutoffMax) {
                multipliers[i] = 0;
            } else {
                multipliers[i] = 1;
            }
            
        }

        for (ImageCoordinate ic : im) {

            double value = im.getValue(ic);

            im.setValue(ic, value*multipliers[(int) value]);
            
            
        }

        

	}

}
