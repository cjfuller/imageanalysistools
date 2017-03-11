package edu.stanford.cfuller.imageanalysistools.filter

import ij.plugin.filter.GaussianBlur
import net.imglib2.algorithm.gauss.GaussFloat
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImgLibPixelData
import edu.stanford.cfuller.imageanalysistools.image.ImagePlusPixelData
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory

/**
 * A Filter that applies a gaussian blur to a 2D Image.
 *
 *
 * This Filter does not use a reference Image.
 *
 *
 * The argument passed to the apply method is the Image to be blurred.

 * @author Colin J. Fuller
 */
class GaussianFilter : Filter() {
    //TODO: deal with more than 2 dimensional blur. (Or make that be the job of other filters and rename this one -2D?)
    internal var width: Int = 0

    /**
     * Constructs a GaussianFilter with a default size blur.
     */
    init {
        this.width = 5
    }

    /**
     * Applies the GaussianFilter to the specified Image, blurring it by convolution with a Gaussian function.
     * @param im    The Image to process, which will be blurred.
     */
    override fun apply(im: WritableImage) {
        val kernelSize = this.width
        val halfKernelSizeCutoff = 8
        val halfKernelSize = (kernelSize - 1) / 2

        if (halfKernelSize > halfKernelSizeCutoff) {
            val gf2 = GaussianFilter()
            gf2.setWidth(halfKernelSize)
            gf2.apply(im)
            gf2.apply(im)
            return
        }

        // if we're dealing with an ImgLib image, use the ImgLib gaussian filtering to avoid duplication of image data.
        if (im.pixelData is ImgLibPixelData) {
            val pd = im.pixelData as ImgLibPixelData
            val imP = pd.img
            // TODO(colin): figure out proper null handling
            val numDim = imP!!.numDimensions()
            val sigmas = DoubleArray(numDim, { 0.0 })
            sigmas[0] = this.width.toDouble()
            sigmas[1] = this.width.toDouble() // only filter in x-y
            GaussFloat(sigmas, imP)
            return
        }
        val imP = im.toImagePlus()
        val gb = GaussianBlur()

        for (i in 0..imP.imageStackSize - 1) {
            imP.setSliceWithoutUpdate(i + 1)
            gb.blur(imP.processor, width.toDouble())
        }
        //only recopy if not an ImagePlusPixelData underneath, which would make duplicating unnecessary
        if (im.pixelData !is ImagePlusPixelData) {
            im.copy(ImageFactory.create(imP))
        }
    }

    /**
     * Sets the width of the Gaussian filter.  This is the standard deviation of the Gaussian function in units of pixels.
     * @param width     The width of the Gaussian to be used for filtering, in pixels.
     */
    fun setWidth(width: Int) {
        this.width = width
        if (this.width % 2 == 0) {
            this.width += 1
        }
    }
}
