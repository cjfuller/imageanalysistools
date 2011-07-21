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

package edu.stanford.cfuller.imageanalysistools.image;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
        if (im.getIsBoxed()) {
            isBoxedIterator = true;
            currCoord.recycle();
            currCoord = ImageCoordinate.cloneCoord(im.getBoxMin());
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


		ImageCoordinate sizes = null;
		
        if (this.isBoxedIterator) {
        	sizes = toIterate.getBoxMax();
        } else {
        	sizes = toIterate.getDimensionSizes();
        }
        
        if (sizes == null) return false;
		
		for (String dim : nextCoord) {
			if (nextCoord.get(dim) >= sizes.get(dim)) {
				return false;
			}
		}
		
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
     * @throws NoSuchElementException if there are no more coordinates available.
     */
	public ImageCoordinate next() throws NoSuchElementException {
		if (this.hasNext()) {

			ImageCoordinate sizes = null;
			
			if (this.isBoxedIterator) {
				sizes = this.toIterate.getBoxMax();
			} else {
				sizes = this.toIterate.getDimensionSizes();
			}
			
			if (sizes == null) throw new NoSuchElementException("Undefined Image size.");
			
            this.currCoord.setCoord(nextCoord);

            int currDimValue = 1;
            
            for (String dim : this.nextCoord) {
            	currDimValue = this.nextCoord.get(dim);
            	currDimValue+=1;
            	currDimValue = currDimValue % sizes.get(dim);
            	this.nextCoord.set(dim, currDimValue);
            	if (currDimValue != 0) break;
            }
			
			return this.currCoord;
			
		} else {
			throw new java.util.NoSuchElementException("No more pixels in image!");
		}
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
