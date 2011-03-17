package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A filter that averages an Image over all points in the time dimension.
 * <p>
 * The reference Image for this filter should be set to the time series Image to be averaged.
 * <p>
 * The argument to the apply method should be an Image of all zeros that will be overwritten with the same dimension sizes as the time series,
 * except for only containing a single time point.
 *
 *
 */
public class TimeAveragingFilter extends Filter{

    /**
     * Time-averages the reference Image, overwriting the argument to this method with the result of the averaging.
     * @param output    An Image containing all zeros that will be overwritten that has the same dimension sizes as the reference Image, but a singleton time dimension.
     */
    public void apply(Image output) {

        Image im = this.referenceImage;

        int size_t = im.getDimensionSizes().getT();

        for (ImageCoordinate ic : im) {
            
            ImageCoordinate ic_t = ImageCoordinate.cloneCoord(ic);

            ic_t.setT(0);

            output.setValue(ic_t, output.getValue(ic_t) + im.getValue(ic)/size_t);

            ic_t.recycle();
        }


    }



}
