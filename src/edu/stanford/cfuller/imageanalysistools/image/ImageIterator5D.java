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

/**
 * An iterator optimized for faster iteration over 5D images.
 * 
 * @author Colin J. Fuller
 *
 */
public class ImageIterator5D extends ImageIterator {

	protected ImageIterator5D() {}
	
	int curr_x;
	int curr_y;
	int curr_z;
	int curr_c;
	int curr_t;
	
	int lower_x;
	int lower_y;
	int lower_z;
	int lower_c;
	int lower_t;
	
	int upper_x;
	int upper_y;
	int upper_z;
	int upper_c;
	int upper_t;
		
	public ImageIterator5D(Image im) {
				
		this.toIterate = im;
		
		this.currCoord = ImageCoordinate.createCoordXYZCT(-1,0,0,0,0);
		
		this.isBoxedIterator = im.getIsBoxed();
		
		if (this.isBoxedIterator) {
			
			ImageCoordinate boxMin = im.getBoxMin();
			
			curr_x = boxMin.get(ImageCoordinate.X)-1;
			curr_y = boxMin.get(ImageCoordinate.Y);
			curr_z = boxMin.get(ImageCoordinate.Z);
			curr_c = boxMin.get(ImageCoordinate.C);
			curr_t = boxMin.get(ImageCoordinate.T);
			
			ImageCoordinate boxMax = im.getBoxMax();
			
			upper_x = boxMax.get(ImageCoordinate.X);
			upper_y = boxMax.get(ImageCoordinate.Y);
			upper_z = boxMax.get(ImageCoordinate.Z);
			upper_c = boxMax.get(ImageCoordinate.C);
			upper_t = boxMax.get(ImageCoordinate.T);
			
		} else {
			
			curr_x = -1;
			curr_y = 0;
			curr_z = 0;
			curr_c = 0;
			curr_t = 0;
			
			ImageCoordinate sizes = im.getDimensionSizes();
			
			upper_x = sizes.get(ImageCoordinate.X);
			upper_y = sizes.get(ImageCoordinate.Y);
			upper_z = sizes.get(ImageCoordinate.Z);
			upper_c = sizes.get(ImageCoordinate.C);
			upper_t = sizes.get(ImageCoordinate.T);
			
			
		}
		
		lower_x = curr_x;
		lower_y = curr_y;
		lower_z = curr_z;
		lower_c = curr_c;
		lower_t = curr_t;
		
		
		//account for the possibility of a zero size dimension
		
		if (upper_x <= lower_x || upper_y <= lower_y || upper_z <= lower_z || upper_c <= lower_c || upper_t <= lower_t) {
			curr_x = upper_x;
			curr_y = upper_y;
			curr_z = upper_z;
			curr_c = upper_c;
			curr_t = upper_t;
		}
		
		
		this.currCoord.setCoordXYZCT(curr_x, curr_y, curr_z, curr_c, curr_t);
				
	}
	
	public boolean hasNext() {
		if (curr_x + 1 < upper_x) {
			return true;
		} else if (curr_y + 1 < upper_y) {
			return true;
		} else if (curr_z + 1 < upper_z) {
			return true;
		} else if (curr_c + 1 < upper_c) {
			return true;
		} else if (curr_t + 1 < upper_t) {
			return true;
		}
		return false;
	}
	
	public ImageCoordinate next() {

		curr_x++;
		if (curr_x < upper_x) {
			this.currCoord.set(ImageCoordinate.X, curr_x);
		} else {
			curr_x = lower_x;
			curr_y++;
			if (curr_y < upper_y) {
				this.currCoord.set(ImageCoordinate.X, curr_x);
				this.currCoord.set(ImageCoordinate.Y, curr_y);
			} else {
				curr_y = lower_y;
				curr_z++;
				if (curr_z < upper_z) {
					this.currCoord.set(ImageCoordinate.X, curr_x);
					this.currCoord.set(ImageCoordinate.Y, curr_y);
					this.currCoord.set(ImageCoordinate.Z, curr_z);
				} else {
					curr_z = lower_z;
					curr_c++;
					if (curr_c < upper_c) {
						this.currCoord.set(ImageCoordinate.X, curr_x);
						this.currCoord.set(ImageCoordinate.Y, curr_y);
						this.currCoord.set(ImageCoordinate.Z, curr_z);
						this.currCoord.set(ImageCoordinate.C, curr_c);
					} else {
						curr_c = lower_c;
						curr_t++;
						this.currCoord.setCoordXYZCT(curr_x, curr_y, curr_z, curr_c, curr_t);
					}
				}
			}
			
		}
				
		return this.currCoord;
	}
	
}
