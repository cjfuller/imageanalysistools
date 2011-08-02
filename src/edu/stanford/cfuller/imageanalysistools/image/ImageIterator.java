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

import java.util.Iterator;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * This class implements the Iterator required for foreach-style looping through the coordinates in an {@link Image}
 *
 * @author Colin J. Fuller
 */

public class ImageIterator implements Iterator<ImageCoordinate> {

	Image toIterate;
	ImageCoordinate currCoord;
    ImageCoordinate nextCoord;
    ImageCoordinate sizes;
    ImageCoordinate lowerBounds;
    
    private static final ImageCoordinate zeroCoord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

    boolean isBoxedIterator;

    protected ImageIterator() {}

    /**
     * Constructs a new ImageIterator for a given Image, and initializes it to the 0th coordinate in each dimension.
     *
     * If the image is currently being boxed with a region of interest, this will set the iterator to the minimal coordinate in each dimension that is in the box.
     *
     * @param im
     */
	public ImageIterator(Image im) {
		toIterate = im;
		currCoord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
        isBoxedIterator = false;
    	sizes = toIterate.getDimensionSizes();
    	lowerBounds = ImageIterator.zeroCoord;
        if (im.getIsBoxed()) {
            isBoxedIterator = true;
            currCoord.recycle();
            currCoord = ImageCoordinate.cloneCoord(im.getBoxMin());
        	sizes = toIterate.getBoxMax();
        	lowerBounds = im.getBoxMin();
        }
        nextCoord = ImageCoordinate.cloneCoord(currCoord);

	}

    /**
     * Queries whether there is another {@link ImageCoordinate} that has not yet been visited in the current Image.
     *
     * If this iterator was constructed on an image boxed with a region of interest, then this will check if there are more
     * coordinates that have not been visited in that region of interest.
     *
     * @return  true if there are more coordinates that have not yet been visited, false otherwise.
     */
	public boolean hasNext() {
        
        if (sizes == null) return false;
		
        Integer dim = nextCoord.getDefinedIndex(nextCoord.getDimension()-1);
                
//		for (String dim : nextCoord) {
			
			
			if (nextCoord.get(dim) >= sizes.get(dim)) {
				return false;
			}
//		}
		
		return true;
	}

    /**
     * Gets the next coordinate in the ImageIterator's {@link Image}.
     *
     * If the Image is boxed with a region of interest, this will get the next coordinate in the region of interest.
     *
     * ImageIterator makes no guarantee as to the order in which coordinates will be visited, so do not rely on any ordering.
     *
     * In particular, the order in which coordinates are visited may not be the same as the order of the underlying byte array representation of the Image.
     *
     *
     * @return  An {@link ImageCoordinate} that is the next location in the ImageIterator's Image.
     */
	public ImageCoordinate next() {
//		if (this.hasNext()) {

//			ImageCoordinate sizes = null;
			
//			if (this.isBoxedIterator) {
//				sizes = this.toIterate.getBoxMax();
//			} else {
//				sizes = this.toIterate.getDimensionSizes();
//			}
			
//			if (sizes == null) throw new NoSuchElementException("Undefined Image size.");
						
			ImageCoordinate temp = this.currCoord;
			this.currCoord = nextCoord;
			this.nextCoord = temp;
			
            //this.currCoord.setCoord(nextCoord);

            int currDimValue = 1;
            
            boolean flag = true;
            
            for (Integer dim : this.currCoord) {
            	if (flag) {
	            	currDimValue = this.currCoord.get(dim);
	            	currDimValue+=1;
	            	if (!(dim == nextCoord.getDefinedIndex(nextCoord.getDimension()-1))) currDimValue = currDimValue % sizes.get(dim);
	            	if (currDimValue != 0) {
	            		flag = false;
	            	} else {
	            		currDimValue+= this.lowerBounds.get(dim);
	            	}
	            	this.nextCoord.set(dim, currDimValue);

            	} else {
            		this.nextCoord.set(dim, this.currCoord.get(dim));
            	}
            }
			
			return this.currCoord;
			
//		} else {
//			throw new java.util.NoSuchElementException("No more pixels in image!");
//		}
	}

	
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported for images.");
	}

	protected void finalize() throws Throwable{
		if (currCoord != null) {
			currCoord.recycle();
			currCoord = null;
		}
        if (nextCoord != null) {
            nextCoord.recycle();
            nextCoord = null;
        }
		super.finalize();
	}
	
}
