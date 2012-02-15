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

package edu.stanford.cfuller.imageanalysistools.fitting;

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.OptimizationException;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.Double;import java.lang.Math;import java.lang.String;import java.lang.Throwable;import java.util.Hashtable;
import java.util.Vector;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An object in an image with some spatial extent that can be fit to some function.
 *
 * Classes that extend ImageObject will supply methods that perform the fit to a specific functional form.
 * 
 * @author Colin J. Fuller
 *
 */
public abstract class ImageObject implements Serializable {

	//TODO: maintain the notion that the ImageObject has some location in real space, but reduce
	//dependence on 5D images.

	public static final long serialVersionUID = 3L;
	public final static String OBJECT_ELEMENT = "image_object";
	public final static String CHANNEL_ELEMENT = "channel";
	public final static String LABEL_ATTR = "label";
	public final static String IMAGE_ATTR = "image_id";
	public final static String CH_ID_ATTR = "channel_id";
	public final static String FIT_ELEMENT = "fit";
	public final static String PARAMETERS_ELEMENT = "parameters";
	public final static String R2_ATTR = "r_squared";
	public final static String ERROR_ATTR = "fit_error";
	public final static String N_PHOTONS_ATTR = "photon_count";
	public final static String POSITION_ELEMENT = "position";
	public final static String SERIAL_ELEMENT = "serialized_form";
	public final static String ENCODING_ATTR = "encoding";
	public final static String ENCODING_NAME = "hex_binary";

	Vector3D centroidInMask;

	int sizeInPixels;

	int label;

	int[] maxIntensityZCoordByChannel;

	double[] xValues;
	double[] yValues;
	double[] zValues;
	double[] functionValues;

	int numberOfChannels;

	java.util.Vector<RealVector> fitParametersByChannel;
	java.util.Vector<Double> fitR2ByChannel;
	java.util.Vector<Double> fitErrorByChannel;

	java.util.Vector<Double> nPhotonsByChannel; 

	java.util.Vector<RealVector> positionsByChannel;
	Hashtable<Integer, RealVector> vectorDifferencesBetweenChannels;
	Hashtable<Integer, Double> scalarDifferencesBetweenChannels;

	ImageCoordinate parentBoxMin;
	ImageCoordinate parentBoxMax;

	Image parent;
	Image mask;

	String imageID;

	boolean hadFittingError;
	boolean correctionSuccessful;


	/**
	 * Initializes the fields of an ImageObject to default or null values.
	 */
	protected void init() {
		this.parentBoxMin = null;
		this.parentBoxMax = null;
		this.parent = null;
		this.sizeInPixels = 0;
		this.fitParametersByChannel = null;
		this.maxIntensityZCoordByChannel = null;
		this.imageID = null;
		this.hadFittingError = true;
		this.fitR2ByChannel = null;
		this.fitErrorByChannel = null;
		this.nPhotonsByChannel = null;
		this.label = 0;
		this.vectorDifferencesBetweenChannels = new Hashtable<Integer, RealVector>();
		this.scalarDifferencesBetweenChannels = new Hashtable<Integer, Double>();
		this.numberOfChannels = 0;
		this.correctionSuccessful = false;
	}


	/**
	 * Initializes the fields of an ImageObject based on supplied image and parameter data.
	 *
	 * Both the mask and the parent will be unmodified, except for boxing a region of interest, so no other thread should
	 * be using these images at the same time.
	 *
	 * @param label     The numerical label of the object in the mask.
	 * @param mask      An image mask, containing a unique greylevel for each object; the greylevel of the object being initialized should correspond to the parameter {@link #label}.
	 * @param parent    The original imgae that the mask corresponds to.
	 * @param p         A {@link ParameterDictionary} containing the parameters for the current analysis.  In particular, this routine makes use of the various box size parameters.
	 */
	protected void init(int label, Image mask, Image parent, ParameterDictionary p) {

		this.fitParametersByChannel = null;
		this.fitR2ByChannel = null;
		this.fitErrorByChannel = null;
		this.nPhotonsByChannel = null;

		this.positionsByChannel = new Vector<RealVector>();

		this.vectorDifferencesBetweenChannels = new Hashtable<Integer, RealVector>();
		this.scalarDifferencesBetweenChannels = new Hashtable<Integer, Double>();

		//this.parent = new Image(parent);
		//this.mask = new Image(mask);

		this.parent = parent;
		this.mask = mask;

		this.label = label;

		this.sizeInPixels = 0;

		this.centroidInMask = new Vector3D(0,0,0);

		this.imageID = null;

		this.hadFittingError = true;

		for (ImageCoordinate i : mask) {

			if (mask.getValue(i) == label) {
				sizeInPixels++;

				this.centroidInMask = this.centroidInMask.add(new Vector3D(i.get(ImageCoordinate.X), i.get(ImageCoordinate.Y), i.get(ImageCoordinate.Z)));

			}

		}

		if (this.sizeInPixels == 0) {
			return;
		}

		this.centroidInMask = this.centroidInMask.scalarMultiply(1.0/sizeInPixels);

		//System.out.println("for object " + label + " centroid is: " + this.centroidInMask.toString());

		int xcoord = (int) Math.round(this.centroidInMask.getX() - p.getIntValueForKey("half_box_size"));
		int ycoord = (int) Math.round(this.centroidInMask.getY() - p.getIntValueForKey("half_box_size"));

		if (xcoord < 0) {xcoord = 0;}
		if (ycoord < 0) {ycoord = 0;}

		this.parentBoxMin = ImageCoordinate.createCoordXYZCT(xcoord, ycoord, 0, 0, 0);

		xcoord = (int) Math.round(this.centroidInMask.getX() + p.getIntValueForKey("half_box_size"))+1;
		ycoord = (int) Math.round(this.centroidInMask.getY() + p.getIntValueForKey("half_box_size"))+1;

		if (xcoord > mask.getDimensionSizes().get(ImageCoordinate.X)) {xcoord = mask.getDimensionSizes().get(ImageCoordinate.X);}
		if (ycoord > mask.getDimensionSizes().get(ImageCoordinate.Y)) {ycoord = mask.getDimensionSizes().get(ImageCoordinate.Y);}

		this.parentBoxMax = ImageCoordinate.createCoordXYZCT(xcoord, ycoord, parent.getDimensionSizes().get(ImageCoordinate.Z), parent.getDimensionSizes().get(ImageCoordinate.C), parent.getDimensionSizes().get(ImageCoordinate.T));

		//handle either 2D or 3D masks

		//2D case:

		if (mask.getDimensionSizes().get(ImageCoordinate.Z) == 1) {


			//find the max intensity pixel in each channel and use this to refine the box

			this.maxIntensityZCoordByChannel = new int[parent.getDimensionSizes().get(ImageCoordinate.C)];

			int minZOverall = parent.getDimensionSizes().get(ImageCoordinate.Z);
			int maxZOverall = 0;

			//for (int c = 0; c < parent.getDimensionSizes().getC(); c++) {
			this.numberOfChannels = p.getIntValueForKey("num_wavelengths"); // use this so that if there's extra wavelengths not to be quantified at the end, these won't skew the initial guess

			for (int c = 0; c < this.numberOfChannels; c++) { 
				this.parentBoxMin.set(ImageCoordinate.C,c);
				this.parentBoxMax.set(ImageCoordinate.C,c+1);

				parent.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);

				double maxValue = 0;
				ImageCoordinate maxCoord = null;

				for (ImageCoordinate ic : parent) {

					if (! ( ic.get(ImageCoordinate.X) == (int) Math.round(this.centroidInMask.getX())) || ! ( ic.get(ImageCoordinate.Y) == (int) Math.round(this.centroidInMask.getY()))) {
						continue;
					}

					if (parent.getValue(ic) > maxValue) {
						maxValue = parent.getValue(ic);
						if (maxCoord != null) maxCoord.recycle();
						maxCoord = ImageCoordinate.cloneCoord(ic);
					}

				}

				if (maxCoord == null) continue;

				if (maxCoord.get(ImageCoordinate.Z) > maxZOverall) maxZOverall = maxCoord.get(ImageCoordinate.Z);
				if (maxCoord.get(ImageCoordinate.Z) < minZOverall) minZOverall = maxCoord.get(ImageCoordinate.Z);

				this.maxIntensityZCoordByChannel[c] = maxCoord.get(ImageCoordinate.Z);

				maxCoord.recycle();

				parent.clearBoxOfInterest();
			}

			if (minZOverall > maxZOverall){
				minZOverall = 0;
				maxZOverall = 0;
				java.util.logging.Logger.getLogger("edu.stanford.cfuller.colocalization3d").warning("Problem when calculating Z range of image stack.");
			}

			int zAverage = (minZOverall+maxZOverall)/2;

			int zcoord = 0;

			this.parentBoxMin.set(ImageCoordinate.C,0);
			zcoord = zAverage - p.getIntValueForKey("half_z_size");
			if (zcoord < 0) zcoord = 0;
			this.parentBoxMin.set(ImageCoordinate.Z,zcoord);

			this.parentBoxMax.set(ImageCoordinate.C,parent.getDimensionSizes().get(ImageCoordinate.C));
			zcoord = zAverage + p.getIntValueForKey("half_z_size")+1;
			if (zcoord > parent.getDimensionSizes().get(ImageCoordinate.Z)) zcoord = parent.getDimensionSizes().get(ImageCoordinate.Z);
			this.parentBoxMax.set(ImageCoordinate.Z,zcoord);

		} else { //3D mask
			
			int zcoord = (int) Math.round(this.centroidInMask.getZ() - p.getIntValueForKey("half_z_size"));
			if (zcoord < 0) {zcoord = 0;}
			
			this.parentBoxMin.set(ImageCoordinate.Z, zcoord);
			
			zcoord = (int) Math.round(this.centroidInMask.getZ() + p.getIntValueForKey("half_z_size") + 1);
			
			this.parentBoxMax.set(ImageCoordinate.Z, zcoord);
			
		}
	}


	/**
	 * Fits the object to the ImageObject's functional form in order to determine its position.
	 * @param p     The parameters for the current analysis.
	 * @throws OptimizationException        if there is an error during the optimization.
	 */
	public abstract void fitPosition(ParameterDictionary p) throws OptimizationException;


	/**
	 * Gets the fitted parameters, one set per channel in the original image of the object.
	 * @return  A vector containing a RealVector of fit parameters, one for each channel in the original object.
	 */
	public Vector<RealVector> getFitParametersByChannel() {
		return fitParametersByChannel;
	}

	/**
	 * Gets the ID of the image from which the object was taken (this could, for instance, be the filename of the original image).
	 *
	 * @return  A string that is the ID of the original image.
	 */
	public String getImageID() {
		return imageID;
	}


	/**
	 * Sets the ID of the image from which the object was taken (this could, for instance, be the filename of the original image).
	 * @param imageID   A string to which to set the ID of the image.
	 */
	public void setImageID(String imageID) {
		this.imageID = imageID;
	}


	/**
	 * Nullifies the references to the parent and mask image, and the internal storage of the image data and coordinates to free them for garbage collection.
	 *
	 * This should only be called after fitting has been completed, as fitting will no longer be possible without the original image data.
	 */
	public void nullifyImages() {
		//this.parent.dispose();
		this.parent = null;
		//this.mask.dispose();
		this.mask = null;
		this.xValues = null;
		this.yValues = null;
		this.zValues = null;
		this.functionValues = null;
	}

	/**
	 * Returns a reference to the mask of the original image that was used to create this ImageObject.
	 * 
	 * @return  A reference to the mask Image.
	 */
	public Image getMask() {
		return mask;
	}

	/**
	 * Returns a reference to the parent Image of this ImageObject (that is, the image used to create it).
	 *
	 * @return  A reference to the parent Image.
	 */
	public Image getParent() {
		return parent;
	}

	/**
	 * Cleans up the ImageCoordinates used internally by ImageCoordinates and recycles them for future use.
	 * @throws Throwable
	 */
	public void finalize() throws Throwable {
		if (this.parentBoxMin != null) {this.parentBoxMin.recycle(); this.parentBoxMin = null;}
		if (this.parentBoxMax != null) {this.parentBoxMax.recycle(); this.parentBoxMax = null;}

		super.finalize();
	}


	/**
	 * Sets the relevant region of interest in the parent image to be the region that boxes this object.
	 */
	public void boxImages() {
		this.parent.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);
		this.mask.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);
	}

	/**
	 * Clears the region of interest in the parent image.
	 */
	public void unboxImages() {
		this.parent.clearBoxOfInterest();
		this.mask.clearBoxOfInterest();
	}

	/**
	 * Gets the internal array containing the x-coordinates of the pixels in the box containing this object.
	 *
	 * No specific order is guaranteed beyond that it must be the same order as the y- and z- coordinates and function values.
	 * 
	 * @return  An array containing the x-coordinates of each pixel in this object's box.
	 */
	public double[] getxValues() {
		return xValues;
	}

	/**
	 * Gets the internal array containing the y-coordinates of the pixels in the box containing this object.
	 *
	 * No specific order is guaranteed beyond that it must be the same order as the x- and z- coordinates and function values.
	 *
	 * @return  An array containing the x-coordinates of each pixel in this object's box.
	 */
	public double[] getyValues() {
		return yValues;
	}

	/**
	 * Gets the internal array containing the z-coordinates of the pixels in the box containing this object.
	 *
	 * No specific order is guaranteed beyond that it must be the same order as the x- and y- coordinates and function values.
	 *
	 * @return  An array containing the x-coordinates of each pixel in this object's box.
	 */
	public double[] getzValues() {
		return zValues;
	}

	/**
	 * Gets the internal array containing the greylevel values of the pixels in the box containing this object.
	 *
	 * No specific order is guaranteed beyond that it must be the same order as the x-, y-, and z- coordinates.
	 *
	 * @return  An array containing the values of each pixel in this object's box.
	 */
	public double[] getFunctionValues() {
		return functionValues;
	}

	/**
	 * Gets information on whether this object has finished fitting with no errors.
	 * 
	 * @return  true if fitting finished normally, false if there was an error or the object was not yet fit.
	 */
	public boolean finishedFitting() {
		return !this.hadFittingError;
	}
	
	/**
	 * Gets information on whether this ImageObject had its position corrected successfully.
	 * @return 	true if the position was corrected successfully, false if it failed or was not corrected at all.
	 */
	public boolean getCorrectionSuccessful() {
		return this.correctionSuccessful;
	}
	
	/**
	 * Sets whether this ImageObject had its position corrected successfully.
	 * @param success	Whether the position has been corrected successfully.
	 */
	public void setCorrectionSuccessful(boolean success) {
		this.correctionSuccessful= success;
	}

	/**
	 * Gets the R^2 values for the fit in each channel.
	 * @return  a Vector containing one R^2 value for the fit in each channel.
	 */
	public Vector<Double> getFitR2ByChannel() {
		return fitR2ByChannel;
	}

	/**
	 * Gets an error estimate for the fitting of the position of the object in each channel.
	 * @return  a Vector containing one fit error estimate for each channel.
	 */
	public Vector<Double> getFitErrorByChannel() {
		return fitErrorByChannel;
	}

	/**
	 * Gets the number of photons above background from the object in each channel.
	 * @return	a Vector containing the number of photons collected in each channel.
	 */
	public Vector<Double> getNPhotonsByChannel() {
		return this.nPhotonsByChannel;
	}

	/**
	 * Gets the label of this object; this corresponds to its greylevel value in the original image mask.
	 * @return  The label of this object.
	 */
	public int getLabel() {return this.label;}

	/**
	 * Gets the position of this object in the specified channel.
	 * 
	 * Do not modify the returned RealVector, as it is a reference to the internally stored vector.
	 * 
	 * @param channel	The index of the channel, either by order in the original multiwavelength image, or in the order specified for split wavelength images.
	 * 
	 * @return	A RealVector containing the x,y,and z coordinates of the position, or null if it has not yet been determined, or the channel is out of range.
	 */
	public RealVector getPositionForChannel(int channel) {

		if (! (channel < this.positionsByChannel.size())) {
			return null;
		}

		return this.positionsByChannel.get(channel);

	}

	/**
	 * Gets the vector difference between the position of the object in two channels.
	 * 
	 * Note that there is no unit conversion here, and the distance is returned in image units of pixels or sections.
	 * 
	 * @param channel0	The index of one channel to use for the difference.
	 * @param channel1	The index of the other channel to use for the difference.
	 * @return			The vector difference between the two channels, as channel1 - channel0, or null if either channel is out of range or has not yet been fit.
	 */
	public RealVector getVectorDifferenceBetweenChannels(int channel0, int channel1) {

		int key = this.numberOfChannels * channel0 + channel1;

		if (! this.vectorDifferencesBetweenChannels.containsKey(key)) {

			RealVector ch0 = this.getPositionForChannel(channel0);
			RealVector ch1 = this.getPositionForChannel(channel1);

			if (ch0 == null || ch1 == null) {return null;}

			this.vectorDifferencesBetweenChannels.put(key, ch1.subtract(ch0));
		}

		return this.vectorDifferencesBetweenChannels.get(key);

	}


	/**
	 * Gets the scalar difference between the position of the object in two channels.
	 * 
	 * Units are converted from image units to real units using the supplied vector of conversions.
	 * 
	 * @param channel0 	The index of one channel to use for the difference.
	 * @param channel1	The index of the other channel to use for the difference.
	 * @param pixelToDistanceConversions	A vector containing the number of realspace distance units per pixel or section, one element per dimension.
	 * @return			The scalar distance between the position of the object in each channel (that is, the length of the vector representing the vector distance), or null if either channel is out of range or has not yet been fit.
	 */
	public Double getScalarDifferenceBetweenChannels(int channel0, int channel1, RealVector pixelToDistanceConversions) {

		int key = this.numberOfChannels * channel0 + channel1;

		if (!this.scalarDifferencesBetweenChannels.containsKey(key)) {

			RealVector vecDifference = this.getVectorDifferenceBetweenChannels(channel0, channel1);

			if (vecDifference == null) {return null;}

			this.scalarDifferencesBetweenChannels.put(key, vecDifference.ebeMultiply(pixelToDistanceConversions).getNorm());

		}

		return this.scalarDifferencesBetweenChannels.get(key);

	}

	public String writeToXMLString() {
		StringWriter sw = new StringWriter();
		try {
			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
			this.writeToXML(xsw);
		} catch (XMLStreamException e) {
			LoggingUtilities.getLogger().severe("Exception encountered while writing XML correction output: " + e.getMessage());
		}
		return sw.toString();
	}

	public void writeToXML(XMLStreamWriter xsw) {

		try {

			xsw.writeStartElement(OBJECT_ELEMENT);
			xsw.writeAttribute(LABEL_ATTR, Integer.toString(this.label));
			xsw.writeAttribute(IMAGE_ATTR, this.imageID);
			xsw.writeCharacters("\n");

			for (int i = 0; i < this.numberOfChannels; i++) {
				xsw.writeStartElement(CHANNEL_ELEMENT);
				xsw.writeAttribute(CH_ID_ATTR, Integer.toString(i));
				xsw.writeCharacters("\n");
				xsw.writeStartElement(FIT_ELEMENT);
				xsw.writeAttribute(R2_ATTR, Double.toString(this.getFitR2ByChannel().get(i)));
				xsw.writeAttribute(ERROR_ATTR, Double.toString(this.getFitErrorByChannel().get(i)));
				xsw.writeAttribute(N_PHOTONS_ATTR, Double.toString(this.getNPhotonsByChannel().get(i)));
				xsw.writeCharacters("\n");
				xsw.writeStartElement(PARAMETERS_ELEMENT);
				xsw.writeCharacters(this.getFitParametersByChannel().get(i).toString().replace(";", ",").replace("}", "").replace("{", ""));
				xsw.writeEndElement(); //parameters
				xsw.writeCharacters("\n");
				xsw.writeStartElement(POSITION_ELEMENT);
				xsw.writeCharacters(this.getPositionForChannel(i).toString().replace(";", ",").replace("}", "").replace("{", ""));
				xsw.writeEndElement(); //position
				xsw.writeCharacters("\n");
				xsw.writeEndElement(); //fit
				xsw.writeCharacters("\n");
				xsw.writeEndElement(); //channel
				xsw.writeCharacters("\n");
			}
			xsw.writeStartElement(SERIAL_ELEMENT);
			xsw.writeAttribute(ENCODING_ATTR, ENCODING_NAME);

			ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

			try {

				ObjectOutputStream oos = new ObjectOutputStream(bytesOutput);

				oos.writeObject(this);

			} catch (java.io.IOException e) {
				LoggingUtilities.getLogger().severe("Exception encountered while serializing ImageObject: " + e.getMessage());

			}

			HexBinaryAdapter adapter = new HexBinaryAdapter();
			xsw.writeCharacters(adapter.marshal(bytesOutput.toByteArray()));

			xsw.writeEndElement(); //serial


			xsw.writeCharacters("\n");

			xsw.writeEndElement(); //object

			xsw.writeCharacters("\n");


		} catch (XMLStreamException e) {

			LoggingUtilities.getLogger().severe("Exception encountered while writing XML correction output: " + e.getMessage());

		}

	}

}
