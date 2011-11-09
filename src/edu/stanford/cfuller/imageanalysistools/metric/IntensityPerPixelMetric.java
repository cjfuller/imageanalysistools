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

package edu.stanford.cfuller.imageanalysistools.metric;

import org.apache.commons.math.linear.RealMatrix;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;

/**
 * A simple Metric that quantifies the total intensity in a region of interest divided by the size of that region of interest.
 * <p>
 * A side note on using intensity per pixel as a metric rather than integrated intensity:
 * <p>
 * When observing objects much smaller than the diffraction limit of light, an increase in brightness of a point source will appear
 * almost the same as multiple point sources of equivalent total brightness close together in that same diffraction-limited volume.  For this reason,
 * the apparent size of objects below the diffraction limit is often solely dependent on the brightess of objects, and not their
 * actual size.  Thus, for an Image segmentation method that may not attempt to compensate for objects of differing brightness, which
 * then quantified on integrated intensity would effectively be multiplying differences in intensity: brighter objects would
 * contain more pixels (as they would have larger apparent size), and these pixels would each be brighter.  Measuring intensity per
 * pixel counters this effect (though in many cases where the sample permits using a constant region size, this would be a better choice).
 * This is even more effective when combined with something like the {@link edu.stanford.cfuller.imageanalysistools.filter.RenormalizationFilter},
 * which tries to locally intensity normalize an image before segmentation, so that differences in brightness will not by themselves
 * lead to changes in apparent object size.
 *
 *
 * @author Colin J. Fuller
 */


public class IntensityPerPixelMetric extends Metric {


    /**
     * Quantifies the (area) average intensity for each region of interest in an image.
     *
     * If no regions of interest are present in the supplied mask, this will return null.
     *
     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1,
     *                  as might be produced by a {@link edu.stanford.cfuller.imageanalysistools.filter.LabelFilter}.
     * @param images    An ImageSet of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * @return          A RealMatrix containing the average intensity value for each region of interest in each input Image.  The (i,j)th
     *                  entry will contain the quantification of ROI (i+1) in Image j.
     */
	@Override
	public RealMatrix quantify(Image mask, ImageSet images) {
		
		edu.stanford.cfuller.imageanalysistools.image.Histogram h = new edu.stanford.cfuller.imageanalysistools.image.Histogram(mask);
		
		if (h.getMaxValue() == 0) return null;
		
		RealMatrix channelIntensities = (new org.apache.commons.math.linear.Array2DRowRealMatrix(images.getImageCount()+2, h.getMaxValue())).scalarMultiply(0);
		
		for (ImageCoordinate i : mask) {
			int regionNum = (int) mask.getValue(i);
			
			if (regionNum > 0) {
				for (int c = 0; c < images.getImageCount(); c++) {
					channelIntensities.addToEntry(c+1, regionNum-1, images.getImageForIndex(c).getValue(i));
				}
			}
		}
		
		for (int i = 0; i < h.getMaxValue(); i++) {
			channelIntensities.setEntry(0, i, i+1);
			channelIntensities.setEntry(images.getImageCount()+1, i, h.getCounts(i+1));
			for (int c = 0; c < images.getImageCount(); c++) {
				channelIntensities.setEntry(c+1, i, channelIntensities.getEntry(c+1,i)/h.getCounts(i+1));
			}
		}
		
		
		return channelIntensities.transpose();
	}

}
