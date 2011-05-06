/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

 package edu.stanford.cfuller.imageanalysistools.fitting;

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.OptimizationException;

import java.io.Serializable;
import java.lang.Double;import java.lang.Math;import java.lang.String;import java.lang.Throwable;import java.util.Vector;

/**
 * An object in an image with some spatial extent that can be fit to some function.
 *
 * Classes that extend ImageObject will supply methods that perform the fit to a specific functional form.
 *
 */
public abstract class ImageObject implements Serializable {

    public static final long serialVersionUID = 1L;

    Vector3D centroidInMask;

    int sizeInPixels;

    int label;

    int[] maxIntensityZCoordByChannel;

    double[] xValues;
    double[] yValues;
    double[] zValues;
    double[] functionValues;

    java.util.Vector<RealVector> fitParametersByChannel;
    java.util.Vector<Double> fitR2ByChannel;
    java.util.Vector<Double> fitErrorByChannel;

    ImageCoordinate parentBoxMin;
    ImageCoordinate parentBoxMax;

    Image parent;
    Image mask;

    String imageID;

    boolean hadFittingError;


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
        this.label = 0;
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

                this.centroidInMask = this.centroidInMask.add(new Vector3D(i.getX(), i.getY(), i.getZ()));

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

        this.parentBoxMin = ImageCoordinate.createCoord(xcoord, ycoord, 0, 0, 0);

        xcoord = (int) Math.round(this.centroidInMask.getX() + p.getIntValueForKey("half_box_size"))+1;
        ycoord = (int) Math.round(this.centroidInMask.getY() + p.getIntValueForKey("half_box_size"))+1;

        if (xcoord > mask.getDimensionSizes().getX()) {xcoord = mask.getDimensionSizes().getX();}
        if (ycoord > mask.getDimensionSizes().getY()) {ycoord = mask.getDimensionSizes().getY();}

        this.parentBoxMax = ImageCoordinate.createCoord(xcoord, ycoord, parent.getDimensionSizes().getZ(), parent.getDimensionSizes().getC(), parent.getDimensionSizes().getT());


        //find the max intensity pixel in each channel and use this to refine the box

        //System.out.println("for object " + label + " cenX=" + this.centroidInMask.getX() + " cenY=" + this.centroidInMask.getY());

        this.maxIntensityZCoordByChannel = new int[parent.getDimensionSizes().getC()];

        int minZOverall = parent.getDimensionSizes().getZ();
        int maxZOverall = 0;

        //for (int c = 0; c < parent.getDimensionSizes().getC(); c++) {
        for (int c = 0; c < p.getIntValueForKey("num_wavelengths"); c++) { // use this so that if there's extra wavelengths not to be quantified at the end, these won't skew the initial guess
            this.parentBoxMin.setC(c);
            this.parentBoxMax.setC(c+1);

            parent.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);

            double maxValue = 0;
            ImageCoordinate maxCoord = null;

            for (ImageCoordinate ic : parent) {

                if (! ( ic.getX() == (int) Math.round(this.centroidInMask.getX())) || ! ( ic.getY() == (int) Math.round(this.centroidInMask.getY()))) {
                    continue;
                }

                if (parent.getValue(ic) > maxValue) {
                    maxValue = parent.getValue(ic);
                    if (maxCoord != null) maxCoord.recycle();
                    maxCoord = ImageCoordinate.cloneCoord(ic);
                }

            }

            if (maxCoord == null) continue;

            if (maxCoord.getZ() > maxZOverall) maxZOverall = maxCoord.getZ();
            if (maxCoord.getZ() < minZOverall) minZOverall = maxCoord.getZ();

            this.maxIntensityZCoordByChannel[c] = maxCoord.getZ();

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

        this.parentBoxMin.setC(0);
        zcoord = zAverage - p.getIntValueForKey("half_z_size");
        if (zcoord < 0) zcoord = 0;
        this.parentBoxMin.setZ(zcoord);

        this.parentBoxMax.setC(parent.getDimensionSizes().getC());
        zcoord = zAverage + p.getIntValueForKey("half_z_size")+1;
        if (zcoord > parent.getDimensionSizes().getZ()) zcoord = parent.getDimensionSizes().getZ();
        this.parentBoxMax.setZ(zcoord);
    }


    /**
     * Fits the object to the ImageObject's functional form in order to determine its position.
     * @param p     The parameters for the current analysis.
     * @throws FunctionEvaluationException  if the functional form of the ImageObject returns an invalid value during the optimization.
     * @throws OptimizationException        if there is an error during the optimization.
     */
    public abstract void fitPosition(ParameterDictionary p) throws FunctionEvaluationException, OptimizationException;


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
     * Gets the label of this object; this corresponds to its greylevel value in the original image mask.
     * @return  The label of this object.
     */
    public int getLabel() {return this.label;}

}
