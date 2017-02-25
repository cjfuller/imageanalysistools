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

package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that labels regions in a mask according to the labels in a second (seed) mask.
 *
 *
 * Each distinct region in the input mask will be assigned
 * the label of any seed region that has any pixel overlap with the region in the input mask.  If multiple seed regions
 * overlap with a single region in the input mask, all the pixels the region in the input mask will be assigned to the same one of those seed values
 * (but it is unspecified which one), except for any pixels overlapping directly with a different seed region, which will always
 * be assigned the same label as the seed region.
 *
 *
 * Any regions in the input mask that do not overlap with a seed region will not be changed.  This could potentially lead to
 * duplicate labeling, so it is a good idea to either use a segmentation method that guarantees that every region has a seed,
 * or to first apply a [SeedFilter].
 *
 *
 * The reference Image should be set to the seed mask (this will not be modified by the Filter).
 *
 *
 * The argument to the apply method should be set to the mask that is to be labeled according to the labels in the seed Image.

 * @author Colin J. Fuller
 */

class OneToOneLabelBySeedFilter : Filter() {


    /**
     * Applies the Filter to the specified Image mask, relabeling it according to the seed regions in the reference Image.
     * @param im    The Image mask to process, whose regions will be relabeled.
     */
    override fun apply(im: WritableImage) {
        val hasSeedSet = java.util.HashMap<Int, Int>()
        val seedIsMapped = java.util.HashSet<Int>()


        for (c in im) {

            val currValue = im.getValue(c).toInt()
            val seedValue = this.referenceImage.getValue(c).toInt()

            if (seedValue > 0 && currValue > 0) {
                hasSeedSet.put(currValue, seedValue)
            }

        }

        for (i in hasSeedSet.values) {
            seedIsMapped.add(i)
        }

        for (c in im) {

            val currValue = im.getValue(c).toInt()

            if (hasSeedSet.containsKey(currValue) && currValue > 0) {

                im.setValue(c, hasSeedSet[currValue].toFloat())
            }

            if (this.referenceImage.getValue(c) > 0 && seedIsMapped.contains(this.referenceImage.getValue(c).toInt())) {

                im.setValue(c, this.referenceImage.getValue(c))

            }

        }


    }

}
