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
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A Filter that labels connected regions in a 3D Image mask.
 *
 *
 * Connected regions are defined as positive-valued regions of an Image that are 4-connected (2D) or 6-connected (3D).  Zero-valued regions
 * separate the connected regions.  Regions will be labeled consecutively, starting with 1 for the first connected region.  Though
 * labels will tend to be numbered top to bottom and left to right, no particular ordering of regions is guaranteed.  Labeling the
 * same original Image multiple times, however, will produce the same result each time.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the mask to be labeled.


 * @author Colin J. Fuller
 */
class Label3DFilter : Filter() {


    /**
     * Applies the LabelFilter to an Image.
     * @param im    The Image mask that will end up with 6-connected regions labeled.
     */
    override fun apply(im: WritableImage) {

        val preliminaryLabeledImage = ImageFactory.createWritable(im.dimensionSizes, 0.0f)
        var labelCounter = 1

        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)


        for (i in im) {

            if (Math.floor(im.getValue(i).toDouble()) > 0) {

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X) - 1)
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                updateLabeling(preliminaryLabeledImage, i, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + 1)
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                updateLabeling(preliminaryLabeledImage, i, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) - 1)
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                updateLabeling(preliminaryLabeledImage, i, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + 1)
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                updateLabeling(preliminaryLabeledImage, i, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) - 1)
                updateLabeling(preliminaryLabeledImage, i, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + 1)
                updateLabeling(preliminaryLabeledImage, i, ic)


                if (preliminaryLabeledImage.getValue(i) == 0f) {
                    preliminaryLabeledImage.setValue(i, labelCounter++.toFloat())
                }

            }

        }


        val labelMapping = IntArray(labelCounter)
        val finalLabelMapping = IntArray(labelCounter)

        for (i in 0..labelCounter - 1) {
            labelMapping[i] = i
            finalLabelMapping[i] = 0
        }

        for (i in im) {
            val currValue = preliminaryLabeledImage.getValue(i).toInt()
            if (currValue > 0) {

                var mappedCurrValue = currValue

                while (mappedCurrValue != labelMapping[currValue]) {
                    mappedCurrValue = labelMapping[currValue]
                }

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X) - 1)
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + 1)
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) - 1)
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + 1)
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z))
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) - 1)
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

                ic.set(ImageCoordinate.X, i.get(ImageCoordinate.X))
                ic.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y))
                ic.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + 1)
                mapRegions(currValue, mappedCurrValue, labelMapping, preliminaryLabeledImage, ic)

            }
        }

        ic.recycle()

        for (i in 0..labelCounter - 1) {
            var currLabel = i
            while (currLabel != labelMapping[currLabel]) currLabel = labelMapping[currLabel]
            finalLabelMapping[i] = currLabel
        }

        for (i in im) {
            im.setValue(i, finalLabelMapping[preliminaryLabeledImage.getValue(i).toInt()].toFloat())
        }


    }

    private fun updateLabeling(preliminaryLabeledImage: WritableImage, i: ImageCoordinate, ic: ImageCoordinate) {
        if (preliminaryLabeledImage.inBounds(ic) && preliminaryLabeledImage.getValue(ic) > 0) {
            preliminaryLabeledImage.setValue(i, preliminaryLabeledImage.getValue(ic))
        }
    }

    private fun mapRegions(currValue: Int, mappedCurrValue: Int, labelMapping: IntArray, preliminaryLabeledImage: Image, ic: ImageCoordinate) {
        if (preliminaryLabeledImage.inBounds(ic) && preliminaryLabeledImage.getValue(ic) > 0) {
            var otherValue = preliminaryLabeledImage.getValue(ic).toInt()

            if (otherValue != currValue && otherValue != mappedCurrValue) {
                while (otherValue != labelMapping[otherValue]) {
                    otherValue = labelMapping[otherValue]
                }

                if (otherValue != mappedCurrValue) {
                    if (otherValue < mappedCurrValue) {
                        labelMapping[mappedCurrValue] = otherValue
                    } else {
                        labelMapping[otherValue] = mappedCurrValue
                    }
                }
            }
        }
    }

}

