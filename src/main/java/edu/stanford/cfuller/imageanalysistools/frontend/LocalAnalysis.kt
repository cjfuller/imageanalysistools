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

import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper
import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader
import edu.stanford.cfuller.imageanalysistools.method.Method
import edu.stanford.cfuller.imageanalysistools.metric.Measurement
import edu.stanford.cfuller.imageanalysistools.metric.Quantification

import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import java.util.Comparator

/**
 * Controls analysis done on the local machine, including routines for threading analysis, and data input and output.

 * @author Colin J. Fuller
 */

class LocalAnalysis protected constructor() {

    private class ImageSetThread(private val am: AnalysisMetadata) : Thread() {

        override fun run() {
            try {
                processFileSet(am)
            } catch (e: java.io.IOException) {
                LoggingUtilities.logger.severe("while processing " + am.inputImages.getImageNameForIndex(0) + ": " + e.toString())
                e.printStackTrace()
            }

        }

    }

    companion object {

        private val threadPool = java.util.LinkedList<ImageSetThread>()

        private val threadWaitTime_ms = 5000

        internal val DATA_OUTPUT_DIR = AnalysisController.DATA_OUTPUT_DIR
        internal val SERIALIZED_DATA_SUFFIX = AnalysisController.SERIALIZED_DATA_SUFFIX
        internal val IMAGE_OUTPUT_DIR = AnalysisController.IMAGE_OUTPUT_DIR
        internal val PARAMETER_OUTPUT_DIR = AnalysisController.PARAMETER_OUTPUT_DIR
        internal val PARAMETER_EXTENSION = AnalysisController.PARAMETER_EXTENSION

        internal val IMAGE_OUTPUT_DIR_PARAM = "image_output_directory"

        /**
         * Runs the analysis on the local machine.

         * The current implementation is multithreaded if specified in the parameter dictionary (and currently the default value
         * specifies as many threads as processor cores on the machine), so analysis methods should be thread safe.

         * Each thread uses [.processFileSet] to do the processing.

         * @param am    The AnalysisMetadata specifying the options for the analysis.
         */
        fun run(am: AnalysisMetadata) {

            val namedFileSets: List<ImageSet>? = null

            val params = am.outputParameters

            var imageSets: List<ImageSet>? = null

            if (params.hasKeyAndTrue("multi_wavelength_file") || !params.hasKey("multi_wavelength_file")) {
                imageSets = DirUtils.makeMultiwavelengthFileSets(params)
            } else {

                imageSets = DirUtils.makeSetsOfMatchingFiles(params)
            }

            var maxThreads = 1

            if (params.hasKey("max_threads")) {

                maxThreads = params.getIntValueForKey("max_threads")

            }

            for (images in imageSets!!) {

                val singleSetMeta = am.makeCopy()

                singleSetMeta.inputImages = images

                val nextSet = ImageSetThread(singleSetMeta)

                if (threadPool.size < maxThreads) {

                    LoggingUtilities.logger.info("Processing " + images.getImageNameForIndex(0))

                    threadPool.add(nextSet)
                    nextSet.start()

                } else {

                    var nextInPool = threadPool.poll()

                    try {
                        nextInPool.join(threadWaitTime_ms.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    while (nextInPool.isAlive) {

                        threadPool.add(nextInPool)
                        nextInPool = threadPool.poll()
                        try {
                            nextInPool.join(threadWaitTime_ms.toLong())
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }


                    }

                    LoggingUtilities.logger.info("Processing " + images.getImageNameForIndex(0))

                    threadPool.add(nextSet)
                    nextSet.start()

                }


            }

            while (!threadPool.isEmpty()) {
                try {
                    val ist = threadPool.poll()
                    ist.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        /**
         * Processes a single set of image files, corresponding to all the wavelengths of a single 3D (or 4D XYZT image).
         * This will write the output to disk to subdirectories of the directory containing the images.

         * @param am    The AnalysisMetadata specifying the options for the analysis and containing the ImageSet of input images.
         * *
         * @throws java.io.IOException  if the images cannot be read or the output cannot be written to disk.
         */
        @Throws(java.io.IOException::class)
        fun processFileSet(am: AnalysisMetadata) {

            val params = am.outputParameters

            params.setValueForKey("filename", am.inputImages.getImageNameForIndex(0))

            loadImages(am)

            //set the numberOfChannels and channelName parameters appropriately for the multi-wavelength file

            params.addIfNotSet("number_of_channels", Parameter("number_of_channels", "number_of_channels", ParameterType.INTEGER_T, am.inputImages.imageCount, null))

            if (params.hasKeyAndTrue("process_max_intensity_projections")) {

                val images = am.inputImages

                val newImages = ImageSet(params)

                for (i in images.indices) {

                    var toProject = images.getImageForIndex(i)

                    if (params.hasKeyAndTrue("swap_z_t")) {
                        toProject = DimensionFlipper.flipZT(toProject)
                    }

                    val proj = MaximumIntensityProjection.projectImage(toProject)

                    newImages.addImageWithImageAndName(proj, images.getImageNameForIndex(i))

                }

                newImages.setMarkerImage(images.markerIndex!!)

                am.inputImages.disposeImages()

                am.inputImages = newImages
            }


            val methodToRun = Method.loadMethod(params.getValueForKey("method_name"))

            am.method = methodToRun

            am.outputFiles.clear() // Need to clear out any previous output from the analysis metadata file.

            methodToRun.analysisMetadata = am

            methodToRun.go()

            am.timestamp()

            writeDataOutput(am)

            try {
                writeImageOutput(am)
                writeParameterOutput(am)
            } catch (e: java.io.IOException) {
                LoggingUtilities.logger.severe("Error while writing output masks to file; skipping write and continuing.")
                e.printStackTrace()
            }

            am.inputImages.disposeImages()
            am.outputImages.disposeImages()

        }

        @Throws(java.io.IOException::class)
        private fun loadImages(am: AnalysisMetadata) {

            am.validateInputImages(true)

            if (!am.inputParameters.hasKey("multi_wavelength_file") || am.inputParameters.getBooleanValueForKey("multi_wavelength_file")) {
                val split = loadSplitMutliwavelengthImages(am.inputImages)
                am.inputImages.disposeImages()
                am.inputImages = split
            } else {
                am.inputImages.loadAllImages()
            }

            if (am.inputParameters.hasKey("marker_channel_index")) {
                am.inputImages.setMarkerImage(am.inputParameters.getIntValueForKey("marker_channel_index"))
            }

        }

        @Synchronized @Throws(java.io.IOException::class)
        private fun loadSplitMutliwavelengthImages(fileSet: ImageSet): ImageSet {

            fileSet.loadAllImages()

            val multiwavelength = fileSet.getImageForIndex(0)


            val split = multiwavelength.splitChannels()


            val splitSet = ImageSet(fileSet.parameters)

            for (i in split) {
                splitSet.addImageWithImageAndName(i, fileSet.getImageNameForIndex(0))
            }

            splitSet.combinedImage = multiwavelength

            return splitSet


        }

        fun generateDataOutputString(data: Quantification?, p: ParameterDictionary): String {

            val output = StringBuilder()

            if (data == null) {
                return ""
            }

            val regions = data.allRegions

            val sortedRegions = java.util.ArrayList<Long>()

            sortedRegions.addAll(regions)

            java.util.Collections.sort(sortedRegions)

            val backgroundSuffix = "_background"

            val columnHeadings = java.util.ArrayList<String>()

            val allOrderedMeasurements = java.util.HashMap<Long, List<Measurement>>()

            for (label in sortedRegions) {

                val measurements = data.getAllMeasurementsForRegion(label)

                val intensityMeasurements = java.util.ArrayList<Measurement>()

                val backgroundMeasurements = java.util.ArrayList<Measurement>()

                val otherMeasurements = java.util.ArrayList<Measurement>()

                for (m in measurements) {

                    if (m.measurementType === Measurement.TYPE_INTENSITY) {
                        intensityMeasurements.add(m)
                    } else if (m.measurementType === Measurement.TYPE_BACKGROUND) {
                        backgroundMeasurements.add(m)
                    } else {
                        otherMeasurements.add(m)
                    }

                }

                java.util.Collections.sort(intensityMeasurements) { o1, o2 -> o1.measurementName.compareTo(o2.measurementName) }

                val orderedMeasurements = java.util.ArrayList<Measurement>()

                for (i in measurements.indices) {
                    orderedMeasurements.add(null)
                }

                for (m in intensityMeasurements) {

                    var i = columnHeadings.indexOf(m.measurementName)

                    if (i == -1) {// not found in list

                        columnHeadings.add(m.measurementName)
                        i = columnHeadings.size - 1

                    }

                    if (i >= orderedMeasurements.size) {
                        for (j in orderedMeasurements.size..i) {
                            orderedMeasurements.add(null)
                        }
                    }

                    orderedMeasurements[i] = m


                }


                for (m in intensityMeasurements) {

                    var bkg: Measurement? = null

                    for (b in backgroundMeasurements) {

                        if (b.measurementName == m.measurementName) {

                            bkg = b

                            break

                        }

                    }

                    if (bkg == null) continue

                    val backgroundName = bkg.measurementName + backgroundSuffix

                    var i = columnHeadings.indexOf(backgroundName)

                    if (i == -1) {// not found in list

                        columnHeadings.add(backgroundName)
                        i = columnHeadings.size - 1

                    }

                    if (i >= orderedMeasurements.size) {
                        for (j in orderedMeasurements.size..i) {
                            orderedMeasurements.add(null)
                        }
                    }

                    orderedMeasurements[i] = bkg

                }

                for (m in otherMeasurements) {

                    var i = columnHeadings.indexOf(m.measurementName)

                    if (i == -1) {// not found in list

                        columnHeadings.add(m.measurementName)
                        i = columnHeadings.size - 1

                    }

                    if (i >= orderedMeasurements.size) {
                        for (j in orderedMeasurements.size..i) {
                            orderedMeasurements.add(null)
                        }
                    }

                    orderedMeasurements[i] = m

                }

                allOrderedMeasurements.put(label, orderedMeasurements)


            }

            output.append("region ")

            for (s in columnHeadings) {
                output.append(s)
                output.append(" ")
            }
            output.append("\n")

            for (l in sortedRegions) {
                val orderedMeasurements = allOrderedMeasurements[l]

                output.append("" + l + " ")

                for (m in orderedMeasurements) {
                    if (m == null) {
                        output.append("N/A ")
                    } else {
                        output.append(m!!.getMeasurement())
                        output.append(" ")
                    }
                }

                output.append("\n")

            }

            return output.toString()
        }

        private fun getOutputMethodNameString(am: AnalysisMetadata): String {

            var longMethodName: String? = am.method.displayName

            if (longMethodName == null) {
                longMethodName = am.method.javaClass.name
            }

            val splitMethodName = longMethodName!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val shortMethodName = splitMethodName[splitMethodName.size - 1]

            return shortMethodName

        }

        private fun getOutputDataFileSuffix(am: AnalysisMetadata): String {

            val shortMethodName = getOutputMethodNameString(am)

            val outputSuffix = ".$shortMethodName.out.txt"

            return outputSuffix

        }

        private fun getOutputImageFileSuffix(am: AnalysisMetadata): String {

            val shortMethodName = getOutputMethodNameString(am)

            val outputSuffix = ".$shortMethodName.out.ome.tif"

            return outputSuffix

        }

        private fun getOutputParameterFileSuffix(am: AnalysisMetadata): String {

            val shortMethodName = getOutputMethodNameString(am)

            val outputSuffix = "." + shortMethodName + PARAMETER_EXTENSION

            return outputSuffix

        }

        @Throws(java.io.IOException::class)
        private fun writeDataOutput(am: AnalysisMetadata) {

            val outputParams = am.outputParameters

            val output_dir_suffix = DATA_OUTPUT_DIR

            val outputPath = java.io.File(outputParams.getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix)

            if (!outputPath.exists()) {
                outputPath.mkdir()
            }

            val serializedOutputPath = java.io.File(outputPath.absolutePath + java.io.File.separator + SERIALIZED_DATA_SUFFIX)

            if (!serializedOutputPath.exists()) {
                serializedOutputPath.mkdir()
            }

            val outputFilename = java.io.File(am.inputImages.getImageNameForIndex(0)).name + getOutputDataFileSuffix(am)

            val relativeOutputFilename = outputPath.name + File.separator + outputFilename

            val dataOutputFilename = outputPath.parent + File.separator + relativeOutputFilename

            val serializedOutputFilename = serializedOutputPath.absolutePath + File.separator + outputFilename

            val output = PrintWriter(FileOutputStream(dataOutputFilename))

            val serializedOutput = ObjectOutputStream(FileOutputStream(serializedOutputFilename))

            val data = am.method.storedDataOutput

            serializedOutput.writeObject(data)

            serializedOutput.close()

            output.write(generateDataOutputString(data, outputParams))

            output.close()

            am.addOutputFile(dataOutputFilename)

        }

        @Throws(java.io.IOException::class)
        private fun writeImageOutput(am: AnalysisMetadata) {

            val output_dir_suffix = IMAGE_OUTPUT_DIR

            val outputParams = am.outputParameters

            var outputPath: java.io.File? = null

            if (outputParams.hasKey(IMAGE_OUTPUT_DIR_PARAM)) {
                outputPath = java.io.File(outputParams.getValueForKey(IMAGE_OUTPUT_DIR_PARAM))
            } else {
                outputPath = java.io.File(outputParams.getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix)
            }
            if (!outputPath.exists()) {
                outputPath.mkdir()
            }

            val splitMethodName = outputParams.getValueForKey("method_name").split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val shortMethodName = splitMethodName[splitMethodName.size - 1]

            val relativeOutputFilename = outputPath.name + File.separator + java.io.File(am.inputImages.getImageNameForIndex(0)).name + getOutputImageFileSuffix(am)

            val maskOutputFilename = outputPath.parent + File.separator + relativeOutputFilename

            val outputImages = ImageSet(outputParams)

            if (am.method.storedImages.size == 1) {

                am.method.storedImage.writeToFile(maskOutputFilename)

                outputImages.addImageWithImageAndName(am.method.storedImage, maskOutputFilename)

            } else {

                var imageCounter = 0

                for (i in am.method.storedImages) {

                    val multiMaskOutputFilename = relativeOutputFilename.replace(".out.ome.tif", ".out." + Integer.toString(imageCounter) + ".ome.tif")

                    val fullFilename = outputPath.parent + File.separator + multiMaskOutputFilename

                    i.writeToFile(fullFilename)

                    outputImages.addImageWithImageAndName(i, fullFilename)

                    ++imageCounter

                }
            }

            am.outputImages = outputImages

        }

        @Throws(java.io.IOException::class)
        private fun writeParameterOutput(am: AnalysisMetadata) {

            val parameterDirectory = PARAMETER_OUTPUT_DIR

            val pd = am.outputParameters

            val outputPath = File(pd.getValueForKey("local_directory") + File.separator + parameterDirectory)

            if (!outputPath.exists()) {
                outputPath.mkdir()
            }

            val splitMethodName = pd.getValueForKey("method_name").split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val shortMethodName = splitMethodName[splitMethodName.size - 1]

            val parameterOutputFilename = outputPath.absolutePath + File.separator + File(am.inputImages.getImageNameForIndex(0)).name + getOutputParameterFileSuffix(am)

            AnalysisMetadataXMLWriter().writeAnalysisMetadataToXMLFile(am, parameterOutputFilename)

        }
    }


}
