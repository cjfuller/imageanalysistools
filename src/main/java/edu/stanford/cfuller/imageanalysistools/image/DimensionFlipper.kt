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

package edu.stanford.cfuller.imageanalysistools.image

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities

/**
 * A Class that swaps two dimensions of an Image.

 * @author Colin J. Fuller
 */
object DimensionFlipper {


    /**
     * Swaps the Z- and T- dimensions of an Image (these can get mixed up, e.g. while reading certain
     * metamorph stacks).
     * @param toFlip    The Image whose dimensions will be swapped.  (This will not be modified.)
     * *
     * @return            A new Image whose dimensions are swapped from the input.
     */
    fun flipZT(toFlip: Image): Image {
        return flip(toFlip, ImageCoordinate.Z, ImageCoordinate.T)
    }


    /**
     * Swaps two dimensions in an Image.

     * @param toFlip    The Image whose dimensions will be swapped.  (This will not be modified.)
     * *
     * @param dim0        The first dimension to swap.  This should correspond to one of the constants defined in ImageCoordinate (or rarely, a user-defined dimension).
     * *
     * @param dim1        The second dimension to swap.  This should correspond to one of the constants defined in ImageCoordinate (or rarely, a user-defined dimension).
     * *
     * @return            A new Image whose dimensions are swapped from the input.
     */
    fun flip(toFlip: Image, dim0: Int, dim1: Int): Image {

        val sizes = ImageCoordinate.cloneCoord(toFlip.dimensionSizes)

        val temp_1 = sizes.get(dim1)

        sizes.set(dim1, sizes.get(dim0))
        sizes.set(dim0, temp_1)

        val newImage = ImageFactory.createWritable(sizes, 0.0f)

        val flipCoord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (ic in toFlip) {
            flipCoord.setCoord(ic)
            flipCoord.set(dim0, ic.get(dim1))
            flipCoord.set(dim1, ic.get(dim0))

            newImage.setValue(flipCoord, toFlip.getValue(ic))
        }

        flipCoord.recycle()
        sizes.recycle()

        return newImage

    }


}
