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

import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;
import org.apache.commons.math.linear.RealMatrix;


/**
 * A method to find isolated chromosomes using DNA stain (designate using parameter marker_channel_index) as well as
 * centromeres using a centromere marker (designate using parameter secondary_marker_channel_index).
 *
 * These will be matched up, and for each chromososome, the centromeric as well as noncentromeric intensities in each channel
 * will be quantified.
 *
 * @author Colin J. Fuller
 *
 */
public class ChromosomeCentromereFindingMethod extends Method {

    Metric metric;

    public ChromosomeCentromereFindingMethod() {
        this.metric = new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric();
    }

    public void go() {

        CentromereFindingMethod ch0_method = new CentromereFindingMethod();
        CentromereFindingMethod ch1_method = new CentromereFindingMethod();

        int originalChannelNumber = this.parameters.getIntValueForKey("marker_channel_index");
        int secondChannelNumber = this.parameters.getIntValueForKey("secondary_marker_channel_index");
        if (originalChannelNumber >= secondChannelNumber) secondChannelNumber++;

        ch0_method.setImages(this.imageSet);
        ch0_method.setParameters(this.parameters);

        ImageSet reordered = new ImageSet(this.imageSet);

        reordered.setMarkerImage(secondChannelNumber);

        ch1_method.setImages(reordered);
        ch1_method.setParameters(this.parameters);

        ch0_method.go();

        int oldMax = this.parameters.getIntValueForKey("max_size");
        int oldMin = this.parameters.getIntValueForKey("min_size");

        if (this.parameters.hasKey("cen_max_size") && this.parameters.hasKey("cen_min_size")) {

            this.parameters.setValueForKey("max_size", this.parameters.getValueForKey("cen_max_size"));
            this.parameters.setValueForKey("min_size", this.parameters.getValueForKey("cen_min_size"));
        }

        ch1_method.go();

        this.parameters.setValueForKey("max_size", Integer.toString(oldMax));
        this.parameters.setValueForKey("min_size", Integer.toString(oldMin));


        MaskFilter mf = new MaskFilter();

        //ch0 = chromosomes
        //ch1 = centromeres

        mf.setReferenceImage(ch1_method.getStoredImage());
        Image chromosomeCentromereMask = new Image(ch0_method.getStoredImage());

        mf.apply(chromosomeCentromereMask);

        //now remove all these regions from the chromosome mask

        Image chromosomeNonCentromereMask = new Image(ch0_method.getStoredImage());

        for (ImageCoordinate ic : chromosomeNonCentromereMask) {

            if (chromosomeCentromereMask.getValue(ic) > 0) {


                chromosomeNonCentromereMask.setValue(ic, 0);
            }


        }

        RealMatrix fullResult = metric.quantify(chromosomeCentromereMask, this.images);

        RealMatrix backgroundResult = metric.quantify(chromosomeNonCentromereMask, this.images);

        if (fullResult != null && backgroundResult != null) {

            RealMatrix masterResult = new org.apache.commons.math.linear.Array2DRowRealMatrix(fullResult.getRowDimension(), fullResult.getColumnDimension() + Integer.parseInt(this.parameters.getValueForKey("number_of_channels")) + 2);

            for (int i = 0; i < fullResult.getRowDimension(); i++) {

                for (int j = 0; j < fullResult.getColumnDimension(); j++) {

                    masterResult.setEntry(i, j, fullResult.getEntry(i, j));

                }

                for (int b = 0; b < Integer.parseInt(this.parameters.getValueForKey("number_of_channels")); b++) {

                    if (i < backgroundResult.getRowDimension()) {

                        masterResult.setEntry(i,fullResult.getColumnDimension() + b, backgroundResult.getEntry(i, b));
                    }
                }

                masterResult.setEntry(i, masterResult.getColumnDimension()-2, i+1);
                masterResult.setEntry(i, masterResult.getColumnDimension()-1, i+1);


            }



            this.storedDataOutput = masterResult;
        } else {
            this.storedDataOutput = null;
        }

        this.storeImageOutput(chromosomeCentromereMask);
        this.storeImageOutput(chromosomeNonCentromereMask);

    }

}

