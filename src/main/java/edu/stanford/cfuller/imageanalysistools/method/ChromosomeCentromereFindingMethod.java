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

import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
import edu.stanford.cfuller.imageanalysistools.metric.Measurement;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;
import edu.stanford.cfuller.imageanalysistools.metric.Quantification;


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
        WritableImage chromosomeCentromereMask = ImageFactory.createWritable(ch0_method.getStoredImage());

        mf.apply(chromosomeCentromereMask);

        //now remove all these regions from the chromosome mask

        WritableImage chromosomeNonCentromereMask = ImageFactory.createWritable(ch0_method.getStoredImage());

        for (ImageCoordinate ic : chromosomeNonCentromereMask) {

            if (chromosomeCentromereMask.getValue(ic) > 0) {


                chromosomeNonCentromereMask.setValue(ic, 0);
            }


        }

        Quantification fullResult = metric.quantify(chromosomeCentromereMask, this.imageSet);

        Quantification chromosomeResult = metric.quantify(chromosomeNonCentromereMask, this.imageSet);

        
        
        if (fullResult != null && chromosomeResult != null) {
        	
        	for (Measurement m : chromosomeResult.getAllMeasurements()) {
            	
            	fullResult.addMeasurement(new Measurement(m.hasAssociatedFeature(), m.getFeatureID(), m.getMeasurement(), m.getMeasurementName() + "_chromosome", m.getMeasurementType(), m.getImageID() ));
            	
            }

            this.storedDataOutput = fullResult;
            
        } else {
        	
            this.storedDataOutput = null;
            
        }

        this.storeImageOutput(chromosomeCentromereMask);
        this.storeImageOutput(chromosomeNonCentromereMask);

    }

}
