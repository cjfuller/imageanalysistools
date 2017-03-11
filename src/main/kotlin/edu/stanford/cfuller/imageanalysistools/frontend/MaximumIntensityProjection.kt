package edu.stanford.cfuller.imageanalysistools.frontend

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader

import ij.plugin.ZProjector

import java.io.File

/**
 * Utilities for creating maximum intensity projections of images.
 * @author Colin J. Fuller
 */
object MaximumIntensityProjection {
    internal val directorySuffix = "maxintproj"
    internal val outputSuffix = "_proj.ome.tif"

    //TODO: handle projecting Z dimension (or an arbitrary, specified dimension?) in images other than 5D.

    /**
     * Makes a maximum intensity projection of an Image and returns it as an Image.
     * @param im    The Image to be projected.
     * @return      The projection, as an Image.
     */
    fun projectImage(im: Image): Image {
        val zp = ZProjector()
        zp.setMethod(ZProjector.MAX_METHOD)
        zp.setImage(im.toImagePlus())
        zp.setStartSlice(1)
        zp.setStopSlice(im.dimensionSizes[ImageCoordinate.Z])
        zp.doHyperStackProjection(true)
        val imProj = ImageFactory.create(zp.projection)
        return imProj
    }

    /**
     * Makes a maximum intensity projection of an image designated by a given filename; writes the projection back out to disk in a subdirectory.
     * @param filename                  The filename of the image to be projected.
     * @throws java.io.IOException      if there is a problem reading the image from disk or writing the projection to disk.
     */
    @Throws(java.io.IOException::class)
    fun project(filename: String) {
        val imR = ImageReader()
        var im = imR.read(filename)!!
        var imProj = projectImage(im)

        val f = File(filename)
        val directory = f.parent
        val outputDirectory = File(directory + File.separator + directorySuffix)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir()
        }
        val output = outputDirectory.absolutePath + File.separator + f.name

        if (!imR.hasMoreSeries(filename)) {
            imProj.writeToFile(output + outputSuffix)
        } else {
            var imCounter = 0
            imProj.writeToFile(output + "_" + imCounter++ + outputSuffix)
            do {
                im = imR.read(filename)!!
                imProj = projectImage(im)
                imProj.writeToFile(output + "_" + imCounter++ + outputSuffix)
            } while (imR.hasMoreSeries(filename))
        }
    }

    /**
     * Makes maximum intensity projections of all the images in a single directory.
     * @param directory                 The directory containing the images to project.
     * @throws java.io.IOException      if there is a problem reading the image from disk or writing the projection to disk.
     */
    @Throws(java.io.IOException::class)
    fun projectDirectory(directory: String) {
        val dirFile = File(directory)
        println("attempting to project: " + dirFile.absolutePath)

        for (eachFile in dirFile.listFiles()!!) {
            println("current file: " + eachFile.absolutePath)
            try {
                project(eachFile.absolutePath)
            } catch (e: java.io.IOException) {
                LoggingUtilities.logger.warning("Exception encountered while projecting: " + eachFile.absolutePath + "\n" + e.message)
            }
        }
    }
}
