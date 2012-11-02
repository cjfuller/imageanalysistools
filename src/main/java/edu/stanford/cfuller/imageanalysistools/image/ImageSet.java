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

package edu.stanford.cfuller.imageanalysistools.image;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerImageReader;
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerInfo;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.util.FileHashCalculator;

import java.util.*;

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

    final static int defaultMarkerIndex = 0;
    
    List<ImageHolder> images;
	
	Image combine;

    Integer markerIndex;

    ParameterDictionary parameters;

    /**
     * Constructs a new, empty ImageSet.
     *
     * @param p     The parameters for the analysis.
     */
    public ImageSet(ParameterDictionary p) {
        this.images = new ArrayList<ImageHolder>();
        this.markerIndex = null;
        this.parameters = p;
		this.combine = null;
    }

    /**
     * Copy constructor.
     *
     * Does not make copies of the Images; this method is primarily useful to allow different marker Images or simultaneous iteration.
     *
     * @param other     The ImageSet to copy.
     */
    public ImageSet(ImageSet other) {

        this.images = new ArrayList<ImageHolder>();
        this.images.addAll(other.images);
        this.markerIndex = other.markerIndex;
        this.parameters = other.parameters;
		this.combine = other.combine;
    }


	/**
	* Stores an Image that is the combine of each image in this set along the appropriate dimension.
	*  (e.g. a color combine if all images represent channels)
	* @param combined the Image that is the combine of the images in the ImageSet.
	*/ 
	public void setCombinedImage(Image combined) {
		this.combine = combined;
	}
	
	/**
	* Gets a stored Image that is the combine of each image in this set along the appropriate dimension if it has been previously supplied.
	*  (e.g. a color combine if all images represent channels)
	* @return the combine of the Images in this set, if it has been set, or null if it has not.
	*/ 
	public Image getCombinedImage() {
		return this.combine;
	}


    /**
     * Adds an Image to the ImageSet using its filename.
     * @param filename  The filename of the Image to add.
     */
    public void addImageWithFilename(String filename) {
        ImageHolder newImage = new ImageHolder(null, filename, null);
        newImage.setDisplayName(filename);
        this.images.add(newImage);
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
     * Gets the filename of the marker Image.
     * 
     * @return	the filename, or null if there is no marker image.
     */
    public String getMarkerImageName() {
    	if (markerIndex == null) {return null;}
        return this.images.get(markerIndex).getFilename();
    	
    }
    
    /**
     * Gets the Image specified as the marker Image.  If one has not been specified,
     * the zeroth image in the set will be chosen as the default and returned.
     *
     * The marker Image might be the one to use for segmentation, for instance.
     *
     * @return  The marker Image, or an Image selected as the default.
     */
    public Image getMarkerImageOrDefault() {
    	if (markerIndex == null) {return this.images.get(defaultMarkerIndex).getImage();}
    	return getMarkerImage();
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

            this.markerIndex = index;

            this.images.get(this.markerIndex);
        

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
    public void loadAllImages() throws java.io.IOException {

        for (ImageHolder imh : this.images) {

            if (imh.getImage() != null) {
				imh.setImageHash("", "");
                if (imh.getDisplayName() == null) {
                    imh.setDisplayName(imh.getImage().getMetadata().getImageName(0));
                }
                continue;
            }


            if (imh.getOmeroId() != null) {

                OmeroServerImageReader osir = new OmeroServerImageReader();
                
                OmeroServerInfo osi = new OmeroServerInfo(this.parameters.getValueForKey("omero_hostname"), this.parameters.getValueForKey("omero_username"), this.parameters.getValueForKey("omero_password").toCharArray());

                String[] names = osir.loadImageFromOmeroServer(imh.getOmeroId(), osi);
                //System.out.println(imh.getOmeroId() + " " +  java.util.Arrays.toString(names));
                imh.setImage(osir.read(names[1]));
                imh.setDisplayName(names[0]);


            } else if (imh.getFilename() != null) {

                ImageReader ir = new ImageReader();

                imh.setImage(ir.read(imh.getFilename()));
                imh.setDisplayName(imh.getFilename());

            }


        }

		this.hashAllImages();



    }

    /**
     * Gets the display name associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The display name of the requested Image, or null if the Image does not exist.
     */
    public String getImageNameForIndex(int index) {
        if (index >= 0 && index < this.size()) {
            return this.images.get(index).getDisplayName();
        } else {
            return null;
        }
    }

	/**
     * Gets the image filename associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The filename of the requested Image, or null if the Image does not exist.
     */
    public String getFilenameForIndex(int index) {
        if (index >= 0 && index < this.size()) {
            return this.images.get(index).getFilename();
        } else {
            return null;
        }
    }

	/**
     * Gets the file hash associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The value of the hash associated with the image, or null if the Image does not exist.
     */
	public String getImageHashForIndex(int index) {
		if (index >= 0 && index < this.size()) {
		    return this.images.get(index).getHash();
		} else {
		    return null;
		}
	}
	
	/**
     * Gets the algorithm used to hash the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The hash algorithm associated with the image, or null if the Image does not exist.
     */
	public String getImageHashAlgorithmForIndex(int index) {
		if (index >= 0 && index < this.size()) {
		    return this.images.get(index).getHashAlgorithm();
		} else {
		    return null;
		}
	}

	/**
     * Disposes of the memory-intensive portions of Images.
     *
     * Useful for programs that retain long-term references to the ImageSet for things like naming, but don't need
     * the pixel data for the full lifetime of the ImageSet.
     *
     */
    public void disposeImages() {
        for (ImageHolder imh : this.images) {
            imh.setImage(null);
        }
    }

	/**
	* Calculates the hash of all the Images in the image set.  For images added
	* from files, this will calculate an actual hash of the file.  For omero images,
	* this will produce something based on the server and omero id.  For added Image 
	* objects, this will do nothing.
	*/
	public void hashAllImages() {
		for (ImageHolder imh : this.images) {
			if (imh.getFilename() != null) {
				try {
					imh.setImageHash(FileHashCalculator.ALG_DEFAULT, FileHashCalculator.calculateHash(FileHashCalculator.ALG_DEFAULT, imh.getFilename()));
				} catch (java.io.IOException e) {
					edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().warning("Unable to calculate hash on image: " + imh.getFilename() + "\n" + e.getMessage());
				}
			} else if (imh.getOmeroId() != null) {
				imh.setImageHash("omero://" + this.parameters.getValueForKey("omero_hostname"), Long.toString(imh.getOmeroId()));
			}
		}
		
	}


    protected static class ImageHolder {

        Image theImage;
        String filename;
        Long omeroId;
        String displayName;

		String hashAlgorithm;
		String hash;

        public ImageHolder(Image theImage, String filename, Long omeroId) {
            this.theImage = theImage;
            this.filename = filename;
            this.omeroId = omeroId;
            this.displayName = null;
        }

        public void setImage(Image theImage) {this.theImage = theImage;}
        public void setFilename(String filename) {this.filename = filename;}
        public void setOmeroId(Long omeroId) {this.omeroId = omeroId;}
        public void setDisplayName(String displayName) {this.displayName = displayName;}
		public void setImageHash(String algorithm, String hash) {this.hashAlgorithm = algorithm; this.hash = hash;}

        public Image getImage(){return this.theImage;}
        public String getFilename(){return this.filename;}
        public Long getOmeroId(){return this.omeroId;}
        public String getDisplayName() {return this.displayName;}
		public String getHashAlgorithm() {return this.hashAlgorithm;}
		public String getHash() {return this.hash;}

        
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

        return new ImageSetIterator(this);
        
    }

    public int size() {
        return this.getImageCount();
    }

    public Object[] toArray() {

        try {

            this.loadAllImages();

        } catch (java.io.IOException e) {
            LoggingUtilities.getLogger().warning("Unable to load images for filename: " + this.images.get(0).getFilename() + " or omero id: " + this.images.get(0).getOmeroId() + " " +  e.getMessage());
            e.printStackTrace();
        }

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


    protected class ImageSetIterator implements Iterator<Image> {

        ImageSet toIterate;
        int currIndex;

        protected ImageSetIterator() {}

        public ImageSetIterator(ImageSet toIterate) {
            this.toIterate = toIterate;
            this.currIndex = 0;
        }

        public boolean hasNext() {
            if (this.currIndex < toIterate.size()) return true;
            return false;
        }

        public Image next() {
            if (currIndex >= toIterate.size()) {throw new NoSuchElementException("No more Images in ImageSet.  Attempted to access Image at index " + currIndex + ".");}
            Image toReturn = toIterate.getImageForIndex(currIndex);
            currIndex++;
            return toReturn;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for iteration over an ImageSet.");
        }
        
    }

}
