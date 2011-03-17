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

/**
 * A Filter that merges nonzero regions in a mask that are 8-connected.
 * <p>
 * This filter is distinct from just applying a {@link LabelFilter} in that it preserves the current labeling.  When
 * two regions are merged, the resulting region will have the the smaller label of the two being merged.  Note that this can
 * cause the output mask not to have consecutively numbered regions.
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the mask whose regions are to be merged.  This will be overwritten with the
 * mask with merged regions.
 * 
 * @author Colin J. Fuller
 *
 */

public class MergeFilter extends Filter {


    /**
     * Applies the MergeFilter to an Image.
     * @param im    The Image whose 8-connected regions will be merged.
     */
	@Override
	public void apply(Image im) {

        Histogram h = new Histogram(im);

        int[] mapping = new int[h.getMaxValue() + 1];

        for (int i =0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        ImageCoordinate ic2 = ImageCoordinate.createCoord(0,0,0,0,0);

        for (ImageCoordinate ic : im) {

            ic2.setZ(ic.getZ());
            ic2.setC(ic.getC());
            ic2.setY(ic.getT());

            int currValue = (int) im.getValue(ic);

            while(currValue != mapping[currValue]) {currValue = mapping[currValue];}

            int x = ic.getX();
            int y = ic.getY();

            if (currValue > 0 ) {


                //check 8 connected pixels for other values, update mapping

                ic2.setX(x-1);
                ic2.setY(y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x-1);
                ic2.setY(y);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x-1);
                ic2.setY(y+1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x);
                ic2.setY(y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x);
                ic2.setY(y+1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x+1);
                ic2.setY(y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x+1);
                ic2.setY(y);

                updateMapping(ic2, currValue, im, mapping);

                ic2.setX(x+1);
                ic2.setY(y+1);

                updateMapping(ic2, currValue, im, mapping);

            }

        }

        for (ImageCoordinate ic : im) {

            int currValue = (int) im.getValue(ic);

            while(currValue != mapping[currValue]) {
                currValue = mapping[currValue];
            }

            im.setValue(ic, currValue);

        }

        ic2.recycle();
	}


    /**
     * Updates the mapping between the region labels in the original Image and their merged labels using the information at
     * a given coordinate in an Image.
     * @param ic2        The ImageCoordinate that is to be remapped.  This should be an 8-connected neighbor of the coordinate whose value is currValue.
     * @param currValue  The current value of a pixel at a coordinate neighboring ic2.
     * @param im         The Image whose regions are being merged (this should be the original unmodifed Image); will not be modified by this method.
     * @param mapping    An int array whose indices correspond to region labels in the original Image and whose elements are the labels to which the original
     *                   regions are being mapped.
     */
    protected void updateMapping(ImageCoordinate ic2, int currValue, Image im, int[] mapping) {

        int otherValue = 0;

        if (im.inBounds(ic2)) {
            otherValue = (int) im.getValue(ic2);
        }

        while (otherValue != mapping[otherValue]) {
            otherValue = mapping[otherValue];
        }

        if (otherValue > 0 && otherValue != currValue) {
            if (otherValue < currValue) {
                mapping[currValue] = otherValue;
            } else {
                mapping[otherValue] = currValue;
            }
        }
    }

}
