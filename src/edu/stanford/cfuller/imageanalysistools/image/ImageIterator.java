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

    private ImageIterator() {}

    /**
     * Constructs a new ImageIterator for a given Image, and initializes it to the 0th coordinate in each dimension.
     *
     * If the image is currently being boxed with a region of interest, this will set the iterator to the minimal coordinate in each dimension that is in the box.
     *
     * @param im
     */
	public ImageIterator(Image im) {
		toIterate = im;
		currCoord = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
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

        if (this.isBoxedIterator) return this.hasNextBoxed();

		ImageCoordinate sizes = toIterate.getDimensionSizes();
		
		//if (currCoord.getX() >= sizes.getX() - 1 && currCoord.getY() >= sizes.getY() - 1 && currCoord.getZ() >= sizes.getZ()-1 && currCoord.getT() >= sizes.getT()-1 && currCoord.getC() >= sizes.getC()-1) {
		if (nextCoord.getX() >= sizes.getX() || nextCoord.getY() >= sizes.getY() || nextCoord.getZ() >= sizes.getZ() || nextCoord.getC() >= sizes.getC() || nextCoord.getT() >= sizes.getT()) {
            
				return false;
		}
		return true;
	}

    /**
     * Queries where there is another {@link ImageCoordinate} that has not yet been visited in the current region of interest in the current Image.
     *
     * @deprecated  Use {@link #hasNext} instead, which will automatically determine whether the Image is boxed and respond appropriately.
     * @return      true if there are more coordinates that have not yet been visited, false otherwise.
     */
    public boolean hasNextBoxed() {

        ImageCoordinate sizes = toIterate.getBoxMax();

        if (nextCoord.getX() >= sizes.getX() || nextCoord.getY() >= sizes.getY() || nextCoord.getZ() >= sizes.getZ() || nextCoord.getC() >= sizes.getC() || nextCoord.getT() >= sizes.getT()) {
			return false;
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
	public ImageCoordinate next() throws NoSuchElementException{
		if (this.hasNext()) {

            if (this.isBoxedIterator) return this.nextBoxed();

            this.currCoord.setCoord(nextCoord);

			int x = this.nextCoord.getX();

			x+=1;
			x = x % toIterate.getDimensionSizes().getX();
            this.nextCoord.setX(x);
			if (x==0) {
                int y = this.nextCoord.getY();
				y+=1;
				y = y % toIterate.getDimensionSizes().getY();
                this.nextCoord.setY(y);
				if (y==0) {
                    int z = this.nextCoord.getZ();
					z+=1;
					z = z % toIterate.getDimensionSizes().getZ();
                    this.nextCoord.setZ(z);
					if (z == 0) {
                        int c = this.nextCoord.getC();
						c+=1;
						c = c % toIterate.getDimensionSizes().getC();
                        this.nextCoord.setC(c);
						if (c == 0) {
                            int t = this.nextCoord.getT();
							t+=1;
                            this.nextCoord.setT(t);
						}
					}
				}
			}
			
			//this.currCoord.recycle();
			//this.currCoord = ImageCoordinate.createCoord(x, y, z, c, t);
			return this.currCoord;
			
		} else {
			throw new java.util.NoSuchElementException("No more pixels in image!");
		}
	}

    /**
     * Gets the next coordinate in the ImageIterator's {@link Image}'s region of interest.
     * @deprecated  Use {@link #next} instead, which will automatically determine whether the Image is boxed and respond appropriately.
     * @return  The next {@link ImageCoordinate} in the Image's region of interest.
     * @throws NoSuchElementException   if there are no more coordinates remaining in the region of interest.
     */
    public ImageCoordinate nextBoxed() throws NoSuchElementException{

        if (this.hasNext()) {

            this.currCoord.setCoord(nextCoord);

			int x = this.nextCoord.getX();

			x+=1;
			x = x % toIterate.getBoxMax().getX();
            this.nextCoord.setX(x);
			if (x==0) {
                x+= toIterate.getBoxMin().getX();
                this.nextCoord.setX(x);
                int y = this.nextCoord.getY();
				y+=1;
				y = y % toIterate.getBoxMax().getY();
                this.nextCoord.setY(y);
				if (y==0) {
                    y+= toIterate.getBoxMin().getY();
                    this.nextCoord.setY(y);
                    int z = this.nextCoord.getZ();
					z+=1;
					z = z % toIterate.getBoxMax().getZ();
                    this.nextCoord.setZ(z);
					if (z == 0) {
                        z+= toIterate.getBoxMin().getZ();
                        this.nextCoord.setZ(z);
                        int c = this.nextCoord.getC();
						c+=1;
						c = c % toIterate.getBoxMax().getC();
                        this.nextCoord.setC(c);
						if (c == 0) {
                            c+= toIterate.getBoxMin().getC();
                            this.nextCoord.setC(c);
                            int t = this.nextCoord.getT();
							t+=1;
                            this.nextCoord.setT(t);

						}
					}
				}
			}

			//this.currCoord.recycle();
			//this.currCoord = ImageCoordinate.createCoord(x, y, z, c, t);
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
