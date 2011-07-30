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
 * A Filter that estimates the background locally in an Image, using a local mean filtering approach.  It is intended for use on three-dimensional images.
 * <p>
 * This filter may be useful for determining and correcting for local intensity variations.
 * <p>
 * The reference image should be set to the Image that is to be median filtered.  This Image will not be changed.
 * <p>
 * The argument to the apply method should be any Image (except a shallow copy of the reference Image) of the same dimensions as the reference Image.
 * The mean filtered Image will be written to this Image.
 * <p>
 *
 * @author Colin J. Fuller
 * 
 */
public class LocalBackgroundEstimation3DFilter extends LocalBackgroundEstimationFilter {

	
	public LocalBackgroundEstimation3DFilter() {
	}
	
	/**
     * Applies the LocalBackgroundEstimationFilter to an Image.
     * @param im    The Image that will be replaced by the output Image.  This can be anything of the correct dimensions except a shallow copy of the reference Image.
     */
	@Override
	public void apply(Image im) {

		if (this.referenceImage == null) return;
		
		//TODO: smarter implementation where just adding/subtracting the values being added/removed to/from the box.
		
		ImageCoordinate boxMin = ImageCoordinate.cloneCoord(this.referenceImage.getDimensionSizes());
		for (Integer s : boxMin) {
			boxMin.set(s, 0);
		}
		
		ImageCoordinate boxMax = ImageCoordinate.cloneCoord(this.referenceImage.getDimensionSizes());
		
		for(ImageCoordinate i : im) {
			
			boxMin.set(ImageCoordinate.X, i.get(ImageCoordinate.X) - boxSize);
			boxMin.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) - boxSize);
			boxMin.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) - boxSize);
			
			boxMax.set(ImageCoordinate.X, i.get(ImageCoordinate.X) + boxSize+1);
			boxMax.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + boxSize+1);
			boxMax.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + boxSize+1);
			
			this.referenceImage.setBoxOfInterest(boxMin, boxMax);
		
			double total = 0;
			int count = 0;
			
			for (ImageCoordinate iBox : this.referenceImage) {
				
				total += this.referenceImage.getValue(iBox);
				count++;
				
			}
			
			im.setValue(i, total/count);
			
			this.referenceImage.clearBoxOfInterest();
			
		}
		
		boxMin.recycle();
		boxMax.recycle();
		
		
		
		
	}

	
	
	
}
