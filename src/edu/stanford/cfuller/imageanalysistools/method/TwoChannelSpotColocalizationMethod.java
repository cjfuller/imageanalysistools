package edu.stanford.cfuller.imageanalysistools.method;

import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
import edu.stanford.cfuller.imageanalysistools.image.Image;

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

        ch0_method.setImages(this.images);
        ch0_method.setParameters(this.parameters);

        Vector<Image> reordered = new Vector<Image>();

        reordered.addAll(this.images);

        Image ch0 =reordered.remove(0);
        Image ch1 =reordered.remove(0);

        reordered.insertElementAt(ch0, 0);
        reordered.insertElementAt(ch1, 0);

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
