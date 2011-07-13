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

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

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
		
		int finalMedianIndex = middleIndex + addedFromRightCount - addedFromLeftCount;
		
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
		
		edu.stanford.cfuller.imageanalysistools.image.Histogram h = new edu.stanford.cfuller.imageanalysistools.image.Histogram(this.referenceImage);

		int numPixelsInBox = boxSize*boxSize*boxSize;
		
		ImageCoordinate ic = this.referenceImage.getDimensionSizes();
		
		ImageCoordinate icnew = ImageCoordinate.createCoord(ic.getX()+2*boxSize, ic.getY()+2*boxSize, ic.getZ()+2*boxSize, ic.getC(), ic.getT());

		Image padded = new Image(icnew, -1.0);
		
		
		for(ImageCoordinate i : this.referenceImage) {
		
			icnew.setX(i.getX()+boxSize);
			icnew.setY(i.getY()+boxSize);
			icnew.setZ(i.getZ()+boxSize);
			icnew.setC(i.getC());
			icnew.setT(i.getT());
			
			padded.setValue(icnew, this.referenceImage.getValue(i));
			
		}
		
		//for each z-section
		
		//if first, calculate median of box
		
		
		
		
		
		
		
		icnew.recycle();

		
	}

	
	
	
}
