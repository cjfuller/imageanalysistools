/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.filter.morph

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * This class represents a structuring element for use in morphological image processing.

 * @author Colin J. Fuller
 */
class StructuringElement
/**
 * Constructs a new StructuingElement from and ImageCoordinate specifying its size in each dimension.
 *
 *
 * All dimensions must have odd size, but this is not checked.  For dimensions over which you do not wish to apply the
 * morphological operation, specify size 1.

 * @param size    an ImageCoordinate containing the sizes of each dimension.
 */
(size: ImageCoordinate) {
    internal var size: ImageCoordinate = ImageCoordinate.cloneCoord(size)
    internal var elements: Array<Array<Array<Array<FloatArray>>>> = Array(this.size[ImageCoordinate.T]) {
        Array(this.size[ImageCoordinate.C]) {
            Array(this.size[ImageCoordinate.Z]) {
                Array(this.size[ImageCoordinate.Y]) {
                    FloatArray(this.size[ImageCoordinate.X])
                }
            }
        }
    }

    /**
     * Sets the box of interest in an Image to be the size of the structuring element surrounding a specified position in that image.

     * @param currentPosition    The position around which the box will be centered.
     * *
     * @param toBeBoxed            The Image that will be boxed.
     */
    fun boxImageToElement(currentPosition: ImageCoordinate, toBeBoxed: Image) {
        val lowerBound = ImageCoordinate.cloneCoord(currentPosition)
        val upperBound = ImageCoordinate.cloneCoord(currentPosition)

        for (i in currentPosition) {
            lowerBound[i] = lowerBound[i] - (size[i] - 1) / 2
            upperBound[i] = upperBound[i] + (size[i] - 1) / 2 + 1
        }
        toBeBoxed.setBoxOfInterest(lowerBound, upperBound, false)
    }

    /**
     * Gets the value of the StructuringElement at a location specified in the coordinates of the StructuringElement.

     * @param strelCoord    The coordinate from which to retrieve the value.
     * *
     * @return                The value of the StructuringElement at that point.
     */
    operator fun get(strelCoord: ImageCoordinate): Float {
        return elements[strelCoord.get(ImageCoordinate.T)][strelCoord.get(ImageCoordinate.C)][strelCoord.get(ImageCoordinate.Z)][strelCoord.get(ImageCoordinate.Y)][strelCoord.get(ImageCoordinate.X)]
    }

    /**
     * Gets the value of the StructuringElement at a location in the coordinates of an Image.
     * @param strelCenterImageCoord        The coordinate in the image where the structuring element is centered.
     * *
     * @param imageCoord                The corresponding coordinate in the image from which to retrieve the value of the structuring element.
     * *
     * @return                            The value of the StructuringElement at that point.
     */
    operator fun get(strelCenterImageCoord: ImageCoordinate, imageCoord: ImageCoordinate): Float {
        val t = imageCoord[ImageCoordinate.T] - strelCenterImageCoord[ImageCoordinate.T] + (size[ImageCoordinate.T] - 1) / 2
        val c = imageCoord[ImageCoordinate.C] - strelCenterImageCoord[ImageCoordinate.C] + (size[ImageCoordinate.C] - 1) / 2
        val z = imageCoord[ImageCoordinate.Z] - strelCenterImageCoord[ImageCoordinate.Z] + (size[ImageCoordinate.Z] - 1) / 2
        val y = imageCoord[ImageCoordinate.Y] - strelCenterImageCoord[ImageCoordinate.Y] + (size[ImageCoordinate.Y] - 1) / 2
        val x = imageCoord[ImageCoordinate.X] - strelCenterImageCoord[ImageCoordinate.X] + (size[ImageCoordinate.X] - 1) / 2
        return elements[t][c][z][y][x]
    }

    /**
     * Sets the value at every location in a StructuringElement to the specified value.
     * @param value        The value to which to set the StructuringElement.
     */
    fun setAll(value: Float) {
        for (t in 0..this.size[ImageCoordinate.T] - 1) {
            for (c in 0..this.size[ImageCoordinate.C] - 1) {
                for (z in 0..this.size[ImageCoordinate.Z] - 1) {
                    for (y in 0..this.size[ImageCoordinate.Y] - 1) {
                        for (x in 0..this.size[ImageCoordinate.X] - 1) {
                            elements[t][c][z][y][x] = value
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the value of the StructuringElement at a location in the coordinates of an Image.
     * @param strelCenterImageCoord        The coordinate in the image where the structuring element is centered.
     * *
     * @param imageCoord                The corresponding coordinate in the image where the value of the structuring element will be set.
     * *
     * @param value                        The value to which to set the structuring element.
     */
    operator fun set(strelCenterImageCoord: ImageCoordinate, imageCoord: ImageCoordinate, value: Float) {
        val t = imageCoord[ImageCoordinate.T] - strelCenterImageCoord[ImageCoordinate.T] - (size[ImageCoordinate.T] - 1) / 2
        val c = imageCoord[ImageCoordinate.C] - strelCenterImageCoord[ImageCoordinate.C] - (size[ImageCoordinate.C] - 1) / 2
        val z = imageCoord[ImageCoordinate.Z] - strelCenterImageCoord[ImageCoordinate.Z] - (size[ImageCoordinate.Z] - 1) / 2
        val y = imageCoord[ImageCoordinate.Y] - strelCenterImageCoord[ImageCoordinate.Y] - (size[ImageCoordinate.Y] - 1) / 2
        val x = imageCoord[ImageCoordinate.X] - strelCenterImageCoord[ImageCoordinate.X] - (size[ImageCoordinate.X] - 1) / 2
        elements[t][c][z][y][x] = value
    }

    /**
     * Sets the value of the StructuringElement at a location specified in the coordinates of the StructuringElement.

     * @param strelCoord    The coordinate at which to set the value.
     * *
     * @param value            The value to which to set the structuring element.
     */
    operator fun set(strelCoord: ImageCoordinate, value: Float) {
        elements[strelCoord[ImageCoordinate.T]][strelCoord[ImageCoordinate.C]][strelCoord[ImageCoordinate.Z]][strelCoord[ImageCoordinate.Y]][strelCoord[ImageCoordinate.X]] = value
    }
}
