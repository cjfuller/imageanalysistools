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
import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter
import edu.stanford.cfuller.imageanalysistools.filter.FillFilter
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * A Filter that computes a convex hull for each region in a specified Image and converts each region to its filled convex hull.
 *
 *
 * Each region must be labeled individually, but regions do not have to be contiguous.  Also, I'm not totally sure what will happen with regions
 * that do not overlap but whose convex hulls would overlap.
 *
 *
 * This filter does not require a reference Image.
 *
 *
 * The Image parameter to the apply function should be set to a mask whose regions will be replaced by their filled convex hulls.
 *
 *
 * This filter should only be applied to 2D (x-y) images.  (That is, images with singleton z, c, t dimensions.)

 * @author Colin J. Fuller
 */


class ConvexHullByLabelFilter : Filter() {

    /*note that in > 2D images, this will probably not do what you expect... I suspect it will most closely
	 * approximate taking the union of the convex hulls in all planes and applying that to the first plane.
	*/

    /**
     * Applies the convex hull filter to the supplied mask.
     * @param im    The Image to process-- a mask whose regions will be replaced by their filled convex hulls.
     */
    override fun apply(im: WritableImage) {

        val RLF = RelabelFilter()

        RLF.apply(im)

        val h = Histogram(im)

        val xLists = java.util.Hashtable<Int, java.util.Vector<Int>>()
        val yLists = java.util.Hashtable<Int, java.util.Vector<Int>>()

        val minValues = java.util.Vector<Int>(h.maxValue + 1)
        val minIndices = java.util.Vector<Int>(h.maxValue + 1)

        for (i in 0..h.maxValue + 1 - 1) {
            minValues.add(im.dimensionSizes.get(ImageCoordinate.X))
            minIndices.add(0)
        }

        for (i in im) {

            val value = im.getValue(i).toInt()

            if (value == 0) continue

            if (!xLists.containsKey(value)) {
                xLists.put(value, java.util.Vector<Int>())
                yLists.put(value, java.util.Vector<Int>())
            }

            xLists[value].add(i.get(ImageCoordinate.X))
            yLists[value].add(i.get(ImageCoordinate.Y))

            if (i.get(ImageCoordinate.X) < minValues[value]) {
                minValues[value] = i.get(ImageCoordinate.X)
                minIndices[value] = xLists[value].size - 1
            }

        }

        val hullPointsX = java.util.Vector<Int>()
        val hullPointsY = java.util.Vector<Int>()

        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (k in 1..h.maxValue + 1 - 1) {
            hullPointsX.clear()
            hullPointsY.clear()

            val xList = xLists[k]
            val yList = yLists[k]

//start at the leftmost point

            var currentIndex = minIndices[k]
            var currentX = xList.get(currentIndex)
            var currentY = yList.get(currentIndex)

            hullPointsX.add(currentX)
            hullPointsY.add(currentY)

            val angles = org.apache.commons.math3.linear.ArrayRealVector(xList.size)


            var currentVector = Vector3D(0.0, -1.0, 0.0)

            val visited = java.util.HashSet<Int>()

            do {

                visited.add(currentIndex)

                var maxIndex = 0
                var maxAngle = -2 * Math.PI
                var dist = java.lang.Double.MAX_VALUE
                for (i in xList.indices) {
                    if (i == currentIndex) continue
                    val next = Vector3D((xList.get(i) - xList.get(currentIndex)).toDouble(), (yList.get(i) - yList.get(currentIndex)).toDouble(), 0.0)

                    val angle = Vector3D.angle(currentVector, next)
                    angles.setEntry(i, angle)
                    if (angle > maxAngle) {
                        maxAngle = angle
                        maxIndex = i
                        dist = next.norm
                    } else if (angle == maxAngle) {
                        val tempDist = next.norm
                        if (tempDist < dist) {
                            dist = tempDist
                            maxAngle = angle
                            maxIndex = i
                        }
                    }
                }



                currentX = xList.get(maxIndex)
                currentY = yList.get(maxIndex)

                currentVector = Vector3D((xList.get(currentIndex) - currentX).toDouble(), (yList.get(currentIndex) - currentY).toDouble(), 0.0)

                hullPointsX.add(currentX)
                hullPointsY.add(currentY)

                currentIndex = maxIndex

            } while (!visited.contains(currentIndex))

            //hull vertices have now been determined .. need to fill in the lines
            //between them so I can apply a fill filter

            //approach: x1, y1 to x0, y0:
            //start at min x, min y, go to max x, max y
            // if x_i, y_i = x0, y0  + slope to within 0.5 * sqrt(2), then add to hull

            val eps = Math.sqrt(2.0)

            for (i in 0..hullPointsX.size - 1 - 1) {

                val x0 = hullPointsX[i]
                val y0 = hullPointsY[i]

                var x1 = hullPointsX[i + 1]
                var y1 = hullPointsY[i + 1]

                val xmin = if (x0 < x1) x0 else x1
                val ymin = if (y0 < y1) y0 else y1
                val xmax = if (x0 > x1) x0 else x1
                val ymax = if (y0 > y1) y0 else y1

                x1 -= x0
                y1 -= y0

                val denom = (x1 * x1 + y1 * y1).toDouble()


                for (x in xmin..xmax) {
                    for (y in ymin..ymax) {

                        val rel_x = x - x0
                        val rel_y = y - y0

                        val projLength = (x1 * rel_x + y1 * rel_y) / denom

                        val projPoint_x = x1 * projLength
                        val projPoint_y = y1 * projLength

                        if (Math.hypot(rel_x - projPoint_x, rel_y - projPoint_y) < eps) {
                            ic.set(ImageCoordinate.X, x)
                            ic.set(ImageCoordinate.Y, y)
                            im.setValue(ic, k.toFloat())
                        }

                    }
                }

            }

        }

        ic.recycle()

        val ff = FillFilter()

        ff.apply(im)

    }

}
