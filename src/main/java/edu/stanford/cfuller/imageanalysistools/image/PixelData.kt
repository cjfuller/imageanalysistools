package edu.stanford.cfuller.imageanalysistools.image

import ij.ImagePlus

/**
 * PixelData objects hold the (5-dimensional X, Y, Z, channel, time) image
 * values themselves for classes implementing Image.
 *
 * A class extending PixelData need only supply methods for intialization and reading.
 * For read-write access, see [WritablePixelData]
 *
 * It is recommended to obtain a PixelData object using [PixelDataFactory] rather
 * than instantiating a particular implementation.
 * @author Colin J. Fuller
 */
abstract class PixelData : java.io.Serializable {
    /**
     * Gets the size of the x-dimension of the PixelData.
     * @return  The size of the x-dimension (in pixels).
     */
    var sizeX: Int = 0
        internal set
    /**
     * Gets the size of the y-dimension of the PixelData.
     * @return  The size of the y-dimension (in pixels).
     */
    var sizeY: Int = 0
        internal set
    /**
     * Gets the size of the z-dimension of the PixelData.
     * @return  The size of the z-dimension (in pixels, or equivalently, planes).
     */
    var sizeZ: Int = 0
        internal set
    /**
     * Gets the size of the c-dimension of the PixelData.
     * @return  The size of the c-dimension (in pixels, or equivalently, color channels).
     */
    var sizeC: Int = 0
        internal set
    /**
     * Gets the size of the t-dimension of the PixelData.
     * @return  The size of the t-dimension (in pixels, or equivalently, timepoints).
     */
    var sizeT: Int = 0
        internal set

    /**
     * Gets the dimension order of the underlying byte representation of the PixelData as a string.
     *
     * An example string for one particular dimension order is "XYZCT"
     * @return  A String containing one character for each dimension specifying the dimension order.
     */
    open var dimensionOrder: String = "XYZCT"
        internal set

    protected constructor() {}

    /**
     * Constructs a new pixeldata object using an [ImageCoordinate] to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     */
    constructor(sizes: ImageCoordinate, dimensionOrder: String) {
        init(sizes[ImageCoordinate.X], sizes[ImageCoordinate.Y], sizes[ImageCoordinate.Z], sizes[ImageCoordinate.C], sizes[ImageCoordinate.T], dimensionOrder)
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
    constructor(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String) {
        init(size_x, size_y, size_z, size_c, size_t, dimensionOrder)
    }

    /**
     * Initializes the internals of the PixelData object using the specified parameters.
     * @param size_x    The size of the PixelData in the x-dimension (in pixels).
     * @param size_y    The size of the PixelData in the y-dimension (in pixels).
     * @param size_z    The size of the PixelData in the z-dimension (in pixels).
     * @param size_c    The size of the PixelData in the c-dimension (in pixels).
     * @param size_t    The size of the PixelData in the t-dimension (in pixels).
     * @param dimensionOrder    A string containing the 5 characters "XYZCT" in some order specifying the order in which the dimensions
     *                          are stored in the underlying byte representation.
     */
    protected abstract fun init(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String)

    /**
     * Queries whether the PixelData object has a non-singleton Z dimension.
     * @return  true if the Z-dimension size is greater than 1, false otherwise.
     */
    fun hasZ(): Boolean {
        return sizeZ > 1
    }

    /**
     * Queries whether the PixelData object has a non-singleton T dimension.
     * @return  true if the T-dimension size is greater than 1, false otherwise.
     */
    fun hasT(): Boolean {
        return sizeT > 1
    }

    /**
     * Queries whether the PixelData object has a non-singleton C dimension.
     * @return  true if the C-dimension size is greater than 1, false otherwise.
     */
    fun hasC(): Boolean {
        return sizeC > 1
    }

    /**
     * Gets the number of planes in the image.
     *
     * A plane is defined as the extent of the PixelData in the x-y direction, so the number of planes is the sizes of all dimensions besides X and Y multiplied together.
     * @return  The number of planes in the PixelData.
     */
    val numPlanes: Int
        get() = sizeZ * sizeT * sizeC

    /**
     * Get a single plane of the image formatted as a raw byte array. (Optional operation.)
     *
     * Always throws an UnsupportedOperationException unless overriden by a subclass.
     * @param index  The index of the plane to return in the specified dimension order.
     * @return      A byte array holding the requested plane encoded in the specified format.
     * @throws UnsupportedOperationException  if not overridden by a subclass.
     */
    @Throws(UnsupportedOperationException::class)
    open fun getPlane(index: Int): ByteArray {
        throw UnsupportedOperationException("getPlane not supported by PixelData")
    }

    /**
     * Gets the value of a single pixel at the specified coordinates.
     *
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     * (All coordinates are zero-indexed.)
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @return      The value of the PixelData at the specified coordinates, as a float.
     */
    abstract fun getPixel(x: Int, y: Int, z: Int, c: Int, t: Int): Float

    /**
     * Gets the data type used for the underlying byte array representation.
     *
     * The meanings of the integers returned are specified as constants in [loci.formats.FormatTools].
     * @return  An integer corresponding to the format of the underlying byte representation of the data.
     */
    abstract val dataType: Int


    /**
     * Returns an ImagePlus representation of the data contained in this PixelData.
     *
     * This may return null if an ImagePlus is not involved in the representation of the PixelData.
     * @return    An ImagePlus representation of the PixelData, or null.
     */
    abstract fun toImagePlus(): ImagePlus


    /**
     * Sets the raw byte representation of one plane of the pixel data to the specified array.  (Optional operation.)
     *
     * Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.
     * This will not be checked for the correct format.
     *
     * The internal numerical representation of the pixel data will be updated immediately.
     * @param zIndex      the z-dimension index of the plane being set (0-indexed)
     * @param cIndex      the c-dimension index of the plane being set (0-indexed)
     * @param tIndex      the t-dimension index of the plane being set (0-indexed)
     * @param plane    A byte array containing the new pixel data for the specified plane.
     */
    @Throws(UnsupportedOperationException::class)
    open fun setPlane(zIndex: Int, cIndex: Int, tIndex: Int, plane: ByteArray) {
        throw UnsupportedOperationException("Set plane not supported for this PixelData type.")
    }

    /**
     * Sets the byte order for the pixel data representation for those PixelData
     * types where this is applicable.  Otherwise does nothing.
     * @param b a ByteOrder constant specifying the byte order to be used.
     */
    open fun setByteOrder(b: java.nio.ByteOrder) {
    }

    companion object {
        internal const val serialVersionUID = 1495619456L
    }
}
