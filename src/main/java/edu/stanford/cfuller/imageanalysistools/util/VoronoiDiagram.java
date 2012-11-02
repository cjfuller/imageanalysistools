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

package edu.stanford.cfuller.imageanalysistools.util;

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

import java.util.List;
import java.util.Map;

import java.util.Comparator;

/**
* This class represents a 2D Voronoi Diagram and the methods to construct one.  It
* takes a series of points, constructs the diagram, and assigns each region in the diagram
* a unique label.
* 
* @author Colin J. Fuller
**/
public class VoronoiDiagram {
	
	
	private static class PointPair implements Comparable<PointPair> {
		
		Vector2D first;
		Vector2D second;
		
		public PointPair(Vector2D first, Vector2D second) {
			
			this.first = first;
			this.second = second;
			
		}
		
		public boolean equals(Object o) {
			if (o instanceof PointPair) {
				PointPair p = (PointPair) o;
				if (this.first.equals(p.first) && this.second.equals(p.second)) {
					return true;
				}
			}
			
			return false;
		}
		
		public int compareTo(PointPair other) {
			return Double.compare(first.distance(second), other.first.distance(other.second));
		}
		
		public Vector2D getFirst() {return this.first;}
		
		public Vector2D getSecond() {return this.second;}
		
		
	}
	
	BSPTree<Euclidean2D> diagram;
	Map<Vector2D, Integer> regionLookup;
	
	/**
	* Constrcuts a new VoronoiDiagram from a list of input points.
	* 
	* Each of these points will be assigned a unique region in the final diagram;
	* the ordering of the points determines the region label (1-indexed) in the diagram.
	* 
	* @param pointsInput	a List of Vector2D objects that describe the set of points
	* 						around which the diagram will be constructed.
	*/
	public VoronoiDiagram(List<Vector2D> pointsInput) {
	
		this.regionLookup = new java.util.HashMap<Vector2D, Integer>();
	
		for (int i = 0; i < pointsInput.size(); i++) {
			regionLookup.put(pointsInput.get(i),i+1);
		}
	
		this.diagram = new BSPTree<Euclidean2D>();
	
		if (pointsInput.size() <= 1) {return;}
		
		List<PointPair> allPairs = new java.util.ArrayList<PointPair>();
	
		for (int i = 0; i < pointsInput.size(); i++) {
			
			for (int j = i+1; j < pointsInput.size(); j++) {
				
				allPairs.add(new PointPair(pointsInput.get(i), pointsInput.get(j)));
				
			}
			
		}
		
		java.util.Collections.sort(allPairs);
		
		divideRecursive(this.diagram, allPairs);
		
		for (Vector2D point : pointsInput) {
			BSPTree<Euclidean2D> node = this.diagram.getCell(point);
			node.setAttribute(this.regionLookup.get(point));		
		}

		
	}	
	
	private void divideRecursive(BSPTree<Euclidean2D> leaf, List<PointPair> pairsToUse) {
		
		if (pairsToUse.isEmpty()) return;
		
		PointPair p = pairsToUse.remove(0);
		
		while(! leaf.insertCut(this.constructBisectingHyperplane(p.getFirst(), p.getSecond()))) {
			if (pairsToUse.isEmpty()) return;
			p = pairsToUse.remove(0);
		}
		
		leaf.setAttribute(null);
		
		BSPTree<Euclidean2D> minus = leaf.getMinus();
		BSPTree<Euclidean2D> plus = leaf.getPlus();
		
		minus.setAttribute(null);
		plus.setAttribute(null);
		
		List<PointPair> secondPairs = new java.util.ArrayList<PointPair>();
		secondPairs.addAll(pairsToUse);
		
		divideRecursive(minus, pairsToUse);
		divideRecursive(plus, secondPairs);
		
	}
	
	protected Hyperplane<Euclidean2D> constructBisectingHyperplane(Vector2D firstPoint, Vector2D secondPoint) {
		
		Vector2D middlePoint = firstPoint.add(secondPoint).scalarMultiply(0.5);
		Vector2D difference = secondPoint.subtract(firstPoint);
	
		double angle = Math.atan2(difference.getY(), difference.getX());
	
		angle -= Math.PI/2.0; // rotate 90 degrees
	
		Line bisector = new Line(middlePoint, angle);
	
		return bisector;
	
	}
	
	protected boolean isLeaf(BSPTree<Euclidean2D> node) {
		return node.getPlus() == null && node.getMinus() == null;
	}
	
	/**
	* Gets the number of the region in which a supplied point lies.
	* 
	* @param point a Vector2D describing the point whose region is being queried.
	* @return an int that is the label of the region in which the supplied vector lies.
	* 			this label corresponds to the (1-indexed) order in which the points were supplied
	* 			to the constructor.
	*/
	public int getRegionNumber(Vector2D point) {
		final int unlabeledValue = 100;
		try {
			BSPTree<Euclidean2D> node = this.diagram.getCell(point);				
				
				while (node.getAttribute() == null) {
					if (isLeaf(node)) {
						Integer value = findClosestRegion(point);
						node.setAttribute(value);
						break;
					}
					final double eps = 1e-6; // bump it slightly if it's exactly on an edge.
					Vector2D newpoint = point.add(new Vector2D(eps, 0));
					node = this.diagram.getCell(newpoint);
					
					if (node.getAttribute() != null) break;
					
					if (isLeaf(node)) {
						Integer value = findClosestRegion(point);
						node.setAttribute(value);
						break;
					}
					
					newpoint = point.add(new Vector2D(0,eps));
					node = this.diagram.getCell(newpoint);
				}
			
			return ((Integer) node.getAttribute());
			//return (node.hashCode());
		} catch (ClassCastException e) {
			return 0;
		}
	}
	
	protected Integer findClosestRegion(Vector2D lookup) {
		
		Vector2D best = null;
		
		double bestDist = Double.MAX_VALUE;
		
		for (Vector2D seedPoint : this.regionLookup.keySet()) {
			
			if (seedPoint.distance(lookup) < bestDist) {
				bestDist = seedPoint.distance(lookup);
				best = seedPoint;
			}
			
		}
		
		return this.regionLookup.get(best);
				
	}
	

}