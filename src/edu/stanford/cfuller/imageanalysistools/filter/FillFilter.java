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
import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter;

/**
 * A Filter that fills in gaps in regions in a labeled mask.  This will fill holes in regions only if for each pixel in a hole
 * the closest region in the +x, -x, +y, and -y directions is the same, and is the same region for every pixel in that hole.
 * It will not fill holes extending to the edge of an Image.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the mask whose gaps will be filled.
 *
 * @author Colin J. Fuller
 */

public class FillFilter extends Filter {

	//TODO: refactor project to call this FillFilter2D.
	
	//TODO: remove explicit reference to dimensions not required for this filter.

    /**
     * Apply the Filter, filling in any gaps in the supplied mask.
     * @param im    The Image that is a mask whose regions will have any holes filled in.
     */
	@Override
	public void apply(Image im) {
		
		LabelFilter LF = new LabelFilter();

		Image copy = new Image(im);
		Image leftRegions = new Image(im.getDimensionSizes(), 0);
		Image rightRegions = new Image(im.getDimensionSizes(), 0);
		Image topRegions = new Image(im.getDimensionSizes(), 0);
		Image bottomRegions = new Image(im.getDimensionSizes(), 0);

		
		
		for (ImageCoordinate c : copy) {
			if (copy.getValue(c) == 0) {
				copy.setValue(c, 1);
			} else {
				copy.setValue(c, 0);
			}
		}
		
		
		//traverse the directional regions manually to ensure defined pixel order
		
		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		ImageCoordinate icmod = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		
		for (int x = 0; x < im.getDimensionSizes().get("x"); x++) {
			for (int y = 0; y < im.getDimensionSizes().get("y"); y++) {
				
				ic.set("x",x);
				ic.set("y",y);
				
				if (x == 0 || y == 0 || im.getValue(ic) > 0) {
					
					leftRegions.setValue(ic, im.getValue(ic));
					topRegions.setValue(ic, im.getValue(ic));
					
				} else {
					icmod.set("x",x-1);
					icmod.set("y",y);
					leftRegions.setValue(ic, leftRegions.getValue(icmod));
					
					icmod.set("x",x);
					icmod.set("y",y-1);
					topRegions.setValue(ic, topRegions.getValue(icmod));
				}
				
			}
		}
		
		for (int x = im.getDimensionSizes().get("x")-1; x >=0; x--) {
			for (int y = im.getDimensionSizes().get("y")-1; y >=0; y--) {
				
				ic.set("x",x);
				ic.set("y",y);
				
				if (x == im.getDimensionSizes().get("x")-1 || y == im.getDimensionSizes().get("y")-1 || im.getValue(ic) > 0) {
					
					rightRegions.setValue(ic, im.getValue(ic));
					bottomRegions.setValue(ic, im.getValue(ic));
					
				} else {
					icmod.set("x",x+1);
					icmod.set("y",y);
					rightRegions.setValue(ic, rightRegions.getValue(icmod));
					
					icmod.set("x",x);
					icmod.set("y",y+1);
					bottomRegions.setValue(ic, bottomRegions.getValue(icmod));
				}
				
			}
		}
		
		ic.recycle();
		icmod.recycle();
		
		LF.apply(copy);

		java.util.HashMap<Integer, Integer> voidsToRegions = new java.util.HashMap<Integer, Integer>();
		
		java.util.HashSet<Integer> voidsNotToFill = new java.util.HashSet<Integer>();
		
		for (ImageCoordinate c : copy) {
			
			if (copy.getValue(c) != 0) {
				
				if (voidsNotToFill.contains(copy.getValue(c))) {
					continue;
				}
				
				int leftRegion = (int) leftRegions.getValue(c);
				int rightRegion = (int) rightRegions.getValue(c);
				int topRegion = (int) topRegions.getValue(c);
				int bottomRegion = (int) bottomRegions.getValue(c);
								
				if (!(topRegion > 0 && bottomRegion > 0 && leftRegion > 0 && rightRegion > 0 && topRegion == bottomRegion && leftRegion == rightRegion && leftRegion == topRegion)) {
					// if it's on an image border or touches multiple regions, don't fill
					voidsNotToFill.add((int) copy.getValue(c));
				} else if (!voidsToRegions.containsKey((int) copy.getValue(c)) || voidsToRegions.get((int) copy.getValue(c)) == topRegion) {
					//if it's surrounded by a single region and we haven't seen it before, or we've seen it before surrounded by the same region, ok to fill so far
					voidsToRegions.put((int) copy.getValue(c), topRegion);
				} else {
					//if we've seen it before surrounded by a different region, don't fill
					voidsNotToFill.add((int) copy.getValue(c));
				}
				
				
			}
			
		}
		
		for (ImageCoordinate c : copy) {
			if (im.getValue(c) == 0 && !voidsNotToFill.contains((int) copy.getValue(c))) {
				im.setValue(c, voidsToRegions.get((int) copy.getValue(c)));
			}
		}
		
	}

}
