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


/**
 * Holds image pixel data in a type-independent manner; handles conversion to the appropriate number format for the data being stored to disk.
 *<p>
 * This implementation is currently rather memory inefficient in order to be able to store any image format in a common way;
 * large images should use {@link LargePixelData}, which will cache all but the current plane in use to disk, as needed.
 * <p>
 * It is recommended that PixelData objects be constructed with a {@link PixelDataFactory}, which will choose whether to
 * use the in-memory or disk-using version depending on the size of the image.
 *
 * @author Colin J. Fuller
 */

public class PixelData implements java.io.Serializable {

	//TODO: reimplement to handle images other than 5D.
	
	static final long serialVersionUID=1L;
	
	int dataType;
	
	int size_x;
	int size_y;
	int size_z;
	int size_c;
	int size_t;
	
	java.util.Hashtable<String, Integer> dimensionSizes;
	
	int x_offset;
	int y_offset;
	int z_offset;
	int c_offset;
	int t_offset;
	
	java.util.Hashtable<String, Integer> offsetSizes;
	
	byte[] pixels;
	double[] convertedPixels;
	
	java.nio.ByteOrder byteOrder;
	
	String dimensionOrder;

    protected PixelData(){}

    /**
     * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */

	public PixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {
        this.dataType = data_type;

		init(sizes.get("x"), sizes.get("y"), sizes.get("z"), sizes.get("c"), sizes.get("t"), dimensionOrder);

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
	public PixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {

		this.dataType = data_type;

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
	protected void init(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {

		dimensionOrder = dimensionOrder.toUpperCase();
		convertedPixels = new double[size_x*size_y*size_z*size_c*size_t];
		pixels = null;
		
		dimensionSizes = new java.util.Hashtable<String, Integer>();
		
		this.size_x = size_x;
		this.size_y = size_y;
		this.size_z = size_z;
		this.size_c = size_c;
		this.size_t = size_t;
		
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
	public void setBytes(byte[] pixelBytes) {

		pixels = pixelBytes;
		
		updateConvertedPixelsFromBytes();
		
	}


    /**
     * Gets the raw byte representation of the pixel data.
     *<p>
     * Pixel values will be represented using the numeric type, byte order, and dimension order specified on initializing the PixelData.
     *<p>
     * Calling this function will encode the byte array data from the internal numerical representation, so in particular, if the byte data
     * was previously set using {@link #setBytes(byte[])}, and then changes were made using {@link #setPixel(int, int, int, int, int, double)}, for example, these changes will be reflected, and this
     * will not return the same byte data originally passed in.
     *
     * @return  A byte array containing the pixel data encoded in the specified format.
     */
	public byte[] getBytes() {
		
		updateBytesFromConvertedPixels();
		return this.pixels;
		
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
     * @return      The value of the PixelData at the specified coordinates, as a double.
     */
	public double getPixel(int x, int y, int z, int c, int t) {
		return convertedPixels[x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset];
	}


	/**
     * Gets the value of a single pixel at the specified coordinates.
     *<p>
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     *<p>
     * Likewise, though the value is passed as a double, it will be converted automatically to the underlying byte representation in the correct format.
     * This may lead to the truncation of the passed double value when retrieving the byte array representation.  However, the double passed in can still be retreived
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
	public void setPixel(int x, int y, int z, int c, int t, double value) {

		convertedPixels[x*x_offset + y*y_offset + z*z_offset + c*c_offset + t*t_offset] = value;
		return;
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
     * Gets the data type used for the underlying byte array representation.
     *<p>
     * The meanings of the integers returned are specified as constants in {@link loci.formats.FormatTools}.
     *
     * @deprecated  Use {@link #getDataType()} instead.
     * @return  An integer corresponding to the format of the underlying byte representation of the data.
     */
	public int getLociDataType() {return this.dataType;}


    /**
     * Reads the underlying byte array using the specified byte order, dimension order, and data format and stores it
     * as an array of doubles, which is accessed by users of this class.
     */
	protected void updateConvertedPixelsFromBytes() {

        //converts the byte array representation of the data into the internal double representation of the data

		java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(this.pixels);
				
		in.order(this.byteOrder);
		
		
		int counter = 0;
		
		
		switch (this.dataType) {
						
			
		case loci.formats.FormatTools.INT8:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.get());
			}
            break;
        
        case loci.formats.FormatTools.UINT8:

			while(in.hasRemaining()) {
                byte b = in.get();
				convertedPixels[counter++] = (double) (b & 0xFF); // this will convert b to int by bits, not by value
			}
            break;
			
			
		case loci.formats.FormatTools.INT16:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.getShort());
			}
            break;


        case loci.formats.FormatTools.UINT16:

			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) ((in.getShort()) & 0xFFFF);
			}
            break;

		case loci.formats.FormatTools.INT32:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.getInt());
			}
            break;

        case loci.formats.FormatTools.UINT32:

            while(in.hasRemaining()) {
                convertedPixels[counter++] = (double) (0xFFFFFFFFL & (in.getInt()));
            }
            break;

			
		case loci.formats.FormatTools.FLOAT:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.getFloat());
			}
            break;
			
			
		case loci.formats.FormatTools.DOUBLE:
			
			while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.getDouble());
			}
            break;

        default:
            
            while(in.hasRemaining()) {
				convertedPixels[counter++] = (double) (in.get() > 0 ? 1.0 : 0.0);
			}
            break;
			
		}

	}

    /**
     * Converts the array of doubles used for access by users of this class back to a byte array representation (suitable for writing to disk)
     * according to the stored byte order, dimension order, and data type.
     */
	protected void updateBytesFromConvertedPixels() {


        //converts the internal double representation of the data into the byte array representation

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
			
			for (double pixel: this.convertedPixels) {
				bytes_out.put((byte) pixel);
			}
			
			break;
			
		case loci.formats.FormatTools.INT16:
        case loci.formats.FormatTools.UINT16:
			
			for (double pixel: this.convertedPixels) {

                bytes_out.putShort((short) pixel);
                
			}
			
			break;
			
		case loci.formats.FormatTools.INT32:
        case loci.formats.FormatTools.UINT32:
			
			for (double pixel: this.convertedPixels) {
				bytes_out.putInt((int) pixel);
			}
            break;
			
		case loci.formats.FormatTools.FLOAT:
			
			for (double pixel: this.convertedPixels) {
				bytes_out.putFloat((float) pixel);
			}
			
			break;
			
		case loci.formats.FormatTools.DOUBLE:
			for (double pixel: this.convertedPixels) {
				bytes_out.putDouble(pixel);
			}
            break;

        default:
            for (double pixel : this.convertedPixels) {
				bytes_out.put((byte) ((pixel > 0) ? 1 : 0));
			}
            break;

		}
		
		this.pixels = bytes_out.array();
		

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
	
}
