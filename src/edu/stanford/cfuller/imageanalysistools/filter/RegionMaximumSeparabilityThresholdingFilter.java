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
				
		//need a heuristic to figure out whether this is in fact necessary (due to a lot of noise) or not, and will discard a lot
		//of good regions.
		
		//approach: calculate mean of lowest 10% of pixels, calculate background between objects (for now just in a box), then compare to
		//average of lowest 10% of pixels, is it within a factor of 1.2?
		
		//a box, or even a convex hull is a bad idea, because with sufficently many bad regions, it will get almost the whole image, and then of course
		//there won't be any difference.
		
		//now try: take a 5 pixel margin around each region, exclusive of that region.
		//bad also-- this ratio to the bottom 10% of pixels is actually larger for the beads...
		
		//ImageCoordinate firstCorner = ImageCoordinate.createCoord();
		
		//ImageCoordinate secondCorner = ImageCoordinate.createCoord();
		//boolean first = true;
		
		//another try: look at the relation between the mean of foreground and background.  Relate to std. dev?
		
		
//		for (ImageCoordinate ic : im) {
//			if (im.getValue(ic) > 0) {
//				if (first) {
//					first = false;
//					firstCorner.setCoord(ic);
//				}
//				secondCorner.setCoord(ic);
//			}
//		}
		
//		java.util.Map<Integer, ImageCoordinate> lowerCoordinates = new java.util.HashMap<Integer, ImageCoordinate>();
//		java.util.Map<Integer, ImageCoordinate> upperCoordinates = new java.util.HashMap<Integer, ImageCoordinate>();
//		
		double fgAvg = 0;
		double bgAvg = 0;
		int fgCount = 0;
		int bgCount = 0;
		
		for (ImageCoordinate ic : im) {
			
			int value = (int) im.getValue(ic);
			
			if (value == 0) {
				bgAvg+= this.referenceImage.getValue(ic);
				bgCount++;
			} else {
				fgAvg+= this.referenceImage.getValue(ic);
				fgCount++;
			}
			
//			if (value == 0) continue;
//			
//			if (!lowerCoordinates.containsKey(value)) {
//				lowerCoordinates.put(value, ImageCoordinate.cloneCoord(ic));
//				upperCoordinates.put(value, ImageCoordinate.cloneCoord(ic));
//			}
//			
//			upperCoordinates.get(value).setCoord(ic);
			
		}
		
		fgAvg /= fgCount;
		bgAvg /= bgCount;
		
		double fgStd = 0;
		double bgStd = 0;
		
		for (ImageCoordinate ic : im) {
			
			int value = (int) im.getValue(ic);
			
			if (value == 0) {
				bgStd+= Math.pow(this.referenceImage.getValue(ic) - bgAvg, 2);
			} else {
				fgStd+= Math.pow(this.referenceImage.getValue(ic) - fgAvg, 2);
			}
			
		}
		
		fgStd/= fgCount;
		bgStd/= bgCount;
		
		fgStd = Math.sqrt(fgStd);
		bgStd = Math.sqrt(bgStd);
		
		System.out.printf("fg mean: %f, bg mean: %f, fg std: %f, bg std: %f\n", fgAvg, bgAvg, fgStd, bgStd);
		
		
		
		
//		for (Integer key : lowerCoordinates.keySet()) {
//			
//			lowerCoordinates.get(key).set(ImageCoordinate.X, lowerCoordinates.get(key).get(ImageCoordinate.X) - 5);
//			lowerCoordinates.get(key).set(ImageCoordinate.Y, lowerCoordinates.get(key).get(ImageCoordinate.Y) - 5);
//			lowerCoordinates.get(key).set(ImageCoordinate.Z, lowerCoordinates.get(key).get(ImageCoordinate.Z) - 5);
//			
//			upperCoordinates.get(key).set(ImageCoordinate.X, upperCoordinates.get(key).get(ImageCoordinate.X) + 6);
//			upperCoordinates.get(key).set(ImageCoordinate.Y, upperCoordinates.get(key).get(ImageCoordinate.Y) + 6);
//			upperCoordinates.get(key).set(ImageCoordinate.Z, upperCoordinates.get(key).get(ImageCoordinate.Z) + 6);
//			upperCoordinates.get(key).set(ImageCoordinate.C, upperCoordinates.get(key).get(ImageCoordinate.C) + 1);
//			upperCoordinates.get(key).set(ImageCoordinate.T, upperCoordinates.get(key).get(ImageCoordinate.T) + 1);
//
//		}
//
//		int count = 0;
//		double average = 0;
//		
//		for (Integer key : lowerCoordinates.keySet()) {
//			
//			ImageCoordinate firstCorner = lowerCoordinates.get(key);
//			ImageCoordinate secondCorner = upperCoordinates.get(key);
//			
//			im.setBoxOfInterest(firstCorner, secondCorner);
//			
//			for (ImageCoordinate ic : im) {
//				if (((int) im.getValue(ic)) == 0) {
//					average += this.referenceImage.getValue(ic);
//					count++;
//				}
//			}
//			
//			im.clearBoxOfInterest();
//			
//		}
//		
//		for (Integer key : lowerCoordinates.keySet()) {
//			lowerCoordinates.get(key).recycle();
//			upperCoordinates.get(key).recycle();
//		}
//		
//		lowerCoordinates = null;
//		upperCoordinates = null;
//
//		
//		average/=count;
//		
//		Histogram href = new Histogram(this.referenceImage);
//		
//		int breakpoint = 0;
//		
//		int totalPixels = href.getTotalCounts() + href.getCounts(0);
//		
//		double average_1_10th = 0;
//		
//		for (int i = 0; i < href.getMaxValue(); i++) {
//						
//			if (href.getCumulativeCounts(i) > 0.1*totalPixels) {
//				breakpoint = i-1;
//				break;
//			}
//			
//			average_1_10th += href.getCounts(i)*i;
//			
//		}
//		
//		int counts = href.getCumulativeCounts(breakpoint);
//
//		System.out.println("breakpoint: " + breakpoint + "   total pixels: " + totalPixels);
//		
//		average_1_10th += (((int) (0.1*totalPixels)) - counts)*(breakpoint + 1);
//		
//		average_1_10th /= ((int) (0.1*totalPixels));
//		
		double thresholdMultiplier = 5;
		
		//double thresholdMultiplier = 0;
		
		boolean shouldApplyFilter = fgAvg < thresholdMultiplier*bgAvg;
		
		System.out.println("Should apply the RMSTF? " + shouldApplyFilter + "  average fg: " + fgAvg + "  average bg: " + bgAvg);
		
		
		
		if (!shouldApplyFilter) return;
		
		
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
