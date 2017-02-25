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

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.util.regex.Pattern
import java.util.regex.Matcher

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter
import edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType
import edu.stanford.cfuller.imageanalysistools.image.ImageSet


/**
 * A parser for new-format XML analysis metadata files.
 *
 *
 * If an old-format XML parameter file is passed in instead, it will be passed
 * off to a [LegacyParameterXMLParser][edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser].

 * @author Colin J. Fuller
 */
open class AnalysisMetadataXMLParser : AnalysisMetadataParser() {

    /**
     * Parses a parameter file to an AnalysisMetadata object.
     * @param filename  The XML-formatted file to parse.
     * *
     * @return          An AnalysisMetadata containing the information described in the file.
     */
    override fun parseFileToAnalysisMetadata(filename: String): AnalysisMetadata {

        val metaDoc = this.getDocumentForFilename(filename)

        val meta = AnalysisMetadata()

        val inputParameters = this.getInputParametersFromDocument(metaDoc)

        meta.inputParameters = inputParameters

        var hasOutput = false

        if (this.hasOutputSection(metaDoc) && this.hasAnalysisSection(metaDoc)) {

            hasOutput = true

            loadInputImageInformation(metaDoc, meta)

            loadPreviousAnalysisData(metaDoc, meta)

            val outputParameters = this.getOutputParametersFromDocument(metaDoc)

            meta.outputParameters = outputParameters

        }

        meta.setHasPreviousOutput(hasOutput)

        return meta

    }

    protected fun getDocumentForFilename(filename: String): Document? {
        var metaDoc: Document? = null

        try {
            metaDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename)
        } catch (e: SAXException) {
            LoggingUtilities.logger.severe("Encountered exception while parsing tasks xml file.")
            e.printStackTrace()
            return null
        } catch (e: ParserConfigurationException) {
            LoggingUtilities.logger.severe("Incorrectly configured xml parser.")
            e.printStackTrace()
            return null
        } catch (e: java.io.IOException) {
            LoggingUtilities.logger.severe("Exception while reading xml file.")
            e.printStackTrace()
            return null
        }

        return metaDoc
    }

    private fun getParametersFromDocument(analysisMetadataDocument: Document, tag: String): ParameterDictionary {

        val inputStateList = analysisMetadataDocument.getElementsByTagName(tag)

        if (inputStateList.length > 0) {

            val inputState = inputStateList.item(0) // only one input state allowed

            val children = inputState.childNodes

            var parametersNode: Node? = null

            for (i in 0..children.length - 1) {
                val child = children.item(i)
                if (child.nodeName == TAG_PARAMETERS) {
                    parametersNode = child
                    break
                }
            }

            if (parametersNode != null) {
                return parseParametersNodeToParameterDictionary(parametersNode)
            }

        }

        return LegacyParameterXMLParser().parseDocumentToParameterDictionary(analysisMetadataDocument)
    }

    private fun getInputParametersFromDocument(analysisMetadataDocument: Document): ParameterDictionary {

        return this.getParametersFromDocument(analysisMetadataDocument, TAG_INPUT_STATE)

    }

    private fun getOutputParametersFromDocument(analysisMetadataDocument: Document): ParameterDictionary {

        return this.getParametersFromDocument(analysisMetadataDocument, TAG_OUTPUT_STATE)

    }

    private fun parseParametersNodeToParameterDictionary(parameters: Node): ParameterDictionary {

        val tasks = parameters.childNodes

        val output = ParameterDictionary.emptyDictionary()

        val parameterNodes = parameters.childNodes

        for (i in 0..parameterNodes.length - 1) {

            val n = parameterNodes.item(i)

            if (n.nodeName != TAG_PARAMETER) continue

            val p = this.parameterWithXMLNode(n)

            output.addParameter(p)

        }

        return output

    }

    protected fun hasOutputSection(metaDoc: Document): Boolean {
        val outputStateList = metaDoc.getElementsByTagName(TAG_OUTPUT_STATE)
        return outputStateList.length > 0
    }

    protected fun hasAnalysisSection(metaDoc: Document): Boolean {
        val analysisList = metaDoc.getElementsByTagName(TAG_ANALYSIS)
        return analysisList.length > 0
    }

    private fun loadInputImageInformation(metaDoc: Document, meta: AnalysisMetadata) {

        val inputStateList = metaDoc.getElementsByTagName(TAG_INPUT_STATE)

        if (inputStateList.length > 0) {

            val inputState = inputStateList.item(0) // only one input state allowed

            val children = inputState.childNodes

            var imagesNode: Node? = null

            for (i in 0..children.length - 1) {
                val child = children.item(i)
                if (child.nodeName == TAG_IMAGES) {
                    imagesNode = child
                    break
                }
            }

            if (imagesNode != null) {

                val allImages = imagesNode.childNodes

                val images = ImageSet(meta.inputParameters)

                for (i in 0..allImages.length - 1) {

                    val im = allImages.item(i)

                    if (im.nodeName != TAG_IMAGE) continue

                    val nnm = im.attributes

                    val filename = nnm.getNamedItem(ATTR_FILENAME).nodeValue
                    val hashAlgorithm = nnm.getNamedItem(ATTR_HASH_ALG).nodeValue
                    val hash = nnm.getNamedItem(ATTR_HASH).nodeValue

                    images.addImageWithFilename(filename)

                    meta.setInputImageHash(filename, hashAlgorithm, hash)

                }

                meta.inputImages = images

            }

        }

    }

    private fun loadOutputImageInformation(metaDoc: Document, meta: AnalysisMetadata) {

        val outputStateList = metaDoc.getElementsByTagName(TAG_OUTPUT_STATE)

        if (outputStateList.length > 0) {

            val outputState = outputStateList.item(0) // only one input state allowed

            val children = outputState.childNodes

            var imagesNode: Node? = null

            for (i in 0..children.length - 1) {
                val child = children.item(i)
                if (child.nodeName == TAG_IMAGES) {
                    imagesNode = child
                    break
                }
            }

            if (imagesNode != null) {

                val allImages = imagesNode.childNodes

                val images = ImageSet(meta.outputParameters)

                for (i in 0..allImages.length - 1) {

                    val im = allImages.item(i)

                    if (im.nodeName != TAG_IMAGE) continue

                    val nnm = im.attributes

                    val filename = nnm.getNamedItem(ATTR_FILENAME).nodeValue


                    images.addImageWithFilename(filename)

                }

                meta.outputImages = images

            }

        }

    }

    private fun loadNonImageOutputInformation(metaDoc: Document, meta: AnalysisMetadata) {

        val outputStateList = metaDoc.getElementsByTagName(TAG_OUTPUT_STATE)

        if (outputStateList.length > 0) {

            val outputState = outputStateList.item(0) // only one input state allowed

            val children = outputState.childNodes

            var quantNode: Node? = null

            for (i in 0..children.length - 1) {
                val child = children.item(i)
                if (child.nodeName == TAG_QUANT) {
                    quantNode = child
                    break
                }
            }

            if (quantNode != null) {

                val allQuant = quantNode.childNodes

                for (i in 0..allQuant.length - 1) {

                    val im = allQuant.item(i)

                    if (im.nodeName != TAG_DATAFILE) continue

                    val nnm = im.attributes

                    val filename = nnm.getNamedItem(ATTR_FILENAME).nodeValue

                    meta.addOutputFile(filename)

                }

            }

        }

    }

    private fun loadPreviousAnalysisData(metaDoc: Document, meta: AnalysisMetadata) {

        val analysisList = metaDoc.getElementsByTagName(TAG_ANALYSIS)

        if (analysisList.length > 0) {

            val analysis = analysisList.item(0) // only one analysis allowed

            val children = analysis.childNodes

            for (i in 0..children.length - 1) {

                val child = children.item(i)

                if (child.nodeName == TAG_LIBRARY) {
                    this.processLibraryNode(child, meta)
                } else if (child.nodeName == TAG_SCRIPT) {
                    this.processScriptNode(child, meta)
                }
            }

            loadOutputImageInformation(metaDoc, meta)
            loadNonImageOutputInformation(metaDoc, meta)

        }

    }

    private fun processLibraryNode(library: Node, meta: AnalysisMetadata) {

        val nnm = library.attributes

        val xmlVersionString = AnalysisMetadata.libraryVersionXMLString
        val versionMatcher = AnalysisMetadataXMLParser.versionGrabber.matcher(xmlVersionString!!)
        val commitMatcher = AnalysisMetadataXMLParser.commitGrabber.matcher(xmlVersionString!!)
        versionMatcher.find()
        commitMatcher.find()
        val currentVersion = versionMatcher.group(1)
        val currentCommit = commitMatcher.group(1)

        val lastVersion = nnm.getNamedItem(ATTR_VERSION).nodeValue
        val lastCommit = nnm.getNamedItem(ATTR_COMMIT).nodeValue

        if (!(currentVersion == lastVersion && currentCommit == lastCommit)) {
            LoggingUtilities.logger.warning("library version does not match that used for previous analysis.\nPrevious version info: $lastVersion $lastCommit\nCurrent version info: $currentVersion $currentCommit")
        }

    }

    private fun processScriptNode(script: Node, meta: AnalysisMetadata) {
        val scriptCData = script.childNodes.item(0)
        val scriptString = scriptCData.nodeValue
        val nnm = script.attributes
        val name = nnm.getNamedItem(ATTR_FILENAME).nodeValue
        meta.script = RubyScript(scriptString, name)
    }


    /**
     * Parses an XML node from an XML parameter file to a Parameter object.
     * @param node  The node to parse.
     * *
     * @return      The Parameter described by the supplied node.
     */
    fun parameterWithXMLNode(node: Node): Parameter {

        val nnm = node.attributes

        var typeString: String? = null
        var name = ""
        var displayName = ""
        var type = ParameterType.STRING_T
        var descriptionString = ""
        var value: Any? = null
        val defaultValue: Any? = null

        if (nnm.getNamedItem(ATTR_TYPE) != null) {
            typeString = nnm.getNamedItem(ATTR_TYPE).nodeValue
        }

        if (typeString != null) {
            if (typeString == ParameterType.BOOLEAN_T.toString())
                type = ParameterType.BOOLEAN_T
            else if (typeString == ParameterType.INTEGER_T.toString())
                type = ParameterType.INTEGER_T
            else if (typeString == ParameterType.FLOATING_T.toString())
                type = ParameterType.FLOATING_T
            else
                type = ParameterType.STRING_T

        }

        if (nnm.getNamedItem(ATTR_NAME) == null && nnm.getNamedItem(ATTR_DISPLAY_NAME) == null) {
            LoggingUtilities.logger.severe("parameter specified without name or display name")
            throw IllegalArgumentException("parameter specified without name or display name")
        }

        if (nnm.getNamedItem(ATTR_NAME) != null) {
            name = nnm.getNamedItem(ATTR_NAME).nodeValue
        } else {
            name = nnm.getNamedItem(ATTR_DISPLAY_NAME).nodeValue
        }

        if (nnm.getNamedItem(ATTR_DISPLAY_NAME) != null) {
            displayName = nnm.getNamedItem(ATTR_DISPLAY_NAME).nodeValue
        } else {
            displayName = nnm.getNamedItem(ATTR_NAME).nodeValue
        }

        if (node.childNodes.length > 0) {
            val children = node.childNodes
            for (i in 0..children.length - 1) {
                val n = children.item(i)

                if (n.nodeName === TAG_DESCRIPTION) {
                    descriptionString += n.textContent
                }
            }
        }

        if (nnm.getNamedItem(ATTR_VALUE) != null) {
            val valueString = nnm.getNamedItem(ATTR_VALUE).nodeValue

            try {

                when (type) {
                    ParameterType.BOOLEAN_T ->

                        value = java.lang.Boolean.valueOf(valueString)

                    ParameterType.INTEGER_T ->

                        value = Integer.valueOf(valueString)

                    ParameterType.FLOATING_T ->

                        value = java.lang.Double.valueOf(valueString)

                    ParameterType.STRING_T ->

                        value = valueString
                }

            } catch (e: NumberFormatException) {
                LoggingUtilities.logger.warning("Exception encountered while parsing value for parameter named: " + name)
                value = null
            }

        }

        val p = Parameter(name, displayName, type, value, descriptionString)

        return p

    }

    companion object {

        //XML tags

        val TAG_ANALYSIS_METADATA = "analysis_metadata"
        val TAG_INPUT_STATE = "input_state"
        val TAG_PARAMETERS = "parameters"
        val TAG_PARAMETER = "parameter"
        val TAG_IMAGES = "images"
        val TAG_IMAGE = "image"
        val TAG_ANALYSIS = "analysis"
        val TAG_LIBRARY = "library"
        val TAG_TIMESTAMP = "timestamp"
        val TAG_METHOD = "method"
        val TAG_SCRIPT = "script"
        val TAG_OUTPUT_STATE = "output_state"
        val TAG_QUANT = "quantification"
        val TAG_DATAFILE = "datafile"
        val TAG_DESCRIPTION = "description"

        //tag attributes

        val ATTR_TIME = "time"
        val ATTR_VERSION = "version"
        val ATTR_COMMIT = "commit"
        val ATTR_FILENAME = "filename"
        val ATTR_HASH_ALG = "hash_algorithm"
        val ATTR_HASH = "hash"
        val ATTR_NAME = "name"
        val ATTR_DISPLAY_NAME = "display_name"
        val ATTR_VALUE = "value"
        val ATTR_TYPE = "type"

        //library version info

        protected val versionGrabber = Pattern.compile("$ATTR_VERSION=\"(\\S*)\"")
        protected val commitGrabber = Pattern.compile("$ATTR_COMMIT=\"(\\S*)\"")
    }


}
