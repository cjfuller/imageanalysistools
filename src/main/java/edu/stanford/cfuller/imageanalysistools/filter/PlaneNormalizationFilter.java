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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * Normalizes an image such that each z-plane is divided by its mean value over all other dimensions.
 * The entire image is then rescaled to its original min/max values.
 * <p>
 * This filter does not use a reference image.
 * 
 * @author Colin J. Fuller
 *
 */
public class PlaneNormalizationFilter extends Filter {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(WritableImage im) {
								
		float origMin = Float.MAX_VALUE;
		float origMax = -1.0f*Float.MAX_VALUE;
		
		for (ImageCoordinate ic : im) {
			if (im.getValue(ic) < origMin) {origMin = im.getValue(ic);}
			if (im.getValue(ic) > origMax) {origMax = im.getValue(ic);}
		}
		
		double[] sums = new double[im.getDimensionSizes().get(ImageCoordinate.Z)];
		int[] counts = new int[sums.length];
		
		java.util.Arrays.fill(sums, 0.0);
		java.util.Arrays.fill(counts, 0);
		
		for (ImageCoordinate ic : im) {
			
			int z = ic.get(ImageCoordinate.Z);
			
			sums[z]+= im.getValue(ic);
			
		}
		
		for (ImageCoordinate ic : im) {
			
			int z = ic.get(ImageCoordinate.Z);
			
			sums[z]+= im.getValue(ic);
			counts[z]++;
			
		}
		
		for (int i = 0; i < sums.length; i++) {
			sums[i]/=counts[i];
		}
		
		for (ImageCoordinate ic : im) {
			
			int z = ic.get(ImageCoordinate.Z);
			
			im.setValue(ic, (float) (im.getValue(ic)/sums[z]));
			
		}
		
		float newMin = Float.MAX_VALUE;
		float newMax = -1.0f*Float.MAX_VALUE;
		
		for (ImageCoordinate ic : im) {
			if (im.getValue(ic) < newMin) {newMin = im.getValue(ic);}
			if (im.getValue(ic) > newMax) {newMax = im.getValue(ic);}
		}
		
		for (ImageCoordinate ic : im) {
			im.setValue(ic, (im.getValue(ic) - newMin)/(newMax-newMin) * (origMax-origMin) + origMin);
		}

	}

}
