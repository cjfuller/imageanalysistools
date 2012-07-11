/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.image;

import ij.ImagePlus;

/**
* PixelData objects hold the (5-dimensional X, Y, Z, channel, time) image 
* values themselves for classes implementing Image.
* <p>
* A class extending PixelData need only supply methods for intialization and reading.
* For read-write access, see {@link WritablePixelData}
* <p>
* It is recommended to obtain a PixelData object using {@link PixelDataFactory} rather
* than instantiating a particular implementation.
* 
* @author Colin J. Fuller
*/
public abstract class PixelData implements java.io.Serializable {
	
	static final long serialVersionUID=1495619456L;
	
	int size_x;
	int size_y;
	int size_z;
	int size_c;
	int size_t;
	
	String dimensionOrder;
	
	protected PixelData() {}
	
	/**
     * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
	public PixelData(ImageCoordinate sizes, String dimensionOrder) {
		init(sizes.get(ImageCoordinate.X), sizes.get(ImageCoordinate.Y), sizes.get(ImageCoordinate.Z), sizes.get(ImageCoordinate.C), sizes.get(ImageCoordinate.T), dimensionOrder);
	}

    /**
     * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
     * @param size_x    Size of the pixel data in the X-dimension.
     * @param size_y    Size of the pixel data in the Y-dimension.
     * @param size_z    Size of the pixel data in the Z-dimension.
     * @param size_c    Size of the pixel data in the C-dimension.
     * @param size_t    Size of the pixel data in the T-dimension.
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
	public PixelData(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {
		init(size_x, size_y, size_z, size_c, size_t, dimensionOrder);		
	}
	
	/**
     * Initializes the internals of the PixelData object using the specified parameters.
     *
     * @param size_x    The size of the PixelData in the x-dimension (in pixels).
     * @param size_y    The size of the PixelData in the y-dimension (in pixels).
     * @param size_z    The size of the PixelData in the z-dimension (in pixels).
     * @param size_c    The size of the PixelData in the c-dimension (in pixels).
     * @param size_t    The size of the PixelData in the t-dimension (in pixels).
     * @param dimensionOrder    A string containing the 5 characters "XYZCT" in some order specifying the order in which the dimensions
     *                          are stored in the underlying byte representation.
     * 
     */
	protected abstract void init(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder);
	
	/**
     * Queries whether the PixelData object has a non-singleton Z dimension.
     * @return  true if the Z-dimension size is greater than 1, false otherwise.
     */
	public boolean hasZ() {
		return size_z > 1;
	}

	/**
     * Queries whether the PixelData object has a non-singleton T dimension.
     * @return  true if the T-dimension size is greater than 1, false otherwise.
     */
	public boolean hasT() {
		return size_t > 1;
	}

    /**
     * Queries whether the PixelData object has a non-singleton C dimension.
     * @return  true if the C-dimension size is greater than 1, false otherwise.
     */
	public boolean hasC() {
		return size_c > 1;
	}
	
	/**
     * Gets the number of planes in the image.
     *<p>
     * A plane is defined as the extent of the PixelData in the x-y direction, so the number of planes is the sizes of all dimensions besides X and Y multiplied together.
     *
     * @return  The number of planes in the PixelData.
     */
	public int getNumPlanes() {
		return size_z*size_t*size_c;
	}
	
	/**
     * Gets the size of the x-dimension of the PixelData.
     * @return  The size of the x-dimension (in pixels).
     */
	public int getSizeX() {
		return this.size_x;
	}

    /**
     * Gets the size of the y-dimension of the PixelData.
     * @return  The size of the y-dimension (in pixels).
     */
	public int getSizeY() {
		return this.size_y;
	}

    /**
     * Gets the size of the z-dimension of the PixelData.
     * @return  The size of the z-dimension (in pixels, or equivalently, planes).
     */
	public int getSizeZ() {
		return this.size_z;
	}

    /**
     * Gets the size of the c-dimension of the PixelData.
     * @return  The size of the c-dimension (in pixels, or equivalently, color channels).
     */
	public int getSizeC() {
		return this.size_c;
	}

    /**
     * Gets the size of the t-dimension of the PixelData.
     * @return  The size of the t-dimension (in pixels, or equivalently, timepoints).
     */
	public int getSizeT() {
		return this.size_t;
	}
	
	/**
     * Gets the dimension order of the underlying byte representation of the PixelData as a string.
     *<p>
     * An example string for one particular dimension order is "XYZCT"
     *
     * @return  A String containing one character for each dimension specifying the dimension order.
     */
	public String getDimensionOrder() {
		return this.dimensionOrder;
	}
	
	/**
     * Get a single plane of the image formatted as a raw byte array. (Optional operation.)
     * <p>
     * Always throws an UnsupportedOperationException unless overriden by a subclass.
     * 
     * @param index  The index of the plane to return in the specified dimension order.
     * @return      A byte array holding the requested plane encoded in the specified format.
     * @throws UnsupportedOperationException  if not overridden by a subclass.
     */
	public byte[] getPlane(int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("getPlane not supported by PixelData");
	}

	/**
     * Gets the value of a single pixel at the specified coordinates.
     *<p>
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     *
     * (All coordinates are zero-indexed.)
     *
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @return      The value of the PixelData at the specified coordinates, as a float.
     */
	public abstract float getPixel(int x, int y, int z, int c, int t);
	
	/**
     * Gets the data type used for the underlying byte array representation.
     *<p>
     * The meanings of the integers returned are specified as constants in {@link loci.formats.FormatTools}.
     *
     * @return  An integer corresponding to the format of the underlying byte representation of the data.
     */
	public abstract int getDataType();
	
	
	/**
	 * Returns an ImagePlus representation of the data contained in this PixelData.
	 * <p>
	 * This may return null if an ImagePlus is not involved in the representation of the PixelData.
	 * 
	 * @return	An ImagePlus representation of the PixelData, or null.
	 */
	public abstract ImagePlus toImagePlus();
	
}

