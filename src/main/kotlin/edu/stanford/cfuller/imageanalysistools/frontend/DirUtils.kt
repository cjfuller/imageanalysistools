package edu.stanford.cfuller.imageanalysistools.frontend

import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * Utilities for getting files from a directory or OMERO data source and matching them together into
 * sets of the different channels of the same image.
 *
 * @author Colin J. Fuller
 */
object DirUtils {
    /**
     * Gets filenames of the images to be processed (either from a directory or OMERO data source),
     * along with a display name (i.e. name without a full path) for each.
     * @param params The ParameterDictionary containing the parameters to be used for the analysis;
     * the data directory or OMERO data source will be pulled from here.
     * *
     * @return A List containing ImageSets, one per file.
     */
    fun makeMultiwavelengthFileSets(params: ParameterDictionary): List<ImageSet> {
        val outputSets = java.util.Vector<ImageSet>()
        val directory = java.io.File(params.getValueForKey("local_directory"))
        var commonFilenameTag = ""
        var imageExtension = ""
        if (params.hasKey("image_extension")) {
            imageExtension = params.getValueForKey("image_extension")!!
        }
        if (params.hasKey("common_filename_tag")) {
            commonFilenameTag = params.getValueForKey("common_filename_tag")!!
        }

        for (f in directory.listFiles()!!) {
            if (f.isDirectory) {
                continue
            }
            if (f.name.matches(".*Thumb.*".toRegex()) ||
                            !f.name.toLowerCase()
                                    .matches(
                                            (".*" + imageExtension.toLowerCase() + "$").toRegex()
                                    ) ||
                            !f.name.matches(".*$commonFilenameTag.*".toRegex())
            ) {
                continue
            }
            println(f.absolutePath)
            val set = ImageSet(params)
            set.addImageWithFilename(f.absolutePath)
            outputSets.add(set)
        }
        return outputSets
    }

    /**
     * Gets a list of String arrays each containing a set of filenames that correspond to image
     * files for the color channels of a split channel image.
     * @param params The ParameterDictionary containing the parameters for the analysis. The
     * directory or OMERO source and channel names will be taken from here.
     * @return A List containing ImageSets. Each set can load the Images for the channels of an
     * image.
     */
    fun makeSetsOfMatchingFiles(params: ParameterDictionary): List<ImageSet> {
        val setTags =
                params.getValueForKey("channel_name")!!
                        .split(" ".toRegex())
                        .dropLastWhile(String::isEmpty)
                        .toTypedArray()

        var numPerSet = 0
        if (params.hasKey("number_of_channels")) {
            numPerSet = Integer.parseInt(params.getValueForKey("number_of_channels"))
        } else {
            numPerSet = setTags.size
            params.addIfNotSet("number_of_channels", Integer.toString(numPerSet))
        }

        val outputSets = java.util.Vector<ImageSet>()
        val directory = java.io.File(params.getValueForKey("local_directory"))
        val idLookupByName = mutableMapOf<String, Long?>()

        for (f in directory.listFiles()!!) {
            if (f.isDirectory) {
                continue
            }
            // added hack here for case insensitive extension
            var commonFilenameTag = ""
            var imageExtension = ""

            if (params.hasKey("image_extension")) {
                imageExtension = params.getValueForKey("image_extension")!!
            }

            if (params.hasKey("common_filename_tag")) {
                commonFilenameTag = params.getValueForKey("common_filename_tag")!!
            }

            if (f.name.matches(".*Thumb.*".toRegex()) ||
                            !f.name.toLowerCase()
                                    .matches(
                                            (".*" + imageExtension.toLowerCase() + "$").toRegex()
                                    ) ||
                            !f.name.matches(".*$commonFilenameTag.*".toRegex())
            ) {
                continue
            }
            idLookupByName.put(f.absolutePath, null)
        }

        for (name in idLookupByName.keys) {
            if (!name.matches((".*" + setTags[0] + ".*").toRegex())) {
                continue
            }

            var tempSet: ImageSet? = ImageSet(params)

            for (i in 0..numPerSet - 1) {
                val subName = name.replace(setTags[0].toRegex(), setTags[i])
                if (!idLookupByName.containsKey(subName)) {
                    tempSet = null
                    break
                }
                tempSet!!.addImageWithFilename(subName)
            }
            outputSets.add(tempSet)
        }
        return outputSets
    }
}
