/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
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

        int ydimPowOfTwo = im.getDimensionSizes().getY();
        int xdimPowOfTwo = im.getDimensionSizes().getX();

        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {

            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().getX()) / Math.log(2)));
            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().getY())/Math.log(2)));
        }

        for (int p =0; p < im.getPlaneCount(); p++) {

            im.selectPlane(p);

            double[][] rowImage = new double[ydimPowOfTwo][xdimPowOfTwo];
            for (int i =0; i < ydimPowOfTwo; i++) {
                java.util.Arrays.fill(rowImage[i], 0); // ensures zero-padding
            }
            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];

            for (ImageCoordinate ic : im) {
                rowImage[ic.getY()][ic.getX()] = im.getValue(ic);
            }

            ImageCoordinate boxMin = ImageCoordinate.createCoord(0,0,0,0,0);
            ImageCoordinate boxMax = ImageCoordinate.createCoord(im.getDimensionSizes().getX(), 1,1,1,1);

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
                im.setValue(ic, (rowImage[ic.getY()][ic.getX()] - newMin)*scaleFactor + oldMin);
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
