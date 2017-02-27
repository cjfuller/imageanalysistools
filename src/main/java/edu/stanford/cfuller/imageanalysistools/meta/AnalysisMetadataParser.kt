package edu.stanford.cfuller.imageanalysistools.meta

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterParser
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * An AnalysisMetadatParser should be able to read a file and fill an AnalysisMetadata object
 * from the information contained in it.
 *
 * @author Colin J. Fuller
 */
abstract class AnalysisMetadataParser : ParameterParser {
    /**
     * Parses a parameter file to a ParameterDictionary.
     * @param filename  The file to parse.
     * @return          A ParameterDictionary with an entry for each parameter described in the file.
     */
    override fun parseFileToParameterDictionary(filename: String): ParameterDictionary {
        return this.parseFileToAnalysisMetadata(filename).inputParameters!!
    }
    /**
     * Parses a parameter file to an AnalysisMetadata object.
     * @param filename  The file to parse.
     * @return          An AnalysisMetadata containing the information described in the file.
     */
    abstract fun parseFileToAnalysisMetadata(filename: String): AnalysisMetadata
}
