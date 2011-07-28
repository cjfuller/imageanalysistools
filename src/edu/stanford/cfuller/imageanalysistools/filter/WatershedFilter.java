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

import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import org.apache.commons.math.geometry.Vector3D;

import java.util.Hashtable;

/**
 * A Filter that segments an Image according to the standard Watershed method.  The Image is first inverted, so that the
 * algorithm will find regions starting from high-intensity areas (which is generally more applicable for Image segmentation).
 * <p>
 * Unlike some implementations, this does not find the gradient of the supplied Image, so if segmentation of the gradient is desired
 * it must be prefiltered.
 * <p>
 * This Filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image to be segmented by the watershed algorithm.
 *
 *
 * @author Colin J. Fuller
 *
 */

public class WatershedFilter extends Filter {

    protected Image seedImage;

    /**
     * Constructs a new WatershedFilter.
     */
    public WatershedFilter() {
        this.seedImage = null;
    }


    /**
     * Applies the WatershedFilter to the specified Image, segmenting it.
     * @param im    The Image to be segmented.
     */
	@Override
	public void apply(Image im) {

        Image imCopy = new Image(im);

        InversionFilter invf = new InversionFilter();

        invf.apply(imCopy);

        Histogram h = new Histogram(imCopy);

        java.util.Hashtable<Double, java.util.Vector<Vector3D> > greylevelLookup = new Hashtable<Double, java.util.Vector<Vector3D> >();

        for (ImageCoordinate ic : imCopy) {
            double value = imCopy.getValue(ic);

            if (greylevelLookup.get(value) == null) {
                greylevelLookup.put(value, new java.util.Vector<Vector3D>());
            }

            greylevelLookup.get(value).add(new Vector3D(ic.get("x"), ic.get("y"), ic.get("z")));


        }

        Image processing = getSeedImage(greylevelLookup, imCopy, h);

        Histogram hSeed = new Histogram(processing);

        int nextLabel = hSeed.getMaxValue() + 1;

        ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);


        for (int i = h.getMinValue() + 1; i < h.getMaxValue(); i++) {

            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("processing greylevel: " + i);

            if (h.getCounts(i) == 0) continue;

            for (Vector3D v : greylevelLookup.get((double) i)) {

                int x = (int) v.getX();
                int y = (int) v.getY();
                int z = (int) v.getZ();

                ic.set("x",x);
                ic.set("y",y);
                ic.set("z",z);

                int label = getCorrectLabel(ic, processing, nextLabel);

                processing.setValue(ic, label);

                if (label == nextLabel) nextLabel++;

                

            }
            
        }

        ic.recycle();

        MaskFilter mf = new MaskFilter();

        mf.setReferenceImage(processing);
        mf.apply(im);

        LabelFilter lf = new LabelFilter();

        lf.apply(im);



	}

    /**
     * Gets the seed Image for the watershed segmentation.  If no seed Image has been set externally, one is created from
     * the set of pixels at the lowest greylevel.  If a seed Image has been set externally, the seedImage retrieved is the union of the external
     * Image and the default one.
     * 
     * @param greylevelLookup       A hashtable mapping greylevel values in the Image to coordinates in the Image.
     * @param im                    The Image being segmented.
     * @param h                     A Histogram of the Image being segmented, constructed before the beginning of segmentation.
     * @return                      An Image containing the seeds for the watershed algorithm.
     */
    protected Image getSeedImage(java.util.Hashtable<Double, java.util.Vector<Vector3D> > greylevelLookup, Image im, Histogram h) {

        Image tempSeed = this.seedImage;
        if (tempSeed == null) tempSeed = new Image(im.getDimensionSizes(), 0.0);

        double minValue = h.getMinValue();

        java.util.Vector<Vector3D> minPoints = greylevelLookup.get(minValue);

        ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

        for (Vector3D v : minPoints) {

            ic.set("x",(int) (v.getX()));
            ic.set("y",(int) (v.getY()));
            ic.set("z",(int) (v.getZ()));

            tempSeed.setValue(ic, 1);
            
        }

        ic.recycle();

        LabelFilter lf = new LabelFilter();

        lf.apply(tempSeed);

        return tempSeed;


    }

    /**
     * Gets the correct labeling in the segmentation for a pixel at a given coordinate, given a segmentation in progress.
     * This will label a pixel as 0 (a barrier) if it would connect two existing regions, label it as an existing region if it is connected
     * only to that region, or as nextLabel if it is not connected to any existing region.
     * 
     * @param ic            The ImageCoordinate that is the location to be labeled.
     * @param processing    The partial segmentation of the Image.
     * @param nextLabel     The next available label for new regions.
     * @return              The appropriate value for the ImageCoordinate: one of 0, an existing region number, or nextLabel.
     */
    protected int getCorrectLabel(ImageCoordinate ic,  Image processing, int nextLabel) {

        int x = ic.get("x");
        int y = ic.get("y");

        double currValue = processing.getValue(ic);

        if (currValue > 0) {return (int) currValue;}

        //check 8-connected neighbors in the plane

        int neighbor = 0;

        ImageCoordinate ic2 = ImageCoordinate.cloneCoord(ic);

        ic2.set("x",x - 1);
        ic2.set("y",y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x - 1);
        ic2.set("y",y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x - 1);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x);
        ic2.set("y",y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x + 1);
        ic2.set("y",y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x + 1);
        ic2.set("y",y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        ic2.set("x",x + 1);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor) {ic2.recycle(); return 0;}
            neighbor = tempNeighbor;
        }

        if (neighbor > 0) {ic2.recycle(); return neighbor;}

        ic2.recycle();
        return nextLabel;

    }

}
