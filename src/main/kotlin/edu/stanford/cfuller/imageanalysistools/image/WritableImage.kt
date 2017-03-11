package edu.stanford.cfuller.imageanalysistools.image

/**
 * Represents a read/write multidimensional image with some pixel data and associated metadata.
 *
 * @see Image for full description.
 *
 * This interface requires methods for both reading and writing; if using an image that will not be modified,
 * specify that using {@link Image}.
 * @author Colin J. Fuller
 */
interface WritableImage : Image {
    /**
     * Resizes the image to the size specified in the ImageCoordinate.
     *
     * Any image data that is still within the bounds of the new size will be transfered.
     * Other image data will be discarded; regions previously outside the image will
     * be filled with zeros.
     *
     * Any existing box of interest will be erased on calling this method.
     * @param newSize an ImageCoordinate containing the new size of each dimension of the image.
     */
    fun resize(newSize: ImageCoordinate)

    /**
     * Copies the pixel values of another Image into this Image.  Any existing pixel values are overwritten.  Metadata is not copied.
     * The two Images must be the same size; this is not checked.
     * @param other     The Image whose pixel values will be copied.
     */
    fun copy(other: Image)

    /**
     * Sets the value of the Image at the specified Image Coordinate.  No bounds checking is performed.
     * @param coord     The ImageCoordinate at which to set the Image's value.
     * @param value     The value to which to set the Image at the specified coordinate.
     */
    fun setValue(coord: ImageCoordinate, value: Float)

    /**
     * Method for converting a single Image with non-singleton specified dimension into a List of Images with
     * singleton specified dimension, each containing the Image for a single point along that dimension.
     *
     * Images will be returned in the List in the order of their dimension index in the original Image.
     * @return      A List of Images, each with one point from the original Image.
     */
    override fun split(dimension: Int): List<Image>

    /**
     * Convenience method for converting a single Image with non-singleton color dimension into a List of Images with
     * singleton color dimension, each containing the Image for a single color channel.
     *
     * Images will be returned in the List in the order of their color dimension index in the original Image.
     * @return      A List of Images, each with one color channel from the original Image.
     */
    override fun splitChannels(): List<Image>
}

