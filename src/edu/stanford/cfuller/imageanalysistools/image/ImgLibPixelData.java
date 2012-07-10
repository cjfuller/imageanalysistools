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

import net.imglib2.img.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.RandomAccess;

/**
 * A type of PixelData that uses an ImgLib2 ImgPlus as its underlying representation.
 * 
 * @author Colin J. Fuller
 *
 */
public class ImgLibPixelData extends PixelData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1435719453195463339L;

	private ImgPlus<FloatType> imgpl;
	
	private RandomAccess<FloatType> ra;

	private static final String defaultDimensionOrder = ImageCoordinate.defaultDimensionOrder;

	private String dimensionOrder;

	final static int numDims = 5;
	
	boolean has_x;
	boolean has_y;
	boolean has_z;
	boolean has_c;
	boolean has_t;
	
	int xi;
	int yi;
	int zi;
	int ci;
	int ti;

	protected void init(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {
		
		this.dimensionOrder = dimensionOrder.toUpperCase();
		
		this.xi = this.dimensionOrder.indexOf("X");
		this.yi = this.dimensionOrder.indexOf("Y");
		this.zi = this.dimensionOrder.indexOf("Z");
		this.ci = this.dimensionOrder.indexOf("C");
		this.ti = this.dimensionOrder.indexOf("T");
		
	
		long[] dimensionSizeArray = new long[numDims];
		java.util.Arrays.fill(dimensionSizeArray, 0L);
		
		this.imgpl.dimensions(dimensionSizeArray);
		
		int numImpglDims = this.imgpl.numDimensions();
		
		if (this.xi < numImpglDims) {
			this.has_x = true;
			size_x = (int) dimensionSizeArray[this.xi];
		}
		if (this.yi < numImpglDims) {
			this.has_y = true;
			size_y = (int) dimensionSizeArray[this.yi];
		}
		if (this.zi < numImpglDims) {
			this.has_z = true;
			size_z = (int) dimensionSizeArray[this.zi];
		}
		if (this.ci < numImpglDims) {
			this.has_c = true;
			size_c = (int) dimensionSizeArray[this.ci];
		}
		if (this.ti < numImpglDims) {
			this.has_t = true;
			size_t = (int) dimensionSizeArray[this.ti];
		}
				
		this.pixels = null;

		this.size_x = size_x;
		this.size_y = size_y;
		this.size_z = size_z;
		this.size_c = size_c;
		this.size_t = size_t;

		this.byteOrder = java.nio.ByteOrder.BIG_ENDIAN;

	}
	
	/**
	* Gets the underlying ImgLib Img object.
	* @return the Img object that is used to store the pixel data (not a copy).
	*/
	public ImgPlus<FloatType> getImg() {
		
		return this.imgpl;
		
	}

	protected void initNewImgPlus() {
		
		this.dimensionOrder = this.defaultDimensionOrder;
		
		ArrayImgFactory<FloatType> imgf = new ArrayImgFactory<FloatType>();
		long[] dims = {this.size_x, this.size_y, this.size_z, this.size_c, this.size_t};
		Img<FloatType> im = imgf.create(dims, new FloatType());
		this.imgpl = new ImgPlus<FloatType>(im);
		this.ra = this.imgpl.randomAccess();
		
	}

	protected ImgLibPixelData() {}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate, int, String)
	 */
	public ImgLibPixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {
		super(sizes, data_type, dimensionOrder);
		this.initNewImgPlus();
	}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(int, int, int, int, int, int, String)
	 */
	public ImgLibPixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {

		super(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder);
		this.initNewImgPlus();

	}

	/**
	 * Creates a new ImgLibPixelData from an existing ImgPlus and a specified dimension order.
	 * 
	 * @param imPl	The ImgPlus to use.  This will not be copied, but used and potentially modified in place.
	 * @param dimensionOrder	a String containing the five characters XYZCT in the order they are in the image (if some dimensions are not present, the can be specified in any order)
	 */
	public ImgLibPixelData(ImgPlus<FloatType> imPl, String dimensionOrder) {

		this.imgpl = imPl;
		this.ra = this.imgpl.randomAccess();

		this.init(1, 1, 1, 1, 1, dimensionOrder);

		this.dataType = loci.formats.FormatTools.FLOAT;

	}

	/**
	* @deprecated makes it difficult not to duplicate storage in memory; use {@link #setPlane(int, int, int, byte[])} instead.
	* 
	* This will throw an UnsupportedOperationException if called.
	* 
	*/
	@Deprecated
	public void setBytes(byte[] bytes) {

		throw new UnsupportedOperationException("setBytes is not supported for ImgLibPixelData");

	}

	/**
	* This will throw an UnsupportedOperationException if called.
    */
	@Override
	public void setPlane(int zIndex, int cIndex, int tIndex, byte[] plane) {

		throw new UnsupportedOperationException("setPlane is not supported for ImgLibPixelData");
		

	}

	/**
	* This will throw an UnsupportedOperationException if called.
    */
	public void getBytes(byte[] bytes) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("getBytes is not supported for ImgLibPixelData");
	}

	/**
	* This will throw an UnsupportedOperationException if called.
    */
	public byte[] getPlane(int index) throws UnsupportedOperationException {

		throw new UnsupportedOperationException("getPlane is not supported for ImgLibPixelData");
		
	}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getPixel(int, int, int, int, int)
	 */
	public float getPixel(int x, int y, int z, int c, int t) {
		
		//if (this.has_x) this.ra.setPosition(x, this.xi);
		this.ra.setPosition(x, this.xi);
		//if (this.has_y) this.ra.setPosition(y, this.yi);
		this.ra.setPosition(y, this.yi);
		if (this.has_z) this.ra.setPosition(z, this.zi);
		if (this.has_c) this.ra.setPosition(c, this.ci);
		if (this.has_t) this.ra.setPosition(t, this.ti);

		return this.ra.get().get();

	}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#setPixel(int, int, int, int, int, float)
	 */
	public void setPixel(int x, int y, int z, int c, int t, float value) {
		
		//if (this.has_x) this.ra.setPosition(x, this.xi);
		this.ra.setPosition(x, this.xi);
		//if (this.has_y) this.ra.setPosition(y, this.yi);
		this.ra.setPosition(y, this.yi);
		if (this.has_z) this.ra.setPosition(z, this.zi);
		if (this.has_c) this.ra.setPosition(c, this.ci);
		if (this.has_t) this.ra.setPosition(t, this.ti);

		this.ra.get().set(value);
		
	}

	/**
	 * Does nothing.
	 */
	protected void updateConvertedPixelsFromBytes() {

	}

	/**
	 * Does nothing.
	 */
	protected void updateBytesFromConvertedPixels() {

	}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#toImagePlus()
	 */
	public ImagePlus toImagePlus() {
		return net.imglib2.img.display.imagej.ImageJFunctions.wrapFloat(this.imgpl, "");
	}

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
	public String getDimensionOrder() {
		return dimensionOrder;
	}

}

