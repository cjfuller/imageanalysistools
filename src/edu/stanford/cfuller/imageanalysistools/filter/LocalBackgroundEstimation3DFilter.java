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

import java.util.LinkedList;
import java.util.List;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.util.SortedDeque;

/**
 * A Filter that estimates the background locally in an Image, using a local median filtering approach.  It is intended for use on three-dimensional images.
 * <p>
 * This filter may be useful for determining and correcting for local intensity variations.
 * <p>
 * The reference image should be set to the Image that is to be median filtered.  This Image will not be changed.
 * <p>
 * The argument to the apply method should be any Image (except a shallow copy of the reference Image) of the same dimensions as the reference Image.
 * The median filtered Image will be written to this Image.
 * <p>
 *
 * @author Colin J. Fuller
 * 
 */
public class LocalBackgroundEstimation3DFilter extends LocalBackgroundEstimationFilter {

	
	List<PixelEntry> available;
	
	public LocalBackgroundEstimation3DFilter() {
		this.available = new LinkedList<PixelEntry>();
	}
	
	protected static class PixelEntry implements Comparable<PixelEntry> {
		
		int columnIndex;
		double value;
		
		public PixelEntry(int columnIndex, double value) {
			this.columnIndex = columnIndex;
			this.value = value;
		}
		
		public int getColumnIndex() {return this.columnIndex;}
		public double getValue() {return this.value;}
		
		
		/**
		 * Note that PixelEntry has a natural ordering inconsistent with equals.
		 */
		public int compareTo(PixelEntry other) {
			
			return Double.compare(this.value, other.value);
			
		}
		
	}
	

	
	protected SortedDeque<PixelEntry> constructMedianDeque(List<PixelEntry> pixels) {
		
		int halfSize = (int) Math.ceil(pixels.size() / 2.0);
		
		if (halfSize % 2 == 0) {halfSize++;}
		
		SortedDeque<PixelEntry> pq = new SortedDeque<PixelEntry>(halfSize, true);
		
		int middleIndex = (int) Math.floor(halfSize/2.0);
		
		int addedFromLeftCount = 0;
		int addedFromRightCount = 0;
		
		for (PixelEntry p : pixels) {
			
			if (! pq.isAtCapacity()) {
				pq.addFirst(p);
			} else {
				
				double median = pq.get(middleIndex).getValue();
				
				if (p.getValue() <= median && p.getValue() > pq.get(0).getValue()) {
					pq.addFirst(p);
					addedFromLeftCount++;
				} else if (p.getValue() > median && p.getValue() < pq.get(pq.size() -1).getValue()) {
					pq.addLast(p);
					addedFromRightCount++;
				}
				
			}
			
		}
		
		//int finalMedianIndex = middleIndex + addedFromRightCount - addedFromLeftCount;
		
		return pq;
		
	}
	
	
	/**
     * Applies the LocalBackgroundEstimationFilter to an Image.
     * @param im    The Image that will be replaced by the output Image.  This can be anything of the correct dimensions except a shallow copy of the reference Image.
     */
	@Override
	public void apply(Image im) {

		if (this.referenceImage == null) return;
		
		//RealVector localArea = new org.apache.commons.math.linear.ArrayRealVector();
		
		//edu.stanford.cfuller.imageanalysistools.image.Histogram h = new edu.stanford.cfuller.imageanalysistools.image.Histogram(this.referenceImage);

		//int numPixelsInBox = boxSize*boxSize*boxSize;
		
		ImageCoordinate ic = this.referenceImage.getDimensionSizes();
		
		ImageCoordinate icnew = ImageCoordinate.createCoordXYZCT(ic.get("x")+2*boxSize, ic.get("y")+2*boxSize, ic.get("z")+2*boxSize, ic.get("c"), ic.get("t"));

		Image padded = new Image(icnew, -1.0);
		
		
		for(ImageCoordinate i : this.referenceImage) {
		
			icnew.set("x",i.get("x")+boxSize);
			icnew.set("y",i.get("y")+boxSize);
			icnew.set("z",i.get("z")+boxSize);
			icnew.set("c",i.get("c"));
			icnew.set("t",i.get("t"));
			
			padded.setValue(icnew, this.referenceImage.getValue(i));
			
		}
		
		icnew.recycle();

		
	}

	
	
	
}
