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

package edu.stanford.cfuller.imageanalysistools.metric

import java.io.Serializable

/**
 * Represents the result of quantifying an image.  Collects information (in the form
 * of Measurement objects) about each region of interest or object in the image into one place.

 * @author Colin J. Fuller
 */
class Quantification : Serializable {

    protected var measurements: MutableList<Measurement>

    protected var measurementsByName: MutableMap<String, List<Measurement>>
    protected var measurementsByType: MutableMap<String, List<Measurement>>
    protected var measurementsByRegion: MutableMap<Long, List<Measurement>>

    protected var globalMeasurements: MutableList<Measurement>

    init {
        measurements = java.util.ArrayList<Measurement>()
        measurementsByName = java.util.HashMap<String, List<Measurement>>()
        measurementsByType = java.util.HashMap<String, List<Measurement>>()
        measurementsByRegion = java.util.HashMap<Long, List<Measurement>>()
        globalMeasurements = java.util.ArrayList<Measurement>()
    }

    /**
     * Adds a measurement to the Quantification.

     * @param m        The measurement to add.
     */
    fun addMeasurement(m: Measurement) {
        measurements.add(m)
        val name = m.measurementName
        val type = m.measurementType
        val id = m.featureID

        if (!measurementsByName.containsKey(name)) {
            measurementsByName.put(name, java.util.ArrayList<Measurement>())
        }

        measurementsByName[name].add(m)

        if (!measurementsByType.containsKey(type)) {
            measurementsByType.put(type, java.util.ArrayList<Measurement>())
        }

        measurementsByType[type].add(m)


        if (m.hasAssociatedFeature()) {
            if (!measurementsByRegion.containsKey(id)) {
                measurementsByRegion.put(id, java.util.ArrayList<Measurement>())
            }

            measurementsByRegion[id].add(m)
        } else {
            globalMeasurements.add(m)
        }

    }

    /**
     * Adds all the measurements present in another quantification.

     * @param q        The Quantification whose measurements will be added.
     */
    fun addAllMeasurements(q: Quantification) {
        for (m in q.measurements) {
            this.addMeasurement(m)
        }
    }

    /**
     * Get all the measurements comprising the Quantification.
     * @return    a List of Measurements in the Quantification.
     */
    val allMeasurements: List<Measurement>
        get() = this.measurements

    /**
     * Gets all the measurements whose name matches the provided name.
     * @param name    A String containing the name of the measurements.  This must be exactly the name used to create the measurement.
     * *
     * @return        A List containing the measurements matching the given name; if there are no matches, an empty list is returned.
     */
    fun getAllMeasurementsForName(name: String): List<Measurement> {
        if (!this.measurementsByName.containsKey(name)) {
            return java.util.ArrayList<Measurement>()
        }
        return this.measurementsByName[name]
    }

    /**
     * Gets all the measurements whose name matches the provided region ID.
     * @param regionID    A long designating the region ID.
     * *
     * @return            A List containing the measurements matching the given ID; if there are no matches, an empty list is returned.
     */
    fun getAllMeasurementsForRegion(regionID: Long): List<Measurement> {
        if (!this.measurementsByRegion.containsKey(regionID)) {
            return java.util.ArrayList<Measurement>()
        }
        return this.measurementsByRegion[regionID]
    }

    /**
     * Gets the region IDs of all the regions that have associated measurements in the Quantification.
     * @return    A Set containing the region IDs.
     */
    val allRegions: Set<Long>
        get() = this.measurementsByRegion.keys

    /**
     * Gets all the measurements in the Quantification matching the specified type.  The type should likely be one
     * of those listed in the constants in [Measurement] but can be any String.
     * @param type    A String specifying the type of the measurements to return.
     * *
     * @return        A List containing the measurements matching the given type; if there are no matches, an empty list is returned.
     */
    fun getAllMeasurementsForType(type: String): List<Measurement> {
        if (!this.measurementsByType.containsKey(type)) {
            return java.util.ArrayList<Measurement>()
        }
        return this.measurementsByType[type]
    }

    /**
     * Gets all the global measurements in the Quantification.  (That is, the measurements without
     * an associated region.)

     * @return    A List containing all the global measurements, or an empty list if there are none.
     */
    val allGlobalMeasurements: List<Measurement>
        get() = this.globalMeasurements

    companion object {

        private const val serialVersionUID = 8194024040087176409L
    }


}
