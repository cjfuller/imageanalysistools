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
		currCoord = ImageCoordinate.createCoordXYZCT(-1, 0, 0, 0, 0);
		isBoxedIterator = false;
		sizes = toIterate.getDimensionSizes();
		lowerBounds = ImageIterator.zeroCoord;
		if (im.getIsBoxed()) {
			isBoxedIterator = true;
			currCoord.recycle();
			currCoord = ImageCoordinate.cloneCoord(im.getBoxMin());
			currCoord.set(ImageCoordinate.X, currCoord.get(ImageCoordinate.X)-1);
			sizes = toIterate.getBoxMax();
			lowerBounds = im.getBoxMin();
		}

		//deal with the possibility of a dimension of size zero or an initial coordinate at the box size

		for (Integer i : sizes) {
			if (sizes.get(i) == 0 || currCoord.get(i) >= sizes.get(i)) {
				currCoord.recycle();
				currCoord = ImageCoordinate.cloneCoord(sizes);

				break;
			}
		}

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

		//Integer dim = currCoord.getDefinedIndex(currCoord.getDimension()-1);

		for (Integer dim : currCoord) {

			if (currCoord.get(dim)+1 < sizes.get(dim)) {
				return true;
			}
		}

		return false;
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

		for (Integer dim : this.currCoord) {
			this.currCoord.set(dim, this.currCoord.get(dim)+1);
			if (this.currCoord.get(dim) >= this.sizes.get(dim)) {
				this.currCoord.set(dim, this.lowerBounds.get(dim));

			} else {
				return this.currCoord;
			}
		}

		throw new java.util.NoSuchElementException("No more pixels in image!");
	}


	public void remove() {
		throw new UnsupportedOperationException("Remove not supported for images.");
	}

	protected void finalize() throws Throwable{
		if (currCoord != null) {
			currCoord.recycle();
			currCoord = null;
		}

		super.finalize();
	}

}
