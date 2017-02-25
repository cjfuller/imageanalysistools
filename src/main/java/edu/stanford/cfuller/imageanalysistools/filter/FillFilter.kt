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

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.filter.Filter
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter

/**
 * A Filter that fills in gaps in regions in a labeled mask.  This will fill holes in regions only if for each pixel in a hole
 * the closest region in the +x, -x, +y, and -y directions is the same, and is the same region for every pixel in that hole.
 * It will not fill holes extending to the edge of an Image.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the mask whose gaps will be filled.

 * @author Colin J. Fuller
 */

class FillFilter : Filter() {

    //TODO: refactor project to call this FillFilter2D.

    //TODO: remove explicit reference to dimensions not required for this filter.

    /**
     * Apply the Filter, filling in any gaps in the supplied mask.
     * @param im    The Image that is a mask whose regions will have any holes filled in.
     */
    override fun apply(im: WritableImage) {

        val LF = LabelFilter()

        val copy = ImageFactory.createWritable(im)
        val leftRegions = ImageFactory.createWritable(im.dimensionSizes, 0f)
        val rightRegions = ImageFactory.createWritable(im.dimensionSizes, 0f)
        val topRegions = ImageFactory.createWritable(im.dimensionSizes, 0f)
        val bottomRegions = ImageFactory.createWritable(im.dimensionSizes, 0f)

        for (c in copy) {
            if (copy.getValue(c) == 0f) {
                copy.setValue(c, 1f)
            } else {
                copy.setValue(c, 0f)
            }
        }

        //traverse the directional regions manually to ensure defined pixel order

        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        val icmod = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (x in 0..im.dimensionSizes.get(ImageCoordinate.X) - 1) {
            for (y in 0..im.dimensionSizes.get(ImageCoordinate.Y) - 1) {

                ic.set(ImageCoordinate.X, x)
                ic.set(ImageCoordinate.Y, y)

                if (x == 0 || y == 0 || im.getValue(ic) > 0) {

                    leftRegions.setValue(ic, im.getValue(ic))
                    topRegions.setValue(ic, im.getValue(ic))

                } else {
                    icmod.set(ImageCoordinate.X, x - 1)
                    icmod.set(ImageCoordinate.Y, y)
                    leftRegions.setValue(ic, leftRegions.getValue(icmod))

                    icmod.set(ImageCoordinate.X, x)
                    icmod.set(ImageCoordinate.Y, y - 1)
                    topRegions.setValue(ic, topRegions.getValue(icmod))
                }

            }
        }

        for (x in im.dimensionSizes.get(ImageCoordinate.X) - 1 downTo 0) {
            for (y in im.dimensionSizes.get(ImageCoordinate.Y) - 1 downTo 0) {

                ic.set(ImageCoordinate.X, x)
                ic.set(ImageCoordinate.Y, y)

                if (x == im.dimensionSizes.get(ImageCoordinate.X) - 1 || y == im.dimensionSizes.get(ImageCoordinate.Y) - 1 || im.getValue(ic) > 0) {

                    rightRegions.setValue(ic, im.getValue(ic))
                    bottomRegions.setValue(ic, im.getValue(ic))

                } else {
                    icmod.set(ImageCoordinate.X, x + 1)
                    icmod.set(ImageCoordinate.Y, y)
                    rightRegions.setValue(ic, rightRegions.getValue(icmod))

                    icmod.set(ImageCoordinate.X, x)
                    icmod.set(ImageCoordinate.Y, y + 1)
                    bottomRegions.setValue(ic, bottomRegions.getValue(icmod))
                }

            }
        }

        ic.recycle()
        icmod.recycle()

        LF.apply(copy)

        val voidsToRegions = java.util.HashMap<Int, Int>()

        val voidsNotToFill = java.util.HashSet<Int>()

        for (c in copy) {

            if (copy.getValue(c) != 0f) {

                if (voidsNotToFill.contains(copy.getValue(c))) {
                    continue
                }

                val leftRegion = leftRegions.getValue(c).toInt()
                val rightRegion = rightRegions.getValue(c).toInt()
                val topRegion = topRegions.getValue(c).toInt()
                val bottomRegion = bottomRegions.getValue(c).toInt()

                if (!(topRegion > 0 && bottomRegion > 0 && leftRegion > 0 && rightRegion > 0 && topRegion == bottomRegion && leftRegion == rightRegion && leftRegion == topRegion)) {
                    // if it's on an image border or touches multiple regions, don't fill
                    voidsNotToFill.add(copy.getValue(c).toInt())
                } else if (!voidsToRegions.containsKey(copy.getValue(c).toInt()) || voidsToRegions[copy.getValue(c).toInt()] === topRegion) {
                    //if it's surrounded by a single region and we haven't seen it before, or we've seen it before surrounded by the same region, ok to fill so far
                    voidsToRegions.put(copy.getValue(c).toInt(), topRegion)
                } else {
                    //if we've seen it before surrounded by a different region, don't fill
                    voidsNotToFill.add(copy.getValue(c).toInt())
                }


            }

        }

        for (c in copy) {
            if (im.getValue(c) == 0f && !voidsNotToFill.contains(copy.getValue(c).toInt())) {
                im.setValue(c, voidsToRegions[copy.getValue(c).toInt()].toFloat())
            }
        }

    }

}
