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

import org.apache.commons.math.linear.RealMatrix;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A simple Metric that quantifies the total intensity in a region of interest divided by the size of that region of interest.
 * <p>
 * A side note on using intensity per pixel as a metric rather than integrated intensity:
 * <p>
 * When observing objects much smaller than the diffraction limit of light, and increase in brightness of a point source will appear
 * almost the same as multiple point sources of equivalent total brightness close together in that same diffraction-limited volume.  For this reason,
 * the apparent size of objects below the diffraction limit is often solely dependent on the brightess of objects, and not their
 * actual size.  Thus, for an Image segmentation method that may not attempt to compensate for objects of differing brightness, which
 * then quantified on integrated intensity would effectively be multiplying differences in intensity: brighter objects would
 * contain more pixels (as they would have larger apparent size), and these pixels would each be brighter.  Measuring intensity per
 * pixel negates this effect.  This is even more effective when combined with something like the {@link edu.stanford.cfuller.imageanalysistools.filter.RenormalizationFilter},
 * which tries to locally intensity normalize an image before segmentation, so that differences in brightness will not by themselves
 * lead to changes in apparent object size.
 *
 *
 * @author Colin J. Fuller
 */


public class IntensityPerPixelMetric extends Metric {


    /**
     * Quantifies the (area) average intensity for each region of interest in an image.
     *
     * If no regions of interest are present in the supplied mask, this will return null.
     *
     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1,
     *                  as might be produced by a {@link edu.stanford.cfuller.imageanalysistools.filter.LabelFilter}.
     * @param images    A Vector of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * @return          A RealMatrix containing the average intensity value for each region of interest in each input Image.  The (i,j)th
     *                  entry will contain the quantification of ROI (i+1) in Image j.
     */
	@Override
	public RealMatrix quantify(Image mask, java.util.Vector<Image> images) {
		
		edu.stanford.cfuller.imageanalysistools.image.Histogram h = new edu.stanford.cfuller.imageanalysistools.image.Histogram(mask);
		
		if (h.getMaxValue() == 0) return null;
		
		RealMatrix channelIntensities = (new org.apache.commons.math.linear.Array2DRowRealMatrix(images.size()+1, h.getMaxValue())).scalarMultiply(0);
		
		for (ImageCoordinate i : mask) {
			int regionNum = (int) mask.getValue(i);
			
			if (regionNum > 0) {
				for (int c = 0; c < images.size(); c++) {
					channelIntensities.addToEntry(c, regionNum-1, images.get(c).getValue(i));
				}
			}
		}
		
		for (int i = 0; i < h.getMaxValue(); i++) {
			channelIntensities.setEntry(images.size(), i, h.getCounts(i+1));
			for (int c = 0; c < images.size(); c++) {
				channelIntensities.setEntry(c, i, channelIntensities.getEntry(c,i)/h.getCounts(i+1));
			}
		}
		
		
		return channelIntensities.transpose();
	}

}
