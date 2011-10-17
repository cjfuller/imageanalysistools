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

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimationFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GradientFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;


/**
 * A Filter that normalizes an Image by local intensity so that features that occur in a dim region of an image will be
 * approximately the same brightness as features in a bright part of an Image.
 * <p>
 * This may be useful, for instance, if multiple cells in an Image both have fluorescence signal that needs to be segmented
 * but are expressing a protein being localized to vastly different levels, which might otherwise cause the lower-expressing cell
 * to be removed from the Image by the segmentation routine.  This is also useful for ensuring that objects of differing brightness
 * are not segmented to be radically different sizes by a segmentation method.
 * <p>
 * The general approach uses median filtering to estimate the local intensity in an Image, and then uses a gradient filter
 * to remove artificial edges due to the size of the median filter.
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image that is to be intensity normalized.  This Image will be overwritten
 * by the normalized version.
 *
 * @author Colin J. Fuller
 */
public class RenormalizationFilter extends Filter {

    /**
     * Applies the filter to an Image, normalizing its intensity.
     * @param im    The Image to be normalized; will be overwritten.
     */
	@Override
	public void apply(Image im) {

		this.referenceImage = new Image(im);
		
		Image input = this.referenceImage;
		Image output = im;

		LocalBackgroundEstimationFilter LBEF = new LocalBackgroundEstimationFilter();		
		
		LBEF.setParameters(this.params);
		LBEF.setReferenceImage(input);
		LBEF.setBoxSize((int) Math.ceil(0.5*Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size")))));
		LBEF.apply(output);
		
		Image gradient = new Image(output);

		GradientFilter GF = new GradientFilter();
		
		GF.apply(gradient);
		GF.apply(gradient);
		
		GaussianFilter GAUF = new GaussianFilter();
		
		GAUF.setWidth((int) (2*Math.ceil(Math.sqrt(Integer.parseInt(this.params.getValueForKey("max_size"))))));

		GAUF.apply(gradient);
		
		Image tempStorage = new Image(input.getDimensionSizes(), 0.0);
		
		double maxValue = 0.0;
		double minValue = Double.MAX_VALUE;
		
		Histogram h = new Histogram(input);
		
		for (ImageCoordinate i : output) {
			
			double denom = output.getValue(i) + gradient.getValue(i);
			denom = (denom < 1) ? 1 : denom;
			
			if (input.getValue(i)/denom > 0) {
				tempStorage.setValue(i, Math.log(input.getValue(i)/denom));
			} 
			
			if (tempStorage.getValue(i) > maxValue) maxValue = tempStorage.getValue(i);
			if (tempStorage.getValue(i) < minValue) minValue = tempStorage.getValue(i);
			
		}
		
		double sumValue = 0;
		
		for (ImageCoordinate i : output) {
			
			double tempValue = tempStorage.getValue(i);
			
			tempValue = (tempValue - minValue)/(maxValue - minValue) * h.getMaxValue();
			
			if (maxValue == minValue || tempValue < 0) tempValue = 0;
			
			sumValue += tempValue;
			
			output.setValue(i, Math.floor(tempValue));
			
		}
		
		sumValue /= (output.getDimensionSizes().get(ImageCoordinate.X) * output.getDimensionSizes().get(ImageCoordinate.Y));
		
		for (ImageCoordinate i : output) {
			double tempValue = output.getValue(i) - sumValue;
			if (tempValue < 0) tempValue = 0;
			output.setValue(i, tempValue);
		}
		
	}

}
