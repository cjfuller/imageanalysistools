package edu.stanford.cfuller.imageanalysistools.metric

import java.io.Serializable

/**
 * Represents the result of quantifying an image.  Collects information (in the form
 * of Measurement objects) about each region of interest or object in the image into one place.

 * @author Colin J. Fuller
 */
class Quantification : Serializable {
    private var measurements: MutableList<Measurement> = java.util.ArrayList<Measurement>()
    private var measurementsByName: MutableMap<String, MutableList<Measurement>> = java.util.HashMap<String, MutableList<Measurement>>()
    private var measurementsByType: MutableMap<String, MutableList<Measurement>> = java.util.HashMap<String, MutableList<Measurement>>()
    private var measurementsByRegion: MutableMap<Long, MutableList<Measurement>> = java.util.HashMap<Long, MutableList<Measurement>>()
    private var globalMeasurements: MutableList<Measurement> = java.util.ArrayList<Measurement>()

    /**
     * Adds a measurement to the Quantification.
     * @param m        The measurement to add.
     */
    fun addMeasurement(m: Measurement) {
        measurements.add(m)
        val name = m.measurementName!!
        val type = m.measurementType!!
        val id = m.featureID

        if (!measurementsByName.containsKey(name)) {
            measurementsByName.put(name, java.util.ArrayList<Measurement>())
        }
        measurementsByName[name]!!.add(m)

        if (!measurementsByType.containsKey(type)) {
            measurementsByType.put(type, java.util.ArrayList<Measurement>())
        }
        measurementsByType[type]!!.add(m)


        if (m.hasAssociatedFeature()) {
            if (!measurementsByRegion.containsKey(id)) {
                measurementsByRegion.put(id, java.util.ArrayList<Measurement>())
            }
            measurementsByRegion[id]!!.add(m)
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
        return this.measurementsByName[name]!!
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
        return this.measurementsByRegion[regionID]!!
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
        return this.measurementsByType[type]!!
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
