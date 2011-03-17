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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.image.Image;

/**
 * This class represents an Image operation.
 * <p>
 * Classes that extend filter implement the apply method, which takes an Image and modifies it in place according to the
 * operation that that particular filter represents.  Filters may also use a second Image, called the reference image, for additional information
 * during the processing, but should not modify the reference image.  For instance, a filter might take a mask for the Image parameter
 * to the apply function, and operate on this mask, but use the intensity information in a reference image to guide the operation on the mask.
 * <p>
 * In general, classes that extend filter should document exactly what they expect for the reference image and the Image parameter to the apply function. 
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
     * Applies the Filter to the supplied Image.
     * @param im    The Image to process.
     */
	public abstract void apply(Image im);

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
