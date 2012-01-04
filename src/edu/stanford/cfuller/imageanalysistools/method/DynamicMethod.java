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
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;
import edu.stanford.cfuller.imageanalysistools.metric.ZeroMetric;

/**
 * This class uses an XML file containing the specification of a method to run
 * analysis.
 * <p>
 * For now, filters are specified in the standard parameters file using sequential
 * numbering: <parameter name="filter_0" .../>, <parameter name="filter_1" .../>
 * etc.  This may change in a later version to be contained within the method parameter.
 * <p>
 * Another parameter, "number_of_filters", is also required.
 * 
 * @author Colin J. Fuller
 *
 */
public class DynamicMethod extends Method {

	public static final String P_NUM_FILTERS = "number_of_filters";
	public static final String P_FILTER_BASE = "filter_";
	public static final String P_METRIC_NAME = "metric_name";
	public static final String P_FILTER_ALL = "filter_all";
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.method.Method#go()
	 */
	@Override
	public void go() {
		
		java.util.List<Filter> filters = new java.util.ArrayList<Filter>();
		
		if (this.parameters.hasKey(P_FILTER_ALL)) {
			String[] filterNames = this.parameters.getValueForKey(P_FILTER_ALL).split(" ");
			for (String fn : filterNames) {
				Filter f = this.getFilter(fn);
				if (f != null) {
					filters.add(f);
				}
			}
		} else {
		
			if (! this.parameters.hasKey(P_NUM_FILTERS)) {
				return;
			}
			
			for (int i = 0; i < this.parameters.getIntValueForKey(P_NUM_FILTERS); i++) {
				String currParamName = P_FILTER_BASE + Integer.toString(i);
			
				String filterName = this.parameters.getValueForKey(currParamName);
				
				Filter f = this.getFilter(filterName);
				
				if (f != null) {
					filters.add(f);
				}
				
			}
			
		}
				
		for (Filter f : filters) {
			f.setParameters(this.parameters);
			f.setReferenceImage(this.imageSet.getMarkerImageOrDefault());
		}
		
		Metric m = null;
		
		m = this.getMetric(this.parameters.getValueForKey(P_METRIC_NAME));
		
		if (m == null) {
			m = new ZeroMetric();
		}
				
		iterateOnFiltersAndStoreResult(filters, new Image(this.imageSet.getMarkerImageOrDefault()), m);
		
	}
	
	/**
     * Retrieves a filter from its name.
     * 
     * @param filterName	The name of the filter to retrieve.
     * @return				A Filter object of the type specified or null if the filter
     * 						cannot be retrieved.
     */
    protected Filter getFilter(String filterName) {

        Filter filter = null;

        try {
            filter = (Filter) Class.forName(filterName).newInstance();
        } catch (ClassNotFoundException e) {
            LoggingUtilities.getLogger().severe("Could not find filter: " + filterName);
            e.printStackTrace();
        } catch (InstantiationException e) {
            LoggingUtilities.getLogger().severe("Could not instantiate filter: " + filterName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LoggingUtilities.getLogger().severe("Could not access filter constructor for: " + filterName);
            e.printStackTrace();
        }

        return filter;

    }
    
    /**
     * Retrieves a metric from its name.
     * 
     * @param metricName	The name of the metric to retrieve.
     * @return				A Metric object of the type specified or null if the metric
     * 						cannot be retrieved.
     */
    protected Metric getMetric(String metricName) {

        
    	Metric metric = null;

        try {
            metric = (Metric) Class.forName(metricName).newInstance();
        } catch (ClassNotFoundException e) {
            LoggingUtilities.getLogger().severe("Could not find filter: " + metricName);
            e.printStackTrace();
        } catch (InstantiationException e) {
            LoggingUtilities.getLogger().severe("Could not instantiate filter: " + metricName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LoggingUtilities.getLogger().severe("Could not access filter constructor for: " + metricName);
            e.printStackTrace();
        }

        return metric;

    }

}
