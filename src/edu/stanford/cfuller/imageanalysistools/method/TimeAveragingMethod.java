package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.TimeAveragingFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 2/18/11
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
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
