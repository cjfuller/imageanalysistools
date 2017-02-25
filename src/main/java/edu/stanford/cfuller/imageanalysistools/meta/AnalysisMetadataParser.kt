/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.meta

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterParser
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * An AnalysisMetadatParser should be able to read a file and fill an AnalysisMetadata object
 * from the information contained in it.

 * @author Colin J. Fuller
 */
abstract class AnalysisMetadataParser : ParameterParser {

    /**
     * Parses a parameter file to a ParameterDictionary.
     * @param filename  The file to parse.
     * *
     * @return          A ParameterDictionary with an entry for each parameter described in the file.
     */
    override fun parseFileToParameterDictionary(filename: String): ParameterDictionary {
        return this.parseFileToAnalysisMetadata(filename).inputParameters
    }

    /**
     * Parses a parameter file to an AnalysisMetadata object.
     * @param filename  The file to parse.
     * *
     * @return          An AnalysisMetadata containing the information described in the file.
     */
    abstract fun parseFileToAnalysisMetadata(filename: String): AnalysisMetadata

}
