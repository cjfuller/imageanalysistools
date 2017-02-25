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

package edu.stanford.cfuller.imageanalysistools.image

import ij.ImagePlus


/**
 * Holds image pixel data in a type-independent manner; handles conversion to the appropriate number format for the data being stored to disk.
 *
 *
 * This implementation is currently rather memory inefficient for ease of converting back and forth to native format;
 * ImgLibPixelData (or its older counterpart, ImagePlusPixelData) are probably better choices.
 *
 *
 * It is recommended that PixelData objects be constructed with a [PixelDataFactory], which will choose what version to use.

 * @author Colin J. Fuller
 */

class BasicPixelData : WritablePixelData, java.io.Serializable {

    internal var dimensionSizes: java.util.Hashtable<String, Int>

    /**
     * Gets the data type used for the underlying byte array representation.
     *
     *
     * The meanings of the integers returned are specified as constants in [loci.formats.FormatTools].

     * @return  An integer corresponding to the format of the underlying byte representation of the data.
     */
    override var dataType: Int = 0
        internal set

    internal var x_offset: Int = 0
    internal var y_offset: Int = 0
    internal var z_offset: Int = 0
    internal var c_offset: Int = 0
    internal var t_offset: Int = 0

    internal var offsetSizes: java.util.Hashtable<String, Int>

    internal var pixels: ByteArray? = null
    internal var convertedPixels: FloatArray

    internal var byteOrder: java.nio.ByteOrder


    protected constructor() {}

    /**
     * Constructs a new pixeldata object using an [ImageCoordinate] to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * *
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from [loci.formats.FormatTools]
     * *
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */

    constructor(sizes: ImageCoordinate, data_type: Int, dimensionOrder: String) : super(sizes, dimensionOrder) {
        this.dataType = data_type
    }

    /**
     * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
     * @param size_x    Size of the pixel data in the X-dimension.
     * *
     * @param size_y    Size of the pixel data in the Y-dimension.
     * *
     * @param size_z    Size of the pixel data in the Z-dimension.
     * *
     * @param size_c    Size of the pixel data in the C-dimension.
     * *
     * @param size_t    Size of the pixel data in the T-dimension.
     * *
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from [loci.formats.FormatTools]
     * *
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
    constructor(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, data_type: Int, dimensionOrder: String) : super(size_x, size_y, size_z, size_c, size_t, dimensionOrder) {

        this.dataType = data_type

    }

    /**
     * Initializes the internals of the PixelData object using the specified parameters.

     * @param size_x    The size of the PixelData in the x-dimension (in pixels).
     * *
     * @param size_y    The size of the PixelData in the y-dimension (in pixels).
     * *
     * @param size_z    The size of the PixelData in the z-dimension (in pixels).
     * *
     * @param size_c    The size of the PixelData in the c-dimension (in pixels).
     * *
     * @param size_t    The size of the PixelData in the t-dimension (in pixels).
     * *
     * @param dimensionOrder    A string containing the 5 characters "XYZCT" in some order specifying the order in which the dimensions
     * *                          are stored in the underlying byte representation.
     */
    override fun init(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String) {
        var dimensionOrder = dimensionOrder

        dimensionOrder = dimensionOrder.toUpperCase()
        pixels = null

        dimensionSizes = java.util.Hashtable<String, Int>()

        this.sizeX = size_x
        this.sizeY = size_y
        this.sizeZ = size_z
        this.sizeC = size_c
        this.sizeT = size_t

        convertedPixels = FloatArray(this.sizeX * this.sizeY * this.sizeZ * this.sizeC * this.sizeT)

        dimensionSizes.put("X", size_x)
        dimensionSizes.put("Y", size_y)
        dimensionSizes.put("Z", size_z)
        dimensionSizes.put("C", size_c)
        dimensionSizes.put("T", size_t)

        offsetSizes = java.util.Hashtable<String, Int>()

        offsetSizes.put(dimensionOrder.substring(0, 1), 1)

        for (c in 1..dimensionOrder.length - 1) {
            val curr = dimensionOrder.substring(c, c + 1)
            val last = dimensionOrder.substring(c - 1, c)


            offsetSizes.put(curr, dimensionSizes[last] * offsetSizes[last])
        }

        x_offset = offsetSizes["X"]
        y_offset = offsetSizes["Y"]
        z_offset = offsetSizes["Z"]
        c_offset = offsetSizes["C"]
        t_offset = offsetSizes["T"]

        this.dimensionOrder = dimensionOrder

        this.byteOrder = java.nio.ByteOrder.BIG_ENDIAN


    }


    /**
     * Sets the raw byte representation of one plane of the pixel data to the specified array.
     *
     *
     * Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.
     * This will not be checked for the correct format.
     *
     *
     * The internal numerical representation of the pixel data will be updated immediately.

     * @param zIndex      the z-dimension index of the plane being set (0-indexed)
     * *
     * @param cIndex      the c-dimension index of the plane being set (0-indexed)
     * *
     * @param tIndex      the t-dimension index of the plane being set (0-indexed)
     * *
     * @param plane    A byte array containing the new pixel data for the specified plane.
     */
    override fun setPlane(zIndex: Int, cIndex: Int, tIndex: Int, plane: ByteArray) {

        if (!(this.dimensionOrder!!.startsWith("XY") || this.dimensionOrder!!.startsWith("YX"))) {
            throw UnsupportedOperationException("Setting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX.")
        }

        val `in` = java.nio.ByteBuffer.wrap(plane)

        `in`.order(this.byteOrder)


        if (this.dataType == loci.formats.FormatTools.INT8) {

            val convBuffer = `in`

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.UINT8) {

            val convBuffer = `in`

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.INT16) {

            val convBuffer = `in`.asShortBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }


        } else if (this.dataType == loci.formats.FormatTools.UINT16) {

            val convBuffer = `in`.asShortBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.INT32) {

            val convBuffer = `in`.asIntBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.UINT32) {

            val convBuffer = `in`.asIntBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }


        } else if (this.dataType == loci.formats.FormatTools.FLOAT) {

            val convBuffer = `in`.asFloatBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset))

                }
            }


        } else if (this.dataType == loci.formats.FormatTools.DOUBLE) {

            val convBuffer = `in`.asDoubleBuffer()


            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        } else {

            val convBuffer = `in`


            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    this.setPixel(x, y, zIndex, cIndex, tIndex, convBuffer.get(offset).toFloat())

                }
            }

        }


    }


    /**
     * Gets the raw byte representation of the pixel data.
     *
     *
     * Pixel values will be represented using the numeric type, byte order, and dimension order specified on initializing the PixelData.
     *
     *
     * Calling this function will encode the byte array data from the internal numerical representation, so in particular, if the byte data
     * was previously set using [.setBytes], and then changes were made using [.setPixel], for example, these changes will be reflected, and this
     * will not return the same byte data originally passed in.

     * @return  A byte array containing the pixel data encoded in the specified format.
     */
    /**
     * @param pixelBytes    A byte array containing the new pixel data.
     */
    var bytes: ByteArray
        get() {

            updateBytesFromConvertedPixels()
            return this.pixels

        }
        @Deprecated("")
        @Deprecated("this method makes it more difficult not to maintain an extra copy of image data in memory.  Use {@link #setPlane(int, int, int, byte[])} instead.\n\t \n     Sets the raw byte representation of the pixel data to the specified array.\n    <p>\n     Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.\n     This will not be checked for the correct format.\n    <p>\n     The internal numerical representation of the pixel data will be updated immediately, and the data in the specified byte array will replace\n     any existing data.\n    \n     ")
        set(pixelBytes) {

            pixels = pixelBytes

            updateConvertedPixelsFromBytes()

        }


    /**
     * Gets the number of bytes needed to store a single X-Y image plane in the underlying byte representation.
     * @return  The number of bytes for a single plane.
     */
    protected val planeSizeInBytes: Int
        get() = this.sizeX * this.sizeY * loci.formats.FormatTools.getBytesPerPixel(this.dataType)

    /**
     * Get a single plane of the image formatted as a raw byte array.
     *
     *
     * A plane is defined as the extend of the PixelData in the x-y direction, so a single plane will reflect a single (Z,C,T) coordinate.
     *
     *
     * Note that this definition requires that the dimension order of the PixelData start with "XY".  If the dimension order were "ZCXYT" or something
     * similarly strange, then this method would not work correctly.
     *
     *
     * The byte array will be encoded as when calling [.getBytes].
     *
     *
     * The planes are indexed according to the dimension order specified when initializing the PixelData.

     * @param index  The index of the plane to return in the specified dimension order.
     * *
     * @return      A byte array holding the requested plane encoded in the specified format.
     * *
     * @throws UnsupportedOperationException  if the dimension order does not start with XY or YX.
     */
    @Throws(UnsupportedOperationException::class)
    override fun getPlane(index: Int): ByteArray {

        updateBytesFromConvertedPixels()

        if (!(this.dimensionOrder!!.startsWith("XY") || this.dimensionOrder!!.startsWith("YX"))) {
            throw UnsupportedOperationException("Getting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX.")
        }

        val toReturn = ByteArray(planeSizeInBytes)
        System.arraycopy(pixels!!, index * toReturn.size, toReturn, 0, toReturn.size)
        return toReturn
    }

    /**
     * Gets the value of a single pixel at the specified coordinates.
     *
     *
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.

     * (All coordinates are zero-indexed.)

     * @param x     The x-coordinate of the pixel to return.
     * *
     * @param y     The y-coordinate of the pixel to return.
     * *
     * @param z     The z-coordinate of the pixel to return.
     * *
     * @param c     The c-coordinate of the pixel to return.
     * *
     * @param t     The t-coordinate of the pixel to return.
     * *
     * @return      The value of the PixelData at the specified coordinates, as a float.
     */
    override fun getPixel(x: Int, y: Int, z: Int, c: Int, t: Int): Float {
        try {
            return convertedPixels[x * x_offset + y * y_offset + z * z_offset + c * c_offset + t * t_offset]
        } catch (e: ArrayIndexOutOfBoundsException) {
            System.out.printf("offsets: %d, %d, %d, %d, %d\n requested: %d, %d, %d, %d, %d\n", x_offset, y_offset, z_offset, c_offset, t_offset, x, y, z, c, t)
            throw e
        }

    }


    /**
     * Gets the value of a single pixel at the specified coordinates.
     *
     *
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     *
     *
     * Likewise, though the value is passed as a float, it will be converted automatically to the underlying byte representation in the correct format.
     * This may lead to the truncation of the passed float value when retrieving the byte array representation.  However, the float passed in can still be retreived
     * without truncation by calling [.getPixel].

     *
     *
     * (All coordinates are zero-indexed.)

     * @param x     The x-coordinate of the pixel to return.
     * *
     * @param y     The y-coordinate of the pixel to return.
     * *
     * @param z     The z-coordinate of the pixel to return.
     * *
     * @param c     The c-coordinate of the pixel to return.
     * *
     * @param t     The t-coordinate of the pixel to return.
     * *
     * @param value The value to which the pixel at the specified coordinates will be set.
     */
    override fun setPixel(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float) {

        convertedPixels[x * x_offset + y * y_offset + z * z_offset + c * c_offset + t * t_offset] = value
        return
    }

    protected fun getPixelIndexForCoords(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float): Int {
        return x * x_offset + y * y_offset + z * z_offset + c * c_offset + t * t_offset
    }

    protected fun getCoordsForPixelIndex(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float): Int {
        return x * x_offset + y * y_offset + z * z_offset + c * c_offset + t * t_offset
    }


    /**
     * Reads the underlying byte array using the specified byte order, dimension order, and data format and stores it
     * as an array of floats, which is accessed by users of this class.
     */
    protected fun updateConvertedPixelsFromBytes() {

        //converts the byte array representation of the data into the internal float representation of the data

        val `in` = java.nio.ByteBuffer.wrap(this.pixels!!)

        `in`.order(this.byteOrder)


        var counter = 0


        when (this.dataType) {


            loci.formats.FormatTools.INT8 ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = `in`.get().toFloat()
                }

            loci.formats.FormatTools.UINT8 ->

                while (`in`.hasRemaining()) {
                    val b = `in`.get()
                    convertedPixels[counter++] = (b and 0xFF).toFloat() // this will convert b to int by bits, not by value
                }


            loci.formats.FormatTools.INT16 ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = `in`.short.toFloat()
                }


            loci.formats.FormatTools.UINT16 ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = (`in`.short and 0xFFFF).toFloat()
                }

            loci.formats.FormatTools.INT32 ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = `in`.int.toFloat()
                }

            loci.formats.FormatTools.UINT32 ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = (0xFFFFFFFFL and `in`.int).toFloat()
                }


            loci.formats.FormatTools.FLOAT ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = `in`.float
                }


            loci.formats.FormatTools.DOUBLE ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = `in`.double.toFloat()
                }

            else ->

                while (`in`.hasRemaining()) {
                    convertedPixels[counter++] = (if (`in`.get() > 0) 1.0 else 0.0).toFloat()
                }
        }

        this.pixels = null

    }

    /**
     * Converts the array of floats used for access by users of this class back to a byte array representation (suitable for writing to disk)
     * according to the stored byte order, dimension order, and data type.
     */
    protected fun updateBytesFromConvertedPixels() {


        //converts the internal float representation of the data into the byte array representation

        var bytes_out: java.nio.ByteBuffer? = null

        if (this.pixels != null) {

            bytes_out = java.nio.ByteBuffer.allocate(this.pixels!!.size)

        } else {

            bytes_out = java.nio.ByteBuffer.allocate(this.convertedPixels.size * loci.formats.FormatTools.getBytesPerPixel(this.dataType))

        }

        bytes_out!!.order(this.byteOrder)

        when (this.dataType) {

            loci.formats.FormatTools.INT8, loci.formats.FormatTools.UINT8 ->

                for (pixel in this.convertedPixels) {
                    bytes_out.put(pixel.toByte())
                }

            loci.formats.FormatTools.INT16, loci.formats.FormatTools.UINT16 ->

                for (pixel in this.convertedPixels) {

                    bytes_out.putShort(pixel.toShort())

                }

            loci.formats.FormatTools.INT32, loci.formats.FormatTools.UINT32 ->

                for (pixel in this.convertedPixels) {
                    bytes_out.putInt(pixel.toInt())
                }

            loci.formats.FormatTools.FLOAT ->

                for (pixel in this.convertedPixels) {
                    bytes_out.putFloat(pixel)
                }

            loci.formats.FormatTools.DOUBLE -> for (pixel in this.convertedPixels) {
                bytes_out.putDouble(pixel.toDouble())
            }

            else -> for (pixel in this.convertedPixels) {
                bytes_out.put((if (pixel > 0) 1 else 0).toByte())
            }
        }

        this.pixels = bytes_out.array()


    }

    /**
     * Sets the byte order of the underlying byte array representation to one of the constants specified in [java.nio.ByteOrder]
     * @param b     The ByteOrder constant to which the byte order of the PixelData will be set.
     */
    override fun setByteOrder(b: java.nio.ByteOrder) {
        this.byteOrder = b
    }

    /**
     * Gets the byte order of the underlying byte array representation of the data, as one of the constants specified in [java.nio.ByteOrder].
     * @return      The ByteOrder constant corresponding to the byte order of the underlying byte array representation of the data.
     */
    fun getByteOrder(): java.nio.ByteOrder {
        return this.byteOrder
    }

    /** (non-Javadoc)
     * @see edu.stanford.cfuller.imageanalysistools.image.PixelData.toImagePlus
     */
    override fun toImagePlus(): ImagePlus? {
        return null
    }

    companion object {

        //TODO: reimplement to handle images other than 5D.

        internal const val serialVersionUID = 19483561495701L
    }

}
