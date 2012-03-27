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

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 * A filter that takes the gradient of a 2D Image.
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

	//TODO: deal with more dimensions, or refactor to call GradientFilter2D or something similar.

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
		
		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		
		for (ImageCoordinate i : im) {
			
			double outputVal = 0;
			double output1 = 0;
			double output2 = 0;
						
			if (i.get(ImageCoordinate.X) == 0 || i.get(ImageCoordinate.Y) == 0 || i.get(ImageCoordinate.X) == copy1.getDimensionSizes().get(ImageCoordinate.X)-1 || i.get(ImageCoordinate.Y) == copy1.getDimensionSizes().get(ImageCoordinate.Y)-1) {
				outputVal = 0;
			} else {
				for (int p =-1*halfKernelSize; p < halfKernelSize+1; p++) {
					for (int q = -1*halfKernelSize; q < halfKernelSize+1; q++) {
						ic.set(ImageCoordinate.X,i.get(ImageCoordinate.X)+p);
						ic.set(ImageCoordinate.Y,i.get(ImageCoordinate.Y)+q);
						output1 += kernel1.getEntry(p+halfKernelSize,q+halfKernelSize) *copy1.getValue(ic);
						output2 += kernel2.getEntry(p+halfKernelSize,q+halfKernelSize) *copy2.getValue(ic);
					}
				}
				
				outputVal =Math.hypot(output1, output2);
			}
			
			im.setValue(i, (float) Math.floor(outputVal));
			
		}
		
		ic.recycle();
		
	}

}
