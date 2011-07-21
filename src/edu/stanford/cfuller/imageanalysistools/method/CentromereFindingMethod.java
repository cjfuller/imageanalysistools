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
import edu.stanford.cfuller.imageanalysistools.method.Method;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.clustering.ObjectClustering;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/**
 * Method to find centromeres in immunofluorescence images.
 * <p>
 * This method operates in two stages: first, a normalization stage, and second, a segmentation stage.
 * <p>
 * In the normalization stage, the image is locally background corrected
 * so that varying brightness of image objects has a reduced effect upon the size of the object in the segmentation, and
 * so that a relatively simple intensity thresholding method can be used to segment objects of different brightness.
 * <p>
 * In the segmentation stage, the objects are segmented based on normalized brightness, and size filtering is
 * applied according to user-specified sizes.
 * <p>
 * Optionally, centromere objects can be clustered into possible cells, and then these cells can be used as the basis
 * for a further (non-normalized) thresholding step in which any dim objects in each cell are removed (as are commonly
 * found for antibodies with high punctate background).
 * <p>
 * As per the specification in the {@link Method} class, the first image in the stored images will be used as the reference
 * image for segmentation and the remaining channels quantified.
 *
 * @author Colin J. Fuller
 *
 */


public class CentromereFindingMethod extends Method {

    Metric metric;


    /**
     * Sole constructor, which creates a default instance.
     */
    public CentromereFindingMethod() {
        this.metric = new edu.stanford.cfuller.imageanalysistools.metric.IntensityPerPixelMetric();
    }

    protected Image centromereFinding(Image input) {

        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

        filters.add(new MaximumSeparabilityThresholdingFilter());
        filters.add(new LabelFilter());
        filters.add(new RecursiveMaximumSeparabilityFilter());
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

    protected void normalizeInputImage(Image input) {

        RenormalizationFilter rnf = new RenormalizationFilter();
        rnf.setParameters(this.parameters);
        rnf.apply(input);


    }


    /**
     *
     * Runs the centromere finding method using the stored images and parameters.
     *
     */
    @Override
    public void go() {

        SizeAbsoluteFilter SELF = new SizeAbsoluteFilter();
        SimpleThresholdingFilter ImThF = new SimpleThresholdingFilter();
        LabelFilter LF = new LabelFilter();
        RelabelFilter RLF = new RelabelFilter();
        MaskFilter MF = new MaskFilter();


        SELF.setParameters(this.parameters);
        ImThF.setParameters(this.parameters);
        LF.setParameters(this.parameters);
        RLF.setParameters(this.parameters);

        Image normalized = new Image(this.images.get(0));


        this.normalizeInputImage(normalized);


        Image groupMask = centromereFinding(normalized);

        this.clearImageOutput();

        ImThF.setReferenceImage(normalized);

        SELF.apply(groupMask);

        ImThF.apply(groupMask);

        LF.apply(groupMask);
        Image allCentromeres = new Image(groupMask);

        Histogram h = new Histogram(groupMask);

        //clustering

        if(this.parameters.hasKeyAndTrue("use_clustering")) {
            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Filtering");

            Image gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask);

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Done filtering");

            boolean decreaseBackground = Boolean.parseBoolean(this.parameters.getValueForKey("decrease_speckle_background"));

            decreaseBackground = decreaseBackground || (this.parameters.hasKey("maximum_number_of_centromeres") && this.parameters.getIntValueForKey("maximum_number_of_centromeres") < h.getMaxValue());


            if (!decreaseBackground) {
                if (this.parameters.hasKeyAndTrue("use_basic_clustering")) {
                    groupMask.copy(ObjectClustering.doBasicClustering(groupMask, normalized, gaussianFilteredMask));
                } else {
                    ObjectClustering.doComplexClustering(groupMask, normalized, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask);
                }
            } else {

                Image output = ObjectClustering.doBasicClustering(groupMask, normalized, new Image(gaussianFilteredMask));

                //output.writeToFile("/Users/cfuller/Desktop/filter_intermediates/basic.ome.tif");

                RegionThresholdingFilter rtf = new RegionThresholdingFilter();
                MaximumSeparabilityThresholdingFilter mstf_clustering = new MaximumSeparabilityThresholdingFilter();

                rtf.setThresholdingFilter(mstf_clustering);
                rtf.setParameters(this.parameters);
                Image ch0_copy = new Image(this.images.get(0));

                Histogram h_clustered = new Histogram(output);

                Image singleClusterTemp = new Image(output);

                for (int c = 1; c<= h_clustered.getMaxValue(); c++) {

                    for (ImageCoordinate i : singleClusterTemp) {

                        if (output.getValue(i) ==  c) {
                            singleClusterTemp.setValue(i,c);
                            ch0_copy.setValue(i, this.images.get(0).getValue(i));
                        } else {
                            singleClusterTemp.setValue(i, 0);
                            ch0_copy.setValue(i,0);
                        }

                    }

                    rtf.setReferenceImage(ch0_copy);
                    LF.apply(singleClusterTemp);
                    rtf.apply(singleClusterTemp);

                    for (ImageCoordinate i : singleClusterTemp) {
                        if (output.getValue(i) == c && singleClusterTemp.getValue(i) == 0) {
                            output.setValue(i, 0);
                        }
                    }

                }

                LF.apply(output);

                //output.writeToFile("/Users/cfuller/Desktop/filter_intermediates/afterspeckle.ome.tif");

                MF.setReferenceImage(output);

                MF.apply(groupMask);

                gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask);

                if (this.parameters.hasKey("use_basic_clustering") && this.parameters.getBooleanValueForKey("use_basic_clustering")) {
                    groupMask.copy(ObjectClustering.doBasicClustering(groupMask, normalized, gaussianFilteredMask));
                } else {
                    ObjectClustering.doComplexClustering(groupMask, normalized, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask);
                }

            }

            MF.setReferenceImage(groupMask);
            MF.apply(allCentromeres);
            RLF.apply(allCentromeres);

            if (Integer.parseInt(parameters.getValueForKey("minimum_cluster_size")) > 0) {

                Histogram h_clustered = new Histogram(groupMask);

                java.util.Vector<java.util.HashSet<Integer> > clusterContents = new java.util.Vector<java.util.HashSet<Integer> >();

                for (int i =0; i < h_clustered.getMaxValue()+1; i++) {

                    clusterContents.add(new java.util.HashSet<Integer>() );

                }


                for (ImageCoordinate i : groupMask) {

                    if (groupMask.getValue(i) > 0 && allCentromeres.getValue(i) > 0) {

                        clusterContents.get((int) (groupMask.getValue(i))).add((int) (allCentromeres.getValue(i)));

                    }

                }

                for (ImageCoordinate i : groupMask) {
                    if (groupMask.getValue(i) > 0 && clusterContents.get((int) groupMask.getValue(i)).size() < Integer.parseInt(this.parameters.getValueForKey("minimum_cluster_size"))) {
                        groupMask.setValue(i, 0);
                    }
                }

                RLF.apply(groupMask);

            }

            MF.setReferenceImage(groupMask);
            MF.apply(allCentromeres);
            RLF.apply(allCentromeres);

        }

        RLF.apply(groupMask);

        //groupMask.writeToFile("/Users/cfuller/Desktop/filter_intermediates/post-cluster.ome.tif");

        this.storeImageOutput(groupMask);

        Image allCentromeresCopy = new Image(allCentromeres);

        this.storeImageOutput(allCentromeresCopy);

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Background correction");

        Image backgroundMask = new Image(groupMask);


        ConvexHullByLabelFilter chblf = new ConvexHullByLabelFilter();

        chblf.setReferenceImage(allCentromeres);
        chblf.apply(backgroundMask);

        for (ImageCoordinate c : backgroundMask) {
            if (allCentromeres.getValue(c) > 0) backgroundMask.setValue(c, 0);
        }

        if (this.parameters.hasKey("use_clustering") && Boolean.parseBoolean(this.parameters.getValueForKey("use_clustering"))) {

            BackgroundEstimationFilter BEF = new BackgroundEstimationFilter();

            BEF.setReferenceImage(this.images.get(0));

            BEF.apply(backgroundMask);

        }

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Quantification");


        RealMatrix fullResult = metric.quantify(allCentromeres, this.images);

        if (fullResult == null) {
            this.storedDataOutput = null;
            return;
        }

        RealMatrix backgroundResult = null;

        if (this.parameters.hasKey("use_clustering") && Boolean.parseBoolean(this.parameters.getValueForKey("use_clustering"))) {

            backgroundResult = metric.quantify(backgroundMask, this.images);

        }

        if (backgroundResult == null) { // either not using clustering of the quantification failed due to no ROIs

            backgroundResult = new Array2DRowRealMatrix(fullResult.getRowDimension(), fullResult.getColumnDimension());

            backgroundResult = backgroundResult.scalarMultiply(0.0);

        }
        
        if (backgroundResult != null) {
    		this.parameters.setValueForKey("background_calculated", "true");
        }


        RealMatrix masterResult = new org.apache.commons.math.linear.Array2DRowRealMatrix(fullResult.getRowDimension(), fullResult.getColumnDimension() + Integer.parseInt(this.parameters.getValueForKey("number_of_channels")) + 2);

        int[] resultMap = new int[fullResult.getRowDimension()];


        //TODO: consider new return type for the quantification (XML document?  How to read info?)


        for (ImageCoordinate i : allCentromeres) {

            int value = (int) allCentromeres.getValue(i);

            if (value > 0) {
                resultMap[value - 1] = (int) groupMask.getValue(i);
            }

        }

        for (int i = 0; i < fullResult.getRowDimension(); i++) {

            for (int j = 0; j < fullResult.getColumnDimension(); j++) {

                masterResult.setEntry(i, j, fullResult.getEntry(i, j));

            }

            for (int b = 0; b < Integer.parseInt(this.parameters.getValueForKey("number_of_channels")); b++) {

                if (resultMap[i]-1 < backgroundResult.getRowDimension() && resultMap[i] > 0 && b < backgroundResult.getColumnDimension()) {
                    masterResult.setEntry(i,fullResult.getColumnDimension() + b, backgroundResult.getEntry(resultMap[i]-1, b));
                } else {
                    masterResult.setEntry(i,fullResult.getColumnDimension() + b, 0);
                }

            }

            masterResult.setEntry(i, masterResult.getColumnDimension()-2, i+1);
            masterResult.setEntry(i, masterResult.getColumnDimension()-1, resultMap[i]);


        }

        this.storedDataOutput = masterResult;

    }

}
