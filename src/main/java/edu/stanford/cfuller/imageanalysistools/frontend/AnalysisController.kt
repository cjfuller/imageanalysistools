/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

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
        am.outputParameters = ParameterDictionary(params)
        addLocalParameters(am.outputParameters)
        LocalAnalysis.run(am)

    }

    /**
     * Run analysis on the local machine.
     * @param am    The [AnalysisMetadata] specifying the options for this analysis run.
     */
    fun runLocal(am: AnalysisMetadata) {
        am.outputParameters = ParameterDictionary(am.inputParameters)
        addLocalParameters(am.outputParameters)
        LocalAnalysis.run(am)
    }

    fun runLocal(parametersFilename: String) {
        val am = loadMetadataFromFile(parametersFilename)
        addLocalParameters(am.outputParameters)
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
