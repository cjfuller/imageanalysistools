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

package edu.stanford.cfuller.imageanalysistools.metric;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import org.apache.commons.math.linear.RealMatrix;

/**
 * A quantitative measurement on an image or set of images with an associated mask of regions of interest.
 *
 * @author Colin J. Fuller
 */

public abstract class Metric {


    /**
     * Quantifies the Images.
     *
     * This should always return a {@link RealMatrix} with one row per region of interest, where each column in a row corresponds to a
     * scalar quantification of some property of that region of interest for a given input image.
     *
     * Thus, the size of the returned matrix should be (# of regions of interest) x (images.size()).
     *
     * @param mask      A mask that specifies the region of interest.  Subclasses should specify whether this should be binary or
     *                  have a unique identifier for each region of interest.
     * @param images    A Vector of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * @return          A RealMatrix containing all the quantified values; rows correspond to ROIs, columns to input Images.
     */
	public abstract RealMatrix quantify(Image mask, java.util.Vector<Image> images);
	
}
