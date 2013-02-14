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

package edu.stanford.cfuller.imageanalysistools.image;

import java.io.Serializable;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * This class represents a histogram of intensity values in an Image.  The bin size is 1 (meaning that intensity values are rounded to integers).
 * <p>
 * Also calculates statistics like mean intensity, mode intensity, and intensity variance.
 *
 * @author Colin J. Fuller
 */

public class Histogram implements Serializable {

	static final long serialVersionUID = 1L;
	
	//fields
	
	int[] counts;
	int[] cumulativeCounts;
	int totalCounts;
	int maxValue;
	int minValue;
    int minValueNonzero;
	double mean;
	double variance;
	double meanNonzero;
	double varianceNonzero;
	int modeCounts;
	int mode;
	
	//constructors

	protected Histogram() {}

    /**
     * Constructs a new Histogram for a given Image.
     * <p>
     * The Histogram will be a snapshot of the current state of the Image, so if the Image changes later, the Histogram will not be updated.
     *
     * @param im
     */
	public Histogram(Image im) {init(im);}
	
	//private methods
	
	private void init(Image im) {
		
		this.mean = 0;
		this.variance = 0;
		this.meanNonzero = 0;
		this.varianceNonzero = 0;
		this.totalCounts = 0;
		this.maxValue = findMaxVal(im);
		this.counts = new int[this.maxValue + 1];
		this.cumulativeCounts = new int[this.counts.length];
		
		java.util.Arrays.fill(this.counts, 0);
		java.util.Arrays.fill(this.cumulativeCounts, 0);
		
		boolean suppressNegativeWarning = false;
		
		for (ImageCoordinate i : im) {
			if (im.getValue(i) < 0) {
				if (!suppressNegativeWarning) {
					suppressNegativeWarning = true;
					LoggingUtilities.getLogger().warning("negative image value");
				}
				continue;
			}
			this.counts[(int) im.getValue(i)]++;
			this.totalCounts++;
			this.mean += im.getValue(i);
			
		}

        for (int i=0; i < this.counts.length; i++) {
            if (this.counts[i] > 0) {
                this.minValue = i;
                break;
            }
        }
        this.minValueNonzero = this.minValue;

        if (this.minValue == 0) {
            for (int i=1; i < this.counts.length; i++) {
                if (this.counts[i] > 0) {
                    this.minValueNonzero = i;
                    break;
                }
            }
        }

		this.mean/=this.totalCounts;
		
		this.modeCounts = 0;
		this.mode = 0;
		
		double sum = 0;
		
		for (int i = 1; i < this.counts.length; i++) {
			
			sum+= counts[i]*i;
		
			this.cumulativeCounts[i] = this.cumulativeCounts[i-1] + this.counts[i];
		
			if (this.counts[i] > this.modeCounts) {
				this.modeCounts = this.counts[i];
				this.mode = i;
			}
			
		}
		
		if (this.maxValue > 0) {
			this.meanNonzero = sum*1.0/(this.totalCounts - this.counts[0]);
		} else {
			this.meanNonzero = 0;
		}
		
		for (int i = 0; i < this.counts.length; i++) {
			this.variance += Math.pow(i - this.mean, 2.0)*this.counts[i]*1.0/this.totalCounts;
			if (i > 0) {
				this.varianceNonzero += Math.pow(i - this.mean, 2.0)*this.counts[i]*1.0/(this.totalCounts - this.counts[0]);
			}
		}
		
	}
	
	//public methods

    /**
     * Finds the maximum value of an image.
     * <p>
     * The maximum value is defined as the value at the ImageCoordinate whose corresponding Image value is greatest.  In particular
     * this makes no discrimination between different dimensions, so will not check just a single color channel, or anything similar.
     *<p>
     * Does not construct a complete Histogram, so if the only purpose of the Histogram is to calculate the maximum value of an Image, then
     * a bit of time can be saved by using this method instead.
     *
     * @param im    The Image whose maximum to find.
     * @return      The maximum value of the Image as an integer.
     */
	public static int findMaxVal(Image im) {
		
		int tempMax = 0;
	
		for (ImageCoordinate i : im) {
			if (im.getValue(i) > tempMax) {
				tempMax = (int) im.getValue(i);
			}
		}
				
		return tempMax;
	}

    /**
     * Gets the count of how many coordinates in the Image used to construct the Histogram had values that cast to the specified integer value.
     * <p>
     * Does not check bounds on the supplied value.  Ensure that the value passed in is between 0 and the result of {@link #getMaxValue},inclusive.
     * @param value     The value whose counts will be retrieved.
     * @return          The number of times the specified value occurred in the Image used to construct the Histogram.
     */
	public int getCounts(int value) {return this.counts[value];}

    /**
     * Gets the cumulative count of all values in the Image up to the specified value.
     * <p>
     * The returned value is the same value as would be obtained by summing the values returned from {@link #getCounts} from 0 to value.
     *
     * @param value     The value up to which to count.
     * @return          The number of times and value less than or equal to the specified value occurred in the Image used to construct the Histogram.
     */
	public int getCumulativeCounts(int value) {return this.cumulativeCounts[value];}

    /**
     * Gets the entire array of counts at every integer value from 0 to the maximum value in the Image used to construct the Histogram.
     * <p>
     * Do not modify the array that is returned.
     *
     * @return  The array of counts at each value.
     */
	public int[] getCountsList() {return this.counts;}

    /**
     * Gets the positive mode value of the histogram.  (That is, the positive value with the highest count.)
     * <p>
     * This will not return 0 as the mode value unless there are no positive values in the Image used to construct the Histogram, and will never return a negative number.
     *
     * @return  The positive mode value of the histogram.
     */
	public int getMode() {return this.mode;}

    /**
     * Gets the number of counts at the positive mode value of the histogram.
     * <p>
     * If there are no positive values in the Image used to construct the Histogram, this will return 0.
     * @return  The counts at the positive mode of the Histogram, or 0 if there are no positive values.
     */
	public int getCountsAtMode() {return this.modeCounts;}

    /**
     * Gets the maximum pixel value in the Image used to construct the Histogram.
     * @return The maximum pixel value (or if there are no positive values, 0).
     */
	public int getMaxValue() {return this.maxValue;}

    /**
     * Gets the minimum nonnegative pixel value in the Image used to construct the Histogram.
     * @return The minimum pixel value.
     */
	public int getMinValue() {return this.minValue;}


    /**
     * Gets the minimum positive pixel value in the Images used to construct the Histogram.
     * @return  The minimum positive pixel value, or zero only if there are no positive pixels in the Image.
     */
    public int getMinValueNonzero() {return this.minValueNonzero;}

    /**
     * Gets the total counts of all nonnegative values in the Image.
     * <p>
     * For Images with no negative values, this should be the same as calling {@link Image#size} on the Image used to construct the Histogram.
     * @return  The total number of nonnegative values in the Image.
     */
	public int getTotalCounts() {return this.totalCounts;}

    /**
     * Gets the mean of all nonnegative values in the Image used to construct the Histogram.
     * 
     * @return  The mean of the nonnegative values.
     */
	public double getMean() {return this.mean;}

    /**
     * Gets the variance of all nonnegative values in the Image used to construct the Histogram.
     *
     * @return  The variance of the nonnegative values.
     */
	public double getVariance() {return this.variance;}

    /**
     * Gets the mean of all positive values in the Image used to construct the Histogram.
     *
     * @return  The mean of the positive values.
     */
	public double getMeanNonzero() {return this.meanNonzero;}

    /**
     * Gets the variance of all positive values in the Image used to construct the Histogram.
     *
     * @return  The variance of the positive values.
     */
	public double getVarianceNonzero() {return this.varianceNonzero;}
	

	/**
	 * Gets a string representation of the histogram: a comma-separated list of ordered pairs
	 * in the format (value, count).
	 *
	 * @return The string representation.
	 */
	public String toString() {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.getMaxValue(); i++) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("(" + i + "," + this.getCounts(i) + ")");
		}

		return sb.toString();

	}

	
	
}
