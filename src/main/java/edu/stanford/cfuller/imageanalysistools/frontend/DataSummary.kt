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

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.PrintWriter

import edu.stanford.cfuller.imageanalysistools.metric.Measurement
import edu.stanford.cfuller.imageanalysistools.metric.Quantification
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory


/**
 * Summarizes the output from analysis routines, combining all output from a directory into a single unified file.
 * If objects have been clustered into groups, summarizes only over those groups, omitting data on individual objects.

 * @author Colin J. Fuller
 */


object DataSummary {


    /**
     * Creates a summary of output files created by the analysis program.

     * @param directory     The full path to the directory containing the output files to be summarized.
     * *
     * @param parameterDirectory       The directory that stores the parameters for the analysis.
     * *
     * @throws java.io.IOException      If any problems reading the analysis output files or writing the summary to disk are encountered.
     */
    @Throws(java.io.IOException::class)
    fun SummarizeData(directory: String, parameterDirectory: String) {


        val outputFileExtension = ".out.txt"

        val dir = File(directory)

        val serialDir = File(directory + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX)

        if (!dir.exists() || !serialDir.exists()) {

            return
        }

        val outputFile = File(directory + File.separator + "summary.txt")


        val output = PrintWriter(FileOutputStream(outputFile))

        for (f in serialDir.listFiles()!!) {


            var params: ParameterDictionary? = null


            if (!f.name.matches((".*" + outputFileExtension).toRegex())) {
                continue
            }

            val parameterFile = File(parameterDirectory + File.separator + f.name.replace(outputFileExtension, AnalysisController.PARAMETER_EXTENSION))

            //params = ParameterDictionary.readParametersFromFile(parameterFile.getAbsolutePath());

            params = AnalysisMetadataParserFactory.createParserForFile(parameterFile.absolutePath).parseFileToAnalysisMetadata(parameterFile.absolutePath).outputParameters

            val o = ObjectInputStream(FileInputStream(f))

            var q: Quantification? = null

            try {
                q = o.readObject() as Quantification
            } catch (e: ClassNotFoundException) {
                q = null
            }

            o.close()

            if (q == null) {
                continue
            }

            val intensityMeasurementsByGroup = java.util.HashMap<Long, Map<String, List<Measurement>>>()

            val backgroundMeasurementsByGroup = java.util.HashMap<Long, Map<String, List<Measurement>>>()

            val sizeMeasurementsByGroup = java.util.HashMap<Long, Map<String, List<Measurement>>>()

            val groupLookup = java.util.HashMap<Long, Long>()

            var imageID: String? = null

            if (q.getAllMeasurementsForType(Measurement.TYPE_GROUPING) == null || q.getAllMeasurementsForType(Measurement.TYPE_GROUPING).size == 0) {
                for (m in q.allMeasurements) {
                    groupLookup.put(m.featureID, m.featureID)
                    if (imageID == null) {
                        imageID = m.imageID
                    }
                }
            } else {

                for (m in q.getAllMeasurementsForType(Measurement.TYPE_GROUPING)) {
                    groupLookup.put(m.featureID, m.measurement.toLong())
                    if (imageID == null) {
                        imageID = m.imageID
                    }
                }
            }

            if (imageID == null) {
                imageID = f.name
            }

            for (m in q.getAllMeasurementsForType(Measurement.TYPE_INTENSITY)) {

                var groupID = m.featureID
                if (groupLookup.containsKey(groupID)) {
                    groupID = groupLookup[m.featureID]
                }

                if (!intensityMeasurementsByGroup.containsKey(groupID)) {
                    intensityMeasurementsByGroup.put(groupID, java.util.HashMap<String, List<Measurement>>())
                }

                val currGroup = intensityMeasurementsByGroup[groupID]

                val name = m.measurementName

                if (!currGroup.containsKey(name)) {
                    currGroup.put(name, java.util.ArrayList<Measurement>())
                }

                currGroup[name].add(m)

            }

            for (m in q.getAllMeasurementsForType(Measurement.TYPE_BACKGROUND)) {

                var groupID = m.featureID
                if (groupLookup.containsKey(groupID)) {
                    groupID = groupLookup[m.featureID]
                }

                if (!backgroundMeasurementsByGroup.containsKey(groupID)) {
                    backgroundMeasurementsByGroup.put(groupID, java.util.HashMap<String, List<Measurement>>())
                }

                val currGroup = backgroundMeasurementsByGroup[groupID]

                val name = m.measurementName

                if (!currGroup.containsKey(name)) {
                    currGroup.put(name, java.util.ArrayList<Measurement>())
                }

                currGroup[name].add(m)

            }

            for (m in q.getAllMeasurementsForType(Measurement.TYPE_SIZE)) {

                var groupID = m.featureID
                if (groupLookup.containsKey(groupID)) {
                    groupID = groupLookup[m.featureID]
                }

                if (!sizeMeasurementsByGroup.containsKey(groupID)) {
                    sizeMeasurementsByGroup.put(groupID, java.util.HashMap<String, List<Measurement>>())
                }

                val currGroup = sizeMeasurementsByGroup[groupID]

                val name = m.measurementName

                if (!currGroup.containsKey(name)) {
                    currGroup.put(name, java.util.ArrayList<Measurement>())
                }

                currGroup[name].add(m)

            }

            val groupQuant = Quantification()

            for (group in intensityMeasurementsByGroup.keys) {

                var count = 0
                var counted = false

                if (intensityMeasurementsByGroup[group] != null) {

                    for (name in intensityMeasurementsByGroup[group].keys) {

                        val m = Measurement(true, group, 0.0, name, Measurement.TYPE_INTENSITY, imageID)

                        for (individual in intensityMeasurementsByGroup[group][name]) {

                            m.measurement = m.measurement + individual.getMeasurement()
                            if (!counted) {
                                count++
                            }
                        }

                        m.measurement = m.measurement / count

                        counted = true

                        groupQuant.addMeasurement(m)

                    }

                }

                if (backgroundMeasurementsByGroup[group] != null) {

                    for (name in backgroundMeasurementsByGroup[group].keys) {

                        val m = Measurement(true, group, 0.0, name, Measurement.TYPE_BACKGROUND, imageID)

                        for (individual in backgroundMeasurementsByGroup[group][name]) {

                            m.measurement = m.measurement + individual.getMeasurement()
                            if (!counted) {
                                count++
                            }
                        }

                        m.measurement = m.measurement / count

                        counted = true

                        groupQuant.addMeasurement(m)

                    }
                }

                if (sizeMeasurementsByGroup[group] != null) {

                    for (name in sizeMeasurementsByGroup[group].keys) {

                        val m = Measurement(true, group, 0.0, name, Measurement.TYPE_SIZE, imageID)

                        for (individual in sizeMeasurementsByGroup[group][name]) {

                            m.measurement = m.measurement + individual.getMeasurement()
                            if (!counted) {
                                count++
                            }
                        }

                        m.measurement = m.measurement / count

                        counted = true

                        groupQuant.addMeasurement(m)

                    }

                }

                groupQuant.addMeasurement(Measurement(true, group, count.toDouble(), "region_count", Measurement.TYPE_SIZE, imageID))

            }

            val data = LocalAnalysis.generateDataOutputString(groupQuant, params)

            output.println(imageID)

            output.println(data)

        }



        output.close()


    }


}
