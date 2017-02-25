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

import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerImageReader
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerInfo
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary


/**
 * Utilities for getting files from a directory or OMERO data source and matching them together into sets of the different channels of the same image.

 * @author Colin J. Fuller
 */
object DirUtils {


    /**
     * Gets filenames of the images to be processed (either from a directory or OMERO data source), along with a display name (i.e. name without a full path) for each.
     * @param params    The ParameterDictionary containing the parameters to be used for the analysis; the data directory or OMERO data source will be pulled from here.
     * *
     * @return          A List containing ImageSets, one per file.
     */
    fun makeMultiwavelengthFileSets(params: ParameterDictionary): List<ImageSet> {


        val outputSets = java.util.Vector<ImageSet>()

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            val imageIds = params.getValueForKey("omero_image_ids").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


            for (id in imageIds) {

                val currSet = ImageSet(params)

                currSet.addImageWithOmeroId(java.lang.Long.parseLong(id))

                outputSets.add(currSet)
            }


        } else {

            val directory = java.io.File(params.getValueForKey("local_directory"))

            var commonFilenameTag = ""
            var imageExtension = ""

            if (params.hasKey("image_extension")) {
                imageExtension = params.getValueForKey("image_extension")
            }

            if (params.hasKey("common_filename_tag")) {
                commonFilenameTag = params.getValueForKey("common_filename_tag")
            }

            for (f in directory.listFiles()!!) {

                if (f.isDirectory) {
                    continue
                }


                if (f.name.matches(".*Thumb.*".toRegex()) || !f.name.toLowerCase().matches((".*" + imageExtension.toLowerCase() + "$").toRegex()) || !f.name.matches(".*$commonFilenameTag.*".toRegex())) {
                    continue
                }

                println(f.absolutePath)


                val set = ImageSet(params)

                set.addImageWithFilename(f.absolutePath)


                outputSets.add(set)

            }
        }


        return outputSets

    }


    /**
     * Gets a list of String arrays each containing a set of filenames that correspond to image files for the color channels of a split channel image.

     * @param params    The ParameterDictionary containing the parameters for the analysis.  The directory or OMERO source and channel names will be taken from here.
     * *
     * @return          A List containing ImageSets.  Each set can load the Images for the channels of an image.
     */
    fun makeSetsOfMatchingFiles(params: ParameterDictionary): List<ImageSet> {

        val setTags = params.getValueForKey("channel_name").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        var numPerSet = 0
        if (params.hasKey("number_of_channels")) {
            numPerSet = Integer.parseInt(params.getValueForKey("number_of_channels"))
        } else {
            numPerSet = setTags.size
            params.addIfNotSet("number_of_channels", Integer.toString(numPerSet))
        }

        val outputSets = java.util.Vector<ImageSet>()


        val directory = java.io.File(params.getValueForKey("local_directory"))

        val idLookupByName = java.util.HashMap<String, Long>()

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            val imageIds = params.getValueForKey("omero_image_ids").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val ir = OmeroServerImageReader()

            try {

                for (id in imageIds) {

                    val idL = java.lang.Long.parseLong(id)

                    val osi = OmeroServerInfo(params.getValueForKey("omero_hostname"), params.getValueForKey("omero_username"), params.getValueForKey("omero_password").toCharArray())

                    val name = ir.getImageNameForOmeroId(idL, osi)

                    idLookupByName.put(name, idL)

                }

            } catch (e: java.io.IOException) {

                LoggingUtilities.logger.severe("Exception encountered while accessing image on OMERO server: ")
                e.printStackTrace()

            } finally {
                ir.closeConnection()
            }


        } else {


            for (f in directory.listFiles()!!) {

                if (f.isDirectory) {
                    continue
                }

                //added hack here for case insensitive extension

                var commonFilenameTag = ""
                var imageExtension = ""

                if (params.hasKey("image_extension")) {
                    imageExtension = params.getValueForKey("image_extension")
                }

                if (params.hasKey("common_filename_tag")) {
                    commonFilenameTag = params.getValueForKey("common_filename_tag")
                }


                if (f.name.matches(".*Thumb.*".toRegex()) || !f.name.toLowerCase().matches((".*" + imageExtension.toLowerCase() + "$").toRegex()) || !f.name.matches(".*$commonFilenameTag.*".toRegex())) {
                    continue
                }

                idLookupByName.put(f.absolutePath, null)
            }

        }

        for (name in idLookupByName.keys) {

            if (!name.matches((".*" + setTags[0] + ".*").toRegex())) {
                continue
            }

            var tempSet: ImageSet? = ImageSet(params)

            for (i in 0..numPerSet - 1) {

                //tempSet[i] = directory.getAbsolutePath() + java.io.File.separator + f.getName().replaceAll(setTags[0], setTags[i]);

                val subName = name.replace(setTags[0].toRegex(), setTags[i])

                if (!idLookupByName.containsKey(subName)) {
                    tempSet = null
                    break
                }

                val id = idLookupByName[subName]


                if (id != null) {
                    tempSet!!.addImageWithOmeroIdAndName(id, subName)
                } else {
                    tempSet!!.addImageWithFilename(subName)
                }
            }

            outputSets.add(tempSet)
        }

        return outputSets

    }

}
