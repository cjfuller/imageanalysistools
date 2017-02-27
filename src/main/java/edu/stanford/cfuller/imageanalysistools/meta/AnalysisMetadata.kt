package edu.stanford.cfuller.imageanalysistools.meta

import edu.stanford.cfuller.imageanalysistools.method.Method
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

import java.io.BufferedReader
import java.io.InputStreamReader

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

import edu.stanford.cfuller.imageanalysistools.util.FileHashCalculator

/**
 * AnalysisMetadata objects hold all the information on an analysis run including
 * parameters used for input and the state of the parameters at output,
 * input and output images, code versions, scripts, and quantification files.
 *
 * @author Colin J. Fuller
 */
class AnalysisMetadata : java.io.Serializable {
    /**
     * Holder for a hash calculcated on a file and a String that names the algorithm used.
     */
    private inner class FileHash {
        var algorithm: String
            internal set
        var value: String
            internal set

        constructor(algorithm: String, value: String) {
            this.algorithm = algorithm
            this.value = value
        }
        constructor(other: FileHash) {
            this.algorithm = other.algorithm
            this.value = other.value
        }
    }

    /**
     * Gets the parameters to be used as input for the analysis.
     * @return a ParameterDictionary containing the input.
     */
    /**
     * Sets the ParameterDictionary used as input for the analysis.
     * @param in the ParameterDictionary initialized with the input parameters.
     */
    var inputParameters: ParameterDictionary? = null
    /**
     * Gets the parameters stored as output from the analysis.
     * @return a ParameterDictionary containing the output.
     */
    /**
     * Sets the ParameterDictionary to be stored as output from the analysis.
     * @param out the ParameterDictionary containing the output parameters.
     */
    var outputParameters: ParameterDictionary? = null

    /**
     * Gets the ImageSet that was stored from the first call to setInputImages.
     * @return an ImageSet containing the original input Images.
     */
    var originalInputImages: ImageSet? = null
        private set
    private var modifiedInputImages: ImageSet? = null // the way that multi-channel image files are handled currently is to split them and replace the image set, but
    // we want to track the initial one as well.  inputImages will be the first thing it was ever set to,
    // modifiedInputImages will hold subsequent sets
    var outputImages: ImageSet? = null

    private val inputImageHashes: MutableMap<String, FileHash>

    private val outputFilenames: MutableList<String>
    private val outputFileHashes: MutableMap<String, FileHash>

    /**
     * Gets any ruby script associated with the analysis.
     * @return a RubyScript containing the script.
     */
    /**
     * Sets the ruby script to be run for the analysis.
     * @param r a RubyScript containing a valid script to be run.
     */
    var script: RubyScript? = null

    /**
     * Gets the timestamp associated with the analysis.
     * @return a Date object containing the timestamp
     */
    var time: java.util.Date? = null
        private set

    /**
     * Gets the method associated with the analysis.
     * @return the Method that was run or will be run.
     */
    /**
     * Sets the method to be run for the analysis.
     * @param m a Method object that will be run, and from which output will be retrieved.
     */
    var method: Method? = null

    private var hasRunPreviously: Boolean = false

    private var outputMetadataFile: String? = null

    /**
     * Creates an empty AnalysisMetadata object.
     */
    init {
        this.inputParameters = null
        this.outputParameters = null
        this.originalInputImages = null
        this.modifiedInputImages = null
        this.outputImages = null
        this.inputImageHashes = java.util.HashMap<String, FileHash>()
        this.outputFilenames = java.util.ArrayList<String>()
        this.outputFileHashes = java.util.HashMap<String, FileHash>()
        this.script = null
        this.time = null
        this.method = null
        this.hasRunPreviously = false
        this.outputMetadataFile = null
    }

    /**
     * Makes a copy of this AnalysisMetadata object.  Everything is deep copied
     * except the contents of ImageSets storing input/output images, and the Method objects,
     * both of which might contain image data.  (The ImageSet objects themselves are copied.)
     *
     * @return another AnalysisMetadata object that is a copy of this one.
     */
    fun makeCopy(): AnalysisMetadata {
        val other = AnalysisMetadata()
        if (this.inputParameters != null) other.inputParameters = ParameterDictionary(this.inputParameters!!)
        if (this.outputParameters != null) other.outputParameters = ParameterDictionary(this.outputParameters!!)
        if (this.originalInputImages != null) other.originalInputImages = ImageSet(this.originalInputImages!!)
        if (this.outputImages != null) other.outputImages = ImageSet(this.outputImages!!)
        for (key in this.inputImageHashes.keys) {
            other.inputImageHashes.put(key, FileHash(this.inputImageHashes[key]!!))
        }

        for (outputFilename in this.outputFilenames) {
            other.outputFilenames.add(outputFilename)
            other.outputFileHashes.put(outputFilename, FileHash(this.outputFileHashes[outputFilename]!!))
        }

        if (this.script != null) {
            other.script = RubyScript(this.script!!.scriptString, this.script!!.name)
        }

        if (this.time != null) {
            other.time = java.util.Date(this.time!!.time)
        }

        other.method = this.method
        other.hasRunPreviously = this.hasRunPreviously
        other.outputMetadataFile = this.outputMetadataFile

        return other
    }

    /**
     * Sets the value of hashing the input Image stored in the named
     * file with the specified algorithm.
     * @param filename The name of the image file
     * @param algorithm A string identifying the algorithm used to calculate the hash
     * @param hash the result of calculating the hash as a hexadecimal string
     */
    fun setInputImageHash(filename: String, algorithm: String, hash: String) {
        this.inputImageHashes.put(filename, FileHash(algorithm, hash))
    }

    /**
     * Sets whether the AnalysisMetadata is loaded from a previous analysis run;
     * this will enable certain checks like matching code versions and file hashes.
     * @param has    whether the analysis has information from a previous run
     */
    fun setHasPreviousOutput(has: Boolean) {
        this.hasRunPreviously = has
    }

    var inputImages: ImageSet
        get() = this.modifiedInputImages!!
        set(`in`) {
            if (this.originalInputImages == null) {
                this.originalInputImages = `in`
                this.originalInputImages!!.hashAllImages()
            }

            this.modifiedInputImages = `in`
            this.modifiedInputImages!!.hashAllImages()

        }

    /**
     * Queries whether the analysis has a script associated with it.
     * @return true if there is a script to run, false otherwise.
     */
    fun hasScript(): Boolean {
        return this.script != null
    }

    /**
     * Timestamps the AnalysisMetadata with the current time.
     */
    fun timestamp() {
        this.time = java.util.Date()
    }

    /**
     * Adds an output file containing some non-image data.
     */
    fun addOutputFile(filename: String) {
        this.outputFilenames.add(filename)
        try {
            this.outputFileHashes.put(filename, FileHash(
                    FileHashCalculator.ALG_DEFAULT,
                    FileHashCalculator.calculateHash(FileHashCalculator.ALG_DEFAULT, filename)!!))
        } catch (e: java.io.IOException) {
            edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.warning("Unable to calculate hash on file: " + filename + "\n" + e.message)
        }
    }

    /**
     * Gets the names of all non-image output files.
     * @return a List containing the filenames.
     */
    val outputFiles: List<String>
        get() = this.outputFilenames

    /**
     * Gets the hash of the named output file using the algorithm obtained
     * by calling [.getOutputFileHashAlgorithm].
     * @param filename the filename of the output file.
     * *
     * @return the hash of that output file.
     */
    fun getOutputFileHash(filename: String): String? {
        val fh = this.outputFileHashes[filename] ?: return null
        return fh.value
    }

    /**
     * Gets the algorithm used to hash the named output file
     * @param filename the filename of the output file.
     * *
     * @return the algorithm used to hash that output file.
     */
    fun getOutputFileHashAlgorithm(filename: String): String? {
        val fh = this.outputFileHashes[filename] ?: return null
        return fh.algorithm
    }

    /**
     * Validates that the input images are the same as those used on a previous
     * analysis run; only warns about images with matching filenames but different
     * hashes.
     * @param logWarnings specifies whether to log a warning if there is a mismatch.
     * *
     * @return true if the validation was ok or there was no filename match; false if the hash mismatched
     */
    fun validateInputImages(logWarnings: Boolean): Boolean {
        if (!this.hasRunPreviously) {
            return true
        }
        var noErrors = true

        for (i in 0..this.originalInputImages!!.imageCount - 1) {
            val filename = this.originalInputImages!!.getFilenameForIndex(i)
            if (filename != null && this.inputImageHashes.containsKey(filename)) {
                val old = this.inputImageHashes[filename]!!
                var newHash: String? = null
                try {
                    newHash = FileHashCalculator.calculateHash(old.algorithm, filename)
                } catch (e: java.io.IOException) {
                    edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.warning("Unable to calculate hash on file: " + filename + "\n" + e.message)
                }

                if (old.value != newHash) {
                    noErrors = false
                    if (logWarnings) {
                        edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.warning("Input image does not match version run with previous analysis.  Filename: " + filename)
                    }
                }
            }
        }
        return noErrors
    }

    companion object {
        const val serialVersionUID = 2395791L
        val LIBRARY_VERSION_RESOURCE_PATH = "edu/stanford/cfuller/imageanalysistools/resources/version_info.xml"

        /**
         * Gets an XML-formatted String containing the library version information,
         * including which commit in the git repository was used to build it.
         * @return a String containing a library element with version and commit attributes, in XML format.
         */
        val libraryVersionXMLString: String?
            get() {
                try {
                    return BufferedReader(InputStreamReader(AnalysisMetadata::class.java.classLoader.getResourceAsStream(LIBRARY_VERSION_RESOURCE_PATH))).readLine()
                } catch (e: java.io.IOException) {
                    edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.warning("Unable to retrieve library version information: " + e.message)
                    return null
                }
            }
    }
}

