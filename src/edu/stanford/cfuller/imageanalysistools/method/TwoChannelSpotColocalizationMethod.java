package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 2/21/11
 * Time: 6:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwoChannelSpotColocalizationMethod extends Method {

    public void go() {

        CentromereFindingMethod ch0_method = new CentromereFindingMethod();
        CentromereFindingMethod ch1_method = new CentromereFindingMethod();

        ch0_method.setImages(this.imageSet);
        ch0_method.setParameters(this.parameters);

        ImageSet reordered = new ImageSet(this.imageSet);

        reordered.setMarkerImage(1);

        ch1_method.setImages(reordered);

        ch1_method.setParameters(this.parameters);

        ch0_method.go();
        ch1_method.go();

        MaskFilter mf = new MaskFilter();

        mf.setReferenceImage(ch0_method.getStoredImage());
        Image mask = ch1_method.getStoredImage();

        mf.apply(mask);

        this.storeImageOutput(mask);

    }

}
