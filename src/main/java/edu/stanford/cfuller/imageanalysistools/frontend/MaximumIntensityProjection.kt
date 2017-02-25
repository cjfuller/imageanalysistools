/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
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
     * *
     * @return      The projection, as an Image.
     */
    fun projectImage(im: Image): Image {


        val zp = ZProjector()

        zp.setMethod(ZProjector.MAX_METHOD)

        zp.setImage(im.toImagePlus())

        zp.setStartSlice(1)
        zp.setStopSlice(im.dimensionSizes.get(ImageCoordinate.Z))

        zp.doHyperStackProjection(true)

        val imProj = ImageFactory.create(zp.projection)

        return imProj
    }

    /**
     * Makes a maximum intensity projection of an image designated by a given filename; writes the projection back out to disk in a subdirectory.
     * @param filename                  The filename of the image to be projected.
     * *
     * @throws java.io.IOException      if there is a problem reading the image from disk or writing the projection to disk.
     */
    @Throws(java.io.IOException::class)
    fun project(filename: String) {

        val imR = ImageReader()

        var im = imR.read(filename)

        //do the projection

        var imProj = projectImage(im)

        //write the output

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
                im = imR.read(filename)
                imProj = projectImage(im)
                imProj.writeToFile(output + "_" + imCounter++ + outputSuffix)
            } while (imR.hasMoreSeries(filename))

        }

    }

    /**
     * Makes maximum intensity projections of all the images in a single directory.
     * @param directory                 The directory containing the images to project.
     * *
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
