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
import java.util.NoSuchElementException;

/**
 * This class implements the Iterator required for foreach-style looping through the dimensions in an {@link ImageCoordinate}
 *
 * @author Colin J. Fuller
 */
public class ImageCoordinateIterator implements Iterator<String> {
	
	int currentIndex;
	ImageCoordinate ic;
	
	protected ImageCoordinateIterator() {}
	
	public ImageCoordinateIterator(ImageCoordinate ic) {
		this.currentIndex = 0;
		this.ic = ic;
	}
	
	public boolean hasNext() {
		return (this.currentIndex < this.ic.getDimension());
	}
	
	public String next() {
		if (this.hasNext()) return this.ic.getDimensionNameByIndex(this.currentIndex++);
		
		throw new NoSuchElementException("No more elements in ImageCoordinate.");
		
	}
	
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported for ImageCoordinate.");
	}
	
}