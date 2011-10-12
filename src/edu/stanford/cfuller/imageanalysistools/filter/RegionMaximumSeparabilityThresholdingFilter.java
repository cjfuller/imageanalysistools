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
 * A Filter that sorts regions in a mask according to their average values in a reference image.
 * <p>
 * This filter takes each region in a mask, computes its average intensity value in a reference
 * image, and then applies a {@link MaximumSeparabilityThresholdingFilter} to these average
 * values and discards the regions that are below the threshold.
 * <p>
 * The input Image should be the mask whose regions will be sorted.
 * <p>
 * The reference Image should be an image whose intensity values will be used for
 * the thresholding.
 * 
 * @author Colin J. Fuller
 *
 */
public class RegionMaximumSeparabilityThresholdingFilter extends Filter {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {
		
		//try grouping all objects, finding average intensity, segmenting into categories based on average intensity of objects
		//(akin to reduce punctate background of the original centromere finder)
		
		Image result = im;
		Image reference = this.referenceImage;
		
		Histogram h = new Histogram(result);
		
		int numRegions = h.getMaxValue();
		
		double[] sums = new double[numRegions];
		
		java.util.Arrays.fill(sums, 0.0);
				
		for (ImageCoordinate ic : result) {
			
			int value = (int) result.getValue(ic);
			
			if (value == 0) continue;
			
			sums[value-1] += reference.getValue(ic);
			
		}
		
		//construct an image, one pixel per region, containing each region's average value
		
		ImageCoordinate dimensionSizes = ImageCoordinate.createCoordXYZCT(numRegions, 1,1,1,1);
		
		Image meanValues = new Image(dimensionSizes, 0.0);
		
		for (ImageCoordinate ic : meanValues) {
			meanValues.setValue(ic, sums[ic.get(ImageCoordinate.X)]/h.getCounts(ic.get(ImageCoordinate.X) + 1));
		}
		
		dimensionSizes.recycle();
		
		//segment the image
		
		MaximumSeparabilityThresholdingFilter MSTF = new MaximumSeparabilityThresholdingFilter();
		
		MSTF.apply(meanValues);
		
		//filter based on the average value segmentation
		
		for (ImageCoordinate ic : result) {
			
			ImageCoordinate ic2 = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
			
			int value = (int) result.getValue(ic);
			
			if (value == 0) continue;
			
			ic2.set(ImageCoordinate.X, value - 1);
			
			if (meanValues.getValue(ic2) == 0.0) {
				result.setValue(ic, 0);
			}
			
		}

	}

}
