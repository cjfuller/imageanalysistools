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
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.FillFilter;
import org.apache.commons.math.geometry.Vector3D;

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
	public void apply(Image im) {
		
		RelabelFilter RLF = new RelabelFilter();

        RLF.apply(im);

		Histogram h = new Histogram(im);
		
		java.util.Hashtable<Integer, java.util.Vector<Integer> > xLists = new java.util.Hashtable<Integer, java.util.Vector<Integer> >();
		java.util.Hashtable<Integer, java.util.Vector<Integer> > yLists = new java.util.Hashtable<Integer, java.util.Vector<Integer> >();
		
		java.util.Vector<Integer> minValues = new java.util.Vector<Integer>(h.getMaxValue() + 1);
		java.util.Vector<Integer> minIndices = new java.util.Vector<Integer>(h.getMaxValue() + 1);

        for (int i =0; i < h.getMaxValue() + 1; i++) {
            minValues.add(im.getDimensionSizes().getX());
            minIndices.add(0);
        }

		for (ImageCoordinate i : im) {
			
			int value = (int) im.getValue(i);
			
			if (value == 0) continue;
			
			if (!xLists.containsKey(value)) {
				xLists.put(value, new java.util.Vector<Integer>());
				yLists.put(value, new java.util.Vector<Integer>());
			}
			
			xLists.get(value).add(i.getX());
			yLists.get(value).add(i.getY());
			
			if (i.getX() < minValues.get(value) ) {
				minValues.set(value, i.getX());
				minIndices.set(value, xLists.get(value).size()-1);
			}
			
		}
		
		java.util.Vector<Integer> hullPointsX = new java.util.Vector<Integer>();
		java.util.Vector<Integer> hullPointsY = new java.util.Vector<Integer>();

		ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
		
		for (int k = 1; k < h.getMaxValue() + 1; k++) {
			hullPointsX.clear();
			hullPointsY.clear();
			
			java.util.Vector<Integer> xList = xLists.get(k);
			java.util.Vector<Integer> yList = yLists.get(k);
			
			int minX = (int) minValues.get(k);
			int minIndex = (int) minIndices.get(k);
			
			//start at the leftmost point
			
			int currentIndex = minIndex;
			int currentX = xList.get(currentIndex);
			int currentY = yList.get(currentIndex);
			
			hullPointsX.add(currentX);
			hullPointsY.add(currentY);
			
			org.apache.commons.math.linear.RealVector angles = new org.apache.commons.math.linear.ArrayRealVector(xList.size());
			

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

            //System.out.println(angles.toString());

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
							ic.setX(x);
							ic.setY(y);
							im.setValue(ic, k);
						}
						
					}
				}
				
			}
			
			
			
			
		}
		
		ic.recycle();
			/*

			ImageCoordinate ic = ImageCoordinate.createCoord(0, 0, 0, 0, 0);
			
			for (int n = 0; n < iList.size(); n++) {
				int c_i = iList.get(n);
				int c_j = jList.get(n);
				ic.setX(c_i);
				ic.setY(c_j);
				int ind = (int) this.referenceImage.getValue(ic);
				
				//find two nearest neighbors
				
				int best_i = 0;
				int best_j = 0;
				double bestDist = Double.MAX_VALUE;
				int bestInd = 0;
				int best2_i = 0;
				int best2_j = 0;
				double best2Dist = Double.MAX_VALUE;
				int best2Ind = 0;
				
				for (int m = 0; m < iList.size(); m++) {
					
					if (m == n) continue;
					
					double dist_2 = Math.pow(iList.get(m) - c_i, 2) + Math.pow(jList.get(m) - c_j, 2);
					
					ic.setX(iList.get(m));
					ic.setY(jList.get(m));
					
					int candidateInd = (int) this.referenceImage.getValue(ic);
					
					if (candidateInd == ind) continue;
					
					if (dist_2 < bestDist && candidateInd != bestInd && candidateInd != best2Ind) {
						best2Dist = bestDist;
						bestDist = dist_2;
						best2_i = best_i;
						best2_j = best_j;
						best_i = iList.get(m);
						best_j = jList.get(m);
						best2Ind = bestInd;
						bestInd = candidateInd;
					} else if (dist_2 < best2Dist && candidateInd != bestInd && candidateInd != best2Ind) {
						best2Dist = dist_2;
						best2_i = iList.get(m);
						best2_j = jList.get(m);
						best2Ind = candidateInd;
					}
					
				}
				
				//draw triangle with two nearest neighbors unless the distance to the nearest neighbors is large compared to the size of the image
				
				if (Math.sqrt(best2Dist) < 0.4 * im.getDimensionSizes().getX() && Math.sqrt(best2Dist) < 0.4 * im.getDimensionSizes().getY()) {
					
					int min_i = (c_i < best_i)? c_i : best_i;
					min_i = (min_i < best2_i)? min_i : best2_i;
					
					int min_j = (c_j < best_j)? c_j : best_j;
					min_j = (min_j < best2_j)? min_j : best2_j;
					
					int max_i = (c_i > best_i)? c_i : best_i;
					max_i = (max_i > best2_i)? max_i : best2_i;
					
					int max_j = (c_j > best_j)? c_j : best_j;
					max_j = (max_j < best2_j)? max_j : best2_j;
					
				
				
					for (int i = min_i; i < max_i; i++) {
						for (int j = min_j; j < max_j; j++) {
							
							
								
							if (Math.abs(j - (c_j + (c_j - best_j)*1.0/(c_i - best_i) * (i - c_i))) <= 0.5 
									 || Math.abs(i - (c_i + (c_i - best_i)*1.0/(c_j - best_j) * (j - c_j))) <= 0.5
									 ||	Math.abs(j - (c_j + (c_j - best2_j)*1.0/(c_i - best2_i) * (i - c_i))) <= 0.5 
									 || Math.abs(i - (c_i + (c_i - best2_i)*1.0/(c_j - best2_j) * (j - c_j))) <= 0.5
									 || Math.abs(j - (best_j + (best_j - best2_j)*1.0/(best_i - best2_i) * (i - best_i))) <= 0.5 
									 || Math.abs(i - (best_i + (best_i - best2_i)*1.0/(best_j - best2_j) * (j - best_j))) <= 0.5) {
										
								ic.setX(i);
								ic.setY(j);
										
								im.setValue(ic, k);
																	
							}
								
						}
					}
				
				}
								
			}
			
		}
		*/
				
		FillFilter ff = new FillFilter();
		
		ff.apply(im);
				
	}

}
