/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */


/**
 * A filter that takes a 2D mask containing labeled regions an converts it to a Voronoi diagram.
 * Each region's geometric centroid is used as the point around which to calculated the diagram.
 * The interior of each region is labeled the same as the region used to generate it.  Edges are
 * filled with zero.  Edges may be slightly wider than expected, in order that pixels
 * in adjacent regions are not 8-connected (so that a LabelFilter will not merge them).
 * <p>
 * The argument to the apply method should be the Image with labeled regions.
 * <p>
 * This filter does not use a reference Image.
 * 
 * @author Colin J. Fuller
 *
 */
package edu.stanford.cfuller.imageanalysistools.filter;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;


import edu.stanford.cfuller.imageanalysistools.util.VoronoiDiagram;

import java.util.Map;


public class VoronoiFilter extends Filter {
	
	
	/**
     * Applies the Filter to the supplied Image.
     * @param im    The Image to whose labeled region will be converted to a Voronoi diagram.
     */
	@Override
	public void apply(WritableImage im) {
				
		Map<Integer, Vector2D> regionCentroids = new java.util.HashMap<Integer, Vector2D>();
		
		for (ImageCoordinate ic : im) {
			
			int label = (int) im.getValue(ic);
			
			if (label == 0) continue;
			
			if (! regionCentroids.containsKey(label) ) {
				regionCentroids.put(label, new Vector2D(0,0));
			}
			
			regionCentroids.put(label, regionCentroids.get(label).add(new Vector2D(ic.get(ImageCoordinate.X), ic.get(ImageCoordinate.Y))));
			
			
		}
		
		Histogram h = new Histogram(im);
		
		java.util.List<Integer> regions = new java.util.ArrayList<Integer>();
		
		
		for (Integer i : regionCentroids.keySet()) {
			
			regionCentroids.put(i, regionCentroids.get(i).scalarMultiply(1.0/h.getCounts(i)));
			regions.add(i);
			
		}
		
		java.util.Collections.sort(regions);
		
		java.util.List<Vector2D> orderedCentroids = new java.util.ArrayList<Vector2D>();
		
		for (Integer i : regions) {
			orderedCentroids.add(regionCentroids.get(i));
		}

		WritableImage regionCentroidsIm = ImageFactory.createWritable(im.getDimensionSizes(), 0.0f);

		WritableImage diagram = ImageFactory.createWritable(im.getDimensionSizes(), 1.0f);

		ImageCoordinate cenTemp = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

		for (Integer i : regions) {

			Vector2D cen = regionCentroids.get(i);

			cenTemp.set(ImageCoordinate.X, (int) cen.getX());
			cenTemp.set(ImageCoordinate.Y, (int) cen.getY());

			regionCentroidsIm.setValue(cenTemp, i);

		}

		cenTemp.recycle();

		for (ImageCoordinate ic : diagram) {

			if (isOnEightConnectedBoundary(orderedCentroids, ic)) {

				diagram.setValue(ic, 0.0f);

			}

		}

		LabelFilter lf = new LabelFilter();
		OneToOneLabelBySeedFilter oolbsf = new OneToOneLabelBySeedFilter();

		lf.apply(diagram);

		oolbsf.setReferenceImage(regionCentroidsIm);

		oolbsf.apply(diagram);

		im.copy(diagram);
		
	}

	/**
	* Checks whether a given coordinate would be on a boundary of a 
	* Voronoi diagram created from the given points.
	*
	**/
	public boolean isOnEightConnectedBoundary(java.util.List<Vector2D> points, ImageCoordinate ic) {

		int x = ic.get(ImageCoordinate.X);
		int y = ic.get(ImageCoordinate.Y);

		int closestIndex = 0;
		int nextIndex = 0;
		double closestDist = Double.MAX_VALUE;
		double nextDist = Double.MAX_VALUE;

		for (int i = 0; i < points.size(); i++) {

			Vector2D pt = points.get(i);

			double dist = Math.hypot(pt.getX() - x, pt.getY() - y);

			if (dist < closestDist) {

				nextDist = closestDist;
				nextIndex = closestIndex;
				closestDist = dist;
				closestIndex = i;

			} else if (dist < nextDist) {
				nextDist = dist;
				nextIndex = i;
			}

		}

		Vector2D projectedCoordinate = this.projectPointOntoVector(points.get(closestIndex), new Vector2D(x, y), points.get(nextIndex));

		double distToNext = points.get(nextIndex).subtract(projectedCoordinate).getNorm();

		double distToClosest = points.get(closestIndex).subtract(projectedCoordinate).getNorm();

		final double cutoff = 1.3*Math.sqrt(2);

		if (distToNext - distToClosest < cutoff) {
			return true;
		}

		return false;

	}
	
	protected Vector2D projectPointOntoVector(Vector2D origin, Vector2D pointToProject, Vector2D pointOnVector) {
		Vector2D onto = pointOnVector.subtract(origin).normalize();
		Vector2D toProj = pointToProject.subtract(origin);
		Vector2D projected = origin.add(onto.scalarMultiply(onto.dotProduct(toProj)));
		return projected;
	}

	private boolean isOnEightConnectedBoundary(ImageCoordinate ic, Image noBoundaryLabels) {
		
		ImageCoordinate icClone = ImageCoordinate.cloneCoord(ic);
		
		float value = noBoundaryLabels.getValue(ic);
		
		for (int x_off = -1; x_off <= 1; x_off++) {
			for (int y_off = -1; y_off <= 1; y_off++) {
				icClone.set(ImageCoordinate.X, ic.get(ImageCoordinate.X) + x_off);
				icClone.set(ImageCoordinate.Y, ic.get(ImageCoordinate.Y) + y_off);
				if (noBoundaryLabels.inBounds(icClone)) {
					float newvalue = noBoundaryLabels.getValue(icClone);
					if (newvalue != value) {
						icClone.recycle();
						return true;
					}
				}
			}
		}
		icClone.recycle();
		return false;
		
	}
	
}
