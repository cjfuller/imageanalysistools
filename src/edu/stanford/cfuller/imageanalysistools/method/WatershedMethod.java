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

import edu.stanford.cfuller.imageanalysistools.filter.*;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 1/27/11
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class WatershedMethod extends Method {


    public void go() {

        LoggingUtilities.logDebugMessages();

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

        GaussianFilter gf = new GaussianFilter();

        if (this.parameters.hasKey("min_size")) {

            gf.setWidth((int) Math.sqrt(this.parameters.getIntValueForKey("min_size")));

        } else {
            gf.setWidth(5);
        }

        filters.add(gf);

        filters.add(new GradientFilter());

        filters.add(new WatershedFilter());

        filters.add(new RelabelFilter());

        filters.add(new SimpleThresholdingFilter(0.1));

        filters.add(new RelabelFilter());
        filters.add(new FillFilter());

        for (Filter f : filters) {
            f.setReferenceImage(this.images.get(0));
            f.setParameters(this.parameters);
        }

        Image toProcess = new Image(this.images.get(0));

        iterateOnFiltersAndStoreResult(filters, toProcess, new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric());

    }

}
