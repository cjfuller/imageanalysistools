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
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that thresholds an Image at a fractional level between the minimum and the maximum value of the Image.
 * <p>
 * The reference Image should be set to the Image to be thresholded.  This Image will not be modified.
 * <p>
 * The argument to the apply method should be set to an Image that will get the output of the thresholding.  This should be the
 * same dimensions as the reference Image.  This Image will be unchanged except that any pixel below the threshold in the reference Image
 * will be set to zero in this Image.
 * 
 * @author Colin J. Fuller
 */

public class SimpleThresholdingFilter extends Filter {

    private double fractionalLevel;

    /**
     * Constructs a SimpleThresholdingFilter that defaults to not thresholding the Image.
     */
    public SimpleThresholdingFilter() {

        this.fractionalLevel = 0;
    }

    /**
     * Constructs a SimpleThresholdingFilter that will threshold an Image at some fractional level of the difference between the minimum
     * and maximum value of the Image.
     * <p>
     * For example, if fractionalLevel is set to 0.1, the threshold will be set at min + 0.1*(max-min).
     * @param fractionalLevel   The fractional level at which to threshold the Image.
     */
    public SimpleThresholdingFilter(double fractionalLevel) {
        this.fractionalLevel = fractionalLevel;
    }


    /**
     * Applies the SimpleThresholdingFilter to an Image.
     * @param im    The Image whose pixels wil be set to zero where the reference Image is below the fractional threshold.
     */
	@Override
	public void apply(WritableImage im) {

        Histogram h = new Histogram(this.referenceImage);

        double cutoff = (h.getMaxValue()-h.getMinValue())*this.fractionalLevel + h.getMinValue();

        for (ImageCoordinate ic : im) {

            if (this.referenceImage.getValue(ic) < cutoff) {
                im.setValue(ic, 0);
            }

        }

	}

    /**
     * Sets a new fractional level for the thresholding of the Image.
     * <p>
     * For example, if fractionalLevel is set to 0.1, the threshold will be set at min + 0.1*(max-min).  Where min and max are the minimum
     * and maximum values of the reference Image.
     * @param level     The fractional level at which to threshold the Image.
     */
    public void setFractionalLevel(double level) {
        this.fractionalLevel = level;
    }

}
