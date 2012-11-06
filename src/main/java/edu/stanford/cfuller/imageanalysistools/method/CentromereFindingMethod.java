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

import java.util.List;

import edu.stanford.cfuller.imageanalysistools.filter.*;
import edu.stanford.cfuller.imageanalysistools.method.Method;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.clustering.ObjectClustering;
import edu.stanford.cfuller.imageanalysistools.metric.Measurement;
import edu.stanford.cfuller.imageanalysistools.metric.Metric;
import edu.stanford.cfuller.imageanalysistools.metric.Quantification;
import edu.stanford.cfuller.imageanalysistools.metric.ZeroMetric;


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

		filters.add(new LocalMaximumSeparabilityThresholdingFilter());
		filters.add(new LabelFilter());
		filters.add(new RecursiveMaximumSeparabilityFilter());
		filters.add(new RelabelFilter());
		filters.add(new SizeAbsoluteFilter());
		filters.add(new RelabelFilter());

		for (Filter i : filters){
			i.setParameters(this.parameters);
			i.setReferenceImage(this.images.get(0));
		}

		WritableImage toProcess = ImageFactory.createWritable(input);

		iterateOnFiltersAndStoreResult(filters, toProcess, metric);

		return this.getStoredImage();
	}

	protected void normalizeInputImage(WritableImage input) {

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
		this.parameters.setValueForKey("DEBUG", "false");

		SizeAbsoluteFilter SELF = new SizeAbsoluteFilter();
		SimpleThresholdingFilter ImThF = new SimpleThresholdingFilter();
		LabelFilter LF = new LabelFilter();
		RelabelFilter RLF = new RelabelFilter();
		MaskFilter MF = new MaskFilter();


		SELF.setParameters(this.parameters);
		ImThF.setParameters(this.parameters);
		LF.setParameters(this.parameters);
		RLF.setParameters(this.parameters);

		WritableImage normalized = ImageFactory.createWritable(this.images.get(0));

		this.normalizeInputImage(normalized);

		BandpassFilter bf = new BandpassFilter();

		bf.setParameters(this.parameters);

		final float band_lower = 3.0f;
		final float band_upper = 4.0f;

		bf.setBand(band_lower, band_upper);

		bf.apply(normalized);

		WritableImage groupMask = ImageFactory.createWritable(centromereFinding(normalized));

		this.clearImageOutput();

		ImThF.setReferenceImage(normalized);

		SELF.apply(groupMask);

		ImThF.apply(groupMask);

		LF.apply(groupMask);
		WritableImage allCentromeres = ImageFactory.createWritable(groupMask);

		Histogram h = new Histogram(groupMask);

		//clustering

		if (this.parameters.hasKeyAndTrue("use_clustering")) {

			if (this.parameters.hasKeyAndTrue("cluster_by_DNA")) {

				int dnaChannel = 0;
				if (this.parameters.hasKey("DNA_channel")) {
					dnaChannel = this.parameters.getIntValueForKey("DNA_channel");
				}

				Image dnaImage = this.imageSet.getImageForIndex(dnaChannel);

				MaximumSeparabilityThresholdingFilter mstf = new MaximumSeparabilityThresholdingFilter();
				LabelFilter lf = new LabelFilter();
				MaskFilter mf = new MaskFilter();

				lf.setParameters(this.parameters);
				mstf.setParameters(this.parameters);

				groupMask = ImageFactory.createWritable(dnaImage);

				mstf.apply(groupMask);
				lf.apply(groupMask);

				mf.setReferenceImage(allCentromeres);

				mf.apply(groupMask);


			} else  {

				WritableImage gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask);

				boolean decreaseBackground = Boolean.parseBoolean(this.parameters.getValueForKey("decrease_speckle_background"));

				decreaseBackground = decreaseBackground || (this.parameters.hasKey("maximum_number_of_centromeres") && this.parameters.getIntValueForKey("maximum_number_of_centromeres") < h.getMaxValue());


				if (!decreaseBackground) {
					if (this.parameters.hasKeyAndTrue("use_basic_clustering")) {
						groupMask.copy(ObjectClustering.doBasicClustering(groupMask, normalized, gaussianFilteredMask));
					} else {
						ObjectClustering.doComplexClustering(groupMask, normalized, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask);
					}
				} else {

					WritableImage output = ImageFactory.createWritable(ObjectClustering.doBasicClustering(groupMask, normalized, ImageFactory.create(gaussianFilteredMask)));

					RegionThresholdingFilter rtf = new RegionThresholdingFilter();
					MaximumSeparabilityThresholdingFilter mstf_clustering = new MaximumSeparabilityThresholdingFilter();

					rtf.setThresholdingFilter(mstf_clustering);
					rtf.setParameters(this.parameters);
					WritableImage ch0_copy = ImageFactory.createWritable(this.images.get(0));

					Histogram h_clustered = new Histogram(output);

					WritableImage singleClusterTemp = ImageFactory.createWritable(output);

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

					MF.setReferenceImage(output);

					MF.apply(groupMask);

					gaussianFilteredMask = ObjectClustering.gaussianFilterMask(groupMask);

					if (this.parameters.hasKey("use_basic_clustering") && this.parameters.getBooleanValueForKey("use_basic_clustering")) {
						groupMask.copy(ObjectClustering.doBasicClustering(groupMask, normalized, gaussianFilteredMask));
					} else {
						ObjectClustering.doComplexClustering(groupMask, normalized, Integer.parseInt(this.parameters.getValueForKey("maximum_clustering_iterations")), gaussianFilteredMask);
					}

				}

			}

			MF.setReferenceImage(groupMask);
			MF.apply(allCentromeres);
			RLF.apply(allCentromeres);

			if (parameters.hasKey("minimum_cluster_size") && Integer.parseInt(parameters.getValueForKey("minimum_cluster_size")) > 0) {

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

		this.storeImageOutput(groupMask);

		Image allCentromeresCopy = ImageFactory.create(allCentromeres);

		this.storeImageOutput(allCentromeresCopy);

		//background estimation

		WritableImage backgroundMask = ImageFactory.createWritable(groupMask);

		ConvexHullByLabelFilter chblf = new ConvexHullByLabelFilter();

		chblf.setReferenceImage(allCentromeres);
		chblf.apply(backgroundMask);

		for (ImageCoordinate c : backgroundMask) {
			if (allCentromeres.getValue(c) > 0) backgroundMask.setValue(c, 0);
		}

		if (this.parameters.hasKeyAndTrue("use_clustering")) {

			BackgroundEstimationFilter BEF = new BackgroundEstimationFilter();

			BEF.setReferenceImage(this.images.get(0));

			BEF.apply(backgroundMask);

		}

		//generate output

		Quantification fullResult = metric.quantify(allCentromeres, this.imageSet);

		if (fullResult == null) {
			this.storedDataOutput = null;
			return;
		}

		Quantification backgroundResult = null;

		if (this.parameters.hasKeyAndTrue("use_clustering")) {

			backgroundResult = metric.quantify(backgroundMask, this.imageSet);

		}

		if (backgroundResult == null) { // either not using clustering or the quantification failed due to no ROIs

			backgroundResult = (new ZeroMetric()).quantify(backgroundMask, this.imageSet);

		}

		if (backgroundResult != null) {
			this.parameters.setValueForKey("background_calculated", "true");
		}


		int[] resultMap = new int[Histogram.findMaxVal(allCentromeres)];


		for (ImageCoordinate i : allCentromeres) {

			int value = (int) allCentromeres.getValue(i);

			if (value > 0) {
				resultMap[value - 1] = (int) groupMask.getValue(i);
			}

		}

		for (int i = 0; i < resultMap.length; i++) {

			fullResult.addMeasurement(new Measurement(true, i+1, resultMap[i], "cell_id", Measurement.TYPE_GROUPING, this.imageSet.getMarkerImageName()));

			List<Measurement> background = backgroundResult.getAllMeasurementsForRegion(resultMap[i]);

			for (Measurement m : background) {

				if (m.getMeasurementType() == Measurement.TYPE_INTENSITY) {

					fullResult.addMeasurement(new Measurement(true, i+1, m.getMeasurement(), m.getMeasurementName(), Measurement.TYPE_BACKGROUND, this.imageSet.getMarkerImageName()));

				}

			}

		}



		this.storedDataOutput = fullResult;

	}

}