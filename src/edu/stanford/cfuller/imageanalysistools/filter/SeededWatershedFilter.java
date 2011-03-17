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
        
        int x = ic.getX();
        int y = ic.getY();

        double currValue = processing.getValue(ic);

        if (currValue > 0) {return (int) currValue;}

        //check 8-connected neighbors in the plane

        int neighbor = 0;

        ImageCoordinate ic2 = ImageCoordinate.cloneCoord(ic);

        ic2.setX(x - 1);
        ic2.setY(y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }
        
        ic2.setX(x - 1);
        ic2.setY(y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.setX(x - 1);
        ic2.setY(y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.setX(x);
        ic2.setY(y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.setX(x);
        ic2.setY(y + 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }
        
        ic2.setX(x + 1);
        ic2.setY(y - 1);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.setX(x + 1);
        ic2.setY(y);

        if (processing.inBounds(ic2)) {
            int tempNeighbor = (int) processing.getValue(ic2);
            if (neighbor > 0 && tempNeighbor > 0 && neighbor != tempNeighbor && neighbor <= this.maxSeedLabel && tempNeighbor <= this.maxSeedLabel) {ic2.recycle(); return 0;}
            if (neighbor != tempNeighbor && neighbor > 0 && tempNeighbor > 0 && (neighbor <= maxSeedLabel || tempNeighbor <= maxSeedLabel)) {this.flaggedForMerge = true;}
            if (neighbor <= 0 || (tempNeighbor < neighbor && tempNeighbor > 0)) neighbor = tempNeighbor;
        }

        ic2.setX(x + 1);
        ic2.setY(y + 1);

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
