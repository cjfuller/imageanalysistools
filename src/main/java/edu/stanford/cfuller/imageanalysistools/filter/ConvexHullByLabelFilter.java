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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.FillFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * A Filter that computes a convex hull for each region in a specified Image and converts each region to its filled convex hull.
 * <p>
 * Each region must be labeled individually, but regions do not have to be contiguous.  Also, I'm not totally sure what will happen with regions
 * that do not overlap but whose convex hulls would overlap.
 * <p>
 * This filter does not require a reference Image.
 * <p>
 * The Image parameter to the apply function should be set to a mask whose regions will be replaced by their filled convex hulls.
 * <p>
 * This filter should only be applied to 2D (x-y) images.  (That is, images with singleton z, c, t dimensions.)
 *
 * @author Colin J. Fuller
 */


public class ConvexHullByLabelFilter extends Filter {

	/*note that in > 2D images, this will probably not do what you expect... I suspect it will most closely
	 * approximate taking the union of the convex hulls in all planes and applying that to the first plane.
	*/

    /**
     * Applies the convex hull filter to the supplied mask.
     * @param im    The Image to process-- a mask whose regions will be replaced by their filled convex hulls.
     */
	@Override
	public void apply(WritableImage im) {
		
		RelabelFilter RLF = new RelabelFilter();

        RLF.apply(im);

		Histogram h = new Histogram(im);
		
		java.util.Hashtable<Integer, java.util.Vector<Integer> > xLists = new java.util.Hashtable<Integer, java.util.Vector<Integer> >();
		java.util.Hashtable<Integer, java.util.Vector<Integer> > yLists = new java.util.Hashtable<Integer, java.util.Vector<Integer> >();
		
		java.util.Vector<Integer> minValues = new java.util.Vector<Integer>(h.getMaxValue() + 1);
		java.util.Vector<Integer> minIndices = new java.util.Vector<Integer>(h.getMaxValue() + 1);

        for (int i =0; i < h.getMaxValue() + 1; i++) {
            minValues.add(im.getDimensionSizes().get(ImageCoordinate.X));
            minIndices.add(0);
        }

		for (ImageCoordinate i : im) {
			
			int value = (int) im.getValue(i);
			
			if (value == 0) continue;
			
			if (!xLists.containsKey(value)) {
				xLists.put(value, new java.util.Vector<Integer>());
				yLists.put(value, new java.util.Vector<Integer>());
			}
			
			xLists.get(value).add(i.get(ImageCoordinate.X));
			yLists.get(value).add(i.get(ImageCoordinate.Y));
			
			if (i.get(ImageCoordinate.X) < minValues.get(value) ) {
				minValues.set(value, i.get(ImageCoordinate.X));
				minIndices.set(value, xLists.get(value).size()-1);
			}
			
		}
		
		java.util.Vector<Integer> hullPointsX = new java.util.Vector<Integer>();
		java.util.Vector<Integer> hullPointsY = new java.util.Vector<Integer>();

		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
		
		for (int k = 1; k < h.getMaxValue() + 1; k++) {
			hullPointsX.clear();
			hullPointsY.clear();
			
			java.util.Vector<Integer> xList = xLists.get(k);
			java.util.Vector<Integer> yList = yLists.get(k);
			
			int minIndex = (int) minIndices.get(k);
			
			//start at the leftmost point
			
			int currentIndex = minIndex;
			int currentX = xList.get(currentIndex);
			int currentY = yList.get(currentIndex);
			
			hullPointsX.add(currentX);
			hullPointsY.add(currentY);
			
			org.apache.commons.math3.linear.RealVector angles = new org.apache.commons.math3.linear.ArrayRealVector(xList.size());
			

            Vector3D currentVector = new Vector3D(0, -1, 0);

            java.util.HashSet<Integer> visited = new java.util.HashSet<Integer>();

			do {

                visited.add(currentIndex);

				int maxIndex = 0;
				double maxAngle = -2*Math.PI;
                double dist = Double.MAX_VALUE;
				for (int i = 0; i < xList.size(); i++) {
					if (i == currentIndex) continue;
                    Vector3D next = new Vector3D(xList.get(i) - xList.get(currentIndex), yList.get(i) - yList.get(currentIndex), 0);
                    
					double angle = Vector3D.angle(currentVector, next);
					angles.setEntry(i, angle);
					if (angle > maxAngle) {
						maxAngle = angle;
						maxIndex = i;
                        dist = next.getNorm();
					} else if (angle == maxAngle) {
                        double tempDist = next.getNorm();
                        if (tempDist < dist) {
                            dist = tempDist;
                            maxAngle = angle;
                            maxIndex = i;
                        }
                    }
				}



				currentX = xList.get(maxIndex);
				currentY = yList.get(maxIndex);

                currentVector = new Vector3D(xList.get(currentIndex) - currentX, yList.get(currentIndex) - currentY, 0);

				hullPointsX.add(currentX);
				hullPointsY.add(currentY);

                currentIndex = maxIndex;
				
			} while (!visited.contains(currentIndex));

			//hull vertices have now been determined .. need to fill in the lines
			//between them so I can apply a fill filter
			
			//approach: x1, y1 to x0, y0:
			//start at min x, min y, go to max x, max y
			// if x_i, y_i = x0, y0  + slope to within 0.5 * sqrt(2), then add to hull
			
			double eps = Math.sqrt(2);
			
			for (int i = 0; i < hullPointsX.size()-1; i++) {
				
				int x0 = hullPointsX.get(i);
				int y0 = hullPointsY.get(i);
				
				int x1 = hullPointsX.get(i+1);
				int y1 = hullPointsY.get(i+1);
				
				int xmin = (x0 < x1) ? x0 : x1;
				int ymin = (y0 < y1) ? y0 : y1;
				int xmax = (x0 > x1) ? x0 : x1;
				int ymax = (y0 > y1) ? y0 : y1;
				
				x1 -= x0;
				y1 -= y0;
								
				double denom = (x1*x1 + y1*y1);
				
				
				for (int x = xmin; x <= xmax; x++) {
					for (int y = ymin; y <= ymax; y++) {
						
						int rel_x = x - x0;
						int rel_y = y - y0;
						
						double projLength = (x1*rel_x + y1*rel_y)/denom; 
						
						double projPoint_x = x1*projLength;
						double projPoint_y = y1*projLength;
						
						if (Math.hypot(rel_x-projPoint_x, rel_y-projPoint_y) < eps) {
							ic.set(ImageCoordinate.X,x);
							ic.set(ImageCoordinate.Y,y);
							im.setValue(ic, k);
						}
						
					}
				}
				
			}
			
		}
		
		ic.recycle();
	
		FillFilter ff = new FillFilter();
		
		ff.apply(im);
				
	}

}
