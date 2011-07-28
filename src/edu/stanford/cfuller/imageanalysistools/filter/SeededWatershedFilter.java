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

/**
 * A seeded version of the {@link WatershedFilter}.  This Filter differs from the standard WatershedFilter in that it will start
 * from a specified seed Image, rather than just from the lowest intensity pixels, and it will construct a barrier between two regions only if
 * those regions each contain a seed.
 *
 * @author Colin J. Fuller
 */

public class SeededWatershedFilter extends WatershedFilter implements SeededFilter {

    int maxSeedLabel = 0;

    boolean flaggedForMerge;

    MergeFilter mgf;

    /**
     * Sets the seed Image to the specified Image.  This will not be modified.
     * @param im    The seed Image to be used as the starting point for the watershed algorithm.
     */
    public void setSeed(Image im) {
        this.seedImage = im;
        Histogram h = new Histogram(im);

        this.maxSeedLabel = h.getMaxValue();
        this.flaggedForMerge = false;
        this.mgf = new MergeFilter();

    }

    /**
     * Reimplements the method for getting the correct label for a specified pixel that is present in the WatershedFilter
     * in order to only construct a barrier between regions when those regions contain a seed region.
     * @param ic    The ImageCoordinate that this method finds the correct label for.
     * @param processing    The Image mask that is being created by the watershed algorithm.
     * @param nextLabel     The next label available for a new region, in case this pixel should start a new region.
     * @return              The correct label for the specified coordinate... 0 if it should be a barrier pixel, the label of an existing region
     *                      if it belongs in that region, or nextLabel if it should start a new region.
     */
    protected int getCorrectLabel(ImageCoordinate ic,  Image processing, int nextLabel) {

        if (this.flaggedForMerge) {
            this.mgf.apply(processing);
            this.flaggedForMerge = false;
        }
        
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
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }
        
        ic2.set("x",x - 1);
        ic2.set("y",y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.set("x",x - 1);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.set("x",x);
        ic2.set("y",y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.set("x",x);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }
        
        ic2.set("x",x + 1);
        ic2.set("y",y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.set("x",x + 1);
        ic2.set("y",y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.set("x",x + 1);
        ic2.set("y",y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        if (neighbor > 0) {
            ic2.recycle();
            return neighbor;
        }

        ic2.recycle();
        return nextLabel;

    }


}
