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
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
import edu.stanford.cfuller.imageanalysistools.image.io.OmeroServerImageReader;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A set of related Images that will be processed together for analysis.  For instance, multiple channels of the same Image might be collected
 * into an ImageSet.  Images in the set can be added by Image object, by filename, or by omero Id.
 *
 * Optionally, one Image in the set can be designated as a marker Image, which might have some specific purpose in the analysis.  For
 * instance, the marker Image might be the Image to use for segmentation, while the others might just be quantified.
 *
 * Images added by filename or omero Id must be loaded using {@link #loadAllImages()} before being used or retrieved.
 *
 * @author Colin J. Fuller
 */
public class ImageSet implements java.io.Serializable, Collection<Image> {

    final static long serialVersionUID=1L;

    List<ImageHolder> images;

    Integer markerIndex;

    ParameterDictionary parameters;

    /**
     * Constructs a new, empty ImageSet.
     *
     * @param p     The parameters for the analysis.
     */
    public ImageSet(ParameterDictionary p) {
        images = new ArrayList<ImageHolder>();
        markerIndex = null;
        this.parameters = p;
    }

    /**
     * Copy constructor.
     *
     * Does not make copies of the Images; this method is primarily useful to allow different marker Images or simultaneous iteration.
     *
     * @param other     The ImageSet to copy.
     */
    public ImageSet(ImageSet other) {

        images = new ArrayList<ImageHolder>();
        images.addAll(other.images);
        markerIndex = other.markerIndex;
        this.parameters = other.parameters;
    }


    /**
     * Adds an Image to the ImageSet using its filename.
     * @param filename  The filename of the Image to add.
     */
    public void addImageWithFilename(String filename) {
        this.images.add(new ImageHolder(null, filename, null));
    }

    /**
     * Adds an Image to the ImageSet using an already constructed Image object and a filename associated with it.
     *
     * This can be used for Images that have already been loaded, in order to keep them associated with some sort of identifier.
     *
     * @param toAdd     The (loaded) Image to add to the ImageSet.
     * @param name      The name to associate with the Image.
     */
    public void addImageWithImageAndName(Image toAdd, String name) {

        ImageHolder imh = new ImageHolder(toAdd, null, null);
        imh.setDisplayName(name);
        this.images.add(imh);
    }

    /**
     * Adds an Image to the ImageSet using an already constructed Image object.
     * @param toAdd     The Image to add to the ImageSet.
     */
    public void addImageWithImage(Image toAdd) {
        this.images.add(new ImageHolder(toAdd, null, null));
    }

    /**
     * Adds an Image to the ImageSet using its omero Id.
     *
     * The necessary information for connecting to the omero server on which the image resides should be in the
     * ParameterDictionary used to construct the ImageSet in the parameters "omero_hostname", "omero_username", and "omero_password".
     *
     * @param omeroId   The ID of the Image to add to the ImageSet.
     */
    public void addImageWithOmeroId(long omeroId) {
        this.images.add(new ImageHolder(null, null, omeroId));
    }

    public void addImageWithOmeroIdAndName(long omeroId, String name) {
        ImageHolder imh = new ImageHolder(null, null, omeroId);
        imh.setDisplayName(name);
        this.images.add(imh);
    }

    /**
     * Gets an Image from the set given its exact filename.
     * @param filename  The filename of the Image to retrieve.
     * @return          The retrieved Image, or null if no Image with this filename is contained in the ImageSet or the Image has not yet been loaded.
     */
    public Image getImageForName(String filename) {
        for (ImageHolder imh : images) {
            if (imh.getFilename() != null && imh.getFilename().equals(filename)) {
                return imh.getImage();
            }
        }

        return null;
    }


    /**
     * Gets an Image from the set whose filename matches the supplied regular expression.
     * @param regexp    The regular expression to match; must follow the syntax in java.util.regex.Pattern.
     * @return          The retrieved Image, or null if no Image with this filename is contained in the ImageSet or the Image has not yet been loaded.
     */
    public Image getImageForNameMatching(String regexp) {
        for (ImageHolder imh : images) {
            if (imh.getFilename() != null && imh.getFilename().matches(regexp)) {
                return imh.getImage();
            }
        }

        return null;
    }

    /**
     * Gets an Image from the set whose omero Id matches the supplied Id.
     * @param id        The omero Id of the Image to retrieve.
     * @return          The retrieved Image, or null if no Image with this id is contained in the ImageSet or the Image has not yet been loaded.
     */
    public Image getImageForOmeroId(long id) {
        for (ImageHolder imh : images) {
            if (imh.getOmeroId() != null && imh.getOmeroId() == id) {
                return imh.getImage();
            }
        }

        return null;
    }

    /**
     * Gets the Image specified as the marker Image.
     *
     * The marker Image might be the one to use for segmentation, for instance.
     *
     * @return  The marker Image, or null if a marker Image has not been specified or it has not yet been loaded.
     */
    public Image getMarkerImage() {
        if (markerIndex == null) {return null;}
        return this.images.get(markerIndex).getImage();
    }

    /**
     * Gets the index of the Image specified as the marker Image.
     *
     * @return  The index of the marker Image in the set, or null if one has not been specified.
     */
    public Integer getMarkerIndex() {
        return this.markerIndex;
    }

    /**
     * Checks if the ImageSet has a marker Image specified.
     * 
     * @return  true if a valid marker Image has been specified; false otherwise.
     */
    public boolean hasMarkerImage() {
        return this.markerIndex != null;
    }

    /**
     * Gets the Image at the specified index.
     *
     * The index refers to the order in which the Image was added to the ImageSet.  (Starts at 0.)
     * 
     *
     * @param index     The index of the Image to return.
     * @return          The retrieved Image, or null if the index was not valid.
     */
    public Image getImageForIndex(int index) {
        try{
            return this.images.get(index).getImage();
        } catch (IndexOutOfBoundsException e) {
            LoggingUtilities.getLogger().warning("Request for Image at index " + index + " in ImageSet was out of bounds.");
            return null;
        }
    }


    /**
     * Specifies that the Image at the provided index is the marker Image.
     *
     * The index refers to the order in which the Image was added to the ImageSet. (Starts at 0.)
     *
     * Supply -1 for the index as a shortcut for the most recently added Image.
     *
     * @param index     The index of the Image to designate as the marker Image.
     */
    public void setMarkerImage(int index) {

        try {
            if (index == -1) {
                this.markerIndex = this.images.size() -1;
                return;
            }

            this.images.get(this.markerIndex);
        
            this.markerIndex = index;
            
        } catch (IndexOutOfBoundsException e) {
            LoggingUtilities.getLogger().warning("Request to set marker image to index " + index + " in ImageSet was out of bounds.");
            this.markerIndex = null;
        }
    }

    /**
     * Gets the number of Images in the ImageSet.
     * @return  The number of Images in the ImageSet.
     */
    public int getImageCount() {
        return this.images.size();
    }

    /**
     * Gets the parameters associated with the ImageSet.
     * @return  A ParameterDictionary containing the analysis parameters.
     */
    public ParameterDictionary getParameters() {
        return this.parameters;
    }


    /**
     * Loads all the Images in the ImageSet.
     *
     * This will ensure that any Images that were added by filename or omero Id are retrieved and stored as Image objects,
     * which can then be processed.
     *
     */
    public void loadAllImages() {

        for (ImageHolder imh : this.images) {

            if (imh.getImage() != null) {
                imh.setDisplayName(imh.getImage().getMetadata().getImageName(0));
                continue;
            }


            if (imh.getOmeroId() != null) {

                OmeroServerImageReader osir = new OmeroServerImageReader();
                try {
                    String[] names = osir.loadImageFromOmeroServer(imh.getOmeroId(), this.parameters.getValueForKey("omero_hostname"), this.parameters.getValueForKey("omero_username"), this.parameters.getValueForKey("omero_password"));
                    //System.out.println(imh.getOmeroId() + " " +  java.util.Arrays.toString(names));
                    imh.setImage(osir.read(names[1]));
                    imh.setDisplayName(names[0]);
                } catch (java.io.IOException e) {
                    LoggingUtilities.getLogger().warning("Unable to load image with id " + imh.getOmeroId() + " from omero server: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (imh.getFilename() != null) {

                ImageReader ir = new ImageReader();

                try {
                    imh.setImage(ir.read(imh.getFilename()));
                    imh.setDisplayName(imh.getFilename());
                } catch (java.io.IOException e) {
                    LoggingUtilities.getLogger().warning("Unable to load image with filename " + imh.getFilename() + ": " + e.getMessage());
                }
            }

            
        }



    }

    /**
     * Gets the display name associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The display name of the requested Image, or null if the Image does not exist.
     */
    public String getImageNameForIndex(int index) {
        if (index >= 0 && index < this.images.size()) {
            return this.images.get(index).getDisplayName();
        } else {
            return null;
        }
    }


    private static class ImageHolder {

        Image theImage;
        String filename;
        Long omeroId;
        String displayName;

        public ImageHolder(Image theImage, String filename, Long omeroId) {
            this.theImage = theImage;
            this.filename = filename;
            this.omeroId = omeroId;
        }

        public void setImage(Image theImage) {this.theImage = theImage;}
        public void setFilename(String filename) {this.filename = filename;}
        public void setOmeroId(Long omeroId) {this.omeroId = omeroId;}
        public void setDisplayName(String displayName) {this.displayName = displayName;}

        public Image getImage(){return this.theImage;}
        public String getFilename(){return this.filename;}
        public Long getOmeroId(){return this.omeroId;}
        public String getDisplayName() {return this.displayName;}

        
    }


    //collection methods

    public boolean contains(Object o) {
        if (! (o.getClass() == this.getImageForIndex(0).getClass())) {
            return false;
        }

        Image o_im = (Image) o;

        for (int i = 0; i < this.images.size(); i++) {

            if (o_im.equals(this.images.get(i).getImage())) {
                return true;
            }
            
        }

        return false;

    }

    public boolean containsAll(Collection<?> c) {

        boolean returnValue = true;

        for (Object o : c) {
            
            returnValue&= this.contains(o);

            if (!returnValue) return returnValue;
        }

        return returnValue;
    }

    public boolean isEmpty() {

        return (this.getImageCount() == 0);
        
    }

    public Iterator<Image> iterator() {

        return null;
        
    }

    public int size() {
        return this.getImageCount();
    }

    public Object[] toArray() {

        this.loadAllImages();
        
        Object[] output = new Object[this.size()];

        for (int i = 0; i < this.size(); i++) {
            output[i] = this.getImageForIndex(i);
        }

        return output;

    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {

        try {

            if (a.length < this.size()) {
                a = (T[]) (new Image[this.size()]);
            }

            for (int i = 0; i < this.size(); i++) {

                a[i] = (T) this.getImageForIndex(i);

            }

            if (a.length > this.size()) {
                a[this.size()] = null;
            }

        } catch (ClassCastException e) {
            throw new ArrayStoreException("Invalid type for array in toArray(T[])");
        }

        return a;

    }



    //unsupported collection methods

    public boolean add(Image e) {
        throw new UnsupportedOperationException("Add not supported for ImageSets.");
    }

    public boolean addAll(Collection<? extends Image> c) {
        throw new UnsupportedOperationException("Add not supported for ImageSets.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Clear not supported for ImageSets.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Remove not supported for ImageSets.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Remove not supported for ImageSets.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Remove not supported for ImageSets.");
    }


}
