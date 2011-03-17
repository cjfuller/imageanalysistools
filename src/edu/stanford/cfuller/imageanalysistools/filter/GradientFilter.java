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

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;

/**
 * A filter that takes the gradient of an Image.
 * <p>
 * This filter applies a horizontal and vertical (3x3 pixel) Prewitt gradient filter to an Image separately, and then replaces the
 * original Image with the sum of the two directional components in quadrature.
 * <p>
 * This filter does not use a reference image.
 * <p>
 * The argument to the apply function should be the Image that will be replaced by its gradient.
 *
 *@author Colin J. Fuller
 *
 */

public class GradientFilter extends Filter {


    /**
     * Applies the GradientFilter to the specified Image.
     * @param im    The Image that will be replaced by its gradient.
     */
	@Override
	public void apply(Image im) {
		
		final int kernelSize = 3;
		int halfKernelSize = (kernelSize -1)/2;
		
		RealMatrix kernel1 = new Array2DRowRealMatrix(kernelSize, kernelSize);
		
		kernel1.setEntry(0, 0, 1);
		kernel1.setEntry(1, 0, 0);
		kernel1.setEntry(2, 0, -1);
		kernel1.setEntry(0, 1, 1);
		kernel1.setEntry(1, 1, 0);
		kernel1.setEntry(2, 1, -1);
		kernel1.setEntry(0, 2, 1);
		kernel1.setEntry(1, 2, 0);
		kernel1.setEntry(2, 2, -1);
		
		RealMatrix kernel2 = new Array2DRowRealMatrix(kernelSize, kernelSize);
		
		kernel2.setEntry(0, 0, -1);
		kernel2.setEntry(1, 0, -1);
		kernel2.setEntry(2, 0, -1);
		kernel2.setEntry(0, 1, 0);
		kernel2.setEntry(1, 1, 0);
		kernel2.setEntry(2, 1, 0);
		kernel2.setEntry(0, 2, 1);
		kernel2.setEntry(1, 2, 1);
		kernel2.setEntry(2, 2, 1);
		
		
		Image copy1 = new Image(im);
		
		Image copy2 = new Image(im);
		
		ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
		
		for (ImageCoordinate i : im) {
			
			double outputVal = 0;
			double output1 = 0;
			double output2 = 0;
			
			if (i.getX() == 0 || i.getY() == 0 || i.getX() == copy1.getDimensionSizes().getX()-1 || i.getY() == copy1.getDimensionSizes().getY()-1) {
				outputVal = 0;
			} else {
				for (int p =-1*halfKernelSize; p < halfKernelSize+1; p++) {
					for (int q = -1*halfKernelSize; q < halfKernelSize+1; q++) {
						ic.setX(i.getX()+p);
						ic.setY(i.getY()+q);
						output1 += kernel1.getEntry(p+halfKernelSize,q+halfKernelSize) *copy1.getValue(ic);
						output2 += kernel2.getEntry(p+halfKernelSize,q+halfKernelSize) *copy2.getValue(ic);
					}
				}
				
				outputVal =Math.hypot(output1, output2);
			}
			
			im.setValue(i, Math.floor(outputVal));
			
		}
		
		ic.recycle();
		
	}

}
