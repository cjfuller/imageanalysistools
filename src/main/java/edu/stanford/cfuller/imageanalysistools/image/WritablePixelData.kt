package edu.stanford.cfuller.imageanalysistools.image

/**
 * An extension of a PixelData object that also supports writing of image data.
 * @see PixelData
 * @author Colin J. Fuller
 */
abstract class WritablePixelData : PixelData, java.io.Serializable {
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
     * Gets the value of a single pixel at the specified coordinates.
     *
     * Note that the parameters are passed in the order x,y,z,c,t regardless of the ordering in the underlying byte array representation and
     * will be converted to the correct ordering automatically.
     *
     * Likewise, though the value is passed as a float, it will be converted automatically to the underlying byte representation in the correct format.
     * This may lead to the truncation of the passed float value when retrieving the byte array representation.  However, the float passed in can still be retreived
     * without truncation by calling [.getPixel].
     *
     * (All coordinates are zero-indexed.)
     * @param x     The x-coordinate of the pixel to return.
     * @param y     The y-coordinate of the pixel to return.
     * @param z     The z-coordinate of the pixel to return.
     * @param c     The c-coordinate of the pixel to return.
     * @param t     The t-coordinate of the pixel to return.
     * @param value The value to which the pixel at the specified coordinates will be set.
     */
    abstract fun setPixel(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float)
    companion object {
        internal const val serialVersionUID = 7975917259L
    }
}
