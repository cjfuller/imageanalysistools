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
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory


/**
 * A Filter that estimates the background locally in an Image, using a local mean filtering approach.  It is intended for use on three-dimensional images.
 *
 *
 * This filter may be useful for determining and correcting for local intensity variations.
 *
 *
 * The reference image should be set to the Image that is to be median filtered.  This Image will not be changed.
 *
 *
 * The argument to the apply method should be any Image (except a shallow copy of the reference Image) of the same dimensions as the reference Image.
 * The mean filtered Image will be written to this Image.
 *
 *

 * @author Colin J. Fuller
 */
class LocalBackgroundEstimation3DFilter : LocalBackgroundEstimationFilter() {

    /**
     * Applies the LocalBackgroundEstimationFilter to an Image.
     * @param im    The Image that will be replaced by the output Image.  This can be anything of the correct dimensions except a shallow copy of the reference Image.
     */
    override fun apply(im: WritableImage) {

        if (this.referenceImage == null) {
            throw ReferenceImageRequiredException("LocalBackgroundEstimation3DFilter requires a reference image.")
        }

        val boxMin = ImageCoordinate.cloneCoord(this.referenceImage!!.dimensionSizes)
        for (s in boxMin) {
            boxMin.set(s!!, 0)
        }

        val boxMax = ImageCoordinate.cloneCoord(this.referenceImage!!.dimensionSizes)

        var first = true

        val counts = ImageFactory.createWritable(this.referenceImage!!.dimensionSizes, 0.0f)

        val lastCoordinate = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in im) {

            if (first) {

                first = false

                boxMin.set(ImageCoordinate.X, i.get(ImageCoordinate.X) - boxSize)
                boxMin.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) - boxSize)
                boxMin.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) - boxSize)

                boxMax.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + boxSize + 1)
                boxMax.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + boxSize + 1)
                boxMax.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + boxSize + 1)

                this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                var total = 0f
                var count = 0

                for (iBox in this.referenceImage!!) {

                    total += this.referenceImage!!.getValue(iBox)
                    count++

                }

                im.setValue(i, total / count)
                counts.setValue(i, count.toFloat())

                this.referenceImage!!.clearBoxOfInterest()


            } else {

                lastCoordinate.setCoord(i)

                val x = i.get(ImageCoordinate.X)
                val y = i.get(ImageCoordinate.Y)
                val z = i.get(ImageCoordinate.Z)

                var sum = 0f

                var count = 0

                if (x > 0) {

                    lastCoordinate.set(ImageCoordinate.X, x - 1)

                    count = counts.getValue(lastCoordinate).toInt()

                    sum = im.getValue(lastCoordinate) * count

                    boxMin.set(ImageCoordinate.X, x - boxSize - 1)
                    boxMin.set(ImageCoordinate.Y, y - boxSize)
                    boxMin.set(ImageCoordinate.Z, z - boxSize)

                    boxMax.set(ImageCoordinate.X, x - boxSize)
                    boxMax.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + boxSize + 1)
                    boxMax.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + boxSize + 1)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {

                        sum -= this.referenceImage!!.getValue(iBox)
                        count--

                    }

                    this.referenceImage!!.clearBoxOfInterest()

                    boxMin.set(ImageCoordinate.X, x + boxSize)
                    boxMax.set(ImageCoordinate.X, x + boxSize + 1)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {
                        try {
                            sum += this.referenceImage!!.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(this.referenceImage!!.dimensionSizes)
                            System.err.println(this.referenceImage!!.boxMax)
                            System.err.println(this.referenceImage!!.boxMin)
                            throw e
                        }

                    }

                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())

                    this.referenceImage!!.clearBoxOfInterest()

                } else if (y > 0) {

                    lastCoordinate.set(ImageCoordinate.Y, y - 1)

                    count = counts.getValue(lastCoordinate).toInt()

                    sum = im.getValue(lastCoordinate) * count

                    boxMin.set(ImageCoordinate.X, x - boxSize)
                    boxMin.set(ImageCoordinate.Y, y - boxSize - 1)
                    boxMin.set(ImageCoordinate.Z, z - boxSize)

                    boxMax.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + boxSize + 1)
                    boxMax.set(ImageCoordinate.Y, y - boxSize)
                    boxMax.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + boxSize + 1)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {

                        sum -= this.referenceImage!!.getValue(iBox)
                        count--

                    }

                    this.referenceImage!!.clearBoxOfInterest()

                    boxMin.set(ImageCoordinate.Y, y + boxSize)
                    boxMax.set(ImageCoordinate.Y, y + boxSize + 1)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {
                        try {
                            sum += this.referenceImage!!.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(this.referenceImage!!.dimensionSizes)
                            System.err.println(this.referenceImage!!.boxMax)
                            System.err.println(this.referenceImage!!.boxMin)
                            throw e
                        }

                    }

                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())

                    this.referenceImage!!.clearBoxOfInterest()

                } else if (z > 0) {

                    lastCoordinate.set(ImageCoordinate.Z, z - 1)

                    count = counts.getValue(lastCoordinate).toInt()

                    sum = im.getValue(lastCoordinate) * count

                    boxMin.set(ImageCoordinate.X, x - boxSize)
                    boxMin.set(ImageCoordinate.Y, y - boxSize)
                    boxMin.set(ImageCoordinate.Z, z - boxSize - 1)

                    boxMax.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + boxSize + 1)
                    boxMax.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + boxSize + 1)
                    boxMax.set(ImageCoordinate.Z, z - boxSize)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {

                        sum -= this.referenceImage!!.getValue(iBox)
                        count--

                    }

                    this.referenceImage!!.clearBoxOfInterest()

                    boxMin.set(ImageCoordinate.Z, z + boxSize)
                    boxMax.set(ImageCoordinate.Z, z + boxSize + 1)

                    this.referenceImage!!.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in this.referenceImage!!) {
                        try {
                            sum += this.referenceImage!!.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(this.referenceImage!!.dimensionSizes)
                            System.err.println(this.referenceImage!!.boxMax)
                            System.err.println(this.referenceImage!!.boxMin)
                            throw e
                        }

                    }

                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())

                    this.referenceImage!!.clearBoxOfInterest()

                }


            }

        }

        lastCoordinate.recycle()
        boxMin.recycle()
        boxMax.recycle()


    }


}
