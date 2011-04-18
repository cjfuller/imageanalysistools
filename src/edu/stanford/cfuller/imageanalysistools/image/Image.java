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

package edu.stanford.cfuller.imageanalysistools.image;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageWriter;
import ome.xml.model.primitives.PositiveInteger;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Represents a multidimensional image with some pixel data and associated metadata.
 *<p>
 * This class implements the Collection<ImageCoordinate> interface, which allows for foreach-style iteration through
 * all the coordinates in the Image, without having users need to explicitly worry about things like number of dimensions or
 * bounds, etc.  This foreach-style iteration is the preferred method for operating on the pixel values of the Image.
 *<p>
 * This class also has the capability of boxing (a single) region of interest in the Image and restricting the foreach-style iteration
 * solely to that box, if only a portion of the Image needs to be processed, viewed, etc.
 *<p>
 * A side note to users of this class: it appears that because this uses a finalize method to recycle ImageCoordinates,
 * it is possible to run out of memory by rapidly creating and destroying many images such that the finalizer queue cannot process
 * the finalize method as rapidly as objects are being created.  If this is happening, explicitly calling the dispose method
 * will nullify the memory-intensive fields, allowing those to be garbage collected immediately.
 *
 * @author Colin J. Fuller
 *
 */
public class Image implements java.io.Serializable, java.util.Collection<ImageCoordinate> {


	//fields
	
	final static long serialVersionUID=1L;
	final String defaultDimensionOrder = "XYZCT";
	
	private loci.formats.meta.IMetadata metadata;
	
	private PixelData pixelData;
	
	private ImageCoordinate dimensionSizes;

    private ImageCoordinate boxMin;
    private ImageCoordinate boxMax;

    private boolean isBoxed;

    private ImageCoordinate[] coordinateArrayStorage;

	//constructors

    /**
     * Constructs a new Image with the specified metadata and PixelData.
     * <p>
     * The metadata and PixelData should already be initialized (and should contain consistent values for things like dimension sizes).
     * <P>
     * This constructor is primarily used by classes that read images in binary format; most users will probably want to use a different constructor.
     * @param m     An object containing the metadata associated with the Image (as a loci.formats.meta.IMetadata, to ease integration with the LOCI bio-formats library).
     * @param p     A PixelData object containing the actual values at each pixel in the Image.
     */
	public Image(loci.formats.meta.IMetadata m, PixelData p) {
		this.isBoxed = false;
        this.boxMin = null;
        this.boxMax = null;
		this.metadata = m;
		this.pixelData = p;
        this.coordinateArrayStorage = null;
		this.dimensionSizes = ImageCoordinate.createCoord(p.getSizeX(), p.getSizeY(), p.getSizeZ(), p.getSizeC(), p.getSizeT());

	}

    /**
     * Constructs a new Image that is a (deep) copy of the specified Image.  The pixel data will be copied exactly, but no guarantee
     * is made that the metadata will be completely copied; only the minimally necessary metadata (like dimension sizes) will definitely be copied.
     * @param toCopy    The Image to copy.
     */
	public Image(Image toCopy){
        this.isBoxed = false;
        this.boxMin = null;
        this.boxMax = null;
        this.coordinateArrayStorage = null;
		PixelData p = toCopy.pixelData;
		this.dimensionSizes = ImageCoordinate.createCoord(p.getSizeX(), p.getSizeY(), p.getSizeZ(), p.getSizeC(), p.getSizeT());
		this.pixelData= (new PixelDataFactory()).createPixelData(toCopy.getDimensionSizes(), toCopy.pixelData.getDataType(), defaultDimensionOrder);
		setupNewMetadata();
		this.copy(toCopy);

	}

    /**
     * Constructs a new Image with specified dimension sizes and all the pixel values set to some specified initial value.  Bare-bones metadata
     * will also be constructed.
     * <p>
     * (Note that even though ImageCoordinates are zero-indexed, the ImageCoordinate's components should be the actual sizes of the dimensions,
     * not the indices of those dimensions.  For instance, for an 512 x by 512 y by 1 z plane by 1 color by 1 timepoint image, the ImageCoordinate
     * specifying sizes should have components = (512, 512, 1, 1, 1), not (511, 511, 0, 0, 0).)
     *
     * @param dimensionSizes    An ImageCoordinate whose components will be the sizes of each dimension of the Image.
     * @param initialValue      The initial value to which all the pixels will be set.
     */
	public Image(ImageCoordinate dimensionSizes, double initialValue){
		this.isBoxed = false;
        this.boxMin = null;
        this.boxMax = null;
        this.coordinateArrayStorage = null;
        this.dimensionSizes = ImageCoordinate.createCoord(dimensionSizes.getX(), dimensionSizes.getY(), dimensionSizes.getZ(), dimensionSizes.getC(), dimensionSizes.getT());
		this.pixelData= (new PixelDataFactory()).createPixelData(dimensionSizes, loci.formats.FormatTools.UINT16, "XYZCT");
		setupNewMetadata();
		
		for (ImageCoordinate i : this) {
			this.setValue(i, initialValue);
		}
        
	}
	
	//public methods

    /**
     * Sets the region of interest in the Image.
     * <p>
     * The region of interest must be a (possibly high-dimensional) rectangle and is represented by two points, the lower bound of the coordinates
     * and the upper bound of the coordinates (conceptually like the upper-left and lower-right corners for the 2D case).  The box will include the
     * lower bound but exclude the upper bound.
     *<p>
     * The region of interest will control what area of the Image will be iterated over using foreach-style iteration, or
     * any of the methods of an {@link ImageIterator}.  This will be the sole region iterated over until {@link #clearBoxOfInterest()} is
     * called, or this method is called again with a new region of interest.  A new region of interest specified will replace any existing
     * region; it is not possible to construct complex regions by successively building with several rectangles.
     * <p>
     * As per the specification in {@link ImageCoordinate}, users are still responsible for recycling the ImageCoordinate parameters; they are
     * not retained and recycled by this class.
     *
     * @param boxMin    The (inclusive) lower coordinate bound of the boxed region of interest.
     * @param boxMax    The (exclusive) upper coodeinate bound of the boxed region of interest.
     */
    public void setBoxOfInterest(ImageCoordinate boxMin, ImageCoordinate boxMax) {

        if (this.boxMin != null) {clearBoxOfInterest();}

        this.boxMin = ImageCoordinate.cloneCoord(boxMin);
        this.boxMax = ImageCoordinate.cloneCoord(boxMax);
        
        //bounds checking
        if (this.boxMin.getX() < 0) this.boxMax.setX(0);
        if (this.boxMin.getY() < 0) this.boxMax.setY(0);
        if (this.boxMin.getZ() < 0) this.boxMax.setZ(0);
        if (this.boxMin.getC() < 0) this.boxMax.setC(0);
        if (this.boxMin.getT() < 0) this.boxMax.setT(0);

        if (this.boxMax.getX() > this.dimensionSizes.getX()) this.boxMax.setX(this.dimensionSizes.getX());
        if (this.boxMax.getY() > this.dimensionSizes.getY()) this.boxMax.setY(this.dimensionSizes.getY());
        if (this.boxMax.getZ() > this.dimensionSizes.getZ()) this.boxMax.setZ(this.dimensionSizes.getZ());
        if (this.boxMax.getC() > this.dimensionSizes.getC()) this.boxMax.setC(this.dimensionSizes.getC());
        if (this.boxMax.getT() > this.dimensionSizes.getT()) this.boxMax.setT(this.dimensionSizes.getT());

        this.isBoxed = true;

    }

    /**
     * Clears any existing region of interest that has been set on this Image.
     *
     * This will cause any foreach-style iteration or ImageIterator-controlled iteration to iterate over the entire image.
     *
     */
    public void clearBoxOfInterest() {
        if (this.boxMax != null && this.boxMin != null) {
            this.boxMax.recycle();
            this.boxMin.recycle();
        }
        this.boxMin = null;
        this.boxMax = null;
        this.isBoxed = false;
    }

    /**
     *  Gets the (exclusive) upper bound of any region of interest currently set on this Image, or null if no region is currently
     * set.
     * <p>
     * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
     * As per the specification in {@link ImageCoordinate}, users should <i>not</i> recycle the ImageCoordinate returned.
     *
     * @return  The ImageCoordinate whose components are the upper bound on the region of interest, or null if there is no region of interest.
     */
    public ImageCoordinate getBoxMax() {
        return this.boxMax;
    }

    /**
     *  Gets the (inclusive) lower bound of any region of interest currently set on this Image, or null if no region is currently
     * set.
     * <p>
     * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
     * As per the specification in {@link ImageCoordinate}, users should <i>not</i> recycle the ImageCoordinate returned.
     *
     * @return  The ImageCoordinate whose components are the lower bound on the region of interest, or null if there is no region of interest.
     */
    public ImageCoordinate getBoxMin() {
        return this.boxMin;
    }

    /**
     * Queries whether the Image is currently boxed with a region of interest.
     * @return  true if there is currently a region of interest set, false otherwise.
     */
    public boolean getIsBoxed() {
        return this.isBoxed;
    }

    /**
     * Gets the metadata associated with this Image.  (The object returned is an {@link loci.formats.meta.IMetadata} to facilitate
     * use with the LOCI bio-formats library.
     * @return  The metadata object associated with the Image.
     */
	public loci.formats.meta.IMetadata getMetadata() {return this.metadata;}

    /**
     * Creates a new Image that is a sub-image of the Image.
     * <p>
     * The new Image will be created with bare-bones metadata and its pixel values set to the pixel values of this Image in the specifed region.
     * The new Image will have the dimension sizes specified (as if the Image constructor were called with this size parameter), and start at the specified
     * coordinate in the current Image (inclusive).  If the start point + new dimension sizes goes outside the current Image, then the new Image will be filled
     * with zeros at any coordinate outside the current Image.
     * <p>
     * As per the specification in {@link ImageCoordinate}, users are responsible for recycling the ImageCoordinate parameters; they are not retained
     * or recycled by this class.
     *
     *
     * @param newDimensions     An ImageCoordinate containing the size of the new sub-image in each dimension.
     * @param startPoint        The (inclusive) point in the current Image where the sub-image will start (this will become coordinate (0,0,0,...) in the new Image).
     * @return                  A new Image with pixel values set to those of the specified sub region of the current Image.
     */
	public Image subImage(ImageCoordinate newDimensions, ImageCoordinate startPoint) {
		
		Image toReturn = new Image(newDimensions, 0.0);
		
		ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
		
		for (ImageCoordinate i : toReturn) {
			
			ic.setCoord(startPoint.getX() + i.getX(), startPoint.getY() + i.getY(), startPoint.getZ() +i.getZ(), startPoint.getC()+ i.getC(), startPoint.getT() + i.getT());


            if (this.inBounds(ic)) {
			    toReturn.setValue(i, this.getValue(ic));
            }
		}
		
		ic.recycle();
		
		return toReturn;
		
		
	}

    /**
     * Copies the pixel values of another Image into this Image.  Any existing pixel values are overwritten.  Metadata is not copied.
     * The two Images must be the same size; this is not checked.
     * @param other     The Image whose pixel values will be copied.
     */
	public void copy(Image other) {
		for (ImageCoordinate i : this) {
			this.setValue(i, other.getValue(i));
		}
	}

    /**
     * Writes the Image to a file.
     * <p>
     *
     * The output format of the Image will be guessed from the extension.  Valid formats are those that the LOCI bio-formats library can write.
     * (The recommended option is the ome-tiff format, which should have the extension .ome.tif).
     * 
     *
     * @param filename  The full absolute path to the file to which the Image is to be written.  The parent directory must exist.
     */
	public void writeToFile(String filename) {
		try {
			(new ImageWriter(this)).write(filename, this.pixelData);
		} catch (java.io.IOException e) {
			LoggingUtilities.getLogger().severe("Error while writing image to file: " + filename + " Skipping write and continuing.");
			e.printStackTrace();
		}
	}

    /**
     * Gets the value of the Image at the coordinate specified.  No bounds checking is performed.
     * @param coord     An ImageCoordinate specifying the location of the value to retrieve.
     * @return          The value of the Image at the specified location as a double.
     */
	public double getValue(ImageCoordinate coord) {
		return pixelData.getPixel(coord.getX(), coord.getY(), coord.getZ(), coord.getC(), coord.getT());
	}

    /**
     * Sets the value of the Image at the 2D coordinate specified.  No bounds checking is performed.
     * @deprecated  Use {@link #setValue(ImageCoordinate, double)} instead.
     * @param row   The row (y-coordinate) to set.
     * @param col   The column (x-coordinate) to set.
     * @param value The value that the Image will be set to at the specified location.
     */
	public void setValue(int row, int col, double value) {
		pixelData.setPixel(col, row, 0, 0, 0, value);
	}

    /**
     * Sets the value of the Image at the specified Image Coordinate.  No bounds checking is performed.
     * @param coord     The ImageCoordinate at which to set the Image's value.
     * @param value     The value to which to set the Image at the specified coordinate.
     */
	public void setValue(ImageCoordinate coord, double value) {
		pixelData.setPixel(coord.getX(), coord.getY(), coord.getZ(), coord.getC(), coord.getT(), value);
	}
	
	//public void clear(int newValue){}
	
	//public int getWidth(){return 0;}
	
	//public int getHeight(){return 0;}

    /**
     * Checks to see whether a specified ImageCoordinate represents a location in the Image.
     * @param c     The ImageCoordinate to check
     * @return      true, if the ImageCoordinate lies within this Image in every dimension, false otherwise.
     */
	public boolean inBounds(ImageCoordinate c) {
		boolean ret_val = true;
		ImageCoordinate sizes = this.getDimensionSizes();

		if (c.getX() < 0 || c.getY() < 0 || c.getZ() < 0 || c.getT() < 0 || c.getC() < 0) {
			ret_val = false;
		}
		if (c.getX() >= sizes.getX() || c.getY() >= sizes.getY() || c.getZ() >= sizes.getZ() || c.getT() >= sizes.getT() || c.getC() >= sizes.getC()) {
			ret_val = false;
		}
		
		return ret_val;
	}

    /**
     * Returns an ImageCoordinate that contains the size of each dimension of the Image.
     * <p>
     * This ImageCoordinate should not be modified by users, nor should it be recycled by users.
     * 
     * @return      An ImageCoordinate containing the size of each dimension of the Image.
     */
	public ImageCoordinate getDimensionSizes(){return this.dimensionSizes;}

    /**
     * Convenience method for converting a single Image with non-singleton color dimension into a List of Images with
     * singleton color dimension, each containing the Image for a single color channel.
     * <p>
     * Images will be returned in the List in the order of their color dimension index in the original Image.
     * @return      A List of Images, each with one color channel from the original Image.
     */
    public java.util.List<Image> splitChannels() {

        final int series_number = 0; // if this Image was created from a multi-series image, this will get metadata from the first series.

        java.util.Vector<Image> split = new java.util.Vector<Image>();

        ImageCoordinate ic = ImageCoordinate.createCoord(0,0,0,0,0);

        /*we need to label the channels individually with some sort of identifier in their metadata.  Try first the name, if null, then
            try the excitation/emission wavelengths, if null then fall back on the channel ID, which will be something nondescript but unique.

            Whatever the result, this will set the metadata property using setChannelName so that this can be retrieved later.
        */

        boolean useName = false;
        boolean useWavelengths = false;
        boolean useID = true;

        int channelCount = this.metadata.getChannelCount(series_number);


        String name0 = this.metadata.getChannelName(series_number, 0);
        PositiveInteger exW = this.metadata.getChannelExcitationWavelength(series_number, 0);
        PositiveInteger emW = this.metadata.getChannelEmissionWavelength(series_number, 0);

        //check if name is ok

        if (name0 != null && name0 != "") {
            useName = true;
            useWavelengths = false;
            useID = false;

        //check if wavelengths are ok
        } else if ((exW != null && exW.getValue() > 0) || (emW != null && emW.getValue() > 0) ){
            useName = false;
            useWavelengths = true;
            useID = false;
        }

        for (int i =0; i < this.dimensionSizes.getC(); i++) {
            ic.recycle();
            ic = ImageCoordinate.cloneCoord(this.getDimensionSizes());
            ic.setC(1);
            Image newChannelImage = new Image(ic, 0.0);
            
            //set the channel name for this new single channel image
            String channelName = "";
            if (useID) {
                channelName = this.metadata.getChannelID(series_number, i);
            }
            if (useWavelengths) {
                channelName = this.metadata.getChannelExcitationWavelength(series_number, i) + "/" + this.metadata.getChannelEmissionWavelength(series_number, i);
            }
            if (useName) {
                channelName = this.metadata.getChannelName(series_number, i);
            }

            newChannelImage.getMetadata().setChannelName(channelName, 0, 0);


            split.add(newChannelImage);
        }

        for (ImageCoordinate i : this) {

            ic.recycle();

            ic = ImageCoordinate.cloneCoord(i);

            ic.setC(0);

            split.get(i.getC()).setValue(ic, this.getValue(i));

        }

        ic.recycle();

        return split;

    }


    /**
     * Converts the Image to a {@link BufferedImage} for use with other java imaging clases.
     *<p>
     * Because the Buffered Image can only represent single-plane images (possibly with down-sampled color channels), this
     * conversion may not convert all the Image data.
     *<p>
     * For multi-plane images, this will convert the first x-y plane only (that is, with c=0, t=0, z=0).  If you want a different
     * plane, create a single plane sub-image using {@link #subImage(ImageCoordinate, ImageCoordinate)}, and then convert that Image.
     * <p>
     * The output format is a 16-bit greyscale BufferedImage.
     *
     * @return  A BufferedImage containing the pixel values from the first plane of the Image converted to 16-bit greyscale format.
     */
    public java.awt.image.BufferedImage toBufferedImage() {

        BufferedImage toReturn = new BufferedImage(this.getDimensionSizes().getX(), this.getDimensionSizes().getY(), BufferedImage.TYPE_USHORT_GRAY);

        WritableRaster r = toReturn.getRaster();

        ImageCoordinate boxMin = ImageCoordinate.createCoord(0,0,0,0,0);
        ImageCoordinate boxMax = ImageCoordinate.cloneCoord(this.getDimensionSizes());
        boxMax.setC(1);
        boxMax.setZ(1);
        boxMax.setT(1);

        this.setBoxOfInterest(boxMin, boxMax);

        for (ImageCoordinate i : this) {

            r.setSample(i.getX(), i.getY(), 0, this.getValue(i));
            
        }

        this.clearBoxOfInterest();
        boxMin.recycle();
        boxMax.recycle();

        return toReturn;

    }

    /**
     * Gets the number of planes in this Image (that is the number of distinct (z, c, t) coordinates).
     *
     * @return  The number of planes.
     */
    public int getPlaneCount() {

        return this.getDimensionSizes().getZ() * this.getDimensionSizes().getT() * this.getDimensionSizes().getC();
    
    }


    /**
     * Selects a plane as active for iteration.  This has the same effect as calling the setBoxOfInterest method
     * with coordinates that would select only the given plane.  This should not be used in conjunction with the setBoxOfInterest method,
     * as internally this method uses the same boxing mechanism.  To clear the selected plane, call clearBoxOfInterest.
     * 
     * @param i     The plane index to set as active; no guarantee is made as to which plane this is, except that this index
     *              will always refer to the same plane for a given Image, and iterating from i = 0 to getPlaneCount() - 1 will
     *              visit all the planes in the Image.
     */
    public void selectPlane(int i) {

        ImageCoordinate tempMin = ImageCoordinate.createCoord(0,0,0,0,0);
        ImageCoordinate tempMax = ImageCoordinate.createCoord(this.getDimensionSizes().getX(),this.getDimensionSizes().getY(),0,0,0);

        int z_index = i % this.getDimensionSizes().getZ();

        int c_index = ((i - z_index)/(this.getDimensionSizes().getZ())) % this.getDimensionSizes().getC();

        int t_index = ((((i - z_index)/(this.getDimensionSizes().getZ())) - c_index)/this.getDimensionSizes().getC());

        tempMin.setZ(z_index);
        tempMin.setC(c_index);
        tempMin.setT(t_index);
        tempMax.setZ(z_index+1);
        tempMax.setC(c_index+1);
        tempMax.setT(t_index+1);

        this.setBoxOfInterest(tempMin, tempMax);
        tempMin.recycle();
        tempMax.recycle();

    }



	// collection interface methods

    /**
     * Collection interface method.  Not supported.
     * 
     * @param c
     * @return  will not return
     * @throws UnsupportedOperationException    if this method is called.
     */
	public boolean add(ImageCoordinate c) throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Add not supported for images.");
	}

    /**
     * Collection interface method.  Not supported.
     * 
     * @param c
     * @return  will not return
     * @throws UnsupportedOperationException    if this method is called.
     */
	public boolean addAll(java.util.Collection<? extends ImageCoordinate> c)  throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Add not supported for images.");
	}

    /**
     * Collection interface method.  Not supported.
     * 
     * @throws UnsupportedOperationException if this method is called.
     */
	public void clear()  throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Clear not supported for images.");
	}

    /**
     * Checks to see whether a given object is contained in this Image.  Collection interface method.
     * <p>
     * This is equivalent to checking that the object is an instance of ImageCoordinate, and then calling {@link #inBounds(ImageCoordinate)}.
     *
     *
     * @param o     The object that will be checked.
     * @return      true if the Object is an ImageCoordinate and is inBounds, false otherwise.
     */
	public boolean contains(Object o) {
		ImageCoordinate o_c = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
		boolean ret_val = false;
		if (o.getClass() == o_c.getClass()) {
			ret_val = true;
			o_c.recycle();
			o_c = (ImageCoordinate) o;
			ret_val = this.inBounds(o_c);
		}
		
		return ret_val;
	}

    /**
     * Checks to see whether a given collection of objects is contained in this Image.  Collection interface method.
     * <p>
     * This is equivalent to taking the logical AND of the call to {@link #contains(Object)} on each object in the Collection.
     * @param c     The Collection of objects to check.
     * @return      true if every Object in the collection is an ImageCoordinate and inBounds, false otherwise.
     */
	public boolean containsAll(java.util.Collection<?> c) {
		for (Object o : c) {
			if (! this.contains(o)) return false;
		}
		return true;
	}

    /**
     * Checks to see if the Image is empty.  Collection interface method.
     * <p>
     * An Image is considered empty if and only if any of the dimensions of the Image have size zero.
     *
     * @return  true if this Image has any zero-size dimensions, false otherwise.
     */
	public boolean isEmpty() {
		ImageCoordinate dimSizes = this.getDimensionSizes();
		if (dimSizes.getX() == 0 || dimSizes.getY() == 0 || dimSizes.getZ() == 0 || dimSizes.getT() == 0 || dimSizes.getC() == 0) {
			return true;
		}
		return false;
	}

    /**
     * Gets an Iterator over the ImageCoordinates in this Image.  Collection interface method.
     * 
     * @return  an Iterator that will traverse all the coordinates in the Image (or all the coordinates in the region of interest if the
     * Image is currently boxed with a region of interest.
     */
	public java.util.Iterator<ImageCoordinate> iterator() {
		return new edu.stanford.cfuller.imageanalysistools.image.ImageIterator(this);
	}

    /**
     * Collection interface method.  Not supported.
     * 
     * @param o
     * @return  will not return
     * @throws UnsupportedOperationException if this method is called.
     */
	public boolean remove(Object o)  throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Remove not supported for images.");
	}

    /**
     * Collection interface method.  Not supported.
     * 
     * @param c
     * @return  will not return
     * @throws UnsupportedOperationException if this method is called.
     */
	public boolean removeAll(java.util.Collection<?> c)  throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Remove not supported for images.");
	}

    /**
     * Collection interface method.  Not supported.
     * 
     * @param c
     * @return  will not return
     * @throws UnsupportedOperationException if this method is called.
     */
	public boolean retainAll(java.util.Collection<?> c)  throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Retain not supported for images.");
	}

    /**
     * Gets the size of the Image, or more specifically, the number of distinct ImageCoordinates making up this Image.  Collection interface method.
     *<p>
     * The result is equivalent to multiplying the size of each dimension of the Image together.
     * @return  The size of this Image, in number of ImageCoordinates (or multidimensional pixels).
     */
	public int size() {
		ImageCoordinate c = this.getDimensionSizes();
		return (c.getC()*c.getX()*c.getY()*c.getZ()*c.getT());
	}

    /**
     * Gets an array of all the ImageCoordinates in the Image.  Collection interface method.
     * <p>
     * Calling this method is almost certainly a bad idea, as it will allocate a massive number of ImageCoordinates, instead of just reusing.
     * Nonetheless, if users call this method, as per the specification in {@link ImageCoordinate}, they should not recycle them.
     *
     * @return  An array containing all the ImageCoordinates in the Image.
     */
	public Object[] toArray() {
        if (this.coordinateArrayStorage == null) {
            this.coordinateArrayStorage = new ImageCoordinate[this.size()];

   		    int index =0;
		    for (ImageCoordinate c : this) {
                this.coordinateArrayStorage[index++] = ImageCoordinate.cloneCoord(c);
		    }
        }
		return this.coordinateArrayStorage;
	}

    /**
     * Gets an array of all the ImageCoordinates in the Image as an array of the same type as the given array.
     * If there is insufficient room in the given array, a new array will be allocated and returned.  Collection interface method.
     * <p>
     * For the same reasons as in {@link #toArray()}, calling this method is almost always a bad idea.
     *
     * @param a     An array of the type to return.
     * @param <T>   The type of the array to return.
     * @return      An array of type T containing all the ImageCoordinates in the Image.
     */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {

        ImageCoordinate[] im_a = new ImageCoordinate[0];

        if (!a.getClass().isInstance(im_a)) {
            throw new ArrayStoreException(a.getClass().getName() + "is not a valid super class of ImageCoordinate[].");
        }
        		
		if (a.length < this.size()) {
			return (T[]) a.getClass().cast(this.toArray());
		} else {
            Object[] coords = this.toArray();
            for (int i =0; i < a.length; i++) {
                if (i < coords.length) {
                    a[i] = (T) coords[i];
                } else {
                    a[i] = null;
                }
            }

            return a;
        }
		
		
	}


    /**
     * Immediately nullifies the portions of the image that consume a lot of memory.  This may help programs that rapidly
     * create and destroy many Images from running out of memory while the Images are being finalized.
     */
    public void dispose() {
        this.pixelData = null;
        if (this.coordinateArrayStorage != null) {
            for (ImageCoordinate ic : this.coordinateArrayStorage) {
                ic.recycle();
            }
            this.coordinateArrayStorage = null;
        }
    }
	
	//recycle image coordinates
	
	protected void finalize() throws Throwable{
		dimensionSizes.recycle();
        if (this.boxMin != null && this.boxMax != null) {
            boxMax.recycle();
            boxMin.recycle();
        }
        if (this.coordinateArrayStorage != null) {
            for (ImageCoordinate ic : this.coordinateArrayStorage) {
                ic.recycle();
            }
            this.coordinateArrayStorage = null;
        }

		super.finalize();
	}
	
	private void setupNewMetadata() {
		try {
		
			this.metadata = (new loci.common.services.ServiceFactory()).getInstance(loci.formats.services.OMEXMLService.class).createOMEXMLMetadata();
			
		} catch (loci.common.services.ServiceException e) {
			e.printStackTrace();
		} catch (loci.common.services.DependencyException e) {
			e.printStackTrace();
		}
	
		this.metadata.createRoot();
		this.metadata.setImageID("Image:0", 0);
		this.metadata.setPixelsID("Pixels:0", 0);
		this.metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
		try {
			this.metadata.setPixelsType(ome.xml.model.enums.PixelType.fromString(loci.formats.FormatTools.getPixelTypeString(this.pixelData.getDataType())), 0);
		} catch (ome.xml.model.enums.EnumerationException e) {
			e.printStackTrace();
		}
		try {
			this.metadata.setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder.fromString(this.pixelData.getDimensionOrder()), 0);
		} catch (ome.xml.model.enums.EnumerationException e) {
			e.printStackTrace();
		}
        
		this.metadata.setPixelsSizeC(new PositiveInteger(dimensionSizes.getC()), 0);
		this.metadata.setPixelsSizeT(new PositiveInteger(dimensionSizes.getT()), 0);
		this.metadata.setPixelsSizeZ(new PositiveInteger(dimensionSizes.getZ()), 0);
		this.metadata.setPixelsSizeX(new PositiveInteger(dimensionSizes.getX()), 0);
		this.metadata.setPixelsSizeY(new PositiveInteger(dimensionSizes.getY()), 0);

        for (int i =0; i < dimensionSizes.getC(); i++) {
		    this.metadata.setChannelID("Channel:0:"+i, 0, i);
		    this.metadata.setChannelSamplesPerPixel(new PositiveInteger(1), 0, i);
        }
	}

}
