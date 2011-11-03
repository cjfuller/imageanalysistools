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

package edu.stanford.cfuller.imageanalysistools.frontend;

import ij.ImagePlus;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
import edu.stanford.cfuller.imageanalysistools.method.Method;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

/**
 * Interface to run analysis routines from an ImageJ plugin.
 * 
 * @author Colin J. Fuller
 *
 */
public class IJAnalysis {
	
	ImagePlus toProcess;
	
	public IJAnalysis(){}
	
	/**
	 * Sets the ImagePlus to analyze.
	 * @param input		The ImagePlus to be analyzed.
	 */
	public void setImagePlus(ImagePlus input) {
		
		this.toProcess = input;
		
	}

	/**
	 * Runs the analysis, using the specified parameters and the stored ImagePlus.
	 * @param params	A ParameterDictionary containing the information for the method to run and any parameters needed by that method.
	 * @return			An ImagePlus containing the output image from the method being run.
	 */
    public ImagePlus run(ParameterDictionary params) {
    	
    	Method m = LocalAnalysis.getMethod(params);
    	
    	m.setParameters(params);
    	
    	Image im_nonsplit = new Image(this.toProcess);
    	
    	java.util.List<Image> split = im_nonsplit.splitChannels();
    	
    	params.addIfNotSet("number_of_channels", Integer.toString(split.size()));
    	
    	ImageSet imSet = new ImageSet(params);
    	
    	for (Image i : split) {
    		imSet.addImageWithImage(i);
    	}
    	
    	 if (params.hasKey("marker_channel_index")) {
             int markerIndex = params.getIntValueForKey("marker_channel_index");
             imSet.setMarkerImage(markerIndex);

         }
    	
    	m.setImages(imSet);
    	
    	StatusUpdater su = new ImageJStatusUpdater();
    	
    	su.update(-1,1,"Processing...");
    	
    	m.setStatusUpdater(su);
    	
    	m.run();
    	
    	Image result = m.getStoredImage();
    	
    	if (result != null) {
    	
    		return result.toImagePlus();
    		
    	} else {
    		return null;
    	}
    	
    }

	
}
