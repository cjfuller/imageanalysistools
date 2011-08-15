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
				
		ImageCoordinate boxMin = ImageCoordinate.cloneCoord(this.referenceImage.getDimensionSizes());
		for (Integer s : boxMin) {
			boxMin.set(s, 0);
		}
		
		ImageCoordinate boxMax = ImageCoordinate.cloneCoord(this.referenceImage.getDimensionSizes());
		
		//boolean first = true;
				
		Image counts = new Image(this.referenceImage.getDimensionSizes(), 0.0);
		
		ImageCoordinate lastCoordinate = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		
		for(ImageCoordinate i : im) {
						
			//if (first) {
			
			if (i.get(ImageCoordinate.X) == 0 ) {
			
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
				counts.setValue(i, count);
				
				this.referenceImage.clearBoxOfInterest();
				
				//first = false;
								
			} else {
				
				lastCoordinate.setCoord(i);
				
				int x = i.get(ImageCoordinate.X);
				int y = i.get(ImageCoordinate.Y);
				int z = i.get(ImageCoordinate.Z);
								
				double sum = 0;
				
				int count = 0;
				
				if (x > 0) {
										
					lastCoordinate.set(ImageCoordinate.X, x-1);
					
					count = (int) counts.getValue(lastCoordinate);
					
					sum = im.getValue(lastCoordinate) * count;
					
					boxMin.set(ImageCoordinate.X, x - boxSize-1);
					boxMin.set(ImageCoordinate.Y, y - boxSize);
					boxMin.set(ImageCoordinate.Z, z - boxSize);
					
					boxMax.set(ImageCoordinate.X, x-boxSize);
					boxMax.set(ImageCoordinate.Y, i.get(ImageCoordinate.Y) + boxSize+1);
					boxMax.set(ImageCoordinate.Z, i.get(ImageCoordinate.Z) + boxSize+1);
					
					this.referenceImage.setBoxOfInterest(boxMin, boxMax);
										
					for (ImageCoordinate iBox : this.referenceImage) {
												
						sum -= this.referenceImage.getValue(iBox);
						count--;
						
					}
					
					this.referenceImage.clearBoxOfInterest();
					
					boxMin.set(ImageCoordinate.X, x+boxSize);
					boxMax.set(ImageCoordinate.X, x+boxSize+1);
					
					this.referenceImage.setBoxOfInterest(boxMin, boxMax);

					for (ImageCoordinate iBox : this.referenceImage) {
						try {
						sum += this.referenceImage.getValue(iBox);
						count++;
						} catch (ArrayIndexOutOfBoundsException e) {
							System.err.println(iBox);
							System.err.println(this.referenceImage.getDimensionSizes());
							System.err.println(this.referenceImage.getBoxMax());
							System.err.println(this.referenceImage.getBoxMin());
							throw e;
						}
						
					}
					
					im.setValue(i, sum/count);
					counts.setValue(i, count);
					
					this.referenceImage.clearBoxOfInterest();
					
				} else if (y > 0) {
					
					//TODO implement this instead of complete calculation for the first column
					
				} else if (z > 0) {
					
					//TODO implement this instead of complete calculation for the first column

				}
				
				
			}
			
		}
		
		lastCoordinate.recycle();
		boxMin.recycle();
		boxMax.recycle();

		
	}
	
	
}
