/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2013 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.metric;

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;

import edu.stanford.cfuller.imageanalysistools.filter.morph.ErosionFilter;

/**
 * A metric that takes each region in a mask and calculates its area and perimeter.
 * 
 * For regions with holes, the perimeter will be the total boundary size both to the
 * outside of the region and to the holes.
 * 
 * Area and perimeter are both calculated in units of pixels (# of pixels in the
 * region and # of pixels on the border, respectively)
 *
 * @author Colin J. Fuller
 */


public class AreaAndPerimeterMetric extends Metric {


    /**
     * Quantifies the area and perimeter for each region in a 2D image.
     *
     * If no regions of interest are present in the supplied mask, this will return null.
     *
     * @param mask      A mask that specifies the region of interest.  This should have regions of interest uniquely labeled consecutively, starting with the value 1, as might be produced by a {@link edu.stanford.cfuller.imageanalysistools.filter.LabelFilter}.
     * @param images    An ImageSet of Images to be quantified; this will be ignored except for using the name of the marker image.
     * @return          A Quantification containing measurements for area and perimeter.
     */
  @Override
  public Quantification quantify(Image mask, ImageSet images) {
    
    Histogram h = new Histogram(mask);
    
    if (h.getMaxValue() == 0) return null;

    Quantification q = new Quantification();

    for (int i = 0; i < h.getMaxValue(); i++) {
      Measurement m = new Measurement(true, i+1, h.getCounts(i+1), "area", Measurement.TYPE_SIZE, images.getMarkerImageName());
      q.addMeasurement(m);
    }
    
    WritableImage eroded = ImageFactory.createWritable(mask);

    int[] dims = {ImageCoordinate.X, ImageCoordinate.Y};
    ErosionFilter ef = new ErosionFilter();
    ef.setStructuringElement(ErosionFilter.getDefaultElement(dims));

    ef.apply(eroded);

    for (ImageCoordinate ic : mask) {
      if (mask.getValue(ic) > 0 && eroded.getValue(ic) == 0) {
        eroded.setValue(ic, mask.getValue(ic));
      } else {
        eroded.setValue(ic, 0);
      }
    }

    Histogram hPerim = new Histogram(eroded);

    for (int i = 0; i < hPerim.getMaxValue(); i++) {
      Measurement m = new Measurement(true, i+1, hPerim.getCounts(i+1), "perimeter", Measurement.TYPE_SIZE, images.getMarkerImageName());
      q.addMeasurement(m);
    }

    return q;
    
  }

}


