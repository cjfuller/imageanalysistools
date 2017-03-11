package edu.stanford.cfuller.imageanalysistools.image.io

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory

import edu.stanford.cfuller.imageanalysistools.image.PixelDataFactory

import net.imglib2.io.ImgOpener
import net.imglib2.img.ImgPlus
import net.imglib2.img.Img
import net.imglib2.type.numeric.real.FloatType
import net.imglib2.img.array.ArrayImgFactory

/**
 * This class reads an Image from a file on disk.
 *
 * Uses the LOCI bio-formats library to do the reading (http://www.loci.wisc.edu/software/bio-formats), so
 * all formats supported by that library are supported in this reader.
 *
 * For image formats that support multiple series per file (that is, multiple (X,Y,Z,C,T) 5D images per file), the reader
 * will return only a single series at a time, keeps track of what series it has last read, and will return the subsequent series
 * on subsequent calls to the read method.
 * @author Colin J. Fuller
 */
class ImgLibImageReader : ImageReader() {
    /**
     * Reads an image file from disk to an Image.
     * @param filename  The full absolute filename of the image file to read.
     * @return          An Image containing the pixel data and (if supported by the format) metadata from the image file.
     * @throws java.io.IOException  if there is an error reading the Image file from disk.
     */
    @Synchronized @Throws(java.io.IOException::class)
    override fun read(filename: String): Image? {
        val o = ImgOpener()
        var reader: loci.formats.IFormatReader? = null

        try {
            ImageReader.lock(filename)
        } catch (e: InterruptedException) {
            LoggingUtilities.logger.warning("interrupted while attempting to lock image file")
            return null
        }

        try {
            reader = ImgOpener.createReader(filename, true)
        } catch (e: loci.formats.FormatException) {
            throw java.io.IOException(e)
        }
        val meta = reader!!.metadataStore as loci.formats.meta.IMetadata
        var img: Img<FloatType>? = null
        try {
            img = o.openImg(reader, ArrayImgFactory<FloatType>(), FloatType())
        } catch (e: net.imglib2.io.ImgIOException) {
            throw java.io.IOException(e)
        }

        val p = PixelDataFactory.createPixelData(ImgPlus(img!!), meta.getPixelsDimensionOrder(0).toString())
        val toReturn = ImageFactory.create(meta, p)
        lociReader.close()
        seriesCounts.put(filename, 1)
        currentSeries.put(filename, 1)
        release(filename, Thread.currentThread())
        return toReturn
    }
}
