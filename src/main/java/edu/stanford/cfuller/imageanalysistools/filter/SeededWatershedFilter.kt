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

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A seeded version of the [WatershedFilter].  This Filter differs from the standard WatershedFilter in that it will start
 * from a specified seed Image, rather than just from the lowest intensity pixels, and it will construct a barrier between two regions only if
 * those regions each contain a seed.

 * @author Colin J. Fuller
 */

class SeededWatershedFilter : WatershedFilter(), SeededFilter {

    internal var maxSeedLabel = 0

    internal var flaggedForMerge: Boolean = false

    internal var mergeTable: java.util.HashMap<Int, Int>
    internal var mergeRegions: java.util.HashSet<Int>

    init {
        this.mergeTable = java.util.HashMap<Int, Int>()
        this.mergeRegions = java.util.HashSet<Int>()
    }

    /**
     * Sets the seed Image to the specified Image.  This will not be modified.
     * @param im    The seed Image to be used as the starting point for the watershed algorithm.
     */
    override fun setSeed(im: Image) {
        this.seedImage = im
        val h = Histogram(im)
        this.maxSeedLabel = h.maxValue
        this.flaggedForMerge = false

    }

    protected fun isOnBoundary(neighbor: Int, tempNeighbor: Int): Boolean {
        return neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel
    }

    protected fun needsMerge(neighbor: Int, tempNeighbor: Int): Boolean {
        return neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0
    }

    protected fun followMergeChain(toChain: Int): Int {
        if (!this.mergeTable.containsKey(toChain) || this.mergeTable[toChain] === toChain) {
            return toChain
        } else {
            return followMergeChain(this.mergeTable[toChain])
        }
    }

    protected fun doMerge(processing: WritableImage, toMerge: java.util.HashSet<Int>) {
        var min = Integer.MAX_VALUE
        for (i in toMerge) {
            if (i < min) {
                min = i
            }
        }
        min = followMergeChain(min)
        for (i in toMerge) {
            this.mergeTable.put(i, min)
        }
        this.flaggedForMerge = false
    }

    /**
     * Reimplements the method for getting the correct label for a specified pixel that is present in the WatershedFilter
     * in order to only construct a barrier between regions when those regions contain a seed region.
     * @param ic    The ImageCoordinate that this method finds the correct label for.
     * *
     * @param processing    The Image mask that is being created by the watershed algorithm.
     * *
     * @param nextLabel     The next label available for a new region, in case this pixel should start a new region.
     * *
     * @return              The correct label for the specified coordinate... 0 if it should be a barrier pixel, the label of an existing region
     * *                      if it belongs in that region, or nextLabel if it should start a new region.
     */
    override fun getCorrectLabel(ic: ImageCoordinate, processing: WritableImage, nextLabel: Int): Int {

        mergeRegions.clear()

        val x = ic.get(ImageCoordinate.X)
        val y = ic.get(ImageCoordinate.Y)

        val currValue = processing.getValue(ic).toDouble()

        if (currValue > 0) {
            return currValue.toInt()
        }

        //check 8-connected neighbors in the plane

        var neighbor = 0

        val ic2 = ImageCoordinate.cloneCoord(ic)
        var lowerInBounds = false
        var upperInBounds = false
        val bothInBounds = lowerInBounds && upperInBounds

        ic2.set(ImageCoordinate.X, x - 1)
        ic2.set(ImageCoordinate.Y, y - 1)

        if (processing.inBounds(ic2)) {
            lowerInBounds = true
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x + 1)
        ic2.set(ImageCoordinate.Y, y + 1)

        if (processing.inBounds(ic2)) {
            upperInBounds = true
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x - 1)
        ic2.set(ImageCoordinate.Y, y)

        if (lowerInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x - 1)
        ic2.set(ImageCoordinate.Y, y + 1)

        if (bothInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x)
        ic2.set(ImageCoordinate.Y, y - 1)

        if (lowerInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x)
        ic2.set(ImageCoordinate.Y, y + 1)

        if (upperInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x + 1)
        ic2.set(ImageCoordinate.Y, y - 1)

        if (bothInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        ic2.set(ImageCoordinate.X, x + 1)
        ic2.set(ImageCoordinate.Y, y)

        if (upperInBounds || processing.inBounds(ic2)) {
            val tempNeighbor = followMergeChain(processing.getValue(ic2).toInt())
            if (isOnBoundary(neighbor, tempNeighbor)) {
                ic2.recycle()
                return 0
            }
            if (needsMerge(neighbor, tempNeighbor)) {
                this.flaggedForMerge = true
                mergeRegions.add(neighbor)
                mergeRegions.add(tempNeighbor)
            }
            if (neighbor <= 0 || tempNeighbor < neighbor && tempNeighbor > 0) neighbor = tempNeighbor
        }

        if (neighbor > 0) {
            ic2.recycle()
            if (this.flaggedForMerge) {
                doMerge(processing, mergeRegions)
            }
            return neighbor
        }

        ic2.recycle()
        return nextLabel

    }


}
