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
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;

import java.util.Arrays;


/**
 * A Filter that thresholds an Image based on a single value for each region in a mask.
 * <p>
 * Takes a mask and and Image, calculates a mean value over all pixels in each region, and then uses a thresholding filter to threshold the Image masked
 * with the mask.  Any region whose mean value is below the threshold found by the thresholding filter is removed.
 * <p>
 * The reference Image should be set to an Image that will be used to compute the mean value of each region.  This Image will not be changed.
 * <p>
 * The argument to the apply method should be set to the mask whose regions will be averaged over and removed if below the threshold.
 *
 * @author Colin J. Fuller
 */
public class RegionThresholdingFilter extends Filter {

    private Filter thresholdingFilter;

    public void setThresholdingFilter(Filter f) {
        this.thresholdingFilter = f;
    }

    /**
     * Applies the Filter to an Image mask
     * @param im    The Image mask to process; its regions will be retained or removed based upon each region's average value
     * compared to a threshold.
     */
	@Override
	public void apply(WritableImage im) {
		
		LabelFilter LF = new LabelFilter();

        LF.apply(im);

        Histogram h = new Histogram(im);

        int n= h.getMaxValue();

        double[] means = new double[n+1];

        Arrays.fill(means, 0);

        for (ImageCoordinate i : im) {

            means[(int) im.getValue(i)] += this.referenceImage.getValue(i);

        }

        for (int k = 0; k < n+1; k++) {
            if (h.getCounts(k) > 0) {
                means[k] /= h.getCounts(k);
            } else {
                means[k] = 0;
            }
        }

        WritableImage refCopy = ImageFactory.createWritable(this.referenceImage);

        MaskFilter mf = new MaskFilter();

        mf.setReferenceImage(im);
        mf.apply(refCopy);

        this.thresholdingFilter.setReferenceImage(refCopy);
        this.thresholdingFilter.apply(refCopy);

        Histogram h_thresh = new Histogram(refCopy);

        int threshold = h_thresh.getMinValueNonzero();

        for (ImageCoordinate i : im) {
            if (im.getValue(i) == 0) continue;

            double mean = means[(int) im.getValue(i)];

            if (mean < threshold) {
                im.setValue(i, 0);
            }

        }



	}

}
