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

import java.io.Serializable

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * This class represents a histogram of intensity values in an Image.  The bin size is 1 (meaning that intensity values are rounded to integers).
 *
 *
 * Also calculates statistics like mean intensity, mode intensity, and intensity variance.

 * @author Colin J. Fuller
 */

class Histogram : Serializable {

    //fields

    /**
     * Gets the entire array of counts at every integer value from 0 to the maximum value in the Image used to construct the Histogram.
     *
     *
     * Do not modify the array that is returned.

     * @return  The array of counts at each value.
     */
    var countsList: IntArray
        internal set
    internal var cumulativeCounts: IntArray
    /**
     * Gets the total counts of all nonnegative values in the Image.
     *
     *
     * For Images with no negative values, this should be the same as calling [Image.size] on the Image used to construct the Histogram.
     * @return  The total number of nonnegative values in the Image.
     */
    var totalCounts: Int = 0
        internal set
    /**
     * Gets the maximum pixel value in the Image used to construct the Histogram.
     * @return The maximum pixel value (or if there are no positive values, 0).
     */
    var maxValue: Int = 0
        internal set
    /**
     * Gets the minimum nonnegative pixel value in the Image used to construct the Histogram.
     * @return The minimum pixel value.
     */
    var minValue: Int = 0
        internal set
    /**
     * Gets the minimum positive pixel value in the Images used to construct the Histogram.
     * @return  The minimum positive pixel value, or zero only if there are no positive pixels in the Image.
     */
    var minValueNonzero: Int = 0
        internal set
    /**
     * Gets the mean of all nonnegative values in the Image used to construct the Histogram.

     * @return  The mean of the nonnegative values.
     */
    var mean: Double = 0.toDouble()
        internal set
    /**
     * Gets the variance of all nonnegative values in the Image used to construct the Histogram.

     * @return  The variance of the nonnegative values.
     */
    var variance: Double = 0.toDouble()
        internal set
    /**
     * Gets the mean of all positive values in the Image used to construct the Histogram.

     * @return  The mean of the positive values.
     */
    var meanNonzero: Double = 0.toDouble()
        internal set
    /**
     * Gets the variance of all positive values in the Image used to construct the Histogram.

     * @return  The variance of the positive values.
     */
    var varianceNonzero: Double = 0.toDouble()
        internal set
    /**
     * Gets the number of counts at the positive mode value of the histogram.
     *
     *
     * If there are no positive values in the Image used to construct the Histogram, this will return 0.
     * @return  The counts at the positive mode of the Histogram, or 0 if there are no positive values.
     */
    var countsAtMode: Int = 0
        internal set
    /**
     * Gets the positive mode value of the histogram.  (That is, the positive value with the highest count.)
     *
     *
     * This will not return 0 as the mode value unless there are no positive values in the Image used to construct the Histogram, and will never return a negative number.

     * @return  The positive mode value of the histogram.
     */
    var mode: Int = 0
        internal set

    //constructors

    protected constructor() {}

    /**
     * Constructs a new Histogram for a given Image.
     *
     *
     * The Histogram will be a snapshot of the current state of the Image, so if the Image changes later, the Histogram will not be updated.

     * @param im
     */
    constructor(im: Image) {
        init(im)
    }

    //private methods

    private fun init(im: Image) {

        this.mean = 0.0
        this.variance = 0.0
        this.meanNonzero = 0.0
        this.varianceNonzero = 0.0
        this.totalCounts = 0
        this.maxValue = findMaxVal(im)
        this.countsList = IntArray(this.maxValue + 1)
        this.cumulativeCounts = IntArray(this.countsList.size)

        java.util.Arrays.fill(this.countsList, 0)
        java.util.Arrays.fill(this.cumulativeCounts, 0)

        var suppressNegativeWarning = false

        for (i in im) {
            if (im.getValue(i) < 0) {
                if (!suppressNegativeWarning) {
                    suppressNegativeWarning = true
                    LoggingUtilities.logger.warning("negative image value")
                }
                continue
            }
            this.countsList[im.getValue(i).toInt()]++
            this.totalCounts++
            this.mean += im.getValue(i).toDouble()

        }

        for (i in this.countsList.indices) {
            if (this.countsList[i] > 0) {
                this.minValue = i
                break
            }
        }
        this.minValueNonzero = this.minValue

        if (this.minValue == 0) {
            for (i in 1..this.countsList.size - 1) {
                if (this.countsList[i] > 0) {
                    this.minValueNonzero = i
                    break
                }
            }
        }

        this.mean /= this.totalCounts.toDouble()

        this.countsAtMode = 0
        this.mode = 0

        var sum = 0.0

        for (i in 1..this.countsList.size - 1) {

            sum += (countsList[i] * i).toDouble()

            this.cumulativeCounts[i] = this.cumulativeCounts[i - 1] + this.countsList[i]

            if (this.countsList[i] > this.countsAtMode) {
                this.countsAtMode = this.countsList[i]
                this.mode = i
            }

        }

        if (this.maxValue > 0) {
            this.meanNonzero = sum * 1.0 / (this.totalCounts - this.countsList[0])
        } else {
            this.meanNonzero = 0.0
        }

        for (i in this.countsList.indices) {
            this.variance += Math.pow(i - this.mean, 2.0) * this.countsList[i].toDouble() * 1.0 / this.totalCounts
            if (i > 0) {
                this.varianceNonzero += Math.pow(i - this.mean, 2.0) * this.countsList[i].toDouble() * 1.0 / (this.totalCounts - this.countsList[0])
            }
        }

    }

    /**
     * Gets the count of how many coordinates in the Image used to construct the Histogram had values that cast to the specified integer value.
     *
     *
     * Does not check bounds on the supplied value.  Ensure that the value passed in is between 0 and the result of [.getMaxValue],inclusive.
     * @param value     The value whose counts will be retrieved.
     * *
     * @return          The number of times the specified value occurred in the Image used to construct the Histogram.
     */
    fun getCounts(value: Int): Int {
        return this.countsList[value]
    }

    /**
     * Gets the cumulative count of all values in the Image up to the specified value.
     *
     *
     * The returned value is the same value as would be obtained by summing the values returned from [.getCounts] from 0 to value.

     * @param value     The value up to which to count.
     * *
     * @return          The number of times and value less than or equal to the specified value occurred in the Image used to construct the Histogram.
     */
    fun getCumulativeCounts(value: Int): Int {
        return this.cumulativeCounts[value]
    }


    /**
     * Gets a string representation of the histogram: a comma-separated list of ordered pairs
     * in the format (value, count).

     * @return The string representation.
     */
    override fun toString(): String {

        val sb = StringBuilder()

        for (i in 0..this.maxValue - 1) {
            if (sb.length > 0) {
                sb.append(",")
            }
            sb.append("(" + i + "," + this.getCounts(i) + ")")
        }

        return sb.toString()

    }

    companion object {

        internal const val serialVersionUID = 1L

        //public methods

        /**
         * Finds the maximum value of an image.
         *
         *
         * The maximum value is defined as the value at the ImageCoordinate whose corresponding Image value is greatest.  In particular
         * this makes no discrimination between different dimensions, so will not check just a single color channel, or anything similar.
         *
         *
         * Does not construct a complete Histogram, so if the only purpose of the Histogram is to calculate the maximum value of an Image, then
         * a bit of time can be saved by using this method instead.

         * @param im    The Image whose maximum to find.
         * *
         * @return      The maximum value of the Image as an integer.
         */
        fun findMaxVal(im: Image): Int {

            var tempMax = 0

            for (i in im) {
                if (im.getValue(i) > tempMax) {
                    tempMax = im.getValue(i).toInt()
                }
            }

            return tempMax
        }
    }


}
