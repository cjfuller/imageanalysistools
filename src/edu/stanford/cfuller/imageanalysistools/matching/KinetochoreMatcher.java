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

package edu.stanford.cfuller.imageanalysistools.matching;

import java.util.ArrayList;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

import edu.stanford.cfuller.imageanalysistools.filter.KernelFilterND;
import edu.stanford.cfuller.imageanalysistools.filter.LaplacianFilterND;
import edu.stanford.cfuller.imageanalysistools.filter.VariableSizeMeanFilter;
import edu.stanford.cfuller.imageanalysistools.fitting.CentroidImageObject;
import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ReadOnlyImage;
import edu.stanford.cfuller.imageanalysistools.image.io.PromptingImageReader;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

/**
 * Matches sister kinetochores at metaphase using a marker (like CREST or anti-
 * CENP-B) that stains between the paired kinetochores.
 * 
 * @author Colin J. Fuller
 *
 */
public class KinetochoreMatcher {

	ParameterDictionary parameters;

	public void setParameters(ParameterDictionary p) {

		this.parameters = p;
	}

	public void makePairs(Image reference, Image pairMarker) {

		PromptingImageReader pir = new PromptingImageReader();

		Image mask = null;

		try {
			mask = pir.promptingRead();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		Histogram h = new Histogram(mask);

		ArrayList<ImageObject> imageObjects = new ArrayList<ImageObject>();

//		for (int i = 1; i <= h.getMaxValue(); i++) {
//
//			ImageObject o = new CentroidImageObject(i, new ReadOnlyImage(mask), new ReadOnlyImage(reference), this.parameters);
//			try {
//				o.fitPosition(this.parameters);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			imageObjects.add(o);
//
//			System.out.printf("%d: %s\n", o.getLabel(), o.getPositionForChannel(0).toString());
//		}
//
		RealMatrix distanceMatrix = new Array2DRowRealMatrix(h.getMaxValue(), h.getMaxValue());
		RealMatrix weightMatrix = new Array2DRowRealMatrix(h.getMaxValue(), h.getMaxValue());

		
		KernelFilterND kf = new KernelFilterND();
		
		double[] d = {0.1, 0.2, 0.4, 0.2, 0.1};
				
		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.X, d);
		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Y, d);
		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d);
		
		Image matchingChannel = new Image(pairMarker);
		
		kf.apply(matchingChannel);
		
		LaplacianFilterND lf = new LaplacianFilterND();
		
		lf.apply(matchingChannel);
		
		double min = Double.MAX_VALUE;
		
		for (ImageCoordinate ic : matchingChannel) {
			if (matchingChannel.getValue(ic) < min) {
				min = matchingChannel.getValue(ic);
			}
		}
		
		for (ImageCoordinate ic : matchingChannel) {
			matchingChannel.setValue(ic, matchingChannel.getValue(ic) -min);
		}
		
		matchingChannel.writeToFile("/Users/cfuller/Desktop/LF_ch2.ome.tif");
		
		VariableSizeMeanFilter vsmf = new VariableSizeMeanFilter();
		
		vsmf.setBoxSize(1);
		
		vsmf.apply(pairMarker);
		
		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_ch2.ome.tif");
		
		
				
		kf.apply(pairMarker);
								
		pairMarker.writeToFile("/Users/cfuller/Desktop/blurred.ome.tif");
		
		lf.apply(pairMarker);
		
		min = Double.MAX_VALUE;
		
		for (ImageCoordinate ic : pairMarker) {
			if (pairMarker.getValue(ic) < min) {
				min = pairMarker.getValue(ic);
			}
		}
		
		for (ImageCoordinate ic : matchingChannel) {
			pairMarker.setValue(ic, pairMarker.getValue(ic) -min);
		}
		
		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_lf_ch2.ome.tif");
		
		kf.apply(pairMarker);
		
		pairMarker.writeToFile("/Users/cfuller/Desktop/vsmf_lf_ch2_blur.ome.tif");

		
		for (int i = 0; i < h.getMaxValue(); i++) {
			for (int j = 0; j < h.getMaxValue(); j++) {
				distanceMatrix.setEntry(i,j, imageObjects.get(i).getPositionForChannel(0).getDistance(imageObjects.get(j).getPositionForChannel(0)));


				if (i == j) {
					weightMatrix.setEntry(i,j,0);
				} else {
					weightMatrix.setEntry(i,j,getWeight(imageObjects.get(i).getPositionForChannel(0), imageObjects.get(j).getPositionForChannel(0), matchingChannel, mask, i+1, j+1));
				}

				System.out.printf("%d --> %d: %f\n", i+1, j+1, weightMatrix.getEntry(i,j));
			}
		}

		double cutoff = 40;

		for (int i = 0; i < h.getMaxValue(); i++) {
			System.out.print((i+1) + "--> " );
			for (int j = 0; j < h.getMaxValue(); j++) {
				if (distanceMatrix.getEntry(i,j) < cutoff) {
					System.out.printf("%d (%1.2f), ", (j+1), weightMatrix.getEntry(i,j));
				}
			}

			System.out.println("");
		}
	}


	double getWeight(RealVector pos0, RealVector pos1, Image weights, Image mask, int label0, int label1) {

		ImageCoordinate boxLower = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
		ImageCoordinate boxUpper = ImageCoordinate.createCoordXYZCT(1,1,1,1,1);

		int x0 = (int) Math.round(pos0.getEntry(0));

		int x1 = (int) Math.round(pos1.getEntry(0));

		int y0 = (int) Math.round(pos0.getEntry(1));

		int y1 = (int) Math.round(pos1.getEntry(1));

		int z0 = (int) Math.round(pos0.getEntry(2));

		int z1 = (int) Math.round(pos1.getEntry(2));

		boxLower.set(ImageCoordinate.X, x0 < x1 ? x0 : x1);
		boxLower.set(ImageCoordinate.Y, y0 < y1 ? y0 : y1);
		boxLower.set(ImageCoordinate.Z, z0 < z1 ? z0 : z1);

		boxUpper.set(ImageCoordinate.X, x0 > x1 ? x0+1 : x1+1);
		boxUpper.set(ImageCoordinate.Y, y0 > y1 ? y0+1 : y1+1);
		boxUpper.set(ImageCoordinate.Z, z0 > z1 ? z0+1 : z1+1);

		weights.setBoxOfInterest(boxLower, boxUpper);

		RealVector unitVector = pos1.subtract(pos0);
		unitVector.unitize();

		double weight = 0;
		int count = 0;

		//try stepping on x; later, choose the best coordinate

		int startx = x0 < x1 ? x0 : x1;
		int finishx = x0 > x1 ? x0 : x1;

		final int cutoff = 2;


		for (int x = (int) Math.floor(startx); x <= (int) Math.ceil(finishx); x+=2) {

			//calculate y, z at current x

			double mult = (x - startx)/unitVector.getEntry(0);

			RealVector currPos =  pos0.add(unitVector.mapMultiply(mult));

			boxLower.set(ImageCoordinate.X, x - 1);
			boxUpper.set(ImageCoordinate.X, x + 2);

			boxLower.set(ImageCoordinate.Y, (int) Math.floor(currPos.getEntry(1)-cutoff));
			boxUpper.set(ImageCoordinate.Y, (int) Math.ceil(currPos.getEntry(1) + cutoff));

			boxLower.set(ImageCoordinate.Z, (int) Math.floor(currPos.getEntry(2)-cutoff));
			boxUpper.set(ImageCoordinate.Z, (int) Math.ceil(currPos.getEntry(2) + cutoff));

			weights.setBoxOfInterest(boxLower, boxUpper);

			RealVector posOffset = currPos.subtract(pos0);

			for (ImageCoordinate ic : weights) {

				int value = (int) mask.getValue(ic);

				if (value != 0) continue; // don't count if we're still in the centromere or in a different one

				currPos.setEntry(0, ic.get(ImageCoordinate.X));
				currPos.setEntry(1, ic.get(ImageCoordinate.Y));
				currPos.setEntry(2, ic.get(ImageCoordinate.Z));


				double dot = unitVector.dotProduct(posOffset);

				RealVector perpendicular = posOffset.subtract(unitVector.mapMultiply(dot));

				double distToPixel = perpendicular.getNorm();


				if (distToPixel < cutoff) {
					weight += weights.getValue(ic);
					count++;
				}


			}

			weights.clearBoxOfInterest();


		}




		boxLower.recycle();
		boxUpper.recycle();

		if (count > 0) {
			weight/=count;
		}

		return weight;


	}
}
