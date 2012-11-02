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


/**
* An extension of a PixelData object that also supports writing of image data.
* @see PixelData
* 
* @author Colin J. Fuller
*/
public abstract class WritablePixelData extends PixelData implements java.io.Serializable {

	static final long serialVersionUID=7975917259L;
	
	protected WritablePixelData(){}
	
	/**
     * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
	public WritablePixelData(ImageCoordinate sizes, String dimensionOrder) {
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
	public WritablePixelData(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {
		init(size_x, size_y, size_z, size_c, size_t, dimensionOrder);		
	}

	/**
     * Gets the value of a single pixel at the specified coordinates.
     *<p>
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     *<p>
     * Likewise, though the value is passed as a float, it will be converted automatically to the underlying byte representation in the correct format.
     * This may lead to the truncation of the passed float value when retrieving the byte array representation.  However, the float passed in can still be retreived
     * without truncation by calling {@link #getPixel(int, int, int, int, int)}.
     *
     *<p>
     * (All coordinates are zero-indexed.)
     *
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @param value The value to which the pixel at the specified coordinates will be set.
     */
	public abstract void setPixel(int x, int y, int z, int c, int t, float value);
	

}
