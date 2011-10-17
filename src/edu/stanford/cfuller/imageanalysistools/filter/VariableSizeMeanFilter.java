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

import java.util.Deque;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.FDistribution;
import org.apache.commons.math.distribution.FDistributionImpl;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * @author cfuller
 *
 */
public class VariableSizeMeanFilter extends Filter {

	private int minBoxSize;
	
	public void setBoxSize(int size) {
		this.minBoxSize = 2*size; //factor of 2 is because it may subdivide once beyond this
	}
	
	protected class OcttreeNode {
		
		ImageCoordinate boxMin;
		ImageCoordinate boxMax;
		
		List<OcttreeNode> children;
		
		public OcttreeNode(ImageCoordinate boxMin, ImageCoordinate boxMax) {
			this.boxMin = boxMin;
			this.boxMax = boxMax;
			this.children = new java.util.ArrayList<OcttreeNode>(8);
		}
		
		public boolean subDivide() {
			
			//System.out.println("initial size: " + this.boxMin.toString() + " to " + this.boxMax.toString());
						
			int[] dim_mults = new int[3];
			
			boolean succeeded = false;
			
			//continue as long as we can subdivide at least on dimension
			
			for (int dim = ImageCoordinate.X; dim <= ImageCoordinate.Z; dim++) { //	TODO: fix this
				
				if (this.boxMin.get(dim) + minBoxSize < this.boxMax.get(dim)) {
					succeeded = true;
					break;
				}
				
			}
			
			if (!succeeded) return succeeded;
			
			// loop over each dimension, dividing it into two
			
			for (int x = 0; x < 2; x++) {
				
				for (int y = 0; y < 2; y++) {
					
					for (int z = 0; z < 2; z++) {
						
						dim_mults[0] = x;
						dim_mults[1] = y;
						dim_mults[2] = z;
						
						ImageCoordinate boxMin_new = ImageCoordinate.cloneCoord(this.boxMin);
						ImageCoordinate boxMax_new = ImageCoordinate.cloneCoord(this.boxMax);
						
						//continue if we can't divide this dimension and would otherwise put in two children with the same range
						
						boolean skip = false;
						
						for (int dim = ImageCoordinate.X; dim <= ImageCoordinate.Z; dim++) { //TODO: fix this
						
							if (this.boxMin.get(dim) + minBoxSize >= this.boxMax.get(dim) && dim_mults[dim] == 0) {skip = true; break;}
						
						}
						
						if (skip) continue;
						
						//otherwise, divide
						
						for (int dim = ImageCoordinate.X; dim <= ImageCoordinate.Z; dim++) { //TODO: fix this
						
							//if we shouldn't divide this dimension, leave it the same size
							if (this.boxMin.get(dim) + minBoxSize >= this.boxMax.get(dim)) {
								continue;
							}
							
							//divide the dimension
							
							boxMin_new.set(dim, this.boxMin.get(dim) + dim_mults[dim]*((this.boxMax.get(dim) - this.boxMin.get(dim))/2));
							boxMax_new.set(dim, dim_mults[dim]*this.boxMax.get(dim) + (1-dim_mults[dim])*(this.boxMin.get(dim) + (1-dim_mults[dim])*((this.boxMax.get(dim) - this.boxMin.get(dim))/2)));
							
						}
						
						//System.out.println("divided size: " + boxMin_new.toString() + " to " + boxMax_new.toString());

						//add new nodes for the divided children
						children.add(new OcttreeNode(boxMin_new, boxMax_new));
						
					}
				}
				
			}
			
			return true;
			
		}
		
		public ImageCoordinate getBoxMin() {
			return this.boxMin;
		}
		
		public ImageCoordinate getBoxMax() {
			return this.boxMax;
		}
		
		public List<OcttreeNode> getChildren() {
			return this.children;
		}
		
		protected void finalize() throws Throwable {
			this.boxMin.recycle();
			this.boxMax.recycle();
			this.boxMin = null;
			this.boxMax = null;
			super.finalize();
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {

		//calculate Laplacian of Image, calculate pseudo-residual (as in Boulanger, 2010)
		
		Image residual = new Image(im);
		
		LaplacianFilter LF = new LaplacianFilter();
		
		LF.apply(residual);
		
		//for 3D, residual is Laplacian divided by sqrt(56)
		
		double norm = Math.sqrt(56);
		
		//for 2D, residual is sqrt(30)
		
		//norm = Math.sqrt(30);
		
		for (ImageCoordinate ic : residual) {
			
			residual.setValue(ic, residual.getValue(ic)/norm);
			
		}
		
		residual.writeToFile("/Users/cfuller/Desktop/residual.ome.tif");
		
		//perform an octtree segmentation of the Image, using a criterion based on relative variance of image and noise
		
		OcttreeNode root = new OcttreeNode(ImageCoordinate.createCoordXYZCT(0,0,0,0,0), ImageCoordinate.cloneCoord(im.getDimensionSizes()));
		
		root.subDivide();
		
		Deque<OcttreeNode> queue = new java.util.ArrayDeque<OcttreeNode>();
		
		queue.addAll(root.getChildren());
		
		List<OcttreeNode> leaves = new java.util.ArrayList<OcttreeNode>();
		
		
		while(!queue.isEmpty()) {
			
			OcttreeNode current = queue.pop();
												
			if (this.shouldSubDivide(current, im, residual) && current.subDivide()) {
				
				queue.addAll(current.getChildren());
				
			} else {
				
								
				leaves.add(current);
				
			}
					
		}
		
		for (OcttreeNode node : leaves) {
			
			double count = 0;
			double mean = 0;
			
			im.setBoxOfInterest(node.getBoxMin(), node.getBoxMax());
			
			for (ImageCoordinate ic : im) {
				
				mean+= im.getValue(ic);
				count++;
				
			}
			
			mean/=count;
			
			for (ImageCoordinate ic : im) {
				
				im.setValue(ic, mean);
				
			}
			
			im.clearBoxOfInterest();
			
		}
		
		im.writeToFile("/Users/cfuller/Desktop/filtered.ome.tif");
		
		
	}
	
	protected boolean shouldSubDivide(OcttreeNode node, Image im, Image laplacianFiltered) {
		
		//TODO: implement this other than as simple comparison of magnitude
		
		im.setBoxOfInterest(node.getBoxMin(), node.getBoxMax());
		laplacianFiltered.setBoxOfInterest(node.getBoxMin(), node.getBoxMax());
		
		double l_sum = 0;
		double sum = 0;
		double count = 0;
		
		for (ImageCoordinate ic : im) {
			
			l_sum += laplacianFiltered.getValue(ic);
			sum += im.getValue(ic);
			count++;
			
		}
		
		l_sum/=count;
		sum/=count;
		
		double l_var = 0;
		double var = 0;
		
		for (ImageCoordinate ic : im) {
			
			l_var += Math.pow(laplacianFiltered.getValue(ic) - l_sum,2);
			var += Math.pow(im.getValue(ic) - sum,2);
			
		}
		
		l_var /= (count-1);
		var /= (count-1);
		
		im.clearBoxOfInterest();
		laplacianFiltered.clearBoxOfInterest();
		
		//System.out.print("coordinate from: " + node.getBoxMin().toString() + " to " + node.getBoxMax().toString() + "     ");
		//System.out.printf("var: %f, l_var: %f\n", var, l_var);
		
		FDistribution f = new FDistributionImpl(count-1, count-1);
		
		double cutoff = 0.001;
		
		double smallerVar = var < l_var ? var : l_var;
		double largerVar = var > l_var ? var : l_var;
		try {
			double valueAtLowerCutoff = f.inverseCumulativeProbability(cutoff);
			double valueAtUpperCutoff = f.inverseCumulativeProbability(1-cutoff);
			boolean result =  (smallerVar/largerVar > valueAtUpperCutoff || smallerVar/largerVar < valueAtLowerCutoff);
			
			//System.out.println("ratio: " + (smallerVar/largerVar) + "   cutoff: " + valueAtLowerCutoff + "     result: " + result);
			return result;

		} catch (MathException e) {
			LoggingUtilities.getLogger().severe("Exception while calculating variable size mean QO partition: " + e.getMessage());
			return false;
		}
	}

}
