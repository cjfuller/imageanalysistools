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

package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;
import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import org.apache.commons.math.linear.RealMatrix;

/**
 * An analysis method that can be run as an independent thread of execution.
 *
 * @author Colin J. Fuller
 */


public abstract class Method extends Thread{
	
	//fields
	
	protected ParameterDictionary parameters;
	protected java.util.Vector<Image> storedImages;
	protected java.util.Vector<Image> images;
    protected ImageSet imageSet;
	protected org.apache.commons.math.linear.RealMatrix storedDataOutput;
	
	//protected methods


    /**
     * Runs through a sequence of Filters and applies them successively to an input image; quantifies images according to the
     * supplied metric and the mask that results from application of the filters.
     * 
     * @param filters       The List of Filters to apply to the image, in order.
     * @param toProcess     The Image that will be processed by the Filters; may be overwritten during the filtering process.
     * @param m             The Metric that will be used to quantify the Images.
     */
	protected void iterateOnFiltersAndStoreResult(java.util.List<Filter> filters, Image toProcess, Metric m) {
		int c = 0;
        if (Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
//		    toProcess.writeToFile("/Users/cfuller/Desktop/filter_intermediates/" + Integer.toString(c++) + ".ome.tif");
        }
		for (Filter f : filters) {
			
			f.apply(toProcess);
            if (Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
//			   toProcess.writeToFile("/Users/cfuller/Desktop/filter_intermediates/" + Integer.toString(c++) + ".ome.tif");
            }
		}
		if (m != null) {
			this.storedDataOutput = m.quantify(toProcess, this.images);
		}
		this.storeImageOutput(new Image(toProcess));
		
	}

    /**
     * Stores an Image to an internal list of Images that can later be written to disk.
     * @param im    The Image to add to the list of output Images.
     */
	protected void storeImageOutput(Image im) {this.storedImages.add(im);}

    /**
     * Clears all Images that have been stored for output.
     */
    protected void clearImageOutput() {this.storedImages.clear();}
	

    /**
     * Runs the analysis method.
     */
	public abstract void go();

    /**
     * Default constructor; sets up storage for the Images to be processed/quantified as well as for the output Images.
     */
	public Method() {
		this.images = new java.util.Vector<Image>();
		this.storedImages = new java.util.Vector<Image>();
	}

    /**
     * Gets the first in the list of stored output Images.
     *
     * Will not remove the image from the list, so successive calls
     * can return the same Image.
     *
     * @return  The first Image in the list of stored output Images, or null if no Images have been stored.
     */
	public Image getStoredImage() {return (this.storedImages.size() > 0) ? this.storedImages.get(0) : null;}

    /**
     * Gets the entire list of stored output Images.
     *
     * If no Images have been stored, this method will return an empty list.  Classes extending Method should retain this behavior
     * so that the users can iterate over the returned (potentially empty) list without having to check for null or
     * special values.
     *
     * @return  The entire list of stored output Images (may be empty).
     */
	public java.util.List<Image> getStoredImages() {return this.storedImages;}

    /**
     * Sets the parameters for this method to those found in a supplied {@link ParameterDictionary}.
     *
     * This retains a reference to the ParameterDictionary and does not copy it, so external changes to the ParameterDictionary
     * will be reflected in the stored one as well.
     *
     * @param params    The ParameterDictionary containing the parameters for running this method.
     */
	public void setParameters(ParameterDictionary params){this.parameters = params;}


    /**
     * Sets the Images to be quantified/processed for this method.
     *
     * If there is one Image that is to receive special treatment (for example, one color channel to be segmented, and this
     * used to quantify all the channels), then that Image should be the first in the supplied List.
     *
     * This method will not overwrite any Images already passed using prior calls this method, but rather append the supplied
     * Images to the list of those already passed in.
     *
     * @param images    The List of images to be processed/quantified.
     */
	public void setImages(ImageSet images) {

        Integer marker = images.getMarkerIndex();

        if (marker != null) {
            this.images.add(images.getMarkerImage());

            for (int i = 0; i < images.size(); i++) {
                if (i == marker) continue;
                this.images.add(images.getImageForIndex(i));

            }
        } else {

            this.images.addAll(images);
        }

        this.imageSet = images;

    }

    /**
     * Gets the stored ParameterDictionary used for this method.
     *
     * This will get a reference, not a copy, so modifications will be reflected in the stored ParameterDictionary.
     *
     * @return      A reference to the ParameterDictionary used for this method.
     */
	public ParameterDictionary getParameters() {return this.parameters;}


    /**
     * Gets the stored quantification data generated by this method.
     * @return  A RealMatrix containing the quantification data, in the format specified by {@link Metric}.
     */
    public RealMatrix getStoredDataOutput() {
        return storedDataOutput;
    }

    /**
     * Method called when the thread is started.
     */
    @Override
    public void run() {
        this.go();
    }

}
