package edu.stanford.cfuller.imageanalysistools.image

import ij.ImagePlus

import net.imglib2.img.ImgPlus
import net.imglib2.img.Img
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.img.planar.PlanarImgFactory
import net.imglib2.RandomAccess

/**
 * A type of WritablePixelData that uses an ImgLib2 ImgPlus as its underlying representation.
 * @author Colin J. Fuller
 */
class ImgLibPixelData : WritablePixelData {
    /**
     * Gets the underlying ImgLib Img object.
     * @return the Img object that is used to store the pixel data (not a copy).
     */
    var img: ImgPlus<FloatType>? = null
        private set

    private var ra: RandomAccess<FloatType>? = null

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
    override var dimensionOrder: String = ImgLibPixelData.defaultDimensionOrder
        set(value: String) {
            super.dimensionOrder = value
        }

    internal var has_x: Boolean = false
    internal var has_y: Boolean = false
    internal var has_z: Boolean = false
    internal var has_c: Boolean = false
    internal var has_t: Boolean = false

    internal var xi: Int = 0
    internal var yi: Int = 0
    internal var zi: Int = 0
    internal var ci: Int = 0
    internal var ti: Int = 0

    override fun init(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String) {
        var size_x = size_x
        var size_y = size_y
        var size_z = size_z
        var size_c = size_c
        var size_t = size_t

        this.dimensionOrder = dimensionOrder.toUpperCase()

        this.xi = this.dimensionOrder.indexOf("X")
        this.yi = this.dimensionOrder.indexOf("Y")
        this.zi = this.dimensionOrder.indexOf("Z")
        this.ci = this.dimensionOrder.indexOf("C")
        this.ti = this.dimensionOrder.indexOf("T")

        this.img?.let { img ->
            val dimensionSizeArray = LongArray(numDims)
            java.util.Arrays.fill(dimensionSizeArray, 0L)
            img.dimensions(dimensionSizeArray)

            val numImpglDims = this.img!!.numDimensions()

            if (this.xi < numImpglDims) {
                this.has_x = true
                size_x = dimensionSizeArray[this.xi].toInt()
            }
            if (this.yi < numImpglDims) {
                this.has_y = true
                size_y = dimensionSizeArray[this.yi].toInt()
            }
            if (this.zi < numImpglDims) {
                this.has_z = true
                size_z = dimensionSizeArray[this.zi].toInt()
            }
            if (this.ci < numImpglDims) {
                this.has_c = true
                size_c = dimensionSizeArray[this.ci].toInt()
            }
            if (this.ti < numImpglDims) {
                this.has_t = true
                size_t = dimensionSizeArray[this.ti].toInt()
            }
        }
        if (this.img == null) {
            this.has_x = true
            this.has_y = true
            this.has_z = true
            this.has_c = true
            this.has_t = true
        }

        this.sizeX = size_x
        this.sizeY = size_y
        this.sizeZ = size_z
        this.sizeC = size_c
        this.sizeT = size_t
    }

    /**
     * This ensures that the dimension order for the ImgPl object does not have
     * any unknown dimension types.
     */
    private fun fixDimensionOrder() {
        for (i in 0..this.dimensionOrder.length - 1) {
            if (i >= this.img!!.numDimensions()) break
            val currChar = this.dimensionOrder[i]
            if (currChar == 'X') {
                this.img!!.setAxis(net.imglib2.meta.Axes.X, i)
            } else if (currChar == 'Y') {
                this.img!!.setAxis(net.imglib2.meta.Axes.Y, i)
            } else if (currChar == 'Z') {
                this.img!!.setAxis(net.imglib2.meta.Axes.Z, i)
            } else if (currChar == 'C') {
                this.img!!.setAxis(net.imglib2.meta.Axes.CHANNEL, i)
            } else if (currChar == 'T') {
                this.img!!.setAxis(net.imglib2.meta.Axes.TIME, i)
            }
        }
    }

    private fun initNewImgPlus() {
        this.dimensionOrder = ImgLibPixelData.defaultDimensionOrder
        val imgf = PlanarImgFactory<FloatType>()
        val dims = longArrayOf(this.sizeX.toLong(), this.sizeY.toLong(), this.sizeZ.toLong(), this.sizeC.toLong(), this.sizeT.toLong())
        val im = imgf.create(dims, FloatType())
        this.img = ImgPlus(im)
        this.fixDimensionOrder()
        this.ra = this.img!!.randomAccess()
    }

    private constructor() {}

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate, int, String)
	 */
    constructor(sizes: ImageCoordinate, dimensionOrder: String) : super(sizes, dimensionOrder) {
        this.initNewImgPlus()
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(int, int, int, int, int, String)
	 */
    constructor(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, dimensionOrder: String) : super(size_x, size_y, size_z, size_c, size_t, dimensionOrder) {
        this.initNewImgPlus()
    }

    /**
     * Creates a new ImgLibPixelData from an existing ImgPlus and a specified dimension order.
     * @param imPl    The ImgPlus to use.  This will not be copied, but used and potentially modified in place.
     * @param dimensionOrder    a String containing the five characters XYZCT in the order they are in the image (if some dimensions are not present, the can be specified in any order)
     */
    constructor(imPl: ImgPlus<FloatType>, dimensionOrder: String) {
        this.img = imPl
        this.init(1, 1, 1, 1, 1, dimensionOrder)
        this.fixDimensionOrder()
        this.ra = this.img!!.randomAccess()
    }


    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getPixel(int, int, int, int, int)
	 */
    override fun getPixel(x: Int, y: Int, z: Int, c: Int, t: Int): Float {
        this.ra!!.setPosition(x, this.xi)
        this.ra!!.setPosition(y, this.yi)
        if (this.has_z) this.ra!!.setPosition(z, this.zi)
        if (this.has_c) this.ra!!.setPosition(c, this.ci)
        if (this.has_t) this.ra!!.setPosition(t, this.ti)
        return this.ra!!.get().get()
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#setPixel(int, int, int, int, int, float)
	 */
    override fun setPixel(x: Int, y: Int, z: Int, c: Int, t: Int, value: Float) {
        this.ra!!.setPosition(x, this.xi)
        this.ra!!.setPosition(y, this.yi)
        if (this.has_z) this.ra!!.setPosition(z, this.zi)
        if (this.has_c) this.ra!!.setPosition(c, this.ci)
        if (this.has_t) this.ra!!.setPosition(t, this.ti)
        this.ra!!.get().set(value)
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#toImagePlus()
	 */
    override fun toImagePlus(): ImagePlus {
        val ippd = ImagePlusPixelData(this.sizeX, this.sizeY, this.sizeZ, this.sizeC, this.sizeT, loci.formats.FormatTools.FLOAT, this.dimensionOrder)
        val a_copy = ImageFactory.createWritable(ReadOnlyImageImpl.createNewMetadata(), ippd)
        for (ic in a_copy) {
            a_copy.setValue(
                    ic,
                    this.getPixel(
                            ic[ImageCoordinate.X], ic[ImageCoordinate.Y],
                            ic[ImageCoordinate.Z], ic[ImageCoordinate.C], ic[ImageCoordinate.T]))
        }
        return a_copy.toImagePlus()
    }

    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
    override val dataType: Int
        get() = loci.formats.FormatTools.FLOAT

    companion object {
        private val serialVersionUID = 1435719453195463339L
        private val defaultDimensionOrder = ImageCoordinate.defaultDimensionOrder
        internal val numDims = 5
    }
}

