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
import ij.ImageStack
import ij.process.FloatProcessor
import ij.process.ImageProcessor


/**
 * A type of WritablePixelData that uses an ImageJ ImagePlus as its underlying representation.

 * @author Colin J. Fuller
 */
class ImagePlusPixelData : WritablePixelData {

    private var imPl: ImagePlus? = null

    internal var currentStackIndex: Int = 0

    internal var dimensionSizes: java.util.Hashtable<String, Int>

    internal var dataType: Int = 0

    internal var x_offset: Int = 0
    internal var y_offset: Int = 0
    internal var z_offset: Int = 0
    internal var c_offset: Int = 0
    internal var t_offset: Int = 0

    internal var offsetSizes: java.util.Hashtable<String, Int>

    internal var byteOrder: java.nio.ByteOrder


    override fun init(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String) {
        var dimensionOrder = dimensionOrder

        dimensionOrder = dimensionOrder.toUpperCase()

        dimensionSizes = java.util.Hashtable<String, Int>()

        this.sizeX = size_x
        this.sizeY = size_y
        this.sizeZ = size_z
        this.sizeC = size_c
        this.sizeT = size_t

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

        this.currentStackIndex = 0

    }

    protected fun initNewImagePlus() {
        val n_planes = this.sizeZ * this.sizeC * this.sizeT

        val width = this.sizeX

        val height = this.sizeY

        val stack = ImageStack(width, height)

        for (i in 0..n_planes - 1) {

            val fp = FloatProcessor(width, height, FloatArray(width * height), null)

            if (i == 0) {
                stack.update(fp)
            }

            stack.addSlice("", fp)

        }

        val imPl = ImagePlus("output", stack)

        imPl.setDimensions(this.sizeC, this.sizeZ, this.sizeT)

        this.imPl = imPl
    }

    protected constructor() {}

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate, int, String)
	 */
    constructor(sizes: ImageCoordinate, data_type: Int, dimensionOrder: String) : super(sizes, dimensionOrder) {
        this.dataType = data_type

        this.initNewImagePlus()
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(int, int, int, int, int, int, String)
	 */
    constructor(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, data_type: Int, dimensionOrder: String) : super(size_x, size_y, size_z, size_c, size_t, dimensionOrder) {
        this.dataType = data_type

        this.initNewImagePlus()

    }

    /**
     * Creates a new ImagePlusPixelData from an existing ImagePlus.

     * @param imPl    The ImagePlus to use.  This will not be copied, but used and potentially modified in place.
     */
    constructor(imPl: ImagePlus) {

        this.imPl = imPl

        this.init(imPl.width, imPl.height, imPl.nSlices, imPl.nChannels, imPl.nFrames, imagePlusDimensionOrder)

        this.dataType = loci.formats.FormatTools.FLOAT

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
    @Synchronized override fun setPlane(zIndex: Int, cIndex: Int, tIndex: Int, plane: ByteArray) {

        if (!(this.dimensionOrder!!.startsWith("XY") || this.dimensionOrder!!.startsWith("YX"))) {
            throw UnsupportedOperationException("Setting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX.")
        }

        val `in` = java.nio.ByteBuffer.wrap(plane)

        `in`.order(this.byteOrder)


        val requestedStackIndex = this.imPl!!.getStackIndex(cIndex + 1, zIndex + 1, tIndex + 1) //planes are 1-indexed in ImagePlus
        if (requestedStackIndex != this.currentStackIndex) {
            this.imPl!!.setSliceWithoutUpdate(requestedStackIndex)
            this.currentStackIndex = requestedStackIndex
        }

        val imp = this.imPl!!.processor



        if (this.dataType == loci.formats.FormatTools.INT8) {

            val convBuffer = `in`

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    //this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));
                    imp.setf(x, y, convBuffer.get(offset).toFloat())
                }
            }

        } else if (this.dataType == loci.formats.FormatTools.UINT8) {

            val convBuffer = `in`

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset


                    var value = convBuffer.get(offset).toFloat()

                    if (value < 0) {
                        value = java.lang.Byte.MAX_VALUE + (value + java.lang.Byte.MAX_VALUE.toFloat() + 1f)
                    }

                    imp.setf(x, y, value)


                }
            }

        } else if (this.dataType == loci.formats.FormatTools.INT16) {

            val convBuffer = `in`.asShortBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    imp.setf(x, y, convBuffer.get(offset).toFloat())

                }
            }


        } else if (this.dataType == loci.formats.FormatTools.UINT16) {

            val convBuffer = `in`.asShortBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    var value = convBuffer.get(offset).toFloat()

                    if (value < 0) {
                        value = java.lang.Short.MAX_VALUE + (value + java.lang.Short.MAX_VALUE.toFloat() + 1f)
                    }

                    imp.setf(x, y, value)

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.INT32) {

            val convBuffer = `in`.asIntBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    imp.setf(x, y, convBuffer.get(offset).toFloat())

                }
            }

        } else if (this.dataType == loci.formats.FormatTools.UINT32) {

            val convBuffer = `in`.asIntBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    var value = convBuffer.get(offset).toFloat()

                    if (value < 0) {
                        value = Integer.MAX_VALUE + (value + Integer.MAX_VALUE.toFloat() + 1f)
                    }

                    imp.setf(x, y, value)


                }
            }


        } else if (this.dataType == loci.formats.FormatTools.FLOAT) {

            val convBuffer = `in`.asFloatBuffer()

            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    imp.setf(x, y, convBuffer.get(offset))

                }
            }


        } else if (this.dataType == loci.formats.FormatTools.DOUBLE) {

            val convBuffer = `in`.asDoubleBuffer()


            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    imp.setf(x, y, convBuffer.get(offset).toFloat())

                }
            }

        } else {

            val convBuffer = `in`


            for (y in 0..this.sizeY - 1) {
                for (x in 0..this.sizeX - 1) {

                    val offset = x * x_offset + y * y_offset

                    imp.setf(x, y, convBuffer.get(offset).toFloat())

                }
            }

        }


    }

    @Synchronized @Throws(UnsupportedOperationException::class)
    override fun getPlane(index: Int): ByteArray {
        var index = index

        //convert the index to xyczt ordering just in case the ImagePlus representation changes under the hood.

        val c_index = index % sizeC

        val z_index = (index - c_index) / sizeC % sizeZ

        val t_index = (index - c_index - sizeC * z_index) / (sizeZ * sizeC)

        index = this.imPl!!.getStackIndex(c_index + 1, z_index + 1, t_index + 1)


        if (index != this.currentStackIndex) {
            this.imPl!!.setSliceWithoutUpdate(index)
            this.currentStackIndex = index
        }


        val pixelData = this.imPl!!.processor.pixels as FloatArray

        val out = java.nio.ByteBuffer.allocate(this.sizeX * this.sizeY * loci.formats.FormatTools.getBytesPerPixel(this.dataType))

        out.order(this.byteOrder)

        for (f in pixelData) {
            out.putFloat(f)
        }

        return out.array()
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getPixel(int, int, int, int, int)
	 */
    @Synchronized override fun getPixel(x: Int, y: Int, z: Int, c: Int, t: Int): Float {
        val requestedStackIndex = this.imPl!!.getStackIndex(c + 1, z + 1, t + 1) //planes are 1-indexed in ImagePlus
        if (requestedStackIndex != this.currentStackIndex) {
            this.imPl!!.setSliceWithoutUpdate(requestedStackIndex)
            this.currentStackIndex = requestedStackIndex
        }

        return imPl!!.processor.getf(x, y)

    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#setPixel(int, int, int, int, int, float)
	 */
    @Synchronized override fun setPixel(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float) {
        val requestedStackIndex = this.imPl!!.getStackIndex(c + 1, z + 1, t + 1) //planes are 1-indexed in ImagePlus
        if (requestedStackIndex != this.currentStackIndex) {
            this.imPl!!.setSliceWithoutUpdate(requestedStackIndex)
            this.currentStackIndex = requestedStackIndex
        }

        imPl!!.processor.setf(x, y, value)
    }


    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#toImagePlus()
	 */
    override fun toImagePlus(): ImagePlus {
        return this.imPl
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
    override var dimensionOrder: String
        get() = imagePlusDimensionOrder
        set(value: String) {
            super.dimensionOrder = value
        }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
    override fun getDataType(): Int {
        return loci.formats.FormatTools.FLOAT
    }

    /**
     * Sets the byte order for the pixel data.
     * @param b a ByteOrder constant specifying the byte order to be used.
     */
    override fun setByteOrder(b: java.nio.ByteOrder) {
        this.byteOrder = b
    }

    companion object {

        private val serialVersionUID = 5430441630713231848L

        private val imagePlusDimensionOrder = "XYCZT"
    }

}
