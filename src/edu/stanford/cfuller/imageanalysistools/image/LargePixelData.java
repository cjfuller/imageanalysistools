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

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Holds image pixel data in a type-independent manner; handles conversion to the appropriate number format for the data being stored to disk.
 *<p>
 * This implementation uses a smaller memory footprint than a {@link PixelData} object by potentially only keeping a single
 * image plane active at a time.  The remainder of the image is stored in a memory-mapped file and converted to and from the double values
 * available to users of this class as needed.  This may lead to extra overhead when repeatedly retrieving pixel values from different planes,
 * so where possible, it is recommended to structure code to do all the single plane processing possible before switching to another plane.
 * <p>
 * It is recommended that PixelData objects be constructed with a {@link PixelDataFactory}, which will choose whether to
 * use the in-memory or disk-using version depending on the size of the image.
 *
 * @author Colin J. Fuller
 */

public class LargePixelData extends PixelData {

	final static long serialVersionUID = 1L;
	
    private RandomAccessFile tempFileStorage;
    private FileChannel tempFileStorageChannel;
    private MappedByteBuffer mappedData;
    private int current_z;
    private int current_c;
    private int current_t;

    protected LargePixelData(){}

    /**
     * Constructs a new LargePixelData object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
    public LargePixelData(ImageCoordinate sizes, int data_type, String dimensionOrder){
        super(sizes, data_type, dimensionOrder);
    }

    /**
     * Constructs a LargePixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
     * @param size_x    Size of the pixel data in the X-dimension.
     * @param size_y    Size of the pixel data in the Y-dimension.
     * @param size_z    Size of the pixel data in the Z-dimension.
     * @param size_c    Size of the pixel data in the C-dimension.
     * @param size_t    Size of the pixel data in the T-dimension.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
    public LargePixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {
        super(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder);
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

        dimensionOrder = dimensionOrder.toLowerCase();
        this.dimensionOrder = dimensionOrder;

        if (!(this.dimensionOrder.startsWith("xy") || this.dimensionOrder.startsWith("yx"))) {
            throw new UnsupportedOperationException("Large Images are not supported for dimension orders not starting with XY or YX.");
        }

        convertedPixels = new double[size_x*size_y];
        pixels = null;

        dimensionSizes = new java.util.Hashtable<String, Integer>();

        this.size_x = size_x;
        this.size_y = size_y;
        this.size_z = size_z;
        this.size_c = size_c;
        this.size_t = size_t;

        dimensionSizes.put(ImageCoordinate.X, size_x);
        dimensionSizes.put(ImageCoordinate.Y, size_y);
        dimensionSizes.put(ImageCoordinate.Z, size_z);
        dimensionSizes.put(ImageCoordinate.C, size_c);
        dimensionSizes.put(ImageCoordinate.T, size_t);

        offsetSizes = new java.util.Hashtable<String, Integer>();

        offsetSizes.put(dimensionOrder.substring(0,1), 1);

        for (int c = 1; c < dimensionOrder.length(); c++) {
            String curr = dimensionOrder.substring(c, c+1);
            String last = dimensionOrder.substring(c-1,c);


            offsetSizes.put(curr, dimensionSizes.get(last)*offsetSizes.get(last));
        }

        x_offset = offsetSizes.get(ImageCoordinate.X);
        y_offset = offsetSizes.get(ImageCoordinate.Y);
        z_offset = offsetSizes.get(ImageCoordinate.Z);
        c_offset = offsetSizes.get(ImageCoordinate.C);
        t_offset = offsetSizes.get(ImageCoordinate.T);


        this.byteOrder = java.nio.ByteOrder.BIG_ENDIAN;

        this.current_c = 0;
        this.current_t = 0;
        this.current_z = 0;

        try {

            File tempFile = File.createTempFile("pixel", null);
            tempFile.deleteOnExit();
            tempFileStorage = new RandomAccessFile(tempFile, "rw");

        } catch (java.io.IOException e) {

            LoggingUtilities.getLogger().severe("Could not get temporary storage for LargePixelData");
            e.printStackTrace();

        }

        tempFileStorageChannel = tempFileStorage.getChannel();

        initMappedPixelData();


    }


    /**
     * Maps a temporary file into memory to be used for the storage of the image binary data.
     */
    protected void initMappedPixelData() {

        int imageSizeBytes = this.size_x*this.size_y*this.size_z*this.size_t*this.size_c*loci.formats.FormatTools.getBytesPerPixel(this.dataType);
        try {
            mappedData = tempFileStorageChannel.map(FileChannel.MapMode.READ_WRITE, 0, imageSizeBytes);

            mappedData.position(0);

            mappedData.order(this.byteOrder);

            updateConvertedPixelsFromBytes();

        } catch (java.io.IOException e) {
            LoggingUtilities.getLogger().severe("Error while mapping pixel bytes to disk.");
            e.printStackTrace();
        }
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
    public synchronized void setBytes(byte[] pixelBytes) {

        mappedData.position(0);

        mappedData.order(this.byteOrder);

        mappedData.put(pixelBytes);

        this.current_c = 0;
        this.current_t = 0;
        this.current_z = 0;

        this.updateConvertedPixelsFromBytes();


    }

    /**
     * Determines the number of bytes into the byte array representation of the current pixel data where the current plane
     * is located.
     * @return  The offset of the first byte of the current plane.
     */
    protected int getByteOffsetForCurrentPlane() {
        int pixelOffset =  this.current_c*c_offset + this.current_t*t_offset + this.current_z*z_offset;
        return pixelOffset * loci.formats.FormatTools.getBytesPerPixel(this.dataType);
    }


    /**
     * Reads the underlying byte array using the specified byte order, dimension order, and data format and stores the current
     * plane from this array as an array of doubles, which is accessed by users of this class.
     * <p>
     * This method is called internally whenever switching between image planes to load the currently selected plane
     * from the memory mapped file to the double array representation of the current plane.
     */
    protected void updateConvertedPixelsFromBytes() {

        //converts the byte array representation of the data into the internal double representation of the data

        int counter = 0;

        this.mappedData.position(this.getByteOffsetForCurrentPlane());

        ByteBuffer in = mappedData.slice();

        in.limit(getPlaneSizeInBytes());

        in.order(this.byteOrder);

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

        //System.out.println(java.util.Arrays.toString(convertedPixels));

    }

    /**
     * Converts the array of doubles used for access by users of this class back to a byte array representation according
     * to the stored byte order, dimension order, and data type.
     * <p>
     * This method is called internally before switching to a new image plane to write the current double array representation
     * if the current image plane back to the memory-mapped file.
     */
    protected void updateBytesFromConvertedPixels() {

        //converts the internal double representation of the data into the byte array representation

        mappedData.position(getByteOffsetForCurrentPlane());


        java.nio.ByteBuffer bytes_out = mappedData.slice();
        bytes_out.limit(getPlaneSizeInBytes());
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


    }




    /**
     * Get a single plane of the image formatted as a raw byte array.
     *<p>
     * A plane is defined as the extend of the PixelData in the x-y direction, so a single plane will reflect a single (Z,C,T) coordinate.
     *<p>
     * The byte array will be encoded as when calling {@link #getBytes}.
     *<p>
     * The planes are indexed according to the dimension order specified when initializing the PixelData.
     *
     * @param index  The index of the plane to return in the specified dimension order.
     * @return      A byte array holding the requested plane encoded in the specified format.
     * @throws UnsupportedOperationException  if the dimension order does not start with XY or YX.  This exception should never be thrown
     *                                          for the LargePixelData, as other dimension orderings are not supported for large images.
     */

    public synchronized byte[] getPlane(int index){

        updateBytesFromConvertedPixels();

        byte[] toReturn = new byte[getPlaneSizeInBytes()];

        this.mappedData.position(toReturn.length*index);

        ByteBuffer temp = this.mappedData.slice();

        temp.limit(getPlaneSizeInBytes());

        ByteBuffer toReturnBuffer = ByteBuffer.wrap(toReturn);

        toReturnBuffer.position(0);

        toReturnBuffer.put(temp);

        return toReturn;
    }




    /**
     * Gets the value of a single pixel at the specified coordinates.
     * <p>
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     * <p>
     * This method will have better performance on repeated calls within the same image plane than on repeated calls switching
     * among different image planes, so a minimum of plane switching is recommended.
     * <p>
     * (All coordinates are zero-indexed.)
     *
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @return      The value of the PixelData at the specified coordinates, as a double.
     */
    public synchronized double getPixel(int x, int y, int z, int c, int t) {
        if (z == current_z && c == current_c && t == current_t) {
            return convertedPixels[x*x_offset + y*y_offset];
        } else {
            this.updateBytesFromConvertedPixels();
            current_z = z;
            current_c = c;
            current_t = t;
            this.updateConvertedPixelsFromBytes();
            return getPixel(x,y,z,c,t);
        }

    }



    /**
     * Gets the value of a single pixel at the specified coordinates.
     * <p>
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     * <p>
     * Likewise, though the value is passed as a double, it will be converted automatically to the underlying byte representation in the correct format.
     * This may lead to the truncation of the passed double value when retrieving the byte array representation.  However, the double passed in can still be retreived
     * without truncation by calling {@link #getPixel} if the image plane has not been switched in the intervening time.
     * <p>
     * This method will have better performance on repeated calls within the same image plane than on repeated calls switching
     * among different image planes, so a minimum of plane switching is recommended.
     * <p>
     * (All coordinates are zero-indexed.)
     *
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @param value The value to which the pixel at the specified coordinates will be set.
     */
    public synchronized void setPixel(int x, int y, int z, int c, int t, double value) {
        if (z == current_z && c == current_c && t == current_t) {
            convertedPixels[x*x_offset + y*y_offset] = value;
        } else {
            this.updateBytesFromConvertedPixels();
            current_z = z;
            current_c = c;
            current_t = t;
            this.updateConvertedPixelsFromBytes();
            setPixel(x,y,z,c,t, value);
        }
    }

    /**
     * Sets the byte order of the underlying byte array representation to one of the constants specified in {@link java.nio.ByteOrder}
     * @param b     The ByteOrder constant to which the byte order of the PixelData will be set.
     */
    public synchronized void setByteOrder(java.nio.ByteOrder b) {
        this.byteOrder = b;
        this.mappedData.order(this.byteOrder);
    }

    protected void finalize() throws Throwable {

        this.tempFileStorage.close();
        super.finalize();
    }

}
