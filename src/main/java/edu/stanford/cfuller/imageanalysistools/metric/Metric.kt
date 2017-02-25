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

package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

import org.apache.commons.math3.linear.RealMatrix

/**
 * A quantitative measurement on an image or set of images with an associated mask of regions of interest.

 * @author Colin J. Fuller
 */

abstract class Metric {


    /**
     * Quantifies the Images.

     * This should always return a [RealMatrix] with one row per region of interest, where each column in a row corresponds to a
     * scalar quantification of some property of that region of interest for a given input image.

     * Thus, the size of the returned matrix should be (# of regions of interest) x (images.size()).

     * @param mask      A mask that specifies the region of interest.  Subclasses should specify whether this should be binary or
     * *                  have a unique identifier for each region of interest.
     * *
     * @param images    An ImageSet of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * *
     * @return          A RealMatrix containing all the quantified values; rows correspond to ROIs, columns to input Images.
     */
    abstract fun quantify(mask: Image, images: ImageSet): Quantification

}
