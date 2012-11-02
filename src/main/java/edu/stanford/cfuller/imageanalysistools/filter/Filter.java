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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.Image;

/**
 * This class represents an Image operation.
 * <p>
 * Classes that extend filter implement the apply method, which takes an Image and modifies it in place according to the
 * operation that that particular filter represents.  Filters may also use a second Image, called the reference image, for additional information
 * during the processing, but should not modify the reference image.  For instance, a filter might take a mask for the Image parameter
 * to the apply function, and operate on this mask, but use the intensity information in a reference image to guide the operation on the mask.
 * <p>
 * Classes that extend filter should document exactly what they expect for the reference image and the Image parameter to the apply function. 
 *
 * @author Colin J. Fuller
 *
 */

public abstract class Filter {

	//fields
	
	protected Image referenceImage;
	protected ParameterDictionary params;
	
	//methods
	
	/**
	 * Constructs a new filter with fields initialized to null.
	 */
	public Filter() {
		this.referenceImage = null;
		this.params = null;
	}

    /**
     * Applies the Filter to the supplied Image.
     * @param im    The Image to process.
     */
	public abstract void apply(WritableImage im);

    /**
     * Sets the reference image for this filter to the specified Image.
     * @param im    The reference Image.
     */
	public void setReferenceImage(Image im){referenceImage = im;}

    /**
     * Sets the parameters for this filter to the specified ParameterDictionary.
     * @param params    The ParameterDictionary used for the analysis.
     */
	public void setParameters(ParameterDictionary params){this.params = params;}
	
}
