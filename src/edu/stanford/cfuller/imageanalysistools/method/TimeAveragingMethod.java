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
import edu.stanford.cfuller.imageanalysistools.filter.TimeAveragingFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A method that time averages images, and stores the time-averaged result in place of the usual mask output.
 * 
 * @author Colin J. Fuller
 */
public class TimeAveragingMethod extends Method {
 
    public void go() {

        TimeAveragingFilter taf = new TimeAveragingFilter();



        taf.setParameters(this.parameters);

        //first create the reference Image

        ImageCoordinate dimSizes = ImageCoordinate.cloneCoord(this.images.get(0).getDimensionSizes());

        dimSizes.setC(this.images.size());

        Image reference = new Image(dimSizes, 0.0);

        for (ImageCoordinate ic : reference) {
            ImageCoordinate ic_c = ImageCoordinate.cloneCoord(ic);
            ic_c.setC(0);
            reference.setValue(ic, this.images.get(ic.getC()).getValue(ic_c));
            ic_c.recycle();
        }

        taf.setReferenceImage(reference);

        //now create the output image

        dimSizes.setT(1);

        Image timeAveraged = new Image(dimSizes, 0.0);

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

        filters.add(taf);

        iterateOnFiltersAndStoreResult(filters, timeAveraged, null);


        dimSizes.recycle();


    }


}
