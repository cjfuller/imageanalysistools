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
 * An Image representation that can only be read, and not written.
 *
 * This class of Image primarily exists to allow sharing of PixelData and metadata among multiple copies of an Image that
 * only need to read the Image data, but still allow them to iterate over the coordinates in the image and box regions of interest
 * independently.
 *
 * @author Colin J. Fuller
 *
 */
public class ReadOnlyImage extends Image {

	//TODO: consider whether this class should be a superclass of Image (or whether some other arrangement of the heirarchy might be better? both extend AbstractImage, for example? Or explicitly call Image WritableImage?)
	
	final static long serialVersionUID = 1L;
    
    public ReadOnlyImage(loci.formats.meta.IMetadata m, PixelData p) {
        super(m, p);
    }


    /**
     * Copy constructor.  This makes a shallow copy of the Image to copy for pixel data and metadata, but gives independent
     * iteration and boxing mechanisms.
     * @param toCopy        The Image to copy.
     */
    public ReadOnlyImage(Image toCopy) {
        this.isBoxed = false;
        this.boxMin = null;
        this.boxMax = null;
        this.coordinateArrayStorage = null;
        this.pixelData = toCopy.pixelData;
        this.dimensionSizes = ImageCoordinate.cloneCoord(toCopy.getDimensionSizes());
        this.metadata = toCopy.metadata;
    }

    /**
     * Overrides the copy function to throw an UnsupportedOperationException.
     *
     * @param other     The Image whose pixel values will (not) be copied.
     */
    @Override
    public void copy(Image other) {
        throw new UnsupportedOperationException("Copy is not supported for read-only Images.");
    }


    /**
     * Overrides the setValue function to throw an UnsupportedOperationException.
     * @deprecated  Use {@link #setValue(ImageCoordinate, double)} instead.
     * @param row   The row (y-coordinate) to set.
     * @param col   The column (x-coordinate) to set.
     * @param value The value that the Image will be set to at the specified location.
     */
    @Override
    public void setValue(int row, int col, double value) {
        throw new UnsupportedOperationException("Setting values is not supported for read-only Images.");
    }

    /**
     * Overrides the setValue function to throw an UnsupportedOperationException.
     * @param coord     The ImageCoordinate at which to set the Image's value.
     * @param value     The value to which to set the Image at the specified coordinate.
     */
    @Override
    public void setValue(ImageCoordinate coord, double value) {
        throw new UnsupportedOperationException("Setting values is not supported for read-only Images.");
    }

    /**
     * Finalization.
     *
     * Just calls the superclass finalize for now.
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable{

        super.finalize();
    }



    
}
