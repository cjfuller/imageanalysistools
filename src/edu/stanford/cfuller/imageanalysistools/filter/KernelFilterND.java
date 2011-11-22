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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * Filters an Image in an arbitrary number of dimensions using a specified kernel.
 * <p>
 * Currently only supports independent kernels for each dimension, such that at 
 * any point, the filtering due to the combination of kernels is the product of
 * the filtering by the kernel in each dimension.
 * 
 * @author Colin J. Fuller
 *
 */
public class KernelFilterND extends Filter {

	List<Integer> dimensionsToFilter;
	
	Map<Integer, double[]> kernelByDimension; //TODO: reimplement Kernel class to be more ND-friendly and use this instead.
	
	Map<Integer, Integer> halfDimensionSizes;
	
	public KernelFilterND() {
		
		this.dimensionsToFilter = new ArrayList<Integer>();
		
		this.kernelByDimension = new HashMap<Integer, double[]>();
		
		this.halfDimensionSizes = new HashMap<Integer, Integer>();
	
	}
	
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
	 */
	@Override
	public void apply(Image im) {

		
		ImageCoordinate boxLower = ImageCoordinate.cloneCoord(im.getDimensionSizes());
		ImageCoordinate boxUpper = ImageCoordinate.cloneCoord(im.getDimensionSizes());
				
		for (int i = 0; i < this.dimensionsToFilter.size(); i++) {
			
			Image original = new Image(im);
			
			Integer dim = this.dimensionsToFilter.get(i);
			int size = halfDimensionSizes.get(dim);
		
			for (ImageCoordinate ic : im) {
				boxLower.setCoord(ic);
				for (Integer i2 : boxLower) {
					boxUpper.set(i2, boxLower.get(i2) + 1);
				}
				boxLower.set(dim, boxLower.get(dim)-size);
				boxUpper.set(dim, boxUpper.get(dim)+size);
			
		
				original.setBoxOfInterest(boxLower, boxUpper);
	
				double kernelTotal = 0;
				
				double filterTotal = 0;
				
				for (ImageCoordinate ic2 : original) {
				
					double currKernelValue = 1;
					
					for (Integer i2 : this.dimensionsToFilter) {
						
						int kernelOffset = ic2.get(i2) - ic.get(i2) + halfDimensionSizes.get(i2);
						
						if (kernelOffset < 0 ) {
							ij.IJ.log("kernel offset: " + kernelOffset);
							ij.IJ.log("ic2: " + ic2);
							ij.IJ.log("ic: " + ic);
							ij.IJ.log("i2: " + i2);
							ij.IJ.log("sizes: " + halfDimensionSizes);
							ij.IJ.log("lower: " + boxLower);
							ij.IJ.log("upper: " + boxUpper);
						}
						
						currKernelValue *= kernelByDimension.get(i2)[kernelOffset];
						
						
					}
					
					kernelTotal+= currKernelValue;
					
					filterTotal+= currKernelValue*original.getValue(ic2);
	
				}
				
				im.setValue(ic, (float) (filterTotal/kernelTotal));
				
				original.clearBoxOfInterest();
			
			}
		
		}
		
		
//		
//		for (ImageCoordinate ic : im) {
//			
//			boxLower.setCoord(ic);
//			
//			for (Integer i : boxLower) {
//				boxUpper.set(i, boxLower.get(i) + 1);
//			}
//			
//			for (int i = 0; i < this.dimensionsToFilter.size(); i++) {
//				Integer dim = this.dimensionsToFilter.get(i);
//				boxLower.set(dim, boxLower.get(dim) - halfDimensionSizes.get(i));
//				boxUpper.set(dim, boxUpper.get(dim) + halfDimensionSizes.get(i)); //no additional +1 as this is already in at the previous step
//			}
//			
//			original.setBoxOfInterest(boxLower, boxUpper);
//			
//			double kernelTotal = 0;
//			
//			double filterTotal = 0;
//			
//			for (ImageCoordinate ic2 : original) {
//			
//				double currKernelValue = 1;
//				
//				for (Integer i : this.dimensionsToFilter) {
//					
//					int kernelOffset = ic2.get(i) - ic.get(i) + halfDimensionSizes.get(i);
//					
//					currKernelValue *= kernelByDimension.get(i)[kernelOffset];
//					
//					
//				}
//				
//				kernelTotal+= currKernelValue;
//				
//				filterTotal+= currKernelValue*original.getValue(ic2);
//
//			}
//			
//			im.setValue(ic, filterTotal/kernelTotal);
//			
//			original.clearBoxOfInterest();
//			
//		}
		
		
		
		boxLower.recycle();
		boxUpper.recycle();
		
	}
	
	/**
	 * Adds a dimension to be filtered.  The integer dimension must be specified
	 * by the same integer used to specify it in an ImageCoordinate.  The kernel
	 * can have any size >= 1, and it must have an odd number of elements.
	 * 
	 * @param dimension		The dimension to filter.
	 * @param kernel		The kernel that will be applied to the specified dimension.
	 */
	public void addDimensionWithKernel(Integer dimension, double[] kernel) {
		
		this.dimensionsToFilter.add(dimension);
		
		this.kernelByDimension.put(dimension, kernel);
		
		this.halfDimensionSizes.put(dimension, (kernel.length-1)/2);
		
	}

}
