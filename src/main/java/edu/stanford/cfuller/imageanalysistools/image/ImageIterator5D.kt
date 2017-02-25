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

/**
 * An iterator optimized for faster iteration over 5D images.

 * @author Colin J. Fuller
 */
class ImageIterator5D : ImageIterator {

    protected constructor() {}

    internal var curr_x: Int = 0
    internal var curr_y: Int = 0
    internal var curr_z: Int = 0
    internal var curr_c: Int = 0
    internal var curr_t: Int = 0

    internal var lower_x: Int = 0
    internal var lower_y: Int = 0
    internal var lower_z: Int = 0
    internal var lower_c: Int = 0
    internal var lower_t: Int = 0

    internal var upper_x: Int = 0
    internal var upper_y: Int = 0
    internal var upper_z: Int = 0
    internal var upper_c: Int = 0
    internal var upper_t: Int = 0

    constructor(im: Image) {

        this.toIterate = im

        this.currCoord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        this.isBoxedIterator = im.isBoxed

        if (this.isBoxedIterator) {

            val boxMin = im.boxMin

            curr_x = boxMin.get(ImageCoordinate.X)
            curr_y = boxMin.get(ImageCoordinate.Y)
            curr_z = boxMin.get(ImageCoordinate.Z)
            curr_c = boxMin.get(ImageCoordinate.C)
            curr_t = boxMin.get(ImageCoordinate.T)

            val boxMax = im.boxMax

            upper_x = boxMax.get(ImageCoordinate.X)
            upper_y = boxMax.get(ImageCoordinate.Y)
            upper_z = boxMax.get(ImageCoordinate.Z)
            upper_c = boxMax.get(ImageCoordinate.C)
            upper_t = boxMax.get(ImageCoordinate.T)

        } else {

            curr_x = 0
            curr_y = 0
            curr_z = 0
            curr_c = 0
            curr_t = 0

            val sizes = im.dimensionSizes

            upper_x = sizes.get(ImageCoordinate.X)
            upper_y = sizes.get(ImageCoordinate.Y)
            upper_z = sizes.get(ImageCoordinate.Z)
            upper_c = sizes.get(ImageCoordinate.C)
            upper_t = sizes.get(ImageCoordinate.T)


        }

        lower_x = curr_x
        lower_y = curr_y
        lower_z = curr_z
        lower_c = curr_c
        lower_t = curr_t


        //account for the possibility of a zero size dimension

        if (upper_x <= lower_x || upper_y <= lower_y || upper_z <= lower_z || upper_c <= lower_c || upper_t <= lower_t) {
            curr_x = upper_x
            curr_y = upper_y
            curr_z = upper_z
            curr_c = upper_c
            curr_t = upper_t
        }

        curr_x--

        this.currCoord!!.setCoordXYZCT(curr_x, curr_y, curr_z, curr_c, curr_t)

    }

    override fun hasNext(): Boolean {
        if (curr_x + 1 < upper_x) {
            return true
        } else if (curr_y + 1 < upper_y) {
            return true
        } else if (curr_z + 1 < upper_z) {
            return true
        } else if (curr_c + 1 < upper_c) {
            return true
        } else if (curr_t + 1 < upper_t) {
            return true
        }
        return false
    }

    override fun next(): ImageCoordinate {

        curr_x++
        if (curr_x < upper_x) {
            this.currCoord!!.quickSet(ImageCoordinate.X, curr_x)
        } else {
            curr_x = lower_x
            curr_y++
            if (curr_y < upper_y) {
                this.currCoord!!.quickSet(ImageCoordinate.X, curr_x)
                this.currCoord!!.quickSet(ImageCoordinate.Y, curr_y)
            } else {
                curr_y = lower_y
                curr_z++
                if (curr_z < upper_z) {
                    this.currCoord!!.quickSet(ImageCoordinate.X, curr_x)
                    this.currCoord!!.quickSet(ImageCoordinate.Y, curr_y)
                    this.currCoord!!.quickSet(ImageCoordinate.Z, curr_z)
                } else {
                    curr_z = lower_z
                    curr_c++
                    if (curr_c < upper_c) {
                        this.currCoord!!.quickSet(ImageCoordinate.X, curr_x)
                        this.currCoord!!.quickSet(ImageCoordinate.Y, curr_y)
                        this.currCoord!!.quickSet(ImageCoordinate.Z, curr_z)
                        this.currCoord!!.quickSet(ImageCoordinate.C, curr_c)
                    } else {
                        curr_c = lower_c
                        curr_t++
                        this.currCoord!!.quickSet(ImageCoordinate.X, curr_x)
                        this.currCoord!!.quickSet(ImageCoordinate.Y, curr_y)
                        this.currCoord!!.quickSet(ImageCoordinate.Z, curr_z)
                        this.currCoord!!.quickSet(ImageCoordinate.C, curr_c)
                        this.currCoord!!.quickSet(ImageCoordinate.T, curr_t)
                    }
                }
            }

        }

        return this.currCoord
    }

}
