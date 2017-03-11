package edu.stanford.cfuller.imageanalysistools.image

/**
 * An extension of a PixelData object that also supports writing of image data.
 * @see PixelData
 * @author Colin J. Fuller
 */
abstract class WritablePixelData : PixelData(), java.io.Serializable {
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
