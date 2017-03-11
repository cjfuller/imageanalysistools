package edu.stanford.cfuller.imageanalysistools.metric

import java.io.Serializable

/**
 * Represents a single scalar measurement made based on the quantification of an image.

 * @author Colin J. Fuller
 */
class Measurement : Serializable {
    protected var hasAssociatedFeature: Boolean = false
    /**
     * Gets the ID of any associated feature.
     * @return the ID of the associated feature, which may not be meaningful if there is no associated feature.
     */
    /**
     * Sets the ID of any associated feature.
     * This does not change whether there is a feature associated with the measurement.
     * @param featureID a long specifying the ID of the feature.
     */
    var featureID: Long = 0

    /**
     * Gets the value of the measurement.
     * @return the measurement
     */
    /**
     * Sets the value of the measurement.
     * @param measurement the measurement value to set
     */
    var measurement: Double = 0.toDouble()
    /**
     * Gets the name of this measurement.
     * @return a string with the name of the measurement.
     */
    /**
     * Sets the name of this measurement.
     * @param measurementName a String containing the name of the measurement.
     */
    var measurementName: String? = null
    /**
     * Gets the type of this measurement.
     * @return a string containing the type of the measurement.
     */
    /**
     * Sets the type of this measurement.
     * @param measurementType a String naming the type of the measurement (e.g. "intensity" or "size").
     */
    var measurementType: String? = null
    /**
     * Gets a string naming the image on which the measurement was made.
     * @return a string containing information on the image.  This will likely be the path to the image.
     */
    /**
     * Sets a string naming the image on which the measurement was made.
     * @param imageID a string containing information on the image; this should probably be the path to the image, where applicable.
     */
    var imageID: String? = null

    /**
     * Creates a new, empty measurement.
     */
    constructor() {
        this.hasAssociatedFeature = false
        this.featureID = -1
        this.measurement = java.lang.Double.NaN
        this.measurementName = null
        this.measurementType = null
        this.imageID = null
    }

    /**
     * Creates a new measurement with all information supplied.
     * @param hasFeature    Indicates whether this measurement is associated with a particular image feature.
     * @param id            Indicates with which feature this measurement is associated.
     * @param measurement    The value of the measurement.
     * @param name            A string naming what the measurement is.
     * @param type            A string indicating what type of measurement this is (e.g. "intensity" or "size").
     * @param image            A string naming the image on which the measurement was made.
     */
    constructor(hasFeature: Boolean, id: Long, measurement: Double, name: String, type: String, image: String) {
        this.hasAssociatedFeature = hasFeature
        this.featureID = id
        this.measurement = measurement
        this.measurementName = name
        this.measurementType = type
        this.imageID = image
    }

    /**
     * Checks whether a feature is associated with this measurement.
     * @return true if this measurement has a feature, false otherwise.
     */
    fun hasAssociatedFeature(): Boolean {
        return hasAssociatedFeature
    }

    companion object {
        private const val serialVersionUID = -1958967973002931120L
        val TYPE_INTENSITY = "intensity"
        val TYPE_SIZE = "size"
        val TYPE_GROUPING = "group"
        val TYPE_BACKGROUND = "background"
    }
}
