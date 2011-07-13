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
package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.Label3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RecursiveMaximumSeparability3DFilter;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.SizeAbsoluteFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
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
		this.centromereFinding(this.images.get(0));
	}
	
	protected Image centromereFinding(Image input) {

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

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
