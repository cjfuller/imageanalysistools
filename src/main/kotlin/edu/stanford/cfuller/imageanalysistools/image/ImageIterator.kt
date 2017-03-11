package edu.stanford.cfuller.imageanalysistools.image

/**
 * This class implements the Iterator required for foreach-style looping through the coordinates in an [Image]
 * @author Colin J. Fuller
 */

open class ImageIterator(im: Image) : Iterator<ImageCoordinate> {
    internal var toIterate: Image = im
    internal var currCoord: ImageCoordinate = ImageCoordinate.createCoordXYZCT(-1, 0, 0, 0, 0)
    internal var sizes: ImageCoordinate = im.dimensionSizes
    internal var lowerBounds: ImageCoordinate = ImageIterator.zeroCoord
    internal var isBoxedIterator: Boolean = false

    init {
        if (im.isBoxed) {
            isBoxedIterator = true
            currCoord.recycle()
            currCoord = ImageCoordinate.cloneCoord(im.boxMin!!)
            currCoord[ImageCoordinate.X] = currCoord[ImageCoordinate.X] - 1
            sizes = toIterate.boxMax!!
            lowerBounds = im.boxMin!!
        }

        //deal with the possibility of a dimension of size zero or an initial coordinate at the box size
        for (i in sizes) {
            if (sizes[i] == 0 || currCoord[i] >= sizes[i]) {
                currCoord.recycle()
                currCoord = ImageCoordinate.cloneCoord(sizes)
                break
            }
        }
    }

    /**
     * Queries whether there is another [ImageCoordinate] that has not yet been visited in the current Image.
     *
     * If this iterator was constructed on an image boxed with a region of interest, then this will check if there are more
     * coordinates that have not been visited in that region of interest.
     * @return  true if there are more coordinates that have not yet been visited, false otherwise.
     */
    override fun hasNext(): Boolean {
        return currCoord.any { currCoord[it] + 1 < sizes[it] }
    }

    /**
     * Gets the next coordinate in the ImageIterator's [Image].
     *
     * If the Image is boxed with a region of interest, this will get the next coordinate in the region of interest.
     * ImageIterator makes no guarantee as to the order in which coordinates will be visited, so do not rely on any ordering.
     * In particular, the order in which coordinates are visited may not be the same as the order of the underlying byte array representation of the Image.
     * @return  An [ImageCoordinate] that is the next location in the ImageIterator's Image.
     */
    override fun next(): ImageCoordinate {
        for (dim in this.currCoord) {
            this.currCoord[dim] = this.currCoord[dim] + 1
            if (this.currCoord[dim] >= this.sizes[dim]) {
                this.currCoord[dim] = this.lowerBounds[dim]
            } else {
                return this.currCoord
            }
        }
        throw java.util.NoSuchElementException("No more pixels in image!")
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        currCoord.recycle()
    }

    companion object {
        private val zeroCoord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
    }
}
