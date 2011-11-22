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
import edu.stanford.cfuller.imageanalysistools.filter.LocalMaximumSeparabilityThresholdingFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparability3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.Renormalization3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;

/**
 * A method analogous to the {@link CentromereFindingMethod}, but intended to be applied to 3D images.
 * <p>
 * Identifies volumes, rather than planar regions, and outputs a 3D mask rather than a single plane mask.
 * 
 * @author Colin J. Fuller
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
    	
		int referenceChannel = 0;
		
		this.parameters.addIfNotSet("marker_channel_index", Integer.toString(referenceChannel));
		
		referenceChannel = this.parameters.getIntValueForKey("marker_channel_index");
		
		this.centromereFinding(this.images.get(0));
		
		
	}
	
	protected Image centromereFinding(Image input) {
		
        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();
                
        Renormalization3DFilter LBE3F = new Renormalization3DFilter();
        
        filters.add(LBE3F);
        
        filters.add(new LocalMaximumSeparabilityThresholdingFilter());
        filters.add(new Label3DFilter());
        filters.add(new RecursiveMaximumSeparability3DFilter());
        filters.add(new RelabelFilter());
        filters.add(new SizeAbsoluteFilter());
        filters.add(new RelabelFilter());

        for (Filter i : filters){
            i.setParameters(this.parameters);
            i.setReferenceImage(this.images.get(0));
        }

        Image toProcess = new Image(this.images.get(0));
        
        iterateOnFiltersAndStoreResult(filters, toProcess, metric);
        

        return this.getStoredImage();
    }

}
