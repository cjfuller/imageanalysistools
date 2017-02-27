package edu.stanford.cfuller.imageanalysistools.image

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.io.ImageWriter
import ome.xml.model.primitives.PositiveInteger

import ij.ImageStack
import ij.process.FloatProcessor
import ij.process.ImageProcessor
import ij.ImagePlus
import loci.formats.meta.IMetadata
import net.imglib2.meta.Metadata

import java.awt.image.BufferedImage
import java.awt.image.WritableRaster

/**
 * A basic read-only implementation of [Image].
 *
 * @see Image
 * @author Colin J. Fuller
 */
open class ReadOnlyImageImpl(m: loci.formats.meta.IMetadata, p: PixelData) : Image {
    internal val defaultDimensionOrder = "xyzct"

    /**
     * Gets the metadata associated with this Image.  (The object returned is an [loci.formats.meta.IMetadata] to facilitate
     * use with the LOCI bio-formats library.
     * @return  The metadata object associated with the Image.
     */
    override var metadata: loci.formats.meta.IMetadata = m
        protected set

    /**
     * Gets a PixelData instance that holds the image data.
     */
    override var pixelData: PixelData = p
        protected set
    protected var writablePixelData: WritablePixelData? = null // this is null if it can be written, and the same as pixelData otherwise

    /**
     * Returns an ImageCoordinate that contains the size of each dimension of the Image.
     *
     * This ImageCoordinate should not be modified by users, nor should it be recycled by users.
     * @return      An ImageCoordinate containing the size of each dimension of the Image.
     */
    override var dimensionSizes: ImageCoordinate
        protected set

    /**
     * Gets the (inclusive) lower bound of any region of interest currently set on this Image, or null if no region is currently
     * set.
     *
     * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
     * As per the specification in [ImageCoordinate], users should *not* recycle the ImageCoordinate returned.
     * @return  The ImageCoordinate whose components are the lower bound on the region of interest, or null if there is no region of interest.
     */
    override var boxMin: ImageCoordinate? = null
        protected set
    /**
     * Gets the (exclusive) upper bound of any region of interest currently set on this Image, or null if no region is currently
     * set.
     *
     * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
     * As per the specification in [ImageCoordinate], users should *not* recycle the ImageCoordinate returned.
     * @return  The ImageCoordinate whose components are the upper bound on the region of interest, or null if there is no region of interest.
     */
    override var boxMax: ImageCoordinate? = null
        protected set

    /**
     * Queries whether the Image is currently boxed with a region of interest.
     * @return  true if there is currently a region of interest set, false otherwise.
     */
    override var isBoxed: Boolean = false
        protected set

    protected var coordinateArrayStorage: Array<ImageCoordinate>? = null


    init {
        this.metadata = m
        this.pixelData = p
        this.writablePixelData = null
        this.coordinateArrayStorage = null
        this.dimensionSizes = ImageCoordinate.createCoordXYZCT(p.sizeX, p.sizeY, p.sizeZ, p.sizeC, p.sizeT)
    }
    /**
     * Constructs a new Image with the specified metadata and PixelData.
     *
     *
     * The metadata and PixelData should already be initialized (and should contain consistent values for things like dimension sizes).
     * This constructor is primarily used by classes that read images in binary format; most users will probably want to use a different constructor.
     * @param m     An object containing the metadata associated with the Image (as a loci.formats.meta.IMetadata, to ease integration with the LOCI bio-formats library).
     * @param p     A PixelData object containing the actual values at each pixel in the Image.
     */

    /**
     * Constructs a new Image that is a (deep) copy of the specified Image.  The pixel data will be copied exactly, but no guarantee
     * is made that the metadata will be completely copied; only the minimally necessary metadata (like dimension sizes) will definitely be copied.
     * @param toCopy    The Image to copy.
     */
    constructor(toCopy: Image, shallow: Boolean) {

        this.isBoxed = false
        this.boxMin = null
        this.boxMax = null
        this.coordinateArrayStorage = null
        this.dimensionSizes = ImageCoordinate.cloneCoord(toCopy.dimensionSizes)
        if (shallow) {
            this.pixelData = toCopy.pixelData
            this.metadata = toCopy.metadata
        } else {
            this.writablePixelData = PixelDataFactory.createPixelData(toCopy.dimensionSizes, toCopy.pixelData.dataType, defaultDimensionOrder)
            this.pixelData = this.writablePixelData
            this.copy(toCopy)
            setupNewMetadata()
        }

    }

    /**
     * Constructs a new Image with specified dimension sizes and all the pixel values set to some specified initial value.  Bare-bones metadata
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
    constructor(dimensionSizes: ImageCoordinate, initialValue: Float) {
        this.isBoxed = false
        this.boxMin = null
        this.boxMax = null
        this.coordinateArrayStorage = null
        this.dimensionSizes = ImageCoordinate.cloneCoord(dimensionSizes)
        this.writablePixelData = PixelDataFactory.createPixelData(dimensionSizes, loci.formats.FormatTools.FLOAT, "XYZCT")
        this.pixelData = this.writablePixelData

        setupNewMetadata()

        for (i in this) {
            this.setValue(i, initialValue)
        }

    }

    /**
     * Constructs a new Image from a java standard BufferedImage.

     * @param bufferedImage        The BufferedImage to convert to an Image.
     */
    constructor(bufferedImage: BufferedImage) {

        val size_x = bufferedImage.width
        val size_y = bufferedImage.height
        val size_z = 1
        val size_c = bufferedImage.raster.numBands
        val size_t = 1

        val dimensionSizes = ImageCoordinate.createCoordXYZCT(size_x, size_y, size_z, size_c, size_t)

        this.isBoxed = false
        this.boxMin = null
        this.boxMax = null
        this.coordinateArrayStorage = null
        this.dimensionSizes = ImageCoordinate.cloneCoord(dimensionSizes)
        this.writablePixelData = PixelDataFactory.createPixelData(dimensionSizes, loci.formats.FormatTools.FLOAT, "XYZCT")
        this.pixelData = this.writablePixelData
        setupNewMetadata()

        dimensionSizes.recycle()

        for (i in this) {
            val x = i.get(ImageCoordinate.X)
            val y = i.get(ImageCoordinate.Y)
            val c = i.get(ImageCoordinate.C)

            this.setValue(i, bufferedImage.raster.getSample(x, y, c).toFloat())
        }

    }

    /**
     * Constructs a new Image from an ImageJ ImagePlus.

     * @param imPl        The ImagePlus to convert to an Image.
     */
    constructor(imPl: ImagePlus) {

        val dimensions = imPl.dimensions

        val dimensionSizes = ImageCoordinate.createCoordXYZCT(dimensions[0], dimensions[1], dimensions[3], dimensions[2], dimensions[4])

        this.isBoxed = false
        this.boxMin = null
        this.boxMax = null
        this.coordinateArrayStorage = null
        this.dimensionSizes = ImageCoordinate.cloneCoord(dimensionSizes)
        this.writablePixelData = ImagePlusPixelData(imPl)
        this.pixelData = this.writablePixelData

        setupNewMetadata()

        dimensionSizes.recycle()


    }

    /**
     * Default constructor that subclasses may use to do the initialization themselves.
     */
    protected constructor() {
    }

    //public methods


    /**
     * Sets the region of interest in the Image, copying the provided image coordinates.

     * @see .setBoxOfInterest
     */
    override fun setBoxOfInterest(boxMin: ImageCoordinate, boxMax: ImageCoordinate) {
        this.setBoxOfInterest(boxMin, boxMax, true)
    }


    /**
     * Sets the region of interest in the Image.
     *
     *
     * The region of interest must be a (possibly high-dimensional) rectangle and is represented by two points, the lower bound of the coordinates
     * and the upper bound of the coordinates (conceptually like the upper-left and lower-right corners for the 2D case).  The box will include the
     * lower bound but exclude the upper bound.
     *
     *
     * The region of interest will control what area of the Image will be iterated over using foreach-style iteration, or
     * any of the methods of an [ImageIterator].  This will be the sole region iterated over until [.clearBoxOfInterest] is
     * called, or this method is called again with a new region of interest.  A new region of interest specified will replace any existing
     * region; it is not possible to construct complex regions by successively building with several rectangles.
     *
     *
     * As per the specification in [ImageCoordinate], users are still responsible for recycling the ImageCoordinate parameters; they are
     * not retained and recycled by this class.

     * @param boxMin    The (inclusive) lower coordinate bound of the boxed region of interest.
     * *
     * @param boxMax    The (exclusive) upper coordinate bound of the boxed region of interest.
     * *
     * @param copy      Indicates whether to copy the input coordinates; if set to false, do not recycle or further modify the inputs.
     */
    override fun setBoxOfInterest(boxMin: ImageCoordinate, boxMax: ImageCoordinate, copy: Boolean) {

        if (this.boxMin != null) {
            clearBoxOfInterest()
        }

        if (copy) {
            this.boxMin = ImageCoordinate.cloneCoord(boxMin)
            this.boxMax = ImageCoordinate.cloneCoord(boxMax)
        } else {
            this.boxMin = boxMin
            this.boxMax = boxMax
        }

        //bounds checking

        for (dim in this.boxMin!!) {
            if (this.boxMin!!.get(dim!!) < 0) this.boxMin!!.set(dim, 0)
        }

        for (dim in this.boxMax!!) {
            if (this.boxMax!!.get(dim!!) > this.dimensionSizes.get(dim)) this.boxMax!!.set(dim, this.dimensionSizes.get(dim))
        }

        this.isBoxed = true

    }

    /**
     * Clears any existing region of interest that has been set on this Image.

     * This will cause any foreach-style iteration or ImageIterator-controlled iteration to iterate over the entire image.

     */
    override fun clearBoxOfInterest() {
        if (this.boxMax != null && this.boxMin != null) {
            this.boxMax!!.recycle()
            this.boxMin!!.recycle()
        }
        this.boxMin = null
        this.boxMax = null
        this.isBoxed = false
    }

    /**
     * Creates a new Image that is a sub-image of the Image.
     *
     *
     * The new Image will be created with bare-bones metadata and its pixel values set to the pixel values of this Image in the specifed region.
     * The new Image will have the dimension sizes specified (as if the Image constructor were called with this size parameter), and start at the specified
     * coordinate in the current Image (inclusive).  If the start point + new dimension sizes goes outside the current Image, then the new Image will be filled
     * with zeros at any coordinate outside the current Image.
     *
     *
     * As per the specification in [ImageCoordinate], users are responsible for recycling the ImageCoordinate parameters; they are not retained
     * or recycled by this class.


     * @param newDimensions     An ImageCoordinate containing the size of the new sub-image in each dimension.
     * *
     * @param startPoint        The (inclusive) point in the current Image where the sub-image will start (this will become coordinate (0,0,0,...) in the new Image).
     * *
     * @return                  A new Image with pixel values set to those of the specified sub region of the current Image.
     */
    override fun subImage(newDimensions: ImageCoordinate, startPoint: ImageCoordinate): Image {

        val toReturn = ImageFactory.createWritable(newDimensions, 0.0f)

        val ic = ImageCoordinate.cloneCoord(startPoint)

        for (i in toReturn) {

            for (dim in ic) {
                ic.set(dim!!, startPoint.get(dim) + i.get(dim))
            }

            if (this.inBounds(ic)) {
                toReturn.setValue(i, this.getValue(ic))
            }
        }

        ic.recycle()

        return ImageFactory.createShallow(toReturn)


    }

    /**
     * Writes the Image to a file.
     *
     *

     * The output format of the Image will be guessed from the extension.  Valid formats are those that the LOCI bio-formats library can write.
     * (The recommended option is the ome-tiff format, which should have the extension .ome.tif).


     * @param filename  The full absolute path to the file to which the Image is to be written.  The parent directory must exist.
     */
    override fun writeToFile(filename: String) {
        try {
            ImageWriter(this).write(filename, this.pixelData)
        } catch (e: java.io.IOException) {
            LoggingUtilities.logger.severe("Error while writing image to file: $filename Skipping write and continuing.")
            e.printStackTrace()
        }

    }

    /**
     * Gets the value of the Image at the coordinate specified.  No bounds checking is performed.
     * @param coord     An ImageCoordinate specifying the location of the value to retrieve.
     * *
     * @return          The value of the Image at the specified location as a float.
     */
    override fun getValue(coord: ImageCoordinate): Float {
        return pixelData!!.getPixel(coord.quickGet(ImageCoordinate.X), coord.quickGet(ImageCoordinate.Y), coord.quickGet(ImageCoordinate.Z), coord.quickGet(ImageCoordinate.C), coord.quickGet(ImageCoordinate.T))
    }


    /**
     * Checks to see whether a specified ImageCoordinate represents a location in the Image.
     * @param c     The ImageCoordinate to check
     * *
     * @return      true, if the ImageCoordinate lies within this Image in every dimension, false otherwise.
     */
    override fun inBounds(c: ImageCoordinate): Boolean {

        val sizes = this.dimensionSizes

        for (s in c) {
            if (c.quickGet(s!!) < 0) return false
            if (c.quickGet(s) >= sizes.get(s)) return false
        }

        return true

    }

    /**
     * Method for converting a single Image with non-singleton specified dimension into a List of Images with
     * singleton specified dimension, each containing the Image for a single point along that dimension.
     *
     *
     * Images will be returned in the List in the order of their dimension index in the original Image.
     * @return      A List of Images, each with one point from the original Image.
     */
    override fun split(dimension: Int): List<Image> {

        //handle special case of color, where we need to rename channels in the metadata

        if (dimension == ImageCoordinate.C) {
            return this.splitChannels()
        }

        val split = java.util.Vector<WritableImage>()

        var ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in 0..this.dimensionSizes.quickGet(dimension) - 1) {
            ic.recycle()
            ic = ImageCoordinate.cloneCoord(this.dimensionSizes)
            ic.quickSet(dimension, 1)
            val newChannelImage = ImageFactory.createWritable(ic, 0.0f)

            split.add(newChannelImage)
        }

        for (i in this) {

            ic.recycle()

            ic = ImageCoordinate.cloneCoord(i)

            ic.quickSet(dimension, 0)

            split[i.quickGet(dimension)].setValue(ic, this.getValue(i))

        }

        ic.recycle()

        val split_ro = java.util.ArrayList<Image>()
        split_ro.addAll(split)

        return split_ro


    }


    /**
     * Convenience method for converting a single Image with non-singleton color dimension into a List of Images with
     * singleton color dimension, each containing the Image for a single color channel.
     *
     *
     * Images will be returned in the List in the order of their color dimension index in the original Image.
     * @return      A List of Images, each with one color channel from the original Image.
     */
    override fun splitChannels(): List<Image> {

        val series_number = 0 // if this Image was created from a multi-series image, this will get metadata from the first series.

        val split = java.util.Vector<WritableImage>()

        var ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        /*we need to label the channels individually with some sort of identifier in their metadata.  Try first the name, if null, then
	  try the excitation/emission wavelengths, if null then fall back on the channel ID, which will be something nondescript but unique.

	  Whatever the result, this will set the metadata property using setChannelName so that this can be retrieved later.
	*/

        var useName = false
        var useWavelengths = false
        var useID = true

        val name0 = this.metadata.getChannelName(series_number, 0)
        val exW = this.metadata.getChannelExcitationWavelength(series_number, 0)
        val emW = this.metadata.getChannelEmissionWavelength(series_number, 0)

        //check if name is ok

        if (name0 != null && name0 !== "") {
            useName = true
            useWavelengths = false
            useID = false

            //check if wavelengths are ok
        } else if (exW != null && exW.value > 0 || emW != null && emW.value > 0) {
            useName = false
            useWavelengths = true
            useID = false
        }

        for (i in 0..this.dimensionSizes.get(ImageCoordinate.C) - 1) {
            ic.recycle()
            ic = ImageCoordinate.cloneCoord(this.dimensionSizes)
            ic.set(ImageCoordinate.C, 1)
            val newChannelImage = ImageFactory.createWritable(ic, 0.0f)

            //set the channel name for this new single channel image
            var channelName = ""

            try {
                if (useID) {
                    channelName = this.metadata.getChannelID(series_number, i)
                }
                if (useWavelengths) {
                    channelName = this.metadata.getChannelExcitationWavelength(series_number, i).toString() + "/" + this.metadata.getChannelEmissionWavelength(series_number, i)
                }
                if (useName) {
                    channelName = this.metadata.getChannelName(series_number, i)
                }
            } catch (e: IndexOutOfBoundsException) {
                try {
                    channelName = this.metadata.getChannelName(series_number, i)
                } catch (e2: IndexOutOfBoundsException) {
                    channelName = Integer.toString(i)
                }

            }

            newChannelImage.metadata.setChannelName(channelName, 0, 0)


            split.add(newChannelImage)
        }

        for (i in this) {

            ic.recycle()

            ic = ImageCoordinate.cloneCoord(i)

            ic.quickSet(ImageCoordinate.C, 0)

            split[i.quickGet(ImageCoordinate.C)].setValue(ic, this.getValue(i))

        }

        ic.recycle()

        val split_ro = java.util.ArrayList<Image>()
        split_ro.addAll(split)

        return split_ro

    }


    /**
     * Converts the Image to a [BufferedImage] for use with other java imaging clases.
     *
     *
     * Because the Buffered Image can only represent single-plane images (possibly with down-sampled color channels), this
     * conversion may not convert all the Image data.
     *
     *
     * For multi-plane images, this will convert the first x-y plane only (that is, with c=0, t=0, z=0).  If you want a different
     * plane, create a single plane sub-image using [.subImage], and then convert that Image.
     *
     *
     * The output format is a 16-bit greyscale BufferedImage.

     * @return  A BufferedImage containing the pixel values from the first plane of the Image converted to 16-bit greyscale format.
     */
    override fun toBufferedImage(): java.awt.image.BufferedImage {

        val toReturn = BufferedImage(this.dimensionSizes.get(ImageCoordinate.X), this.dimensionSizes.get(ImageCoordinate.Y), BufferedImage.TYPE_USHORT_GRAY)

        val r = toReturn.raster

        val boxMin = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        val boxMax = ImageCoordinate.cloneCoord(this.dimensionSizes)
        boxMax.set(ImageCoordinate.C, 1)
        boxMax.set(ImageCoordinate.Z, 1)
        boxMax.set(ImageCoordinate.T, 1)

        this.setBoxOfInterest(boxMin, boxMax)

        for (i in this) {

            r.setSample(i.get(ImageCoordinate.X), i.get(ImageCoordinate.Y), 0, this.getValue(i))

        }

        this.clearBoxOfInterest()
        boxMin.recycle()
        boxMax.recycle()

        return toReturn

    }


    /**
     * Converts the Image to an ImageJ ImagePlus.

     * @return    The converted Image.
     */
    override fun toImagePlus(): ImagePlus {


        val pixelPlus = this.pixelData!!.toImagePlus()

        if (pixelPlus != null) return pixelPlus

        //TODO: add multiple pixel formats

        val n_planes = this.planeCount

        val width = this.dimensionSizes.get(ImageCoordinate.X)

        val height = this.dimensionSizes.get(ImageCoordinate.Y)

        val stack = ImageStack(width, height)

        for (i in 0..n_planes - 1) {

            val fp = FloatProcessor(width, height, FloatArray(width * height), null)

            if (i == 0) {
                stack.update(fp)
            }

            stack.addSlice("", fp)

        }

        val imPl = ImagePlus("output", stack)

        imPl.setDimensions(this.dimensionSizes.get(ImageCoordinate.C), this.dimensionSizes.get(ImageCoordinate.Z), this.dimensionSizes.get(ImageCoordinate.T))

        for (ic in this) {

            imPl.setPositionWithoutUpdate(ic.get(ImageCoordinate.C) + 1, ic.get(ImageCoordinate.Z) + 1, ic.get(ImageCoordinate.T) + 1)

            val imP = imPl.processor

            imP.setf(ic.get(ImageCoordinate.X), ic.get(ImageCoordinate.Y), this.getValue(ic))

        }

        imPl.resetDisplayRange()

        return imPl


    }


    /**
     * Gets the number of planes in this Image (that is the number of distinct (z, c, t) coordinates).

     * @return  The number of planes.
     */
    override val planeCount: Int
        get() = this.dimensionSizes.get(ImageCoordinate.Z) * this.dimensionSizes.get(ImageCoordinate.T) * this.dimensionSizes.get(ImageCoordinate.C)


    /**
     * Selects a plane as active for iteration.  This has the same effect as calling the setBoxOfInterest method
     * with coordinates that would select only the given plane.  This should not be used in conjunction with the setBoxOfInterest method,
     * as internally this method uses the same boxing mechanism.  To clear the selected plane, call clearBoxOfInterest.

     * @param i     The plane index to set as active; no guarantee is made as to which plane this is, except that this index
     * *              will always refer to the same plane for a given Image, and iterating from i = 0 to getPlaneCount() - 1 will
     * *              visit all the planes in the Image.
     */
    override fun selectPlane(i: Int) {

        val tempMin = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        val tempMax = ImageCoordinate.createCoordXYZCT(this.dimensionSizes.get(ImageCoordinate.X), this.dimensionSizes.get(ImageCoordinate.Y), 0, 0, 0)

        val z_index = i % this.dimensionSizes.get(ImageCoordinate.Z)

        val c_index = (i - z_index) / this.dimensionSizes.get(ImageCoordinate.Z) % this.dimensionSizes.get(ImageCoordinate.C)

        val t_index = ((i - z_index) / this.dimensionSizes.get(ImageCoordinate.Z) - c_index) / this.dimensionSizes.get(ImageCoordinate.C)

        tempMin.set(ImageCoordinate.Z, z_index)
        tempMin.set(ImageCoordinate.C, c_index)
        tempMin.set(ImageCoordinate.T, t_index)
        tempMax.set(ImageCoordinate.Z, z_index + 1)
        tempMax.set(ImageCoordinate.C, c_index + 1)
        tempMax.set(ImageCoordinate.T, t_index + 1)

        this.setBoxOfInterest(tempMin, tempMax)
        tempMin.recycle()
        tempMax.recycle()

    }


    /**
     * Queries whether this Image's implementation can support writing.
     * @return    false, as this read-only instance cannot be written.
     */
    override val isWritable: Boolean
        get() = false

    /**
     * Gets a writable version of this Image.  If the Image on which this method is
     * called is already writable, then it is returned unchanged.  Otherwise,
     * a new object will be returned that contains a writable copy of the image data.
     */
    override val writableInstance: WritableImage
        get() = ImageFactory.writableImageInstance(this)

    //this is included here for ease of initialization
    private fun setValue(coord: ImageCoordinate, value: Float) {
        writablePixelData!!.setPixel(coord.quickGet(ImageCoordinate.X), coord.quickGet(ImageCoordinate.Y), coord.quickGet(ImageCoordinate.Z), coord.quickGet(ImageCoordinate.C), coord.quickGet(ImageCoordinate.T), value)
    }

    //this is included here for ease of initialization
    private fun copy(other: Image) {
        for (i in this) {
            this.setValue(i, other.getValue(i))
        }
    }

    // collection interface methods

    /**
     * Collection interface method.  Not supported.

     * @param c
     * *
     * @return  will not return
     * *
     * @throws UnsupportedOperationException    if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun add(c: ImageCoordinate): Boolean {
        throw UnsupportedOperationException("Add not supported for images.")
    }

    /**
     * Collection interface method.  Not supported.

     * @param c
     * *
     * @return  will not return
     * *
     * @throws UnsupportedOperationException    if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun addAll(c: Collection<ImageCoordinate>): Boolean {
        throw UnsupportedOperationException("Add not supported for images.")
    }

    /**
     * Collection interface method.  Not supported.

     * @throws UnsupportedOperationException if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun clear() {
        throw UnsupportedOperationException("Clear not supported for images.")
    }

    /**
     * Checks to see whether a given object is contained in this Image.  Collection interface method.
     *
     *
     * This is equivalent to checking that the object is an instance of ImageCoordinate, and then calling [.inBounds].


     * @param o     The object that will be checked.
     * *
     * @return      true if the Object is an ImageCoordinate and is inBounds, false otherwise.
     */
    override operator fun contains(o: Any): Boolean {
        var o_c = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        var ret_val = false
        if (o.javaClass == o_c.javaClass) {
            ret_val = true
            o_c.recycle()
            o_c = o as ImageCoordinate
            ret_val = this.inBounds(o_c)
        }

        return ret_val
    }

    /**
     * Checks to see whether a given collection of objects is contained in this Image.  Collection interface method.
     *
     *
     * This is equivalent to taking the logical AND of the call to [.contains] on each object in the Collection.
     * @param c     The Collection of objects to check.
     * *
     * @return      true if every Object in the collection is an ImageCoordinate and inBounds, false otherwise.
     */
    override fun containsAll(c: Collection<*>): Boolean {
        for (o in c) {
            if (!this.contains(o)) return false
        }
        return true
    }

    /**
     * Checks to see if the Image is empty.  Collection interface method.
     *
     *
     * An Image is considered empty if and only if any of the dimensions of the Image have size zero.

     * @return  true if this Image has any zero-size dimensions, false otherwise.
     */
    override fun isEmpty(): Boolean {
        val dimSizes = this.dimensionSizes
        for (s in dimSizes) {
            if (dimSizes.get(s!!) == 0) return true
        }

        return false
    }

    /**
     * Gets an Iterator over the ImageCoordinates in this Image.  Collection interface method.

     * @return  an Iterator that will traverse all the coordinates in the Image (or all the coordinates in the region of interest if the
     * * Image is currently boxed with a region of interest.
     */
    override fun iterator(): Iterator<ImageCoordinate> {
        return edu.stanford.cfuller.imageanalysistools.image.ImageIterator5D(this)
    }

    /**
     * Collection interface method.  Not supported.

     * @param o
     * *
     * @return  will not return
     * *
     * @throws UnsupportedOperationException if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun remove(o: Any): Boolean {
        throw UnsupportedOperationException("Remove not supported for images.")
    }

    /**
     * Collection interface method.  Not supported.

     * @param c
     * *
     * @return  will not return
     * *
     * @throws UnsupportedOperationException if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun removeAll(c: Collection<*>): Boolean {
        throw UnsupportedOperationException("Remove not supported for images.")
    }

    /**
     * Collection interface method.  Not supported.

     * @param c
     * *
     * @return  will not return
     * *
     * @throws UnsupportedOperationException if this method is called.
     */
    @Throws(UnsupportedOperationException::class)
    override fun retainAll(c: Collection<*>): Boolean {
        throw UnsupportedOperationException("Retain not supported for images.")
    }

    /**
     * Gets the size of the Image, or more specifically, the number of distinct ImageCoordinates making up this Image.  Collection interface method.
     *
     *
     * The result is equivalent to multiplying the size of each dimension of the Image together.
     * @return  The size of this Image, in number of ImageCoordinates (or multidimensional pixels).
     */
    override fun size(): Int {
        val c = this.dimensionSizes
        var total = 1
        for (s in c) {
            total *= c.get(s!!)
        }
        return total
    }

    /**
     * Gets an array of all the ImageCoordinates in the Image.  Collection interface method.
     *
     *
     * Calling this method is almost certainly a bad idea, as it will allocate a massive number of ImageCoordinates, instead of just reusing.
     * Nonetheless, if users call this method, as per the specification in [ImageCoordinate], they should not recycle them.

     * @return  An array containing all the ImageCoordinates in the Image.
     */
    override fun toArray(): Array<Any> {
        if (this.coordinateArrayStorage == null) {
            this.coordinateArrayStorage = arrayOfNulls<ImageCoordinate>(this.size)

            var index = 0
            for (c in this) {
                this.coordinateArrayStorage[index++] = ImageCoordinate.cloneCoord(c)
            }
        }
        return this.coordinateArrayStorage
    }

    /**
     * Gets an array of all the ImageCoordinates in the Image as an array of the same type as the given array.
     * If there is insufficient room in the given array, a new array will be allocated and returned.  Collection interface method.
     *
     *
     * For the same reasons as in [.toArray], calling this method is almost always a bad idea.

     * @param a     An array of the type to return.
     * *
     * @param <T>   The type of the array to return.
     * *
     * @return      An array of type T containing all the ImageCoordinates in the Image.
    </T> */
    override fun <T> toArray(a: Array<T>): Array<T> {

        val im_a = arrayOfNulls<ImageCoordinate>(0)

        if (!a.javaClass.isInstance(im_a)) {
            throw ArrayStoreException(a.javaClass.name + "is not a valid super class of ImageCoordinate[].")
        }

        if (a.size < this.size) {
            return a.javaClass.cast(this.toTypedArray())
        } else {
            val coords = this.toTypedArray()
            for (i in a.indices) {
                if (i < coords.size) {
                    a[i] = coords[i] as T
                } else {
                    a[i] = null
                }
            }

            return a
        }


    }


    /**
     * Immediately nullifies the portions of the image that consume a lot of memory.  This may help programs that rapidly
     * create and destroy many Images from running out of memory while the Images are being finalized.
     */
    fun dispose() {
        this.pixelData = null
        if (this.coordinateArrayStorage != null) {
            for (ic in this.coordinateArrayStorage!!) {
                ic.recycle()
            }
            this.coordinateArrayStorage = null
        }
    }

    //recycle image coordinates

    @Throws(Throwable::class)
    protected open fun finalize() {
        dimensionSizes.recycle()
        if (this.boxMin != null && this.boxMax != null) {
            boxMax!!.recycle()
            boxMin!!.recycle()
        }
        if (this.coordinateArrayStorage != null) {
            for (ic in this.coordinateArrayStorage!!) {
                ic.recycle()
            }
            this.coordinateArrayStorage = null
        }

        super.finalize()
    }

    /**
     * Sets up a new Metadata object that contains the bare minimum required for reading and writing the image.
     */
    protected fun setupNewMetadata() {
        try {

            this.metadata = loci.common.services.ServiceFactory().getInstance(loci.formats.services.OMEXMLService::class.java).createOMEXMLMetadata()

        } catch (e: loci.common.services.ServiceException) {
            e.printStackTrace()
        } catch (e: loci.common.services.DependencyException) {
            e.printStackTrace()
        }

        this.metadata.createRoot()
        this.metadata.setImageID("Image:0", 0)
        this.metadata.setPixelsID("Pixels:0", 0)
        this.metadata.setPixelsBinDataBigEndian(java.lang.Boolean.TRUE, 0, 0)

        this.setMetadataPixelCharacteristics(this.pixelData)

        this.setMetadataDimensionSizes(this.dimensionSizes)

        for (i in 0..dimensionSizes.get(ImageCoordinate.C) - 1) {
            this.metadata.setChannelID("Channel:0:" + i, 0, i)
            this.metadata.setChannelSamplesPerPixel(PositiveInteger(1), 0, i)
        }
    }

    /**
     * Sets the metadata object to have the same pixel format and dimension order as the supplied PixelData.
     * @param toUse the PixelData whose characteristics will be copied.
     */
    protected fun setMetadataPixelCharacteristics(toUse: PixelData) {
        try {
            this.metadata.setPixelsType(ome.xml.model.enums.PixelType.fromString(loci.formats.FormatTools.getPixelTypeString(this.pixelData!!.dataType)), 0)
        } catch (e: ome.xml.model.enums.EnumerationException) {
            e.printStackTrace()
        }

        try {
            this.metadata.setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder.fromString(this.pixelData!!.dimensionOrder.toUpperCase()), 0)
        } catch (e: ome.xml.model.enums.EnumerationException) {
            e.printStackTrace()
        }

    }

    /**
     * Sets the metadata object to have the same dimension sizes as the supplied ImageCoordinate.
     * @param dimensionSizes the ImageCoordinate specifying the sizes.
     */
    protected fun setMetadataDimensionSizes(dimensionSizes: ImageCoordinate) {

        var size_c = dimensionSizes.get(ImageCoordinate.C)
        if (size_c == 0) size_c++
        var size_t = dimensionSizes.get(ImageCoordinate.T)
        if (size_t == 0) size_t++
        var size_z = dimensionSizes.get(ImageCoordinate.Z)
        if (size_z == 0) size_z++
        var size_x = dimensionSizes.get(ImageCoordinate.X)
        if (size_x == 0) size_x++
        var size_y = dimensionSizes.get(ImageCoordinate.Y)
        if (size_y == 0) size_y++


        this.metadata.setPixelsSizeC(PositiveInteger(size_c), 0)
        this.metadata.setPixelsSizeT(PositiveInteger(size_t), 0)
        this.metadata.setPixelsSizeZ(PositiveInteger(size_z), 0)
        this.metadata.setPixelsSizeX(PositiveInteger(size_x), 0)
        this.metadata.setPixelsSizeY(PositiveInteger(size_y), 0)
    }

    companion object {

        //TODO: add support for Images with dimensions other than 5.

        //fields

        internal val serialVersionUID = 1L
    }


}
