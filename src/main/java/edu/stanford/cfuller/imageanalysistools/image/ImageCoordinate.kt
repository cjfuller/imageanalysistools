package edu.stanford.cfuller.imageanalysistools.image

import java.util.NoSuchElementException

/**
 * Represents an integer-valued coordinate in an n-dimensional image.
 *
 * To avoid unnecessary object allocation when iterating over large images, this class provides basic object pooling capabilities.
 * ImageCoordinates can only be created using this class's static factory methods ([.createCoord], [.createCoordXYZCT], [.cloneCoord]), which will reuse ImageCoordinates if there are any
 * available or allocate more as necessary.
 *
 * Users of this class are responsible for calling [.recycle] on any ImageCoordinate they explicitly create using one of the static factory methods
 * but not any coordinates that they obtain by any other means.  Calling recycle on an ImageCoordinate will return it to the pool of available coordinates.
 * ImageCoordinates are not guaranteed to retain their value after being recycled, so they should not be used after being recycled.
 *
 * This class is partly thread-safe in the sense that multiple threads can use the same pool of ImageCoordinates safely.  However, ImageCoordinate objects themselves
 * are not thread-safe and should not be shared between threads.
 *
 * Arbitrary dimensions are defined by an int identifier, which can be any valid int.  This class defines constants for X,Y,Z,C,T as 0 to 4.
 * When defining additional dimensions, using lower numbers and not leaving gaps will improve performance.
 *
 * @author Colin J. Fuller
 */

class ImageCoordinate private constructor() : java.io.Serializable, Collection<Int> {

    private var dimensionCoordinates: IntArray = intArrayOf()
    private var undefinedCoordinates: ByteArray = byteArrayOf()
    /**
     * Gets the dimension of the ImageCoordinate: that is, the number of defined named dimensions
     * it contains.
     * @return        the dimension of the ImageCoordinate.
     */
    var dimension: Int = 0
        private set

    init {
        this.dimensionCoordinates = IntArray(initialDimensionCapacity)
        this.undefinedCoordinates = ByteArray(this.dimensionCoordinates.size)
        java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one)
        this.dimension = 0

    }

    /**
     * Gets the specified coordinate by index.
     *
     * The order of coordinates will be the order in which they were set if done manually,
     * or the order from the coordinate this ImageCoordinate was cloned from,
     * or otherwise ordered unpredictably (though consistent between calls), probably
     * according to hash key.
     *
     * If the coordinate index is out of bounds, returns 0 instead of throwing an exception.
     * @param dimensionConstant    the constant corresponding to the dimension whose value will be retrieved.
     * @return                    the value of the specified dimension component, or 0 if the index was out of bounds.
     */
    operator fun get(dimensionConstant: Int): Int {
        if (dimensionConstant < this.dimensionCoordinates.size) {
            return this.dimensionCoordinates[dimensionConstant]
        }
        return 0
    }

    /**
     * Sets the specified coordinate component of the ImageCoordinate by its index.
     *
     * This checks that the dimension exists and adds it if it does not exist.
     * @param dimensionConstant        the constant corresponding to the dimension to set.
     * @param value                    the value to which to set the coordinate.
     */
    operator fun set(dimensionConstant: Int, value: Int) {
        if (dimensionConstant >= this.dimensionCoordinates.size) {
            val oldCoords = this.dimensionCoordinates
            val oldUndefined = this.undefinedCoordinates
            val newSize = if (dimensionConstant + 1 > 2 * this.dimensionCoordinates.size) {
                dimensionConstant + 1
            } else { 2 * this.dimensionCoordinates.size }
            this.dimensionCoordinates = IntArray(newSize)
            this.undefinedCoordinates = ByteArray(newSize)

            java.util.Arrays.fill(this.dimensionCoordinates, 0)
            java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one)

            for (i in oldCoords.indices) {
                this.dimensionCoordinates[i] = oldCoords[i]
                this.undefinedCoordinates[i] = oldUndefined[i]
            }
        }
        this.dimension += this.undefinedCoordinates[dimensionConstant].toInt()
        this.undefinedCoordinates[dimensionConstant] = ImageCoordinate.zero
        this.dimensionCoordinates[dimensionConstant] = value
    }

    /**
     * Sets the specified coordinate component of the ImageCoordinate by its index.
     *
     * This does not check that the dimension exists before accessing it.
     * @param dimensionConstant        the constant corresponding to the dimension to set.
     * @param value                    the value to which to set the coordinate.
     */
    fun quickSet(dimensionConstant: Int, value: Int) {
        this.dimensionCoordinates[dimensionConstant] = value
    }

    /**
     * Gets the specified coordinate component of the ImageCoordinate by its index.
     *
     * This does not check that the dimension exists before accessing it.
     * @param dimensionConstant        the constant corresponding to the dimension whose value will be retrieved.
     * @return                        the value of the specified dimension component
     */
    fun quickGet(dimensionConstant: Int): Int {
        return this.dimensionCoordinates[dimensionConstant]
    }

    /**
     * Returns an ImageCoordinate to the pool of ImageCoordinates available for reuse.
     *
     * This method should be called on an ImageCoordinate if and only if that coordinate was obtained using one of the static factory methods
     * ([.createCoord] or [.cloneCoord]).
     *
     * Each ImageCoordinate must only be recycled once, or else future calls to the factory methods may return the same coordinate multiple times with
     * different values, which will lead to unpredictable behavior.  No checking is done to ensure that each coordinate was only recycled once (this was found
     * to cause enough overhead that the benefits of reusing objects were cancelled out).
     */
    fun recycle() {
        synchronized(ImageCoordinate::class.java) {
            availableStaticCoords!!.add(this)
        }
    }

    /**
     * Gets the defined index of the specified coordinate.
     * @param i        The coordinate to retrieve (i.e. the value i such that the ith index that has been assigned a value will be returned.)
     * @return        The index of the ith component.
     */
    fun getDefinedIndex(i: Int): Int? {
        var c = -1
        for (j in this.undefinedCoordinates.indices) {
            c += 1 - this.undefinedCoordinates[j]
            if (c == i) return j
        }
        throw NoSuchElementException("The specified coordinate is not defined: " + i + ".  Number of defined coordinates: " + this.dimension)
    }

    /**
     * Converts the ImageCoordinate to a string representation.
     *
     * The returned string will be "(a,b,c,d,e)", where a through e
     * are placeholders for the actual coordinates of the ImageCoordinate.
     * @return  The string representation of the ImageCoordinate.
     */
    override fun toString(): String {
        if (this.dimension == 0) {
            return "()"
        }
        var valueString = "("
        for (i in this) {
            valueString += this[i].toString() + ", "
        }
        valueString = valueString.substring(0, valueString.length - 2) // strip off the last ", "
        valueString += ")"
        return valueString
    }

    /**
     * Sets all the components of the ImageCoordinate to the specified values.
     * @param x     The new x-component of the ImageCoordinate.
     * @param y     The new y-component of the ImageCoordinate.
     * @param z     The new z-component of the ImageCoordinate.
     * @param c     The new c-component of the ImageCoordinate.
     * @param t     The new t-component of the ImageCoordinate.
     */
    fun setCoordXYZCT(x: Int, y: Int, z: Int, c: Int, t: Int) {
        this[ImageCoordinate.X] = x
        this[ImageCoordinate.Y] = y
        this[ImageCoordinate.Z] = z
        this[ImageCoordinate.C] = c
        this[ImageCoordinate.T] = t
    }

    /**
     * Sets the components of the ImageCoordinate to the values of the components of another ImageCoordinate.
     * @param other     The ImageCoordinate whose component values will be copied.
     */
    fun setCoord(other: ImageCoordinate) {
        this.clear()
        for (i in other) {
            this[i] = other[i]
        }
    }

    /**
     * Clears all stored coordinate dimensions and values.
     *
     * After calling this method, the ImageCoordinate will be zero-dimensional.
     */
    fun clear() {
        java.util.Arrays.fill(this.undefinedCoordinates, ImageCoordinate.one)
        this.dimension = 0
    }

    override operator fun contains(element: Int): Boolean {
        return this.any { it == element }
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        return elements.all { this.contains(it) }
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun iterator(): Iterator<Int> {
        return ImageCoordinateIterator(this)
    }

    override val size: Int
        get() {
            return this.dimension
        }

    private class ImageCoordinateIterator(internal var ic: ImageCoordinate) : Iterator<Int> {
        internal var currentIndex: Int = 0
        internal var currentDefinedCount: Int = 0

        init {
            this.currentIndex = 0
            this.currentDefinedCount = 0
        }

        override fun hasNext(): Boolean {
            return this.currentDefinedCount < this.ic.dimension
        }

        override fun next(): Int {
            while (this.hasNext()) {
                if (ic.undefinedCoordinates[this.currentIndex++].toInt() == 0) {
                    this.currentDefinedCount += 1
                    return this.currentIndex - 1
                }
            }
            throw NoSuchElementException("No more elements in ImageCoordinate.")
        }
    }

    companion object {
        //TODO: consider whether coordinates should continue to be discrete as they are in the current implementation
        val X = 0
        val Y = 1
        val Z = 2
        val C = 3
        val T = 4
        val defaultDimensionOrder = "XYZCT"
        internal const val serialVersionUID = 1L
        internal val initialStaticCoordCount = 8
        internal val initialDimensionCapacity = 10
        private var availableStaticCoords: java.util.Deque<ImageCoordinate>? = null
        private val one: Byte = 1
        private val zero: Byte = 0

        // TODO(colin): reevaluate whether pooling is still a performance win on modern JVMs
        init {
            synchronized(ImageCoordinate::class.java) {
                availableStaticCoords = java.util.LinkedList<ImageCoordinate>()
                for (i in 0..initialStaticCoordCount - 1) {
                    val c = ImageCoordinate()
                    availableStaticCoords!!.add(c)
                }
            }
        }

        private val nextAvailableCoordinate: ImageCoordinate
            get() {
                var coord: ImageCoordinate = ImageCoordinate()
                synchronized(ImageCoordinate::class.java) {
                    if (!(availableStaticCoords?.isEmpty() ?: true)) {
                        coord = availableStaticCoords?.removeFirst() ?: coord
                    }
                }
                coord.clear()
                return coord
            }

        /**
         * Factory method that creates a 5D ImageCoordinate with the specified coordinates.
         *
         * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
         * Users of this method should call [.recycle] on the ImageCoordinate returned by this method when finished with it in order to
         * make the coordinate available for reuse on other calls of this method.
         * @param x     The x-component of the ImageCoordinate to be created.
         * @param y     The y-component of the ImageCoordinate to be created.
         * @param z     The z-component of the ImageCoordinate to be created.
         * @param c     The c-component of the ImageCoordinate to be created.
         * @param t     The t-component of the ImageCoordinate to be created.
         * @return      An ImageCoordinate whose components have been set to the specified values.
         */
        fun createCoordXYZCT(x: Int, y: Int, z: Int, c: Int, t: Int): ImageCoordinate {
            val staticCoord = ImageCoordinate.nextAvailableCoordinate
            //setting T first avoids multiple allocations of internal storage for new coordinates
            staticCoord[ImageCoordinate.T] = t
            staticCoord[ImageCoordinate.X] = x
            staticCoord[ImageCoordinate.Y] = y
            staticCoord[ImageCoordinate.Z] = z
            staticCoord[ImageCoordinate.C] = c
            return staticCoord
        }

        /**
         * Factory method that creates a zero-dimensional ImageCoordinate that can be manually extended.
         *
         * This method will attempt to recycle an existing ImageCoordinate if any are available, or create a new one if none are available.
         * Users of this method should call [.recycle] on the ImageCoordinate returned by this method when finished with it in order to
         * make the coordinate available for reuse on other calls of this method.
         * @return      An ImageCoordinate with no specified dimensions.
         */
        fun createCoord(): ImageCoordinate {
            return ImageCoordinate.nextAvailableCoordinate
        }

        /**
         * Factory method that creates and ImageCoordinate that represents the same position in coordinate space as the supplied coordinate.
         * Users of this method should call [.recycle] on the ImageCoordinate returned by this method when finished with it in order to
         * make the coordinate available for reuse on other calls of this method.
         * @param toClone   The ImageCoordinate to clone.
         * @return          An ImageCoordinate whose components are set to the same values as the components of the supplied ImageCoordinate.
         */
        fun cloneCoord(toClone: ImageCoordinate): ImageCoordinate {
            val newCoord = ImageCoordinate.nextAvailableCoordinate
            newCoord.setCoord(toClone)
            return newCoord
        }
    }
}
