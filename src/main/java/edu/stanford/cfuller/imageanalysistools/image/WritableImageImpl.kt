/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

import java.awt.image.BufferedImage
import ij.ImagePlus


/**
 * A basic read-write implementation of [WritableImage].
 *
 *
 * @see WritableImage


 * @author Colin J. Fuller
 */
class WritableImageImpl : ReadOnlyImageImpl, WritableImage {


    //constructors

    /**
     * Constructs a new WritableImage with the specified metadata and PixelData.
     *
     *
     * The metadata and PixelData should already be initialized (and should contain consistent values for things like dimension sizes).
     * <P>
     * This constructor is primarily used by classes that read images in binary format; most users will probably want to use a different constructor.
     * @param m     An object containing the metadata associated with the Image (as a loci.formats.meta.IMetadata, to ease integration with the LOCI bio-formats library).
     * *
     * @param p     A PixelData object containing the actual values at each pixel in the Image.
    </P> */
    constructor(m: loci.formats.meta.IMetadata, p: WritablePixelData) : super(m, p) {
        this.writablePixelData = p
    }

    /**
     * Constructs a new WritableImage that is a (deep) copy of the specified Image.  The pixel data will be copied exactly, but no guarantee
     * is made that the metadata will be completely copied; only the minimally necessary metadata (like dimension sizes) will definitely be copied.
     * @param toCopy    The Image to copy.
     */
    constructor(toCopy: Image) : super(toCopy, false) {
    }

    /**
     * Constructs a new WritableImage with specified dimension sizes and all the pixel values set to some specified initial value.  Bare-bones metadata
     * will also be constructed.
     *
     *
     * (Note that even though ImageCoordinates are zero-indexed, the ImageCoordinate's components should be the actual sizes of the dimensions,
     * not the indices of those dimensions.  For instance, for an 512 x by 512 y by 1 z plane by 1 color by 1 timepoint image, the ImageCoordinate
     * specifying sizes should have components = (512, 512, 1, 1, 1), not (511, 511, 0, 0, 0).)

     * @param dimensionSizes    An ImageCoordinate whose components will be the sizes of each dimension of the Image.
     * *
     * @param initialValue      The initial value to which all the pixels will be set.
     */
    constructor(dimensionSizes: ImageCoordinate, initialValue: Float) : super(dimensionSizes, initialValue) {
    }

    /**
     * Constructs a new WritableImage from a java standard BufferedImage.

     * @param bufferedImage        The BufferedImage to convert to an Image.
     */
    constructor(bufferedImage: BufferedImage) : super(bufferedImage) {
    }

    /**
     * Constructs a new WritableImage from an ImageJ ImagePlus.

     * @param imPl        The ImagePlus to convert to an Image.
     */
    constructor(imPl: ImagePlus) : super(imPl) {
    }

    /**
     * Default constructor that subclasses may use to do the initialization themselves.
     */
    protected constructor() {
    }


    /**
     * Resizes the image to the size specified in the ImageCoordinate.
     *
     *
     * Any image data that is still within the bounds of the new size will be transfered.
     * Other image data will be discarded; regions previously outside the image will
     * be filled with zeros.
     *
     *
     * Any existing box of interest will be erased on calling this method.

     * @param newSize an ImageCoordinate containing the new size of each dimension of the image.
     */
    override fun resize(newSize: ImageCoordinate) {
        val fillValue = 0.0f
        this.clearBoxOfInterest()
        this.coordinateArrayStorage = null
        val oldPixelData = this.pixelData
        val oldDimensionSizes = this.dimensionSizes
        this.dimensionSizes = ImageCoordinate.cloneCoord(newSize)
        this.writablePixelData = PixelDataFactory.createPixelData(newSize, loci.formats.FormatTools.FLOAT, "XYZCT")
        this.pixelData = this.writablePixelData
        this.setMetadataPixelCharacteristics(this.pixelData)
        this.setMetadataDimensionSizes(newSize)

        for (i in this) {
            var inBounds = true
            for (dim in i) {
                inBounds = inBounds and (i.get(dim!!) < oldDimensionSizes.get(dim))
            }
            if (inBounds) {
                this.setValue(i, oldPixelData!!.getPixel(i.get(ImageCoordinate.X), i.get(ImageCoordinate.Y), i.get(ImageCoordinate.Z), i.get(ImageCoordinate.C), i.get(ImageCoordinate.T)))
            } else {
                this.setValue(i, fillValue)
            }
        }

        oldDimensionSizes.recycle()

    }

    /**
     * Copies the pixel values of another Image into this Image.  Any existing pixel values are overwritten.  Metadata is not copied.
     * The two Images must be the same size; this is not checked.
     * @param other     The Image whose pixel values will be copied.
     */
    override fun copy(other: Image) {
        for (i in this) {
            this.setValue(i, other.getValue(i))
        }
    }

    /**
     * Sets the value of the Image at the specified Image Coordinate.  No bounds checking is performed.
     * @param coord     The ImageCoordinate at which to set the Image's value.
     * *
     * @param value     The value to which to set the Image at the specified coordinate.
     */
    override fun setValue(coord: ImageCoordinate, value: Float) {
        writablePixelData!!.setPixel(coord.quickGet(ImageCoordinate.X), coord.quickGet(ImageCoordinate.Y), coord.quickGet(ImageCoordinate.Z), coord.quickGet(ImageCoordinate.C), coord.quickGet(ImageCoordinate.T), value)
    }


    /**
     * Queries whether this Image's implementation can support writing.
     * @return    true, as this instance is writable.
     */
    override val isWritable: Boolean
        get() = true

    /**
     * Just calls super.finalize() for now.
     */
    @Throws(Throwable::class)
    override fun finalize() {
        super.finalize()
    }

    companion object {

        internal val serialVersionUID = 360707451L
    }

}
