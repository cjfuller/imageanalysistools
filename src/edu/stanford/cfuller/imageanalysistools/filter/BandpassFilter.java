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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 * A filter that removes a section of frequency space from an Image's Fourier transform.
 *
 * Note that this is actually the opposite of the usual definition of a bandpass filter, in that the specified band is removed instead of retained.
 *
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image to be bandpass filtered.
 * 
 * @author Colin J. Fuller
 */
public class BandpassFilter extends Filter {

    double bandLow;
    double bandHigh;

    /**
     * Default constructor; creates a filter that is effectively a no-op until the setBand method is called.
     */
    public BandpassFilter() {
        bandLow = 0;
        bandHigh = 0;
    }


    /**
     * Applies the bandpass filter to an Image, removing the range of frequency space specified by the setBand method.
     * @param im    The Image to be bandpass filtered; it will be replaced by its filtered version.
     */
    public void apply(Image im) {

        FastFourierTransformer fft = new org.apache.commons.math.transform.FastFourierTransformer();

        int ydimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.Y);
        int xdimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.X);

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.X)) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.Y))/Math.log(2)));
        }

        for (int p =0; p < im.getPlaneCount(); p++) {

            im.selectPlane(p);

            double[][] rowImage = new double[ydimPowOfTwo][xdimPowOfTwo];
            for (int i =0; i < ydimPowOfTwo; i++) {
                java.util.Arrays.fill(rowImage[i], 0); // ensures zero-padding
            }
            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];

            for (ImageCoordinate ic : im) {
                rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] = im.getValue(ic);
            }

            for (int r = 0; r < rowImage.length; r++) {
                double[] row = rowImage[r];
                Complex[] transformedRow = fft.transform(row);

                for (int c = 0; c < colMajorImage.length; c++) {
                    colMajorImage[c][r] = transformedRow[c];
                }
            }

            for (int c = 0; c < colMajorImage.length; c++) {
                colMajorImage[c] = fft.transform(colMajorImage[c]);
            }

            int NFx = xdimPowOfTwo/2 + 1;
            int NFy = ydimPowOfTwo/2 + 1;

            double cutoffXUpper = NFx*this.bandHigh;
            double cutoffXLower = NFx*this.bandLow;

            double cutoffYUpper = NFy* this.bandHigh;
            double cutoffYLower = NFy* this.bandLow;

            //zero the frquency components

            for (int c = 0; c < NFx; c++) {
                for (int r = 0; r < NFy; r++) {

                    int cOpp = colMajorImage.length - c;

                    int rOpp = colMajorImage[0].length - r;

                    if (c < cutoffXUpper && c > cutoffXLower) {

                        colMajorImage[c][r] = new Complex(0,0);

                        if (c > 0) {
                            colMajorImage[cOpp][r] = new Complex(0,0);
                            if (r > 0) {
                                colMajorImage[cOpp][rOpp] = new Complex(0,0);
                            }
                        }

                    } else if (r < cutoffYUpper && r > cutoffYLower) {
                        colMajorImage[c][r] = new Complex(0,0);

                        if (r > 0) {
                            colMajorImage[c][rOpp] = new Complex(0,0);
                            if (c > 0) {
                                colMajorImage[cOpp][rOpp] = new Complex(0,0);
                            }
                        }
                    }


                }

            }

            //inverse transform

            for (int c = 0; c < colMajorImage.length; c++) {
                colMajorImage[c] = fft.inversetransform(colMajorImage[c]);
            }

            Complex[] tempRow = new Complex[rowImage.length];

            //also calculate min/max values
            double newMin = Double.MAX_VALUE;
            double newMax = 0;

            for (int r = 0; r < rowImage.length; r++) {

                for (int c = 0; c < colMajorImage.length; c++) {
                    tempRow[c] = colMajorImage[c][r];
                }

                Complex[] transformedRow = fft.inversetransform(tempRow);

                for (int c = 0; c < colMajorImage.length; c++) {
                    rowImage[r][c] = transformedRow[c].abs();
                    if (rowImage[r][c] < newMin) newMin = rowImage[r][c];
                    if (rowImage[r][c] > newMax) newMax = rowImage[r][c];
                }
            }

            //rescale values to same min/max as before

            Histogram h = new Histogram(im);

            double oldMin = h.getMinValue();
            double oldMax = h.getMaxValue();

            double scaleFactor = (oldMax - oldMin)/(newMax - newMin);


            for (ImageCoordinate ic : im) {
                im.setValue(ic, (rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] - newMin)*scaleFactor + oldMin);
            }


        }

        im.clearBoxOfInterest();


    }


    /**
     * Sets the band to be removed from frequency space in the Fourier transformed Image.
     * <p>
     * The two arguments specify the lower and upper bounds of the range to be removed as a fraction of the total
     * frequency space.  Both arguments should therefore be between 0 and 1, and filtering will happen only if low < high.
     * 0 refers to the lowest frequency, 1 refers to the highest frequency, so for example to filter out the highest 20% of frequencies,
     * set low to 0.8 and high to 1.  Likewise, to filter out the lowest 10% of frequencies, set low to 0 and high to 0.1.
     *
     *
     * @param low   The lower bound of the proportional frequencies to be filtered, between 0 and 1, inclusive.
     * @param high  The upper bound of the proportional frequencies to be filtered, between 0 and 1, inclusive.
     */
    public void setBand(double low, double high) {
        this.bandLow = low;
        this.bandHigh = high;
    }


}
