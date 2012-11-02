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

import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
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
	public void apply(WritableImage im) {

		for (int i = 0; i < this.dimensionsToFilter.size(); i++) {
			
			Image original = ImageFactory.create(im);
			
			Integer dim = this.dimensionsToFilter.get(i);
			int size = halfDimensionSizes.get(dim);
			
			double[] currDimKernel = kernelByDimension.get(dim);
			
			for (ImageCoordinate ic : im) {
				
				ImageCoordinate ic2 = ImageCoordinate.cloneCoord(ic);
	
				double kernelTotal = 0;
				
				double filterTotal = 0;
				
				int currPosOffset = size - ic.get(dim);
				
				int min = ic.get(dim)-size;
				int max = ic.get(dim) + size + 1;
				
				for (int dimValue = min; dimValue < max; dimValue++) {
					
					ic2.set(dim, dimValue);
					
					int kernelOffset = dimValue + currPosOffset;
					
					double currKernelValue = currDimKernel[kernelOffset];
					
					kernelTotal+= currKernelValue;
					
					float imageValue = 0;
					
					if (original.inBounds(ic2)) {
						imageValue = original.getValue(ic2);
					}
					
					filterTotal+= currKernelValue*imageValue;
					
				}
				
				
				im.setValue(ic, (float) (filterTotal/kernelTotal));
							
			}
		
		}	
				
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
