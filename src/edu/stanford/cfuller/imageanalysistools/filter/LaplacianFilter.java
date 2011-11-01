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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * Applies a Laplacian filter to an Image.
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image to be filtered.
 * 
 * @author Colin J. Fuller
 */
public class LaplacianFilter extends Filter{

    static double[][] kernel;

    static final int kernelHalfSize = 1;

    static{
        kernel = new double[2*kernelHalfSize+1][2*kernelHalfSize+1];

        kernel[0][0] = -1;
        kernel[0][1] = -1;
        kernel[0][2] = -1;
        kernel[1][0] = -1;
        kernel[1][1] = 8;
        kernel[1][2] = -1;
        kernel[2][0] = -1;
        kernel[2][1] = -1;
        kernel[2][2] = -1;
    }


    /**
     * Appies a Laplacian filter to an Image.
     * @param im    The Image to be filtered; this will be replaced by the Laplacian-filtered Image
     */
    public void apply(Image im) {

        final int numEl = (int) kernel[kernelHalfSize][kernelHalfSize];


        Image newIm = new Image(im);

        float minValue = Float.MAX_VALUE;

        for (ImageCoordinate ic : im) {

            float newValue = 0;

            ImageCoordinate icTemp = ImageCoordinate.cloneCoord(ic);

            int count = -1; //subtract one for the center pixel

            for (int i =0; i < kernel.length; i++) {
                for (int j = 0; j < kernel[0].length; j++) {

                    icTemp.set(ImageCoordinate.X,ic.get(ImageCoordinate.X) + j-kernelHalfSize);
                    icTemp.set(ImageCoordinate.Y,ic.get(ImageCoordinate.Y) + i-kernelHalfSize);

                    if (! im.inBounds(icTemp)) {
                        continue;
                    }

                    count++;

                    newValue += im.getValue(icTemp)*kernel[i][j];

                }

            }


            if (count < numEl) {
                newValue -= (numEl - count) * im.getValue(ic);
            }

            newIm.setValue(ic, (float) newValue);

            if (newValue < minValue) minValue = newValue;

        }

        for (ImageCoordinate ic : im) {
            im.setValue(ic, (newIm.getValue(ic)-minValue));
        }

        
    }


}
