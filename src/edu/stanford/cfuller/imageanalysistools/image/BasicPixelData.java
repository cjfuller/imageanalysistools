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

import ij.ImagePlus;


/**
 * Holds image pixel data in a type-independent manner; handles conversion to the appropriate number format for the data being stored to disk.
 *<p>
 * This implementation is currently rather memory inefficient for ease of converting back and forth to native format;
 * ImgLibPixelData (or its older counterpart, ImagePlusPixelData) are probably better choices.
 * <p>
 * It is recommended that PixelData objects be constructed with a {@link PixelDataFactory}, which will choose what version to use.
 *
 * @author Colin J. Fuller
 */

public class BasicPixelData extends WritablePixelData implements java.io.Serializable {

	//TODO: reimplement to handle images other than 5D.
	
	static final long serialVersionUID=19483561495701L;
		
	java.util.Hashtable<String, Integer> dimensionSizes;
	
	int dataType;
	
	int x_offset;
	int y_offset;
	int z_offset;
	int c_offset;
	int t_offset;
	
	java.util.Hashtable<String, Integer> offsetSizes;
	
	byte[] pixels;
	float[] convertedPixels;
	
	java.nio.ByteOrder byteOrder;
	

    protected BasicPixelData(){}

    /**
     * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */

	public BasicPixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {
        super(sizes, dimensionOrder);
		this.dataType = data_type;
	}

    /**
     * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
     * @param size_x    Size of the pixel data in the X-dimension.
     * @param size_y    Size of the pixel data in the Y-dimension.
     * @param size_z    Size of the pixel data in the Z-dimension.
     * @param size_c    Size of the pixel data in the C-dimension.
     * @param size_t    Size of the pixel data in the T-dimension.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
	public BasicPixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {
		super(size_x, size_y, size_z, size_c, size_t, dimensionOrder);

		this.dataType = data_type;
		
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
	protected void init(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {

		dimensionOrder = dimensionOrder.toUpperCase();
		pixels = null;
		
		dimensionSizes = new java.util.Hashtable<String, Integer>();
		
		this.size_x = size_x;
		this.size_y = size_y;
		this.size_z = size_z;
		this.size_c = size_c;
		this.size_t = size_t;
		
		convertedPixels = new float[this.size_x*this.size_y*this.size_z*this.size_c*this.size_t];
		
		dimensionSizes.put("X", size_x);
		dimensionSizes.put("Y", size_y);
		dimensionSizes.put("Z", size_z);
		dimensionSizes.put("C", size_c);
		dimensionSizes.put("T", size_t);
		
		offsetSizes = new java.util.Hashtable<String, Integer>();
		
		offsetSizes.put(dimensionOrder.substring(0,1), 1);
		
		for (int c = 1; c < dimensionOrder.length(); c++) {
			String curr = dimensionOrder.substring(c, c+1);
			String last = dimensionOrder.substring(c-1,c);


			offsetSizes.put(curr, dimensionSizes.get(last)*offsetSizes.get(last));
		}
		
		x_offset = offsetSizes.get("X");
		y_offset = offsetSizes.get("Y");
		z_offset = offsetSizes.get("Z");
		c_offset = offsetSizes.get("C");
		t_offset = offsetSizes.get("T");
		
		this.dimensionOrder = dimensionOrder;
		
		this.byteOrder = java.nio.ByteOrder.BIG_ENDIAN;
		
		
	}

    

    /**
    * @deprecated this method makes it more difficult not to maintain an extra copy of image data in memory.  Use {@link #setPlane(int, int, int, byte[])} instead.
	* 
    * Sets the raw byte representation of the pixel data to the specified array.
    *<p>
    * Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.
    * This will not be checked for the correct format.
    *<p>
    * The internal numerical representation of the pixel data will be updated immediately, and the data in the specified byte array will replace
    * any existing data.
    *
    * @param pixelBytes    A byte array containing the new pixel data.
    */
	@Deprecated
	public void setBytes(byte[] pixelBytes) {

		pixels = pixelBytes;
		
		updateConvertedPixelsFromBytes();
		
	}
	
	
	/**
    * Sets the raw byte representation of one plane of the pixel data to the specified array.
    *<p>
    * Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.
    * This will not be checked for the correct format.
    *<p>
    * The internal numerical representation of the pixel data will be updated immediately.
    * 
    * @param zIndex	  the z-dimension index of the plane being set (0-indexed)
    * @param cIndex	  the c-dimension index of the plane being set (0-indexed)
    * @param tIndex	  the t-dimension index of the plane being set (0-indexed)
    * @param plane    A byte array containing the new pixel data for the specified plane.
    */
	public void setPlane(int zIndex, int cIndex, int tIndex, byte[] plane) {
		
		if (!(this.dimensionOrder.startsWith("XY") || this.dimensionOrder.startsWith("YX"))) {
            throw new UnsupportedOperationException("Setting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX."); 
        }

		java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(plane);
				
		in.order(this.byteOrder);

						
		if (this.dataType == loci.formats.FormatTools.INT8) {
			
			java.nio.ByteBuffer convBuffer = in;
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}
			
        } else if (this.dataType == loci.formats.FormatTools.UINT8) {

			java.nio.ByteBuffer convBuffer = in;

			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}			
			
		} else if (this.dataType == loci.formats.FormatTools.INT16) {
		
			java.nio.ShortBuffer convBuffer = in.asShortBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}


        } else if (this.dataType == loci.formats.FormatTools.UINT16) {

			java.nio.ShortBuffer convBuffer = in.asShortBuffer();

			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}

		} else if (this.dataType == loci.formats.FormatTools.INT32) {
			
			java.nio.IntBuffer convBuffer = in.asIntBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}

        } else if (this.dataType == loci.formats.FormatTools.UINT32) {

			java.nio.IntBuffer convBuffer = in.asIntBuffer();

            for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}

			
		} else if (this.dataType == loci.formats.FormatTools.FLOAT) {
			
			java.nio.FloatBuffer convBuffer = in.asFloatBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (convBuffer.get(offset)));

				}
			}
			
			
		} else if (this.dataType == loci.formats.FormatTools.DOUBLE) {
		
			java.nio.DoubleBuffer convBuffer = in.asDoubleBuffer();
		
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}

        } else {
            
			java.nio.ByteBuffer convBuffer = in;


            for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));

				}
			}
			
		}

		
		
	}


    /**
    * Gets the raw byte representation of the pixel data.
    *<p>
    * Pixel values will be represented using the numeric type, byte order, and dimension order specified on initializing the PixelData.
    *<p>
    * Calling this function will encode the byte array data from the internal numerical representation, so in particular, if the byte data
    * was previously set using {@link #setBytes(byte[])}, and then changes were made using {@link #setPixel(int, int, int, int, int, float)}, for example, these changes will be reflected, and this
    * will not return the same byte data originally passed in.
    *
    * @return  A byte array containing the pixel data encoded in the specified format.
    */
	public byte[] getBytes() {
		
		updateBytesFromConvertedPixels();
		return this.pixels;
		
	}


    /**
     * Gets the number of bytes needed to store a single X-Y image plane in the underlying byte representation.
     * @return  The number of bytes for a single plane.
     */
    protected int getPlaneSizeInBytes() {
        return this.size_x * this.size_y * loci.formats.FormatTools.getBytesPerPixel(this.dataType);
    }

    /**
     * Get a single plane of the image formatted as a raw byte array.
     *<p>
     * A plane is defined as the extend of the PixelData in the x-y direction, so a single plane will reflect a single (Z,C,T) coordinate.
     *<p>
     * Note that this definition requires that the dimension order of the PixelData start with "XY".  If the dimension order were "ZCXYT" or something
     * similarly strange, then this method would not work correctly.
     *<p>
     * The byte array will be encoded as when calling {@link #getBytes}.
     *<p>
     * The planes are indexed according to the dimension order specified when initializing the PixelData.
     * 
     * @param index  The index of the plane to return in the specified dimension order.
     * @return      A byte array holding the requested plane encoded in the specified format.
     * @throws UnsupportedOperationException  if the dimension order does not start with XY or YX.
     */
	public byte[] getPlane(int index) throws UnsupportedOperationException{

		updateBytesFromConvertedPixels();

        if (!(this.dimensionOrder.startsWith("XY") || this.dimensionOrder.startsWith("YX"))) {
            throw new UnsupportedOperationException("Getting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX."); 
        }

		byte[] toReturn = new byte[getPlaneSizeInBytes()];
		System.arraycopy(pixels, index*toReturn.length, toReturn, 0, toReturn.length);
		return toReturn;
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
	public float getPixel(int x, int y, int z, int c, int t) {
		try {
		return convertedPixels[x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.printf("offsets: %d, %d, %d, %d, %d\n requested: %d, %d, %d, %d, %d\n", x_offset, y_offset, z_offset, c_offset, t_offset, x, y, z, c, t);
			throw e;
		}
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
	public void setPixel(int x, int y, int z, int c, int t, float value) {

		convertedPixels[x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset] = value;
		return;
	}
	
	protected int getPixelIndexForCoords(int x, int y, int z, int c, int t, float value) {
		return x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset;
	}
	
	protected int getCoordsForPixelIndex(int x, int y, int z, int c, int t, float value) {
		return x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset;
	}

    /**
     * Gets the data type used for the underlying byte array representation.
     *<p>
     * The meanings of the integers returned are specified as constants in {@link loci.formats.FormatTools}.
     *
     * @return  An integer corresponding to the format of the underlying byte representation of the data.
     */
	public int getDataType() {return this.dataType;}



    /**
     * Reads the underlying byte array using the specified byte order, dimension order, and data format and stores it
     * as an array of floats, which is accessed by users of this class.
     */
	protected void updateConvertedPixelsFromBytes() {

        //converts the byte array representation of the data into the internal float representation of the data

		java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(this.pixels);
				
		in.order(this.byteOrder);
		
		
		int counter = 0;
		
		
		switch (this.dataType) {
						
			
		case loci.formats.FormatTools.INT8:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) (in.get());
			}
            break;
        
        case loci.formats.FormatTools.UINT8:

			while(in.hasRemaining()) {
                byte b = in.get();
				convertedPixels[counter++] = (float) (b & 0xFF); // this will convert b to int by bits, not by value
			}
            break;
			
			
		case loci.formats.FormatTools.INT16:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) (in.getShort());
			}
            break;


        case loci.formats.FormatTools.UINT16:

			while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) ((in.getShort()) & 0xFFFF);
			}
            break;

		case loci.formats.FormatTools.INT32:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) (in.getInt());
			}
            break;

        case loci.formats.FormatTools.UINT32:

            while(in.hasRemaining()) {
                convertedPixels[counter++] = (float) (0xFFFFFFFFL & (in.getInt()));
            }
            break;

			
		case loci.formats.FormatTools.FLOAT:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (in.getFloat());
			}
            break;
			
			
		case loci.formats.FormatTools.DOUBLE:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) (in.getDouble());
			}
            break;

        default:
            
            while(in.hasRemaining()) {
				convertedPixels[counter++] = (float) (in.get() > 0 ? 1.0 : 0.0);
			}
            break;
			
		}
		
		this.pixels = null;

	}

    /**
     * Converts the array of floats used for access by users of this class back to a byte array representation (suitable for writing to disk)
     * according to the stored byte order, dimension order, and data type.
     */
	protected void updateBytesFromConvertedPixels() {


        //converts the internal float representation of the data into the byte array representation

		java.nio.ByteBuffer bytes_out = null;
		
		if (this.pixels != null) {
			
			bytes_out = java.nio.ByteBuffer.allocate(this.pixels.length);
			
		} else {
			
			bytes_out = java.nio.ByteBuffer.allocate(this.convertedPixels.length * loci.formats.FormatTools.getBytesPerPixel(this.dataType));

		}
				
		bytes_out.order(this.byteOrder);
		
		switch (this.dataType) {

		case loci.formats.FormatTools.INT8:
        case loci.formats.FormatTools.UINT8:
			
			for (float pixel: this.convertedPixels) {
				bytes_out.put((byte) pixel);
			}
			
			break;
			
		case loci.formats.FormatTools.INT16:
        case loci.formats.FormatTools.UINT16:
			
			for (float pixel: this.convertedPixels) {

                bytes_out.putShort((short) pixel);
                
			}
			
			break;
			
		case loci.formats.FormatTools.INT32:
        case loci.formats.FormatTools.UINT32:
			
			for (float pixel: this.convertedPixels) {
				bytes_out.putInt((int) pixel);
			}
            break;
			
		case loci.formats.FormatTools.FLOAT:
			
			for (float pixel: this.convertedPixels) {
				bytes_out.putFloat(pixel);
			}
			
			break;
			
		case loci.formats.FormatTools.DOUBLE:
			for (float pixel: this.convertedPixels) {
				bytes_out.putDouble((double) pixel);
			}
            break;

        default:
            for (float pixel : this.convertedPixels) {
				bytes_out.put((byte) ((pixel > 0) ? 1 : 0));
			}
            break;

		}
		
		this.pixels = bytes_out.array();
		

	}

    /**
     * Sets the byte order of the underlying byte array representation to one of the constants specified in {@link java.nio.ByteOrder}
     * @param b     The ByteOrder constant to which the byte order of the PixelData will be set.
     */
	public void setByteOrder(java.nio.ByteOrder b) {
		this.byteOrder = b;
	}

    /**
     * Gets the byte order of the underlying byte array representation of the data, as one of the constants specified in {@link java.nio.ByteOrder}.
     * @return      The ByteOrder constant corresponding to the byte order of the underlying byte array representation of the data.
     */
	public java.nio.ByteOrder getByteOrder() {
		return this.byteOrder;
	}
	
	/** (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#toImagePlus()
	 */
	public ImagePlus toImagePlus() {
		return null;
	}
	
}
