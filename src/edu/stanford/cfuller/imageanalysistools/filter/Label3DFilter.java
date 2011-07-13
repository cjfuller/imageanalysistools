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

		ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);


		for (ImageCoordinate i : im) {

			if (Math.floor(im.getValue(i)) > 0) {

				ic.setX(i.getX()-1);
				ic.setY(i.getY());
				ic.setZ(i.getZ());
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.setX(i.getX()+1);
				ic.setY(i.getY());
				ic.setZ(i.getZ());
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.setX(i.getX());
				ic.setY(i.getY()-1);
				ic.setZ(i.getZ());
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.setX(i.getX());
				ic.setY(i.getY()+1);
				ic.setZ(i.getZ());
				updateLabeling(preliminaryLabeledImage, i, ic);
				
				ic.setX(i.getX());
				ic.setY(i.getY());
				ic.setZ(i.getZ()-1);
				updateLabeling(preliminaryLabeledImage, i, ic);

				ic.setX(i.getX());
				ic.setY(i.getY());
				ic.setZ(i.getZ()+1);
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

				ic.setX(i.getX()-1);
				ic.setY(i.getY());
				ic.setZ(i.getZ());
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.setX(i.getX()+1);
				ic.setY(i.getY());
				ic.setZ(i.getZ());
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.setX(i.getX());
				ic.setY(i.getY()-1);
				ic.setZ(i.getZ());
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.setX(i.getX());
				ic.setY(i.getY()+1);
				ic.setZ(i.getZ());
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);
				
				ic.setX(i.getX());
				ic.setY(i.getY());
				ic.setZ(i.getZ()-1);
				mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic);

				ic.setX(i.getX());
				ic.setY(i.getY());
				ic.setZ(i.getZ()+1);
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

		//edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter rlf = new edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter();

		//rlf.apply(im);

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

