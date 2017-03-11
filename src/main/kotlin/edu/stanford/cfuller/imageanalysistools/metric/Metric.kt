package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

import org.apache.commons.math3.linear.RealMatrix

/**
 * A quantitative measurement on an image or set of images with an associated mask of regions of interest.

 * @author Colin J. Fuller
 */
interface Metric {
    /**
     * Quantifies the Images.
     * This should always return a [RealMatrix] with one row per region of interest, where each column in a row corresponds to a
     * scalar quantification of some property of that region of interest for a given input image.
     * Thus, the size of the returned matrix should be (# of regions of interest) x (images.size()).
     * @param mask      A mask that specifies the region of interest.  Subclasses should specify whether this should be binary or
     *                  have a unique identifier for each region of interest.
     * @param images    An ImageSet of Images to be quantified using the same masks (perhaps corresponding to different color channels, for example).
     * @return          A RealMatrix containing all the quantified values; rows correspond to ROIs, columns to input Images.
     */
    fun quantify(mask: Image, images: ImageSet): Quantification
}
