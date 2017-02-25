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

package edu.stanford.cfuller.imageanalysistools.meta.parameters

import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParser

/**
 * Utilities for processing analysis parameters from suitably formatted XML files; this
 * processor uses the old format, which is just a list of parameter elements inside some
 * root element.

 * @author Colin J. Fuller
 */
class LegacyParameterXMLParser : AnalysisMetadataParser() {

    /**
     * Parses the list of some of the known Parameters from an XML file to a List of Parameters; useful for allowing users to interacitively
     * select from known parameters.
     * @return      A List containing a Parameter object for each Parameter described in the known parameters file.
     */
    fun parseKnownParametersToParameterList(): ParameterDictionary {
        return parseFileToParameterDictionary(this.javaClass.classLoader.getResource(KNOWN_PARAMETER_XML_FILE)!!.toString())
    }


    /**
     * Parses an XML file to a ParameterDictionary.
     * @param filename  The XML file to parse.
     * *
     * @return          A ParameterDictionary with an entry for each parameter described in the XML file.
     * *
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link ParameterParser#parseFileToParameterDictionary(String)} instead")
    fun parseXMLFileToParameterDictionary(filename: String): ParameterDictionary {
        return this.parseFileToParameterDictionary(filename)
    }

    /**
     * Parses an XML file to an AnalysisMetadata.
     * @param filename      The filename of the XML file describing the parameters.
     * *
     * @return              A ParameterDictionary containing one Parameter object for each parameter described by the XML file.
     */
    override fun parseFileToAnalysisMetadata(filename: String): AnalysisMetadata? {

        val output = AnalysisMetadata()

        var taskDoc: Document? = null

        try {
            taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename)
        } catch (e: SAXException) {
            LoggingUtilities.logger.severe("Encountered exception while parsing tasks xml file.")
            e.printStackTrace()
            return null
        } catch (e: ParserConfigurationException) {
            LoggingUtilities.logger.severe("Incorrectly configured xml parser.")
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            LoggingUtilities.logger.severe("Exception while reading xml file.")
            e.printStackTrace()
            return null
        }

        val pd = this.parseDocumentToParameterDictionary(taskDoc)

        output.inputParameters = pd

        return output

    }


    /**
     * Parses an XML document to a ParameterDictionary.
     * @param taskDoc an XML document containing XML-formatted parameters.
     * *
     * @return a ParameterDictionary containing the parameters read from the XML document.
     */
    fun parseDocumentToParameterDictionary(taskDoc: Document): ParameterDictionary {

        val pd = ParameterDictionary.emptyDictionary()

        val tasks = taskDoc.getElementsByTagName(PARAMETER_TAG_NAME)

        for (i in 0..tasks.length - 1) {

            val n = tasks.item(i)

            val p = this.parameterWithXMLNode(n)

            pd.addParameter(p)

        }

        return pd

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

        if (nnm.getNamedItem(TYPE_ATTR_NAME) != null) {
            typeString = nnm.getNamedItem(TYPE_ATTR_NAME).nodeValue
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

        if (nnm.getNamedItem(NAME_ATTR_NAME) == null && nnm.getNamedItem(DISPLAY_ATTR_NAME) == null) {
            LoggingUtilities.logger.severe("parameter specified without name or display name")
            throw IllegalArgumentException("parameter specified without name or display name")
        }

        if (nnm.getNamedItem(NAME_ATTR_NAME) != null) {
            name = nnm.getNamedItem(NAME_ATTR_NAME).nodeValue
        } else {
            name = nnm.getNamedItem(DISPLAY_ATTR_NAME).nodeValue
        }

        if (nnm.getNamedItem(DISPLAY_ATTR_NAME) != null) {
            displayName = nnm.getNamedItem(DISPLAY_ATTR_NAME).nodeValue
        } else {
            displayName = nnm.getNamedItem(NAME_ATTR_NAME).nodeValue
        }

        if (node.childNodes.length > 0) {
            val children = node.childNodes
            for (i in 0..children.length - 1) {
                val n = children.item(i)

                if (n.nodeName === DESCRIPTION_NODE_NAME) {
                    descriptionString += n.textContent
                }
            }
        }

        if (nnm.getNamedItem(VALUE_ATTR_NAME) != null) {
            val valueString = nnm.getNamedItem(VALUE_ATTR_NAME).nodeValue

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

        if (value == null) value = defaultValue

        val p = Parameter(name, displayName, type, value, descriptionString)

        return p

    }

    companion object {

        internal val PARAMETER_TAG_NAME = "parameter"

        internal val TYPE_ATTR_NAME = "type"
        internal val NAME_ATTR_NAME = "name"
        internal val DISPLAY_ATTR_NAME = "displayname"
        internal val VALUE_ATTR_NAME = "value"
        internal val DESCRIPTION_NODE_NAME = "description"


        internal val KNOWN_PARAMETER_XML_FILE = "edu/stanford/cfuller/imageanalysistools/resources/knownparameters.xml"
    }

}