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

package edu.stanford.cfuller.imageanalysistools.metric;

/**
 * Represents a single scalar measurement made based on the quantification of an image.
 * 
 * @author Colin J. Fuller
 *
 */
public class Measurement {

	protected boolean hasAssociatedFeature;
	protected long featureID;
	
	protected double measurement;
	protected String measurementName;
	protected String measurementType;
	protected String imageID;
	
	public static final String TYPE_INTENSITY = "intensity";
	public static final String TYPE_SIZE = "size";
	public static final String TYPE_GROUPING = "group";
	public static final String TYPE_BACKGROUND = "background";
	
	/**
	 * Creates a new, empty measurement.
	 */
	public Measurement() {
		
		this.hasAssociatedFeature = false;
		this.featureID = -1;
		
		this.measurement = Double.NaN;
		
		this.measurementName = null;
		this.measurementType = null;
		this.imageID = null;
		
	}
	
	
	/**
	 * Creates a new measurement with all information supplied.
	 * 
	 * @param hasFeature	Indicates whether this measurement is associated with a particular image feature.
	 * @param id			Indicates with which feature this measurement is associated.
	 * @param measurement	The value of the measurement.
	 * @param name			A string naming what the measurement is.
	 * @param type			A string indicating what type of measurement this is (e.g. "intensity" or "size").
	 * @param image			A string naming the image on which the measurement was made.
	 */
	public Measurement(boolean hasFeature, long id, double measurement, String name, String type, String image) {
		this.hasAssociatedFeature = hasFeature;
		this.featureID = id;
		this.measurement = measurement;
		this.measurementName = name;
		this.measurementType = type;
		this.imageID = image;
	}

	/**
	 * Checks whether a feature is associated with this measurement.
	 * 
	 * @return true if this measurement has a feature, false otherwise.
	 */
	public boolean hasAssociatedFeature() {
		return hasAssociatedFeature;
	}


	/**
	 * Sets whether a feature is associated with this measurement.
	 * 
	 * @param hasAssociatedFeature a boolean saying whether the measurement has a feature.
	 */
	public void setHasAssociatedFeature(boolean hasAssociatedFeature) {
		this.hasAssociatedFeature = hasAssociatedFeature;
	}


	/**
	 * Gets the ID of any associated feature.
	 * 
	 * @return the ID of the associated feature, which may not be meaningful if there is no associated feature.
	 */
	public long getFeatureID() {
		return featureID;
	}


	/**
	 * Sets the ID of any associated feature.
	 * <p>
	 * This does not change whether there is a feature associated with the measurement.
	 * 
	 * @param featureID a long specifying the ID of the feature.
	 */
	public void setFeatureID(long featureID) {
		this.featureID = featureID;
	}


	/**
	 * Gets the value of the measurement.
	 * 
	 * @return the measurement
	 */
	public double getMeasurement() {
		return measurement;
	}


	/**
	 * Sets the value of the measurement.
	 * 
	 * @param measurement the measurement value to set
	 */
	public void setMeasurement(double measurement) {
		this.measurement = measurement;
	}


	/**
	 * Gets the name of this measurement.
	 * 
	 * @return a string with the name of the measurement.
	 */
	public String getMeasurementName() {
		return measurementName;
	}


	/**
	 * Sets the name of this measurement.
	 * 
	 * @param measurementName a String containing the name of the measurement.
	 */
	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}


	/**
	 * Gets the type of this measurement.
	 * 
	 * @return a string containing the type of the measurement.
	 */
	public String getMeasurementType() {
		return measurementType;
	}


	/**
	 * Sets the type of this measurement.
	 * 
	 * @param measurementType a String naming the type of the measurement (e.g. "intensity" or "size").
	 */
	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}


	/**
	 * Gets a string naming the image on which the measurement was made.
	 * @return a string containing information on the image.  This will likely be the path to the image.
	 */
	public String getImageID() {
		return imageID;
	}


	/**
	 * Sets a string naming the image on which the measurement was made.
	 * @param imageID a string containing information on the image; this should probably be the path to the image, where applicable.
	 */
	public void setImageID(String imageID) {
		this.imageID = imageID;
	}
	
	
	
	
	
}
