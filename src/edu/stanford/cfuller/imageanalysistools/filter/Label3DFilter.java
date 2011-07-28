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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that labels connected regions in a 3D Image mask.
 * <p>
 * Connected regions are defined as positive-valued regions of an Image that are 4-connected (2D) or 6-connected (3D).  Zero-valued regions
 * separate the connected regions.  Regions will be labeled consecutively, starting with 1 for the first connected region.  Though
 * labels will tend to be numbered top to bottom and left to right, no particular ordering of regions is guaranteed.  Labeling the
 * same original Image multiple times, however, will produce the same result each time.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the mask to be labeled.
 *
 * 
 *@author Colin J. Fuller
 *
 */
public class Label3DFilter extends Filter {


	/**
	 * Applies the LabelFilter to an Image.
	 * @param im    The Image mask that will end up with 6-connected regions labeled.
	 */
	@Override
	public void apply(Image im) {

		Image preliminaryLabeledImage = new Image(im.getDimensionSizes(), 0.0);
		int labelCounter = 1;

		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);


		for (ImageCoordinate i : im) {

			if (Math.floor(im.getValue(i)) > 0) {

				ic.set("x",i.get("x")-1);
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z"));
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.set("x",i.get("x")+1);
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z"));
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y")-1);
				ic.set("z",i.get("z"));
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y")+1);
				ic.set("z",i.get("z"));
				updateLabeling(preliminaryLabeledImage, i, ic);
				
				ic.set("x",i.get("x"));
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z")-1);
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z")+1);
				updateLabeling(preliminaryLabeledImage, i, ic);

				
				if (preliminaryLabeledImage.getValue(i) == 0) {preliminaryLabeledImage.setValue(i, labelCounter++);}

			}

		}


		int[] labelMapping = new int[labelCounter];
		int[] finalLabelMapping = new int[labelCounter];

		for (int i = 0; i < labelCounter; i++) {
			labelMapping[i] = i;
			finalLabelMapping[i] = 0;
		}

		for (ImageCoordinate i : im) {
			int currValue = (int) preliminaryLabeledImage.getValue(i);
			if (currValue > 0) {

				int mappedCurrValue = currValue;

				while(mappedCurrValue != labelMapping[currValue]) {
					mappedCurrValue = labelMapping[currValue];
				}

				ic.set("x",i.get("x")-1);
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z"));
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.set("x",i.get("x")+1);
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z"));
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y")-1);
				ic.set("z",i.get("z"));
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y")+1);
				ic.set("z",i.get("z"));
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);
				
				ic.set("x",i.get("x"));
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z")-1);
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.set("x",i.get("x"));
				ic.set("y",i.get("y"));
				ic.set("z",i.get("z")+1);
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

			}
		}

		ic.recycle();

		for (int i = 0; i < labelCounter; i++) {
			int currLabel = i;
			while(currLabel != labelMapping[currLabel]) currLabel = labelMapping[currLabel];
			finalLabelMapping[i] = currLabel;
		}

		for (ImageCoordinate i : im) {
			im.setValue(i, finalLabelMapping[(int) preliminaryLabeledImage.getValue(i)]);
		}


	}

	private void updateLabeling(Image preliminaryLabeledImage, ImageCoordinate i, ImageCoordinate ic){
		if (preliminaryLabeledImage.inBounds(ic) && preliminaryLabeledImage.getValue(ic) > 0) {
			preliminaryLabeledImage.setValue(i, preliminaryLabeledImage.getValue(ic));
		}
	}

	private void mapRegions(int currValue, int mappedCurrValue, int[] labelMapping, Image preliminaryLabeledImage, ImageCoordinate ic) {
		if (preliminaryLabeledImage.inBounds(ic) && preliminaryLabeledImage.getValue(ic) > 0) {
			int otherValue = (int) preliminaryLabeledImage.getValue(ic);

			if (otherValue != currValue && otherValue != mappedCurrValue) {
				while (otherValue != labelMapping[otherValue]) {
					otherValue = labelMapping[otherValue];
				}

				if (otherValue != mappedCurrValue) {
					if (otherValue < mappedCurrValue) {
						labelMapping[mappedCurrValue] = otherValue;
					} else {
						labelMapping[otherValue] = mappedCurrValue;
					}
				}
			}
		}
	}

}

