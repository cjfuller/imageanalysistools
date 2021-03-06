package edu.stanford.cfuller.imageanalysistools.meta

/**
 * Class representing a ruby script that can be used as an analysis method.
 * Holds the content of the script and an identifier of the script, such as
 * the file from which it was read.
 *
 * @author Colin J. Fuller
 */
class RubyScript {
    /**
     * Gets the name of the script.
     * @return a String containing the name of the script; this is the filename if it was read from disk.
     */
    var name: String
        internal set
    /**
     * Gets the code of the ruby script as a String.
     * @return a String containing the ruby code.
     */
    var scriptString: String
        internal set

    /**
     * Reads a new RubyScript from the specified file, which should be a valid ruby script.
     * @param filename a String containing the full path and filename
     */
    constructor(filename: String) {
        this.name = filename
        this.scriptString = ""

        try {
            val sb = StringBuilder()
            val br = java.io.BufferedReader(java.io.FileReader(filename))
            var line: String? = br.readLine()
            while (line != null) {
                sb.append(line)
                sb.append("\n")
                line = br.readLine()
            }
            this.scriptString = sb.toString()
        } catch (e: java.io.IOException) {
            edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.severe("Exception while reading ruby script for analysis: " + e.message)
        }
    }

    /**
     * Creates a new RubyScript from a string containing ruby code and a name for the script.
     * @param scriptString a String containing ruby code
     * *
     * @param name a name to call the script (this could be a filename or other identifier)
     */
    constructor(scriptString: String, name: String) {
        this.scriptString = scriptString
        this.name = name
    }
}


