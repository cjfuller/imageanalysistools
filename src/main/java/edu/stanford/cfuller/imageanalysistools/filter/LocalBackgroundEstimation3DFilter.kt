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
        val referenceImage = this.referenceImage ?:
                throw ReferenceImageRequiredException("LocalBackgroundEstimation3DFilter requires a reference image.")

        val boxMin = ImageCoordinate.cloneCoord(referenceImage.dimensionSizes)
        for (s in boxMin) {
            boxMin[s] = 0
        }

        val boxMax = ImageCoordinate.cloneCoord(referenceImage.dimensionSizes)
        var first = true
        val counts = ImageFactory.createWritable(referenceImage.dimensionSizes, 0.0f)
        val lastCoordinate = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in im) {
            if (first) {
                first = false
                boxMin[ImageCoordinate.X] = i[ImageCoordinate.X] - boxSize
                boxMin[ImageCoordinate.Y] = i[ImageCoordinate.Y] - boxSize
                boxMin[ImageCoordinate.Z] = i[ImageCoordinate.Z] - boxSize

                boxMax[ImageCoordinate.X] = i[ImageCoordinate.X] + boxSize + 1
                boxMax[ImageCoordinate.Y] = i[ImageCoordinate.Y] + boxSize + 1
                boxMax[ImageCoordinate.Z] = i[ImageCoordinate.Z] + boxSize + 1

                referenceImage.setBoxOfInterest(boxMin, boxMax)

                var total = 0f
                var count = 0

                for (iBox in referenceImage) {
                    total += referenceImage.getValue(iBox)
                    count++
                }

                im.setValue(i, total / count)
                counts.setValue(i, count.toFloat())
                referenceImage.clearBoxOfInterest()
            } else {
                lastCoordinate.setCoord(i)
                val x = i[ImageCoordinate.X]
                val y = i[ImageCoordinate.Y]
                val z = i[ImageCoordinate.Z]
                var sum = 0f
                var count = 0
                if (x > 0) {
                    lastCoordinate[ImageCoordinate.X] = x - 1
                    count = counts.getValue(lastCoordinate).toInt()
                    sum = im.getValue(lastCoordinate) * count

                    boxMin[ImageCoordinate.X] = x - boxSize - 1
                    boxMin[ImageCoordinate.Y] = y - boxSize
                    boxMin[ImageCoordinate.Z] = z - boxSize

                    boxMax[ImageCoordinate.X] = x - boxSize
                    boxMax[ImageCoordinate.Y] = i[ImageCoordinate.Y] + boxSize + 1
                    boxMax[ImageCoordinate.Z] = i[ImageCoordinate.Z] + boxSize + 1

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage!!) {
                        sum -= referenceImage.getValue(iBox)
                        count--
                    }

                    referenceImage.clearBoxOfInterest()

                    boxMin[ImageCoordinate.X] = x + boxSize
                    boxMax[ImageCoordinate.X] = x + boxSize + 1

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage) {
                        try {
                            sum += referenceImage.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(referenceImage.dimensionSizes)
                            System.err.println(referenceImage.boxMax)
                            System.err.println(referenceImage.boxMin)
                            throw e
                        }
                    }

                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())
                    referenceImage.clearBoxOfInterest()
                } else if (y > 0) {
                    lastCoordinate[ImageCoordinate.Y] = y - 1
                    count = counts.getValue(lastCoordinate).toInt()
                    sum = im.getValue(lastCoordinate) * count

                    boxMin[ImageCoordinate.X] = x - boxSize
                    boxMin[ImageCoordinate.Y] = y - boxSize - 1
                    boxMin[ImageCoordinate.Z] = z - boxSize

                    boxMax[ImageCoordinate.X] = i[ImageCoordinate.X] + boxSize + 1
                    boxMax[ImageCoordinate.Y] = y - boxSize
                    boxMax[ImageCoordinate.Z] = i[ImageCoordinate.Z] + boxSize + 1

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage) {
                        sum -= referenceImage.getValue(iBox)
                        count--
                    }

                    referenceImage.clearBoxOfInterest()

                    boxMin[ImageCoordinate.Y] = y + boxSize
                    boxMax[ImageCoordinate.Y] = y + boxSize + 1

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage) {
                        try {
                            sum += referenceImage.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(referenceImage.dimensionSizes)
                            System.err.println(referenceImage.boxMax)
                            System.err.println(referenceImage.boxMin)
                            throw e
                        }
                    }

                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())
                    referenceImage.clearBoxOfInterest()
                } else if (z > 0) {
                    lastCoordinate[ImageCoordinate.Z] = z - 1
                    count = counts.getValue(lastCoordinate).toInt()
                    sum = im.getValue(lastCoordinate) * count

                    boxMin[ImageCoordinate.X] = x - boxSize
                    boxMin[ImageCoordinate.Y] = y - boxSize
                    boxMin[ImageCoordinate.Z] = z - boxSize - 1

                    boxMax[ImageCoordinate.X] = i[ImageCoordinate.X] + boxSize + 1
                    boxMax[ImageCoordinate.Y] = i[ImageCoordinate.Y] + boxSize + 1
                    boxMax[ImageCoordinate.Z] = z - boxSize

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage) {
                        sum -= referenceImage.getValue(iBox)
                        count--
                    }

                    referenceImage.clearBoxOfInterest()

                    boxMin[ImageCoordinate.Z] = z + boxSize
                    boxMax[ImageCoordinate.Z] = z + boxSize + 1

                    referenceImage.setBoxOfInterest(boxMin, boxMax)

                    for (iBox in referenceImage) {
                        try {
                            sum += referenceImage.getValue(iBox)
                            count++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            System.err.println(iBox)
                            System.err.println(referenceImage.dimensionSizes)
                            System.err.println(referenceImage.boxMax)
                            System.err.println(referenceImage.boxMin)
                            throw e
                        }
                    }
                    im.setValue(i, sum / count)
                    counts.setValue(i, count.toFloat())
                    referenceImage.clearBoxOfInterest()
                }
            }
        }
        lastCoordinate.recycle()
        boxMin.recycle()
        boxMax.recycle()
    }
}
