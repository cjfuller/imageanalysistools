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

        double minValue = Double.MAX_VALUE;

        for (ImageCoordinate ic : im) {

            double newValue = 0;

            ImageCoordinate icTemp = ImageCoordinate.cloneCoord(ic);

            int count = -1; //subtract one for the center pixel

            for (int i =0; i < kernel.length; i++) {
                for (int j = 0; j < kernel[0].length; j++) {

                    icTemp.setX(ic.getX() + j-kernelHalfSize);
                    icTemp.setY(ic.getY() + i-kernelHalfSize);

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

            newIm.setValue(ic, newValue);

            if (newValue < minValue) minValue = newValue;

        }

        for (ImageCoordinate ic : im) {
            im.setValue(ic, newIm.getValue(ic)-minValue);
        }

        
    }


}
