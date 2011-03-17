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
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 1/4/11
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
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

    public void init() {
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

    public void init(int label, Image mask, Image parent, ParameterDictionary p) {

        this.fitParametersByChannel = null;
        this.fitR2ByChannel = null;
        this.fitErrorByChannel = null;

        this.parent = new Image(parent);
        this.mask = new Image(mask);

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

        this.centroidInMask = this.centroidInMask.scalarMultiply(1.0/sizeInPixels);

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

        this.maxIntensityZCoordByChannel = new int[parent.getDimensionSizes().getC()];

        int minZOverall = parent.getDimensionSizes().getZ();
        int maxZOverall = 0;

        for (int c = 0; c < parent.getDimensionSizes().getC(); c++) {
            this.parentBoxMin.setC(c);
            this.parentBoxMax.setC(c+1);

            parent.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);

            double maxValue = 0;
            ImageCoordinate maxCoord = null;

            for (ImageCoordinate ic : parent) {

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

        int zcoord = 0;

        this.parentBoxMin.setC(0);
        zcoord = minZOverall - p.getIntValueForKey("half_z_size");
        if (zcoord < 0) zcoord = 0;
        this.parentBoxMin.setZ(zcoord);

        this.parentBoxMax.setC(parent.getDimensionSizes().getC());
        zcoord = maxZOverall + p.getIntValueForKey("half_z_size")+1;
        if (zcoord > parent.getDimensionSizes().getZ()) zcoord = parent.getDimensionSizes().getZ();
        this.parentBoxMax.setZ(zcoord);
    }



    public abstract void fitPosition(ParameterDictionary p) throws FunctionEvaluationException, OptimizationException;


    public Vector<RealVector> getFitParametersByChannel() {
        return fitParametersByChannel;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public void nullifyImages() {
        this.parent = null;
        this.mask = null;
        this.xValues = null;
        this.yValues = null;
        this.zValues = null;
        this.functionValues = null;
    }

    public Image getMask() {
        return mask;
    }

    public Image getParent() {
        return parent;
    }

    public void finalize() throws Throwable {
        if (this.parentBoxMin != null) {this.parentBoxMin.recycle(); this.parentBoxMin = null;}
        if (this.parentBoxMax != null) {this.parentBoxMax.recycle(); this.parentBoxMax = null;}

        super.finalize();
    }

    public void boxImages() {
        this.parent.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);
        this.mask.setBoxOfInterest(this.parentBoxMin, this.parentBoxMax);
    }

    public void unboxImages() {
        this.parent.clearBoxOfInterest();
        this.mask.clearBoxOfInterest();
    }

    public double[] getxValues() {
        return xValues;
    }

    public double[] getyValues() {
        return yValues;
    }

    public double[] getzValues() {
        return zValues;
    }

    public double[] getFunctionValues() {
        return functionValues;
    }

    public boolean finishedFitting() {
        return !this.hadFittingError;
    }

    public Vector<Double> getFitR2ByChannel() {
        return fitR2ByChannel;
    }

    public Vector<Double> getFitErrorByChannel() {
        return fitErrorByChannel;
    }

    public int getLabel() {return this.label;}

}
