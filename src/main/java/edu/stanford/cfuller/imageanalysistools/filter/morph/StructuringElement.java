/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.filter.morph;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * This class represents a structuring element for use in morphological image processing.
 * 
 * @author Colin J. Fuller
 *
 */
public class StructuringElement {

    ImageCoordinate size;
    float[][][][][] elements;
	
    /**
     * Constructs a new StructuingElement from and ImageCoordinate specifying its size in each dimension.
     * <p>
     * All dimensions must have odd size, but this is not checked.  For dimensions over which you do not wish to apply the
     * morphological operation, specify size 1.
     * 
     * @param size	an ImageCoordinate containing the sizes of each dimension.
     */
    public StructuringElement(ImageCoordinate size) {
	this.size = ImageCoordinate.cloneCoord(size);
	this.elements = new float[this.size.get(ImageCoordinate.T)][this.size.get(ImageCoordinate.C)][this.size.get(ImageCoordinate.Z)][this.size.get(ImageCoordinate.Y)][this.size.get(ImageCoordinate.X)];
    }

    /**
     * Sets the box of interest in an Image to be the size of the structuring element surrounding a specified position in that image.
     * 
     * @param currentPosition	The position around which the box will be centered.
     * @param toBeBoxed			The Image that will be boxed.
     */
    public void boxImageToElement(ImageCoordinate currentPosition, Image toBeBoxed) {
		
	ImageCoordinate lowerBound = ImageCoordinate.cloneCoord(currentPosition);
	ImageCoordinate upperBound = ImageCoordinate.cloneCoord(currentPosition);
		
	for (Integer i : currentPosition) {
	    lowerBound.set(i, lowerBound.get(i) - (size.get(i) - 1)/2);
	    upperBound.set(i, upperBound.get(i) + (size.get(i) - 1)/2 + 1);
	}
		
	toBeBoxed.setBoxOfInterest(lowerBound, upperBound, false);	        
    }
	
    /**
     * Gets the value of the StructuringElement at a location specified in the coordinates of the StructuringElement. 
     * 
     * @param strelCoord	The coordinate from which to retrieve the value.
     * @return				The value of the StructuringElement at that point.
     */
    public float get(ImageCoordinate strelCoord) {
	return elements[strelCoord.get(ImageCoordinate.T)][strelCoord.get(ImageCoordinate.C)][strelCoord.get(ImageCoordinate.Z)][strelCoord.get(ImageCoordinate.Y)][strelCoord.get(ImageCoordinate.X)];
    }
	
    /**
     * Gets the value of the StructuringElement at a location in the coordinates of an Image.
     * @param strelCenterImageCoord		The coordinate in the image where the structuring element is centered.
     * @param imageCoord				The corresponding coordinate in the image from which to retrieve the value of the structuring element.
     * @return							The value of the StructuringElement at that point.
     */
    public float get(ImageCoordinate strelCenterImageCoord, ImageCoordinate imageCoord) {
	int t = imageCoord.get(ImageCoordinate.T) - strelCenterImageCoord.get(ImageCoordinate.T) + (size.get(ImageCoordinate.T) -1)/2;
	int c = imageCoord.get(ImageCoordinate.C) - strelCenterImageCoord.get(ImageCoordinate.C) + (size.get(ImageCoordinate.C) -1)/2;
	int z = imageCoord.get(ImageCoordinate.Z) - strelCenterImageCoord.get(ImageCoordinate.Z) + (size.get(ImageCoordinate.Z) -1)/2;
	int y = imageCoord.get(ImageCoordinate.Y) - strelCenterImageCoord.get(ImageCoordinate.Y) + (size.get(ImageCoordinate.Y) -1)/2;
	int x = imageCoord.get(ImageCoordinate.X) - strelCenterImageCoord.get(ImageCoordinate.X) + (size.get(ImageCoordinate.X) -1)/2;
	return elements[t][c][z][y][x];
    }
	
    /**
     * Sets the value at every location in a StructuringElement to the specified value.
     * @param value		The value to which to set the StructuringElement.
     */
    public void setAll(float value) {
	for (int t = 0; t < this.size.get(ImageCoordinate.T); t++) {
	    for (int c = 0; c < this.size.get(ImageCoordinate.C); c++) {
		for (int z = 0; z < this.size.get(ImageCoordinate.Z); z++) {
		    for (int y = 0; y < this.size.get(ImageCoordinate.Y); y++) {
			for (int x = 0; x < this.size.get(ImageCoordinate.X); x++) {
			    elements[t][c][z][y][x] = value;
			}
		    }
		}
	    }
	}
    }
	
    /**
     * Sets the value of the StructuringElement at a location in the coordinates of an Image.
     * @param strelCenterImageCoord		The coordinate in the image where the structuring element is centered.
     * @param imageCoord				The corresponding coordinate in the image where the value of the structuring element will be set.
     * @param value						The value to which to set the structuring element.
     */
    public void set(ImageCoordinate strelCenterImageCoord, ImageCoordinate imageCoord, float value) {
	int t = imageCoord.get(ImageCoordinate.T) - strelCenterImageCoord.get(ImageCoordinate.T) - (size.get(ImageCoordinate.T) -1)/2;
	int c = imageCoord.get(ImageCoordinate.C) - strelCenterImageCoord.get(ImageCoordinate.C) - (size.get(ImageCoordinate.C) -1)/2;
	int z = imageCoord.get(ImageCoordinate.Z) - strelCenterImageCoord.get(ImageCoordinate.Z) - (size.get(ImageCoordinate.Z) -1)/2;
	int y = imageCoord.get(ImageCoordinate.Y) - strelCenterImageCoord.get(ImageCoordinate.Y) - (size.get(ImageCoordinate.Y) -1)/2;
	int x = imageCoord.get(ImageCoordinate.X) - strelCenterImageCoord.get(ImageCoordinate.X) - (size.get(ImageCoordinate.X) -1)/2;
	elements[t][c][z][y][x] = value;
    }
	
    /**
     * Sets the value of the StructuringElement at a location specified in the coordinates of the StructuringElement. 
     * 
     * @param strelCoord	The coordinate at which to set the value.
     * @param value			The value to which to set the structuring element.
     */
    public void set(ImageCoordinate strelCoord, float value) {
	elements[strelCoord.get(ImageCoordinate.T)][strelCoord.get(ImageCoordinate.C)][strelCoord.get(ImageCoordinate.Z)][strelCoord.get(ImageCoordinate.Y)][strelCoord.get(ImageCoordinate.X)] = value;
    }
	
}
