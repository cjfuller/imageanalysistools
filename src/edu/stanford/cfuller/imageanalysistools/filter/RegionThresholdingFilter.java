/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

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
	public void apply(Image im) {
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

        Image refCopy = new Image(this.referenceImage);

//        ImageCoordinate newDim = ImageCoordinate.createCoord(n, 1, 1, 1, 1);
//
//        Image means_im = new Image(newDim, 0.0);
//
//
//        newDim.recycle();
//
//        for (ImageCoordinate i : means_im) {
//            means_im.setValue(i, means[i.getX()+1]);
//        }

        //Image copy = new Image(means_im);

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
