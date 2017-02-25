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

package edu.stanford.cfuller.imageanalysistools.meta

import java.io.IOException
import java.io.PrintWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter
import java.util.regex.Pattern
import java.util.regex.Matcher

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter
import edu.stanford.cfuller.imageanalysistools.image.ImageSet

/**
 * Utilites for writing existing analysis parameters to XML files.

 * @author cfuller
 */
class AnalysisMetadataXMLWriter : AnalysisMetadataXMLParser() {


    /**
     * Writes the parameters in a ParameterDictionary to an XML file that can be read at a later time by a ParameterXMLParser.

     * @param pd            The ParameterDictionary to write to XML.
     * *
     * @param filename      The full path and filename to the XML file that will be written.
     */
    fun writeParameterDictionaryToXMLFile(pd: ParameterDictionary, filename: String) {

        try {
            val xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(PrintWriter(filename))
            xsw.writeStartDocument()
            xsw.writeCharacters("\n")
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_ANALYSIS_METADATA)
            xsw.writeCharacters("\n")
            this.writeInputSectionToXMLStream(pd, xsw)
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
            xsw.writeEndDocument()
            xsw.close()
        } catch (e: java.io.IOException) {
            LoggingUtilities.logger.severe("Exception while writing XML file: " + e.message)
        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception while writing parameter to XML: " + e.message)
        }

    }

    /**
     * Writes the data in an AnalysisMetadata object to an XML file.

     * @param am           The AnalysisMetadata to write to XML.
     * *
     * @param filename      The full path and filename to the XML file that will be written.
     */
    fun writeAnalysisMetadataToXMLFile(am: AnalysisMetadata, filename: String) {

        try {
            val xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(PrintWriter(filename))
            xsw.writeStartDocument()
            xsw.writeCharacters("\n")
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_ANALYSIS_METADATA)
            xsw.writeCharacters("\n")
            this.writeInputSectionToXMLStream(am, xsw)
            this.writeAnalysisSectionToXMLStream(am, xsw)
            this.writeOutputSectionToXMLStream(am, xsw)
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
            xsw.writeEndDocument()
            xsw.close()
        } catch (e: java.io.IOException) {
            LoggingUtilities.logger.severe("Exception while writing XML file: " + e.message)
        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception while writing parameter to XML: " + e.message)
        }

    }

    /**
     * Writes the parameters in a ParameterDictionary to an XML String representation.

     * The ordering of all parameters with the same name will be preserved, but otherwise ordering
     * is not guaranteed.

     * @param pd            ParameterDictionary to write to XML
     * *
     * @return            A String containing the XML representation of the ParameterDictionary
     */
    fun writeParameterDictionaryToXMLString(pd: ParameterDictionary): String {

        val sw = java.io.StringWriter()

        try {

            val xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw)

            this.writeParameterDictionaryToXMLStream(pd, xsw)

        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception while writing parameter to XML: " + e.message)
        }

        return sw.buffer.toString()

    }

    /**
     * Writes a single parameter to an XML String representation.

     * @param p            ParameterDictionary to write to XML
     * *
     * @return            A String containing the XML representation of the ParameterDictionary
     */
    fun writeParameterToXMLString(p: Parameter): String {

        val sw = java.io.StringWriter()

        try {

            val xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw)

            this.writeParameterToXMLStream(p, xsw)
        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception while writing parameter to XML: " + e.message)
        }

        return sw.buffer.toString()

    }

    @Throws(XMLStreamException::class)
    private fun writeParameterToXMLStream(p: Parameter, xsw: XMLStreamWriter) {

        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_PARAMETER)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_NAME, p.name)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_DISPLAY_NAME, p.displayName)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_TYPE, p.type.toString())
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_VALUE, p.stringValue())

        if (p.description != null && p.description.trim { it <= ' ' } != "") {
            xsw.writeCharacters("\n")
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_DESCRIPTION)
            xsw.writeCharacters("\n")
            xsw.writeCharacters(p.description)
            xsw.writeCharacters("\n")
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
        }

        xsw.writeEndElement()
        xsw.writeCharacters("\n")


    }

    @Throws(XMLStreamException::class)
    private fun writeParameterDictionaryToXMLStream(pd: ParameterDictionary, xsw: XMLStreamWriter) {

        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_PARAMETERS)
        xsw.writeCharacters("\n")

        val keys = pd.keys

        for (key in keys) {

            val count = pd.getValueCountForKey(key)

            for (i in 0..count - 1) {
                val p = pd.getParameterForKey(key, i)
                this.writeParameterToXMLStream(p, xsw)
            }

        }

        xsw.writeEndElement()
        xsw.writeCharacters("\n")

    }

    @Throws(XMLStreamException::class)
    private fun writeImageSetToXMLStream(`is`: ImageSet, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_IMAGES)
        xsw.writeCharacters("\n")
        for (i in 0..`is`.imageCount - 1) {
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_IMAGE)
            var filename = `is`.getFilenameForIndex(i)
            if (filename == null) {
                filename = `is`.getImageNameForIndex(i)
            }
            xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_FILENAME, filename)
            if (`is`.getImageHashAlgorithmForIndex(i) != null) {
                xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_HASH_ALG, `is`.getImageHashAlgorithmForIndex(i))
                xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_HASH, `is`.getImageHashForIndex(i))
            }
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
        }
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeInputSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_INPUT_STATE)
        xsw.writeCharacters("\n")
        this.writeParameterDictionaryToXMLStream(am.inputParameters, xsw)
        this.writeImageSetToXMLStream(am.originalInputImages, xsw)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeOutputSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_OUTPUT_STATE)
        xsw.writeCharacters("\n")
        this.writeParameterDictionaryToXMLStream(am.outputParameters, xsw)
        this.writeImageSetToXMLStream(am.outputImages, xsw)
        this.writeQuantificationSectionToXMLStream(am, xsw)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeQuantificationSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_QUANT)
        xsw.writeCharacters("\n")
        val outputFilenames = am.outputFiles

        for (f in outputFilenames) {
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_DATAFILE)
            xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_FILENAME, f)
            xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_HASH_ALG, am.getOutputFileHashAlgorithm(f))
            xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_HASH, am.getOutputFileHash(f))
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
        }

        xsw.writeEndElement()
        xsw.writeCharacters("\n")

    }

    @Throws(XMLStreamException::class)
    private fun writeInputSectionToXMLStream(pd: ParameterDictionary, xsw: XMLStreamWriter) {


        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_INPUT_STATE)
        xsw.writeCharacters("\n")
        this.writeParameterDictionaryToXMLStream(pd, xsw)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")

    }

    @Throws(XMLStreamException::class)
    private fun writeAnalysisSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {

        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_ANALYSIS)
        xsw.writeCharacters("\n")
        this.writeLibrarySectionToXMLStream(am, xsw)
        this.writeTimestampSectionToXMLStream(am, xsw)
        this.writeMethodSectionToXMLStream(am, xsw)
        this.writeScriptSectionToXMLStream(am, xsw)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeLibrarySectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        val libraryInfo = AnalysisMetadata.libraryVersionXMLString
        val versionMatcher = AnalysisMetadataXMLParser.versionGrabber.matcher(libraryInfo!!)
        val commitMatcher = AnalysisMetadataXMLParser.commitGrabber.matcher(libraryInfo!!)

        versionMatcher.find()
        commitMatcher.find()
        val version = versionMatcher.group(1)
        val commit = commitMatcher.group(1)

        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_LIBRARY)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_VERSION, version)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_COMMIT, commit)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeTimestampSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_TIMESTAMP)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_TIME, am.time.toString())
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeMethodSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_METHOD)
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_NAME, am.method.javaClass.name)
        var displayName: String? = am.method.displayName
        if (displayName == null) {
            displayName = am.method.javaClass.name
        }
        xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_DISPLAY_NAME, displayName)
        xsw.writeEndElement()
        xsw.writeCharacters("\n")
    }

    @Throws(XMLStreamException::class)
    private fun writeScriptSectionToXMLStream(am: AnalysisMetadata, xsw: XMLStreamWriter) {
        if (am.hasScript()) {
            xsw.writeStartElement(AnalysisMetadataXMLParser.TAG_SCRIPT)
            xsw.writeAttribute(AnalysisMetadataXMLParser.ATTR_FILENAME, am.script.name)
            xsw.writeCData(am.script.scriptString)
            xsw.writeEndElement()
            xsw.writeCharacters("\n")
        }
    }


}
