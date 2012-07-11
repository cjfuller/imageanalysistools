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

import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
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
	public void apply(WritableImage im) {
		
		LabelFilter LF = new LabelFilter();

		WritableImage copy = ImageFactory.createWritable(im);
		WritableImage leftRegions = ImageFactory.createWritable(im.getDimensionSizes(), 0);
		WritableImage rightRegions = ImageFactory.createWritable(im.getDimensionSizes(), 0);
		WritableImage topRegions = ImageFactory.createWritable(im.getDimensionSizes(), 0);
		WritableImage bottomRegions = ImageFactory.createWritable(im.getDimensionSizes(), 0);
		
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
		
		for (int x = 0; x < im.getDimensionSizes().get(ImageCoordinate.X); x++) {
			for (int y = 0; y < im.getDimensionSizes().get(ImageCoordinate.Y); y++) {
				
				ic.set(ImageCoordinate.X,x);
				ic.set(ImageCoordinate.Y,y);
				
				if (x == 0 || y == 0 || im.getValue(ic) > 0) {
					
					leftRegions.setValue(ic, im.getValue(ic));
					topRegions.setValue(ic, im.getValue(ic));
					
				} else {
					icmod.set(ImageCoordinate.X,x-1);
					icmod.set(ImageCoordinate.Y,y);
					leftRegions.setValue(ic, leftRegions.getValue(icmod));
					
					icmod.set(ImageCoordinate.X,x);
					icmod.set(ImageCoordinate.Y,y-1);
					topRegions.setValue(ic, topRegions.getValue(icmod));
				}
				
			}
		}
		
		for (int x = im.getDimensionSizes().get(ImageCoordinate.X)-1; x >=0; x--) {
			for (int y = im.getDimensionSizes().get(ImageCoordinate.Y)-1; y >=0; y--) {
				
				ic.set(ImageCoordinate.X,x);
				ic.set(ImageCoordinate.Y,y);
				
				if (x == im.getDimensionSizes().get(ImageCoordinate.X)-1 || y == im.getDimensionSizes().get(ImageCoordinate.Y)-1 || im.getValue(ic) > 0) {
					
					rightRegions.setValue(ic, im.getValue(ic));
					bottomRegions.setValue(ic, im.getValue(ic));
					
				} else {
					icmod.set(ImageCoordinate.X,x+1);
					icmod.set(ImageCoordinate.Y,y);
					rightRegions.setValue(ic, rightRegions.getValue(icmod));
					
					icmod.set(ImageCoordinate.X,x);
					icmod.set(ImageCoordinate.Y,y+1);
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
