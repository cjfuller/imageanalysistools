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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of quantifying an image.  Collects information (in the form
 * of Measurement objects) about each region of interest or object in the image into one place.
 * 
 * @author Colin J. Fuller
 *
 */
public class Quantification implements Serializable {

	private static final long serialVersionUID = 8194024040087176409L;

	protected List<Measurement> measurements;
	
	protected Map<String, List<Measurement> > measurementsByName;
	protected Map<String, List<Measurement> > measurementsByType;
	protected Map<Long, List<Measurement> > measurementsByRegion;
	
	protected List<Measurement> globalMeasurements;
	
	
	public Quantification() {
		measurements = new java.util.ArrayList<Measurement>();
		measurementsByName = new java.util.HashMap<String, List<Measurement> >();
		measurementsByType = new java.util.HashMap<String, List<Measurement> >();
		measurementsByRegion = new java.util.HashMap<Long, List<Measurement> >();
		globalMeasurements = new java.util.ArrayList<Measurement>();
	}
	
	/**
	 * Adds a measurement to the Quantification.
	 * 
	 * @param m		The measurement to add.
	 */
	public void addMeasurement(Measurement m) {
		measurements.add(m);
		String name = m.getMeasurementName();
		String type = m.getMeasurementType();
		Long id = m.getFeatureID();
		
		if (!measurementsByName.containsKey(name)) {
			measurementsByName.put(name, new java.util.ArrayList<Measurement>());
		}
		
		measurementsByName.get(name).add(m);
		
		if (!measurementsByType.containsKey(type)) {
			measurementsByType.put(type, new java.util.ArrayList<Measurement>());
		}
		
		measurementsByType.get(type).add(m);
		
		
		if (m.hasAssociatedFeature()) {
			if (!measurementsByRegion.containsKey(id)) {
				measurementsByRegion.put(id, new java.util.ArrayList<Measurement>());
			}
			
			measurementsByRegion.get(id).add(m);
		} else {
			globalMeasurements.add(m);
		}
		
	}
	
	/**
	 * Adds all the measurements present in another quantification.
	 * 
	 * @param q		The Quantification whose measurements will be added.
	 */
	public void addAllMeasurements(Quantification q) {
		for (Measurement m : q.measurements) {
			this.addMeasurement(m);
		}
	}
	
	/**
	 * Get all the measurements comprising the Quantification.
	 * @return	a List of Measurements in the Quantification.
	 */
	public List<Measurement> getAllMeasurements() {
		return this.measurements;
	}
	
	/**
	 * Gets all the measurements whose name matches the provided name.
	 * @param name	A String containing the name of the measurements.  This must be exactly the name used to create the measurement.
	 * @return		A List containing the measurements matching the given name; if there are no matches, an empty list is returned.
	 */
	public List<Measurement> getAllMeasurementsForName(String name) {
		if (! this.measurementsByName.containsKey(name)) {return new java.util.ArrayList<Measurement>();}
		return this.measurementsByName.get(name);
	}
	
	/**
	 * Gets all the measurements whose name matches the provided region ID.
	 * @param regionID	A long designating the region ID.
	 * @return			A List containing the measurements matching the given ID; if there are no matches, an empty list is returned.
	 */
	public List<Measurement> getAllMeasurementsForRegion(long regionID) {
		if (! this.measurementsByRegion.containsKey(regionID)) {return new java.util.ArrayList<Measurement>();}
		return this.measurementsByRegion.get(regionID);
	}
	
	/**
	 * Gets the region IDs of all the regions that have associated measurements in the Quantification.
	 * @return	A Set containing the region IDs.
	 */
	public java.util.Set<Long> getAllRegions() {
		return this.measurementsByRegion.keySet();
	}
	
	/**
	 * Gets all the measurements in the Quantification matching the specified type.  The type should likely be one
	 * of those listed in the constants in {@link Measurement} but can be any String.
	 * @param type	A String specifying the type of the measurements to return.
	 * @return		A List containing the measurements matching the given type; if there are no matches, an empty list is returned.
	 */
	public List<Measurement> getAllMeasurementsForType(String type) {
		if (! this.measurementsByType.containsKey(type)) {return new java.util.ArrayList<Measurement>();}
		return this.measurementsByType.get(type);
	}
	
	/**
	 * Gets all the global measurements in the Quantification.  (That is, the measurements without
	 * an associated region.)
	 * 
	 * @return	A List containing all the global measurements, or an empty list if there are none.
	 */
	public List<Measurement> getAllGlobalMeasurements() {
		return this.globalMeasurements;
	}
	

	
}
