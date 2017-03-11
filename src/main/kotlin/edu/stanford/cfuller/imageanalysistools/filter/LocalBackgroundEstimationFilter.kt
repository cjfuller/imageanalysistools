package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.RealMatrix

/**
 * A Filter that estimates the background locally in an Image, using a local median filtering approach.
 *
 * This filter may be useful for determining and correcting for local intensity variations.
 *
 * The reference image should be set to the Image that is to be median filtered.  This Image will not be changed.
 *
 * The argument to the apply method should be any Image (except a shallow copy of the reference Image) of the same dimensions as the reference Image.
 * The median filtered Image will be written to this Image.

 * @author Colin J. Fuller
 */
open class LocalBackgroundEstimationFilter : Filter() {
    internal var boxSize: Int = 0

    /**
     * Constructs a LocalBackgroundEstimationFilter with a default size.
     */
    init {
        this.boxSize = 25
    }

    /**
     * Sets the size of the box used for local median calculations.
     * @param boxSize   The radius of the box (the final box will be 2*boxSize + 1 square).
     */
    fun setBoxSize(boxSize: Int) {
        this.boxSize = boxSize
    }

    /**
     * Applies the LocalBackgroundEstimationFilter to an Image.
     * @param im    The Image that will be replaced by the output Image.  This can be anything of the correct dimensions except a shallow copy of the reference Image.
     */
    override fun apply(im: WritableImage) {
        var referenceImage = this.referenceImage
        if (referenceImage == null) {
            throw ReferenceImageRequiredException("LocalBackgroundEstimationFilter requires a reference image.")
        }
        val h = edu.stanford.cfuller.imageanalysistools.image.Histogram(referenceImage)
        val numPixelsInBox = boxSize * boxSize
        val ic = referenceImage.dimensionSizes

        val icnew = ImageCoordinate.createCoordXYZCT(
                ic[ImageCoordinate.X] + 2 * boxSize,
                ic[ImageCoordinate.Y] + 2 * boxSize,
                ic[ImageCoordinate.Z],
                ic[ImageCoordinate.C],
                ic[ImageCoordinate.T])

        val padded = ImageFactory.createWritable(icnew, -1.0f)
        var maxValue = 0f

        for (i in referenceImage) {
            icnew.quickSet(ImageCoordinate.X, i.quickGet(ImageCoordinate.X) + boxSize)
            icnew.quickSet(ImageCoordinate.Y, i.quickGet(ImageCoordinate.Y) + boxSize)
            icnew.quickSet(ImageCoordinate.Z, i.quickGet(ImageCoordinate.Z))
            icnew.quickSet(ImageCoordinate.C, i.quickGet(ImageCoordinate.C))
            icnew.quickSet(ImageCoordinate.T, i.quickGet(ImageCoordinate.T))

            padded.setValue(icnew, referenceImage.getValue(i))
            if (referenceImage.getValue(i) > maxValue) maxValue = referenceImage!!.getValue(i)
        }

        val overallCounts = org.apache.commons.math3.linear.ArrayRealVector(h.maxValue + 1)
        var countsByRow: RealMatrix = org.apache.commons.math3.linear.Array2DRowRealMatrix(2 * boxSize + 1, h.maxValue + 1)

        //loop over columns
        for (i in boxSize..im.dimensionSizes.quickGet(ImageCoordinate.X) + boxSize - 1) {
            overallCounts.mapMultiplyToSelf(0.0)
            val overallCounts_a = overallCounts.toArray()
            countsByRow = countsByRow.scalarMultiply(0.0)
            val countsByRow_a = countsByRow.data
            var countsByRow_rowZero_pointer = 0

            for (m in i - boxSize..i + boxSize) {
                for (n in 0..2 * boxSize + 1 - 1) {
                    icnew.quickSet(ImageCoordinate.X, m)
                    icnew.quickSet(ImageCoordinate.Y, n)
                    val value = padded.getValue(icnew).toInt()
                    if (value == -1) continue
                    overallCounts_a[value]++
                    countsByRow_a[(n + countsByRow_rowZero_pointer) % countsByRow.rowDimension][value]++
                }
            }
            var currMedian = 0
            var runningSum = 0
            var k = 0
            while (runningSum < numPixelsInBox shr 1) {
                runningSum += overallCounts_a[k++].toInt()
            }
            runningSum -= overallCounts_a[k - 1].toInt()
            currMedian = k - 1

            icnew.quickSet(ImageCoordinate.X, i - boxSize)
            icnew.quickSet(ImageCoordinate.Y, 0)
            im.setValue(icnew, currMedian.toFloat())
            val num_rows = countsByRow.rowDimension
            for (j in boxSize + 1..im.dimensionSizes.quickGet(ImageCoordinate.Y) + boxSize - 1) {
                val toRemove = countsByRow_a[countsByRow_rowZero_pointer % num_rows]
                for (oc_counter in overallCounts_a.indices) {
                    overallCounts_a[oc_counter] -= toRemove[oc_counter]
                }

                for (c in toRemove.indices) {
                    if (c < currMedian) {
                        runningSum -= toRemove[c].toInt()
                    }
                    countsByRow_a[countsByRow_rowZero_pointer % num_rows][c] *= 0.0
                }
                countsByRow_rowZero_pointer++

                for (c in i - boxSize..i + boxSize) {
                    icnew.quickSet(ImageCoordinate.X, c)
                    icnew.quickSet(ImageCoordinate.Y, j + boxSize)
                    val value = padded.getValue(icnew).toInt()
                    if (value < 0) continue
                    countsByRow_a[(countsByRow_rowZero_pointer + num_rows - 1) % num_rows][value] += 1.0
                    overallCounts_a[value]++
                    if (value < currMedian) {
                        runningSum++
                    }
                }

                //case 1: runningSum > half of box
                if (runningSum > numPixelsInBox shr 1) {
                    k = currMedian - 1
                    while (runningSum > numPixelsInBox shr 1) {
                        runningSum -= overallCounts_a[k--].toInt()
                    }
                    currMedian = k + 1

                // case 2: runningSum < half of box
                } else if (runningSum < numPixelsInBox shr 1) {
                    k = currMedian
                    while (runningSum < numPixelsInBox shr 1) {
                        runningSum += overallCounts_a[k++].toInt()
                    }
                    currMedian = k - 1
                    runningSum -= overallCounts_a[k - 1].toInt()
                }

                //case 3: spot on, do nothing
                icnew.quickSet(ImageCoordinate.X, i - boxSize)
                icnew.quickSet(ImageCoordinate.Y, j - boxSize)
                im.setValue(icnew, currMedian.toFloat())
            }
        }
        icnew.recycle()
    }

    companion object {
        protected fun swap(first: Int, second: Int, toProcess: RealVector) {
            val value = toProcess.getEntry(first)
            toProcess.setEntry(first, toProcess.getEntry(second))
            toProcess.setEntry(second, value)
        }

        /**
         * Finds the kth item sorted by increasing value in a possibly unsorted vector.
         *
         * This will likely not completely sort the vector, but will almost certainly
         * change the order of the items in the vector in place.
         * @param k            The index of the item (in the sorted vector) to find.
         * @param toFind    The RealVector in which to find the kth item.
         * @return            The value of the kth item (in the sorted vector).
         */
        fun quickFindKth(k: Int, toFind: RealVector): Double {
            val n = toFind.dimension
            var l = 0
            var ir = n - 1

            while (true) {
                if (ir <= l + 1) {
                    if (ir == l + 1 && toFind.getEntry(ir) < toFind.getEntry(l)) {
                        swap(ir, l, toFind)
                    }
                    return toFind.getEntry(k)
                } else {
                    val mid = l + ir shr 1
                    swap(mid, l + 1, toFind)
                    if (toFind.getEntry(l) > toFind.getEntry(ir)) swap(l, ir, toFind)
                    if (toFind.getEntry(l + 1) > toFind.getEntry(ir)) swap(l + 1, ir, toFind)
                    if (toFind.getEntry(l) > toFind.getEntry(l + 1)) swap(l, l + 1, toFind)
                    var i = l + 1
                    var j = ir
                    val a = toFind.getEntry(l + 1)
                    while (true) {
                        do {
                            i++
                        } while (toFind.getEntry(i) < a)
                        do {
                            j--
                        } while (toFind.getEntry(j) > a)
                        if (j < i) break
                        swap(i, j, toFind)
                    }
                    toFind.setEntry(l + 1, toFind.getEntry(j))
                    toFind.setEntry(j, a)
                    if (j >= k) ir = j - 1
                    if (j <= k) l = i
                }
            }
        }
    }
}
