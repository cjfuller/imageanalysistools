package edu.stanford.cfuller.imageanalysistools.image

/**
 * A Class that swaps two dimensions of an Image.
 * @author Colin J. Fuller
 */
object DimensionFlipper {
    /**
     * Swaps the Z- and T- dimensions of an Image (these can get mixed up, e.g. while reading certain
     * metamorph stacks).
     * @param toFlip    The Image whose dimensions will be swapped.  (This will not be modified.)
     * @return            A new Image whose dimensions are swapped from the input.
     */
    fun flipZT(toFlip: Image): Image {
        return flip(toFlip, ImageCoordinate.Z, ImageCoordinate.T)
    }

    /**
     * Swaps two dimensions in an Image.
     * @param toFlip    The Image whose dimensions will be swapped.  (This will not be modified.)
     * @param dim0        The first dimension to swap.  This should correspond to one of the constants defined in ImageCoordinate (or rarely, a user-defined dimension).
     * @param dim1        The second dimension to swap.  This should correspond to one of the constants defined in ImageCoordinate (or rarely, a user-defined dimension).
     * @return            A new Image whose dimensions are swapped from the input.
     */
    fun flip(toFlip: Image, dim0: Int, dim1: Int): Image {
        val sizes = ImageCoordinate.cloneCoord(toFlip.dimensionSizes)
        val temp_1 = sizes[dim1]
        sizes[dim1] = sizes[dim0]
        sizes[dim0] = temp_1

        val newImage = ImageFactory.createWritable(sizes, 0.0f)
        val flipCoord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (ic in toFlip) {
            flipCoord.setCoord(ic)
            flipCoord[dim0] = ic[dim1]
            flipCoord[dim1] = ic[dim0]
            newImage.setValue(flipCoord, toFlip.getValue(ic))
        }
        flipCoord.recycle()
        sizes.recycle()
        return newImage
    }
}
