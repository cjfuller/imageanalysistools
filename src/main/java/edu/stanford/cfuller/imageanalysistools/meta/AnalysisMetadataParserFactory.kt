package edu.stanford.cfuller.imageanalysistools.meta

/**
 * A factory for generating an appropriate parameter parser for a given file.
 * Currently, this just looks at the extension.  A .rb file is treated as a
 * ruby script, and a .xml file (or any other extension) is treated as an xml file.
 *
 * @author Colin J. Fuller
 */
object AnalysisMetadataParserFactory {
    /**
     * Generate a ParameterParser appropriate for the supplied file.
     * @param filename    The filename of the parameter file
     * @return    a ParameterParser object appropriate for reading parameters from the file.
     */
    fun createParserForFile(filename: String): AnalysisMetadataParser {
        if (filename.endsWith(".rb")) {
            return AnalysisMetadataRubyParser()
        } else {
            return AnalysisMetadataXMLParser()
        }
    }
}

