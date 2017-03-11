package edu.stanford.cfuller.imageanalysistools.frontend

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory


import java.util.logging.Handler

/**
 * Master controller of analysis routines.  Sets up critical default parameters if they have not been set up and runs
 * the analysis method.
 * @author Colin J. Fuller
 */

class AnalysisController {
    /**
     * Run analysis on the local machine.
     * @param params    The [ParameterDictionary] specifying the options for this analysis run.
     */
    fun runLocal(params: ParameterDictionary) {
        val am = AnalysisMetadata()
        am.inputParameters = params
        ParameterDictionary(params).let {
            am.outputParameters = it
            addLocalParameters(it)
        }
        LocalAnalysis.run(am)
    }

    /**
     * Run analysis on the local machine.
     * @param am    The [AnalysisMetadata] specifying the options for this analysis run.
     */
    fun runLocal(am: AnalysisMetadata) {
        am.outputParameters = ParameterDictionary(am.inputParameters!!)
        addLocalParameters(am.outputParameters!!)
        LocalAnalysis.run(am)
    }

    fun runLocal(parametersFilename: String) {
        val am = loadMetadataFromFile(parametersFilename)
        addLocalParameters(am.outputParameters!!)
        LocalAnalysis.run(am)
    }

    private fun addLocalParameters(params: ParameterDictionary) {
        params.addIfNotSet("temp_dir", System.getProperty("user.dir") + java.io.File.separator + "temp")
        params.addIfNotSet("image_extension", "")
        params.addIfNotSet("DEBUG", "false")
    }

    fun addAnalysisLoggingHandler(h: Handler) {
        LoggingUtilities.addHandler(h)
    }

    private fun loadMetadataFromFile(parametersFilename: String): AnalysisMetadata {
        return AnalysisMetadataParserFactory.createParserForFile(parametersFilename).parseFileToAnalysisMetadata(parametersFilename)
    }

    companion object {
        val DATA_OUTPUT_DIR = "output"
        val IMAGE_OUTPUT_DIR = "output_mask"
        val PARAMETER_OUTPUT_DIR = "parameters"
        val SERIALIZED_DATA_SUFFIX = "serialized"
        internal val PARAMETER_EXTENSION = ".xml"
    }
}
