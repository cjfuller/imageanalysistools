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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.RealMatrix;

/**
 * A Filter that estimates the background locally in an Image, using a local median filtering approach.
 * <p>
 * This filter may be useful for determining and correcting for local intensity variations.
 * <p>
 * The reference image should be set to the Image that is to be median filtered.  This Image will not be changed.
 * <p>
 * The argument to the apply method should be any Image (except a shallow copy of the reference Image) of the same dimensions as the reference Image.
 * The median filtered Image will be written to this Image.
 *
 * @author Colin J. Fuller
 * 
 */

public class LocalBackgroundEstimationFilter extends Filter {

	int boxSize;

    /**
     * Constructs a LocalBackgroundEstimationFilter with a default size.
     */
	public LocalBackgroundEstimationFilter() {
	
		this.boxSize = 25;
		
	}

    /**
     * Sets the size of the box used for local median calculations.
     * @param boxSize   The radius of the box (the final box will be 2*boxSize + 1 square).
     */
	public void setBoxSize(int boxSize) {
		this.boxSize = boxSize;
	}

    /**
     * Applies the LocalBackgroundEstimationFilter to an Image.
     * @param im    The Image that will be replaced by the output Image.  This can be anything of the correct dimensions except a shallow copy of the reference Image.
     */
	@Override
	public void apply(Image im) {

		if (this.referenceImage == null) return;
		
		//RealVector localArea = new org.apache.commons.math.linear.ArrayRealVector();
		
		edu.stanford.cfuller.imageanalysistools.image.Histogram h = new edu.stanford.cfuller.imageanalysistools.image.Histogram(this.referenceImage);

		int numPixelsInBox = boxSize*boxSize;
		
		ImageCoordinate ic = this.referenceImage.getDimensionSizes();
		
		ImageCoordinate icnew = ImageCoordinate.createCoordXYZCT(ic.get(ImageCoordinate.X)+2*boxSize, ic.get(ImageCoordinate.Y)+2*boxSize, ic.get(ImageCoordinate.Z), ic.get(ImageCoordinate.C), ic.get(ImageCoordinate.T));

		Image padded = new Image(icnew, -1.0);
		
		
		for(ImageCoordinate i : this.referenceImage) {
		
			icnew.set(ImageCoordinate.X,i.get(ImageCoordinate.X)+boxSize);
			icnew.set(ImageCoordinate.Y,i.get(ImageCoordinate.Y)+boxSize);
			icnew.set(ImageCoordinate.Z,i.get(ImageCoordinate.Z));
			icnew.set(ImageCoordinate.C,i.get(ImageCoordinate.C));
			icnew.set(ImageCoordinate.T,i.get(ImageCoordinate.T));
			
			padded.setValue(icnew, this.referenceImage.getValue(i));
			
		}
		
		
		RealVector overallCounts = new org.apache.commons.math.linear.ArrayRealVector(h.getMaxValue() + 1);

		RealMatrix countsByRow = new org.apache.commons.math.linear.Array2DRowRealMatrix(2*boxSize + 1, h.getMaxValue() + 1);


		//loop over columns
		
		for (int i = boxSize; i < im.getDimensionSizes().get(ImageCoordinate.X)+boxSize; i++) {
			
			overallCounts.mapMultiplyToSelf(0.0);
            double[] overallCounts_a = overallCounts.getData();
			countsByRow = countsByRow.scalarMultiply(0.0);
            double[][] countsByRow_a = countsByRow.getData();

			int countsByRow_rowZero_pointer = 0;

			
			for (int m = i-boxSize; m <= i+boxSize; m++) {
				for (int n = 0; n < 2*boxSize + 1; n++) {
					icnew.set(ImageCoordinate.X,m);
					icnew.set(ImageCoordinate.Y,n);
					int value = (int) padded.getValue(icnew);

					if (value == -1) continue;
					
					//overallCounts.setEntry(value, overallCounts.getEntry(value) +1.0);
                    overallCounts_a[value]++;
					//countsByRow.addToEntry((n+countsByRow_rowZero_pointer) % countsByRow.getRowDimension(), value, 1.0);
					countsByRow_a[(n+countsByRow_rowZero_pointer) % countsByRow.getRowDimension()][value]++;
					
				}
			}
			
			int currMedian = 0;
			int runningSum = 0;
			int k = 0;
			
			while(runningSum < (numPixelsInBox >> 1) ) {
				//runningSum += overallCounts.getEntry(k++);
                runningSum += overallCounts_a[k++];
			}
			
			//runningSum -= overallCounts.getEntry(k-1);

            runningSum -= overallCounts_a[k-1];

			currMedian = k-1;
			
			icnew.set(ImageCoordinate.X,i-boxSize);
			icnew.set(ImageCoordinate.Y,0);
			
			im.setValue(icnew, currMedian);
			
			for (int j = boxSize + 1; j < im.getDimensionSizes().get(ImageCoordinate.Y)+boxSize; j++) {
				
				//double[] toRemove = countsByRow.getRow((countsByRow_rowZero_pointer) % countsByRow.getRowDimension());

                double[] toRemove = countsByRow_a[(countsByRow_rowZero_pointer) % countsByRow.getRowDimension()];

				//overallCounts = overallCounts.subtract(toRemove);

                for (int oc_counter = 0; oc_counter < overallCounts_a.length; oc_counter++) {
                    overallCounts_a[oc_counter]-=toRemove[oc_counter];
                }

				for (int c = 0; c<toRemove.length; c++) {
					if (c < currMedian) {
						runningSum -= toRemove[c];
					}
					
					//countsByRow.multiplyEntry(countsByRow_rowZero_pointer % countsByRow.getRowDimension(), c, 0.0);
                    countsByRow_a[countsByRow_rowZero_pointer % countsByRow.getRowDimension()][c]*= 0.0;
				}
				
				countsByRow_rowZero_pointer++;
				
				for (int c = i - boxSize; c <= i+boxSize; c++) {
					icnew.set(ImageCoordinate.X,c);
					icnew.set(ImageCoordinate.Y,j+boxSize);
					int value = (int) padded.getValue(icnew);
					
					if (value == -1) continue;
					
					//countsByRow.addToEntry((countsByRow_rowZero_pointer + countsByRow.getRowDimension() - 1) % countsByRow.getRowDimension(), value, 1);
					countsByRow_a[(countsByRow_rowZero_pointer + countsByRow.getRowDimension() - 1) % countsByRow.getRowDimension()][value]+= 1;
                    //overallCounts.setEntry(value, overallCounts.getEntry(value) + 1);
					overallCounts_a[value]++;


					if (value < currMedian) {
						runningSum++;
					}
				
				}
				
				//case 1: runningSum > half of box
				
				if (runningSum > (numPixelsInBox >>1)) {
					
					k = currMedian -1;
					while(runningSum > (numPixelsInBox>>1)) {
						//runningSum -= overallCounts.getEntry(k--);
                        runningSum -= overallCounts_a[k--];
					}
					
					currMedian = k+1;
					
				} else if(runningSum < (numPixelsInBox >> 1)) { // case 2: runningSum < half of box
					
					k = currMedian;
					
					while(runningSum < (numPixelsInBox >> 1)) {
						//runningSum += overallCounts.getEntry(k++);
                        runningSum += overallCounts_a[k++];
					}
					
					currMedian = k-1;
					
					//runningSum -= overallCounts.getEntry(k-1);
                    runningSum -= overallCounts_a[k-1];
					
				}
				
				//cast 3: spot on, do nothing
				
				icnew.set(ImageCoordinate.X,i-boxSize);
				icnew.set(ImageCoordinate.Y,j-boxSize);
				
				im.setValue(icnew,currMedian);
				
				
				
				
				
			}
			
		}
		
		icnew.recycle();

		
	}
	
	protected static void swap(int first, int second, RealVector toProcess) {
		double value = toProcess.getEntry(first);
		toProcess.setEntry(first, toProcess.getEntry(second));
		toProcess.setEntry(second, value);
	}
	
	
	/**
	 * Finds the kth item sorted by increasing value in a possibly unsorted vector.
	 * <p>
	 * This will likely not completely sort the vector, but will almost certainly
	 * change the order of the items in the vector in place.
	 * 
	 * @param k			The index of the item (in the sorted vector) to find.
	 * @param toFind	The RealVector in which to find the kth item.
	 * @return			The value of the kth item (in the sorted vector).
	 */
	public static double quickFindKth(int k, RealVector toFind) {
		
		int n = toFind.getDimension();
		
		int l = 0;
		
		int ir = n-1;
		
		while(true) {
		
			if (ir <= l+1) {
				if (ir == l+1 && toFind.getEntry(ir) < toFind.getEntry(l)) {
					swap(ir, l, toFind);
				}
				
				return toFind.getEntry(k);
			} else {
				int mid = (l+ir) >> 1;
				
				swap(mid, l+1, toFind);
				
				if (toFind.getEntry(l) > toFind.getEntry(ir)) swap(l, ir, toFind);
				if (toFind.getEntry(l+1) > toFind.getEntry(ir)) swap(l+1, ir, toFind);
				if (toFind.getEntry(l) > toFind.getEntry(l+1)) swap(l, l+1, toFind);
				
				int i = l+1;
				int j = ir;
				
				double a = toFind.getEntry(l+1);
				
				while(true) {
					do{
						i++;
					} while (toFind.getEntry(i) < a);
					
					do{
						j--;
					} while (toFind.getEntry(j) > a);
					
					if (j < i) break;
					
					swap(i,j,toFind);
				}
				
				toFind.setEntry(l+1, toFind.getEntry(j));
				toFind.setEntry(j, a);
				
				if (j >= k) ir = j-1;
				if (j <= k) l = i;
				
			}
			
		}
				
	}

}
