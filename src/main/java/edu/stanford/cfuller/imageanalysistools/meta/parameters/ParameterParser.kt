package edu.stanford.cfuller.imageanalysistools.meta.parameters

/**
 * Utilities for processing analysis parameters from suitably formatted files.
 * @author Colin J. Fuller
 */
interface ParameterParser {
    /**
     * Parses a parameter file to a ParameterDictionary.
     * @param filename  The file to parse.
     * @return          A ParameterDictionary with an entry for each parameter described in the file.
     */
    fun parseFileToParameterDictionary(filename: String): ParameterDictionary
}

