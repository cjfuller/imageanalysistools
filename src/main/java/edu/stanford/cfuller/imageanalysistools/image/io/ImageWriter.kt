package edu.stanford.cfuller.imageanalysistools.image.io

import java.io.File

import ome.xml.model.enums.EnumerationException
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.PixelData
import edu.stanford.cfuller.imageanalysistools.image.ImgLibPixelData

import net.imglib2.io.ImgSaver

/**
 * This class writes an Image to disk as one of several possible formats.

 * @author Colin J. Fuller
 */

class ImageWriter(im: Image) {
    /**
     * Constructs an ImageWriter for a given Image.
     *
     *
     * The writer retains a reference to the Image, so it can be used multiple times for writing the same Image to disk.  Only the Image's metadata will be used;
     * pixel data is passed in separately, allowing several Images that share the same metadata to be written with the same writer.
     * @param im    The Image whose metadata will be used to write Images to disk.
     */
    /**
     * The image that will be written.
     */
    internal var toWrite: Image = im

    @Throws(java.io.IOException::class)
    private fun imgLibWrite(filename: String, p: ImgLibPixelData) {
        val `is` = ImgSaver()
        try {
            `is`.saveImg(filename, p.img)
        } catch (e: net.imglib2.io.ImgIOException) {
            throw java.io.IOException(e)
        } catch (e: net.imglib2.exception.IncompatibleTypeException) {
            throw java.io.IOException(e)
        }
    }

    /**
     * Writes the specified pixels to disk with the ImageWriter's Image's metadata.
     *
     * The output format will be specified by the filename's extenstion and must be a valid writable extension for the LOCI bioformats library.
     * The recommended format is ome-tiff, with extension ".ome.tif".
     * @param filename  The absolute path of the file to which the image will be written.  The parent directory should exist.
     * @param imagePixels   The PixelData containing the values to write to disk.
     * @throws java.io.IOException  if there is an exception while opening or writing the file.
     */
    @Throws(java.io.IOException::class)
    fun write(filename: String, imagePixels: PixelData) {
        //until I phase out other types of pixel data
        if (imagePixels is ImgLibPixelData) {
            this.imgLibWrite(filename, imagePixels)
            return
        }
        val writer = loci.formats.ImageWriter()

        try {
            toWrite.metadata.setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder.fromString(imagePixels.dimensionOrder.toUpperCase()), 0)
            toWrite.metadata.setPixelsType(ome.xml.model.enums.PixelType.fromString(loci.formats.FormatTools.getPixelTypeString(imagePixels.dataType)), 0)

        } catch (e: EnumerationException) {
            LoggingUtilities.logger.severe("Exception while trying to update dimension order of pixel data: " + e.message)
        }
        val m = toWrite.metadata
        writer.metadataRetrieve = toWrite.metadata
        try {
            val f = File(filename)
            if (f.exists()) {
                // there seems to be some bug with adding to the end of an existing image rather than overwriting if
                // it's there already; delete any existing file to avoid this.
                f.delete()
            }
            writer.setId(filename)
            for (i in 0..imagePixels.numPlanes - 1) {
                val bytes = imagePixels.getPlane(i)
                writer.savePlane(i, bytes)
            }
        } catch (e: loci.formats.FormatException) {
            LoggingUtilities.logger.severe("Encountered exception " + e.toString() + " while writing " + filename + " skipping write and proceeding.")
            e.printStackTrace()
            return
        } finally {
            writer.close()
        }
    }
}
