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

package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.Label3DFilter;
//import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimation3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimation3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparability3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;

/**
 * A method analogous to the {@link CentromereFindingMethod}, but intended to be applied to 3D images.
 * <p>
 * Identifies volumes, rather than planar regions, and outputs a 3D mask rather than a single plane mask.
 * 
 * 
 * 
 * @author cfuller
 *
 */
public class CentromereFinding3DMethod extends Method {

    Metric metric;

	
	/**
     * Sole constructor, which creates a default instance.
     */
    public CentromereFinding3DMethod() {
        this.metric = new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric();
    }
	
	
    /**
    *
    * Runs the centromere finding method using the stored images and parameters.
    *
    */
    @Override
	public void go() {
		this.parameters.setValueForKey("DEBUG", "true");

		int referenceChannel = 0;
		
		if (this.parameters.hasKey("marker_channel_index")) {
			referenceChannel = this.parameters.getIntValueForKey("marker_channel_index");
		}
		
		this.centromereFinding(this.images.get(referenceChannel));
		
		//try grouping all objects, finding average intensity, segmenting into categories based on average intensity of objects
		//(akin to reduce punctate background of the original centromere finder)
		
		Image result = this.getStoredImage();
		Image reference = this.images.get(referenceChannel);
		
		Histogram h = new Histogram(result);
		
		int numRegions = h.getMaxValue();
		
		double[] sums = new double[numRegions];
		
		java.util.Arrays.fill(sums, 0.0);
				
		for (ImageCoordinate ic : result) {
			
			int value = (int) result.getValue(ic);
			
			if (value == 0) continue;
			
			sums[value-1] += reference.getValue(ic);
			
		}
		
		//construct an image, one pixel per region, containing each region's average value
		
		ImageCoordinate dimensionSizes = ImageCoordinate.createCoordXYZCT(numRegions, 1,1,1,1);
		
		Image meanValues = new Image(dimensionSizes, 0.0);
		
		for (ImageCoordinate ic : meanValues) {
			meanValues.setValue(ic, sums[ic.get(ImageCoordinate.X)]/h.getCounts(ic.get(ImageCoordinate.X) + 1));
		}
		
		dimensionSizes.recycle();
		
		//segment the image
		
		MaximumSeparabilityThresholdingFilter MSTF = new MaximumSeparabilityThresholdingFilter();
		
		MSTF.apply(meanValues);
		
		//filter based on the average value segmentation
		
		for (ImageCoordinate ic : result) {
			
			ImageCoordinate ic2 = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
			
			int value = (int) result.getValue(ic);
			
			if (value == 0) continue;
			
			ic2.set(ImageCoordinate.X, value - 1);
			
			if (meanValues.getValue(ic2) == 0.0) {
				result.setValue(ic, 0);
			}
			
		}
		
	}
	
	protected Image centromereFinding(Image input) {

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();
        
        LocalBackgroundEstimation3DFilter LBE3F = new LocalBackgroundEstimation3DFilter();
        
        LBE3F.setBoxSize(5);

        
        filters.add(LBE3F);
        filters.add(new MaximumSeparabilityThresholdingFilter());
        filters.add(new Label3DFilter());
        filters.add(new RecursiveMaximumSeparability3DFilter());
        filters.add(new RelabelFilter());
        filters.add(new SizeAbsoluteFilter());
        filters.add(new RelabelFilter());

        for (Filter i : filters){
            i.setParameters(this.parameters);
            i.setReferenceImage(this.images.get(0));
        }

        Image toProcess = new Image(input);

        iterateOnFiltersAndStoreResult(filters, toProcess, metric);

        return this.getStoredImage();
    }

}
