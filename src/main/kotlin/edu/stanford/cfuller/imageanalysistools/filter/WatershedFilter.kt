package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import java.util.Hashtable

/**
 * A Filter that segments an Image according to the standard Watershed method.  The Image is first inverted, so that the
 * algorithm will find regions starting from high-intensity areas (which is generally more applicable for Image segmentation).
 *
 *
 * Unlike some implementations, this does not find the gradient of the supplied Image, so if segmentation of the gradient is desired
 * it must be prefiltered.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image to be segmented by the watershed algorithm.


 * @author Colin J. Fuller
 */

open class WatershedFilter : Filter() {
    protected var seedImage: Image? = null

    /**
     * Applies the WatershedFilter to the specified Image, segmenting it.
     * @param im    The Image to be segmented.
     */
    override fun apply(im: WritableImage) {
        val imCopy = ImageFactory.createWritable(im)
        val invf = InversionFilter()
        invf.apply(imCopy)
        val h = Histogram(imCopy)
        val greylevelLookup = Hashtable<Double, java.util.Vector<Vector3D>>()

        for (ic in imCopy) {
            val value = imCopy.getValue(ic).toDouble()
            if (greylevelLookup[value] == null) {
                greylevelLookup.put(value, java.util.Vector<Vector3D>())
            }
            greylevelLookup[value]!!.add(
                    Vector3D(ic[ImageCoordinate.X].toDouble(), ic[ImageCoordinate.Y].toDouble(), ic[ImageCoordinate.Z].toDouble()))
        }

        val processing = getSeedImage(greylevelLookup, imCopy, h)
        val hSeed = Histogram(processing)
        var nextLabel = hSeed.maxValue + 1
        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in h.minValue + 1..h.maxValue - 1) {
            if (h.getCounts(i) == 0) continue
            for (v in greylevelLookup[i.toDouble()]!!) {
                val x = v.x.toInt()
                val y = v.y.toInt()
                val z = v.z.toInt()

                ic[ImageCoordinate.X] = x
                ic[ImageCoordinate.Y] = y
                ic[ImageCoordinate.Z] = z

                val label = getCorrectLabel(ic, processing, nextLabel)
                processing.setValue(ic, label.toFloat())
                if (label == nextLabel) nextLabel++
            }
        }
        ic.recycle()
        val mf = MaskFilter()
        mf.referenceImage = processing
        mf.apply(im)
        val lf = LabelFilter()
        lf.apply(im)
    }

    /**
     * Gets the seed Image for the watershed segmentation.  If no seed Image has been set externally, one is created from
     * the set of pixels at the lowest greylevel.  If a seed Image has been set externally, the seedImage retrieved is the external one.
     *
     * @param greylevelLookup       A hashtable mapping greylevel values in the Image to coordinates in the Image.
     * @param im                    The Image being segmented.
     * @param h                     A Histogram of the Image being segmented, constructed before the beginning of segmentation.
     * @return                      An Image containing the seeds for the watershed algorithm.
     */
    protected fun getSeedImage(greylevelLookup: java.util.Hashtable<Double, java.util.Vector<Vector3D>>, im: Image, h: Histogram): WritableImage {
        // TODO(colin): proper seed image null handling
        if (this.seedImage != null) {
            return ImageFactory.createWritable(this.seedImage!!)
        }

        var tempSeed: WritableImage? = null

        if (this.seedImage == null) {
            tempSeed = ImageFactory.createWritable(im.dimensionSizes, 0.0f)
        } else {
            tempSeed = ImageFactory.createWritable(this.seedImage!!)
        }
        val minValue = h.minValue.toDouble()
        val minPoints = greylevelLookup[minValue]!!
        val ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        for (v in minPoints) {
            ic[ImageCoordinate.X] = v.x.toInt()
            ic[ImageCoordinate.Y] = v.y.toInt()
            ic[ImageCoordinate.Z] = v.z.toInt()
            tempSeed.setValue(ic, 1f)
        }
        ic.recycle()
        val lf = LabelFilter()
        lf.apply(tempSeed)
        if (this.seedImage != null) this.seedImage = tempSeed
        return tempSeed
    }

    /**
     * Gets the correct labeling in the segmentation for a pixel at a given coordinate, given a segmentation in progress.
     * This will label a pixel as 0 (a barrier) if it would connect two existing regions, label it as an existing region if it is connected
     * only to that region, or as nextLabel if it is not connected to any existing region.

     * @param ic            The ImageCoordinate that is the location to be labeled.
     * *
     * @param processing    The partial segmentation of the Image.
     * *
     * @param nextLabel     The next available label for new regions.
     * *
     * @return              The appropriate value for the ImageCoordinate: one of 0, an existing region number, or nextLabel.
     */
    protected open fun getCorrectLabel(ic: ImageCoordinate, processing: WritableImage, nextLabel: Int): Int {
        val x = ic[ImageCoordinate.X]
        val y = ic[ImageCoordinate.Y]
        val currValue = processing.getValue(ic).toDouble()

        if (currValue > 0) {
            return currValue.toInt()
        }
        //check 8-connected neighbors in the plane
        var neighbor = 0
        val ic2 = ImageCoordinate.cloneCoord(ic)
        ic2[ImageCoordinate.X] = x - 1
        ic2[ImageCoordinate.Y] = y - 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x - 1
        ic2[ImageCoordinate.Y] = y
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x - 1
        ic2[ImageCoordinate.Y] = y + 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x
        ic2[ImageCoordinate.Y] = y - 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x
        ic2[ImageCoordinate.Y] = y + 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x + 1
        ic2[ImageCoordinate.Y] = y - 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x + 1
        ic2[ImageCoordinate.Y] = y
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        ic2[ImageCoordinate.X] = x + 1
        ic2[ImageCoordinate.Y] = y + 1
        if (processing.inBounds(ic2)) {
            val tempNeighbor = processing.getValue(ic2).toInt()
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {
                ic2.recycle()
                return 0
            }
            neighbor = tempNeighbor
        }

        if (neighbor > 0) {
            ic2.recycle()
            return neighbor
        }
        ic2.recycle()
        return nextLabel
    }
}
