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
	public void apply(WritableImage im) {

        Histogram h = new Histogram(im);

        int[] mapping = new int[h.getMaxValue() + 1];

        for (int i =0; i < mapping.length; i++) {
            mapping[i] = i;
        }

        ImageCoordinate ic2 = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

        for (ImageCoordinate ic : im) {

            ic2.set(ImageCoordinate.Z,ic.get(ImageCoordinate.Z));
            ic2.set(ImageCoordinate.C,ic.get(ImageCoordinate.C));
            ic2.set(ImageCoordinate.Y,ic.get(ImageCoordinate.T));

            int currValue = (int) im.getValue(ic);

            while(currValue != mapping[currValue]) {currValue = mapping[currValue];}

            int x = ic.get(ImageCoordinate.X);
            int y = ic.get(ImageCoordinate.Y);

            if (currValue > 0 ) {


                //check 8 connected pixels for other values, update mapping

                ic2.set(ImageCoordinate.X,x-1);
                ic2.set(ImageCoordinate.Y,y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x-1);
                ic2.set(ImageCoordinate.Y,y);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x-1);
                ic2.set(ImageCoordinate.Y,y+1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x);
                ic2.set(ImageCoordinate.Y,y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x);
                ic2.set(ImageCoordinate.Y,y+1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x+1);
                ic2.set(ImageCoordinate.Y,y-1);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x+1);
                ic2.set(ImageCoordinate.Y,y);

                updateMapping(ic2, currValue, im, mapping);

                ic2.set(ImageCoordinate.X,x+1);
                ic2.set(ImageCoordinate.Y,y+1);

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
