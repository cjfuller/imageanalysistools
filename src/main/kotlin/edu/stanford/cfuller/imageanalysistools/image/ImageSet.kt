package edu.stanford.cfuller.imageanalysistools.image

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerImageReader
import edu.stanford.cfuller.imageanalysistools.image.io.omero.OmeroServerInfo
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.util.FileHashCalculator

import java.util.*

/**
 * A set of related Images that will be processed together for analysis.  For instance, multiple channels of the same Image might be collected
 * into an ImageSet.  Images in the set can be added by Image object, by filename, or by omero Id.

 * Optionally, one Image in the set can be designated as a marker Image, which might have some specific purpose in the analysis.  For
 * instance, the marker Image might be the Image to use for segmentation, while the others might just be quantified.

 * Images added by filename or omero Id must be loaded using [.loadAllImages] before being used or retrieved.

 * @author Colin J. Fuller
 */
class ImageSet : java.io.Serializable, Collection<Image> {
    private var images: MutableList<ImageHolder> = mutableListOf()

    /**
     * Gets a stored Image that is the combine of each image in this set along the appropriate dimension if it has been previously supplied.
     * (e.g. a color combine if all images represent channels)
     * @return the combine of the Images in this set, if it has been set, or null if it has not.
     */
    /**
     * Stores an Image that is the combine of each image in this set along the appropriate dimension.
     * (e.g. a color combine if all images represent channels)
     * @param combined the Image that is the combine of the images in the ImageSet.
     */
    var combinedImage: Image? = null

    /**
     * Gets the index of the Image specified as the marker Image.

     * @return  The index of the marker Image in the set, or null if one has not been specified.
     */
    var markerIndex: Int? = null
        internal set

    /**
     * Gets the parameters associated with the ImageSet.
     * @return  A ParameterDictionary containing the analysis parameters.
     */
    var parameters: ParameterDictionary
        internal set

    /**
     * Constructs a new, empty ImageSet.
     * @param p     The parameters for the analysis.
     */
    constructor(p: ParameterDictionary) {
        this.images = ArrayList<ImageHolder>()
        this.markerIndex = null
        this.parameters = p
        this.combinedImage = null
    }

    /**
     * Copy constructor.
     * Does not make copies of the Images; this method is primarily useful to allow different marker Images or simultaneous iteration.
     * @param other     The ImageSet to copy.
     */
    constructor(other: ImageSet) {
        this.images = ArrayList<ImageHolder>()
        this.images.addAll(other.images)
        this.markerIndex = other.markerIndex
        this.parameters = other.parameters
        this.combinedImage = other.combinedImage
    }


    /**
     * Adds an Image to the ImageSet using its filename.
     * @param filename  The filename of the Image to add.
     */
    fun addImageWithFilename(filename: String) {
        val newImage = ImageHolder(null, filename, null)
        newImage.displayName = filename
        this.images.add(newImage)
    }

    /**
     * Adds an Image to the ImageSet using an already constructed Image object and a filename associated with it.
     * This can be used for Images that have already been loaded, in order to keep them associated with some sort of identifier.
     * @param toAdd     The (loaded) Image to add to the ImageSet.
     * @param name      The name to associate with the Image.
     */
    fun addImageWithImageAndName(toAdd: Image, name: String) {
        val imh = ImageHolder(toAdd, null, null)
        imh.displayName = name
        this.images.add(imh)
    }

    /**
     * Adds an Image to the ImageSet using an already constructed Image object.
     * @param toAdd     The Image to add to the ImageSet.
     */
    fun addImageWithImage(toAdd: Image) {
        this.images.add(ImageHolder(toAdd, null, null))
    }

    /**
     * Adds an Image to the ImageSet using its omero Id.
     * The necessary information for connecting to the omero server on which the image resides should be in the
     * ParameterDictionary used to construct the ImageSet in the parameters "omero_hostname", "omero_username", and "omero_password".
     * @param omeroId   The ID of the Image to add to the ImageSet.
     */
    fun addImageWithOmeroId(omeroId: Long) {
        this.images.add(ImageHolder(null, null, omeroId))
    }

    fun addImageWithOmeroIdAndName(omeroId: Long, name: String) {
        val imh = ImageHolder(null, null, omeroId)
        imh.displayName = name
        this.images.add(imh)
    }

    /**
     * Gets an Image from the set given its exact filename.
     * @param filename  The filename of the Image to retrieve.
     * @return          The retrieved Image, or null if no Image with this filename is contained in the ImageSet or the Image has not yet been loaded.
     */
    fun getImageForName(filename: String): Image? {
        return images
                .firstOrNull { it.filename != null && it.filename == filename }
                ?.image
    }

    /**
     * Gets an Image from the set whose filename matches the supplied regular expression.
     * @param regexp    The regular expression to match; must follow the syntax in java.util.regex.Pattern.
     * @return          The retrieved Image, or null if no Image with this filename is contained in the ImageSet or the Image has not yet been loaded.
     */
    fun getImageForNameMatching(regexp: String): Image? {
        return images
                .firstOrNull { it.filename != null && it.filename?.matches(regexp.toRegex()) ?: false }
                ?.image
    }

    /**
     * Gets an Image from the set whose omero Id matches the supplied Id.
     * @param id        The omero Id of the Image to retrieve.
     * @return          The retrieved Image, or null if no Image with this id is contained in the ImageSet or the Image has not yet been loaded.
     */
    fun getImageForOmeroId(id: Long): Image? {
        return images
                .firstOrNull { it.omeroId != null && it.omeroId == id }
                ?.image
    }

    /**
     * Gets the Image specified as the marker Image.
     * The marker Image might be the one to use for segmentation, for instance.
     * @return  The marker Image, or null if a marker Image has not been specified or it has not yet been loaded.
     */
    val markerImage: Image?
        get() {
            return markerIndex?.let { this.images[it].image }
        }

    /**
     * Gets the filename of the marker Image.
     * @return    the filename, or null if there is no marker image.
     */
    val markerImageName: String?
        get() {
            return markerIndex?.let { this.images[it].filename }
        }

    /**
     * Gets the Image specified as the marker Image.  If one has not been specified,
     * the zeroth image in the set will be chosen as the default and returned.
     * The marker Image might be the one to use for segmentation, for instance.
     * @return  The marker Image, or an Image selected as the default.
     */
    val markerImageOrDefault: Image?
        get() {
            val index = markerIndex ?: defaultMarkerIndex
            return this.images[index].image
        }

    /**
     * Checks if the ImageSet has a marker Image specified.
     * @return  true if a valid marker Image has been specified; false otherwise.
     */
    fun hasMarkerImage(): Boolean {
        return this.markerIndex != null
    }

    /**
     * Gets the Image at the specified index.
     * The index refers to the order in which the Image was added to the ImageSet.  (Starts at 0.)
     * @param index     The index of the Image to return.
     * @return          The retrieved Image, or null if the index was not valid.
     */
    fun getImageForIndex(index: Int): Image? {
        try {
            return this.images[index].image
        } catch (e: IndexOutOfBoundsException) {
            LoggingUtilities.logger.warning("Request for Image at index $index in ImageSet was out of bounds.")
            return null
        }
    }

    /**
     * Specifies that the Image at the provided index is the marker Image.
     * The index refers to the order in which the Image was added to the ImageSet. (Starts at 0.)
     * Supply -1 for the index as a shortcut for the most recently added Image.
     * @param index     The index of the Image to designate as the marker Image.
     */
    fun setMarkerImage(index: Int) {
        try {
            if (index == -1) {
                this.markerIndex = this.images.size - 1
                return
            }
            this.markerIndex = index
            this.images[this.markerIndex!!]
        } catch (e: IndexOutOfBoundsException) {
            LoggingUtilities.logger.warning("Request to set marker image to index $index in ImageSet was out of bounds.")
            this.markerIndex = null
        }
    }

    /**
     * Gets the number of Images in the ImageSet.
     * @return  The number of Images in the ImageSet.
     */
    val imageCount: Int
        get() = this.images.size

    /**
     * Loads all the Images in the ImageSet.
     * This will ensure that any Images that were added by filename or omero Id are retrieved and stored as Image objects,
     * which can then be processed.
     */
    @Throws(java.io.IOException::class)
    fun loadAllImages() {
        for (imh in this.images) {
            if (imh.image != null) {
                imh.setImageHash("", "")
                if (imh.displayName == null) {
                    imh.displayName = imh.image?.metadata?.getImageName(0)
                }
            } else if (imh.omeroId != null) {
                val osir = OmeroServerImageReader()
                val osi = OmeroServerInfo(
                        this.parameters.getValueForKey("omero_hostname")!!,
                        this.parameters.getValueForKey("omero_username")!!,
                        this.parameters.getValueForKey("omero_password")!!.toCharArray())

                val names = osir.loadImageFromOmeroServer(imh.omeroId!!, osi)
                imh.image = osir.read(names!![1]!!)
                imh.displayName = names[0]
            } else {
                imh.filename?.let { filename ->
                    val ir = ImageReader()
                    imh.image = ir.read(filename)
                    imh.displayName = filename
                }
            }
        }
        this.hashAllImages()
    }

    /**
     * Gets the display name associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The display name of the requested Image, or null if the Image does not exist.
     */
    fun getImageNameForIndex(index: Int): String? {
        if (index >= 0 && index < this.size) {
            return this.images[index].displayName
        } else {
            return null
        }
    }

    /**
     * Gets the image filename associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The filename of the requested Image, or null if the Image does not exist.
     */
    fun getFilenameForIndex(index: Int): String? {
        if (index >= 0 && index < this.size) {
            return this.images[index].filename
        } else {
            return null
        }
    }

    /**
     * Gets the file hash associated with the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The value of the hash associated with the image, or null if the Image does not exist.
     */
    fun getImageHashForIndex(index: Int): String? {
        if (index >= 0 && index < this.size) {
            return this.images[index].hash
        } else {
            return null
        }
    }

    /**
     * Gets the algorithm used to hash the requested Image.
     * @param index     The index of the Image in the ImageSet to use.
     * @return          The hash algorithm associated with the image, or null if the Image does not exist.
     */
    fun getImageHashAlgorithmForIndex(index: Int): String? {
        if (index >= 0 && index < this.size) {
            return this.images[index].hashAlgorithm
        } else {
            return null
        }
    }

    /**
     * Disposes of the memory-intensive portions of Images.
     *
     * Useful for programs that retain long-term references to the ImageSet for things like naming, but don't need
     * the pixel data for the full lifetime of the ImageSet.
     */
    fun disposeImages() {
        for (imh in this.images) {
            imh.image = null
        }
    }

    /**
     * Calculates the hash of all the Images in the image set.  For images added
     * from files, this will calculate an actual hash of the file.  For omero images,
     * this will produce something based on the server and omero id.  For added Image
     * objects, this will do nothing.
     */
    fun hashAllImages() {
        for (imh in this.images) {
            imh.filename?.let {
                try {
                    imh.setImageHash(
                            FileHashCalculator.ALG_DEFAULT,
                            FileHashCalculator.calculateHash(
                                    FileHashCalculator.ALG_DEFAULT, it))
                } catch (e: java.io.IOException) {
                    LoggingUtilities.logger.warning("Unable to calculate hash on image: " + imh.filename + "\n" + e.message)
                }
            }
            imh.omeroId?.let {
                imh.setImageHash(
                        "omero://" + this.parameters.getValueForKey("omero_hostname"),
                        java.lang.Long.toString(imh.omeroId!!))
            }
        }
    }

    private class ImageHolder(var image: Image?, var filename: String?, var omeroId: Long?) {
        var displayName: String? = null
        var hashAlgorithm: String? = null
            internal set
        var hash: String? = null
            internal set

        init {
            this.displayName = null
        }

        fun setImageHash(algorithm: String, hash: String?) {
            this.hashAlgorithm = algorithm
            this.hash = hash
        }
    }

    override operator fun contains(element: Image): Boolean {
        return this.images.any { element == it.image }
    }

    override fun containsAll(elements: Collection<Image>): Boolean {
        return elements.all { this.contains(it) }
    }

    override fun isEmpty(): Boolean {
        return this.imageCount == 0
    }

    override fun iterator(): Iterator<Image> {
        return ImageSetIterator(this)
    }

    override val size: Int
        get() {
            return this.imageCount
        }

    private inner class ImageSetIterator(val toIterate: ImageSet) : Iterator<Image> {
        internal var currIndex: Int = 0

        override fun hasNext(): Boolean {
            if (this.currIndex < toIterate.size) return true
            return false
        }

        override fun next(): Image {
            if (currIndex >= toIterate.size) {
                throw NoSuchElementException("No more Images in ImageSet.  Attempted to access Image at index $currIndex.")
            }
            val toReturn = toIterate.getImageForIndex(currIndex)
            currIndex++
            return toReturn ?: throw NoSuchElementException(
                    "No more Images in ImageSet.  Attempted to access Image at index ${currIndex - 1}.")
        }
    }

    companion object {
        internal const val serialVersionUID = 1L
        internal val defaultMarkerIndex = 0
        fun emptySet(): ImageSet {
            return ImageSet(ParameterDictionary.emptyDictionary())
        }
    }

}
