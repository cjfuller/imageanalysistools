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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A Filter that applies a mask to an Image.  A mask is any Image that has nonzero pixel values in regions and zero pixel
 * values elsewhere.
 * <p>
 * The reference Image should be set to the mask that will be applied.  This will not be modified.
 * <p>
 * The argument to the apply method should be the Image that will be masked by the reference Image.  The masking operation will leave
 * the Image unchanged except for setting to zero any pixel that has a zero value in the mask.
 *
 * @author Colin J. Fuller
 */
public class MaskFilter extends Filter {


    /**
     * Applies the MaskFilter to a given Image.
     * @param im    The Image that will be masked by the reference Image.
     */
	@Override
	public void apply(Image im) {

		for (ImageCoordinate c : im) {
			if (this.referenceImage.getValue(c) == 0) {
				im.setValue(c, 0);
			}
		}
		
	}

}
