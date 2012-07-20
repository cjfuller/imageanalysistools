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

package edu.stanford.cfuller.imageanalysistools.meta;

import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter;
import edu.stanford.cfuller.imageanalysistools.image.ImageSet;

/**
 * Utilites for writing existing analysis parameters to XML files.
 *
 * @author cfuller
 */
public class AnalysisMetadataXMLWriter extends AnalysisMetadataXMLParser {


    /**
     * Writes the parameters in a ParameterDictionary to an XML file that can be read at a later time by a ParameterXMLParser.
     *
     * @param pd            The ParameterDictionary to write to XML.
     * @param filename      The full path and filename to the XML file that will be written.
     */
    public void writeParameterDictionaryToXMLFile(ParameterDictionary pd, String filename) {

		try {
			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(new PrintWriter(filename));
			xsw.writeStartDocument();
			xsw.writeCharacters("\n");
			xsw.writeStartElement(TAG_ANALYSIS_METADATA);
			xsw.writeCharacters("\n");
			this.writeInputSectionToXMLStream(pd, xsw);
			xsw.writeEndElement();
			xsw.writeCharacters("\n");
			xsw.writeEndDocument();
			xsw.close();
		} catch (java.io.IOException e) {
			LoggingUtilities.getLogger().severe("Exception while writing XML file: " + e.getMessage());
		} catch (XMLStreamException e) {
            LoggingUtilities.getLogger().severe("Exception while writing parameter to XML: " + e.getMessage());
        }

    }

	/**
     * Writes the data in an AnalysisMetadata object to an XML file.
     *
     * @param am           The AnalysisMetadata to write to XML.
     * @param filename      The full path and filename to the XML file that will be written.
     */
    public void writeAnalysisMetadataToXMLFile(AnalysisMetadata am, String filename) {

		try {
			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(new PrintWriter(filename));
			xsw.writeStartDocument();
			xsw.writeCharacters("\n");
			xsw.writeStartElement(TAG_ANALYSIS_METADATA);
			xsw.writeCharacters("\n");
			this.writeInputSectionToXMLStream(am, xsw);
			this.writeAnalysisSectionToXMLStream(am, xsw);
			this.writeOutputSectionToXMLStream(am, xsw);
			xsw.writeEndElement();
			xsw.writeCharacters("\n");
			xsw.writeEndDocument();
			xsw.close();
		} catch (java.io.IOException e) {
			LoggingUtilities.getLogger().severe("Exception while writing XML file: " + e.getMessage());
		} catch (XMLStreamException e) {
            LoggingUtilities.getLogger().severe("Exception while writing parameter to XML: " + e.getMessage());
        }

    }

	/**
     * Writes the parameters in a ParameterDictionary to an XML String representation.
     * 
     * The ordering of all parameters with the same name will be preserved, but otherwise ordering
     * is not guaranteed.
     *
     * @param pd    		ParameterDictionary to write to XML
     * @return      		A String containing the XML representation of the ParameterDictionary
     */
	public String writeParameterDictionaryToXMLString(ParameterDictionary pd) {
		
		java.io.StringWriter sw = new java.io.StringWriter();

		try {

			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);

			this.writeParameterDictionaryToXMLStream(pd, xsw);
						
		} catch (XMLStreamException e) {
            LoggingUtilities.getLogger().severe("Exception while writing parameter to XML: " + e.getMessage());
        }

		return sw.getBuffer().toString();
	
	}
	
	/**
     * Writes a single parameter to an XML String representation.
     *
     * @param p 		    ParameterDictionary to write to XML
     * @return      		A String containing the XML representation of the ParameterDictionary
     */
	public String writeParameterToXMLString(Parameter p) {
		
		java.io.StringWriter sw = new java.io.StringWriter();
		
		try {
		
			XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
		
			this.writeParameterToXMLStream(p, xsw);
		} catch (XMLStreamException e) {
            LoggingUtilities.getLogger().severe("Exception while writing parameter to XML: " + e.getMessage());
        }
		
		return sw.getBuffer().toString();

	}
	
	private void writeParameterToXMLStream(Parameter p, XMLStreamWriter xsw) throws XMLStreamException {

		xsw.writeStartElement(TAG_PARAMETER);
		xsw.writeAttribute(ATTR_NAME, p.getName());
		xsw.writeAttribute(ATTR_DISPLAY_NAME, p.getDisplayName());
		xsw.writeAttribute(ATTR_TYPE, p.getType().toString());
		xsw.writeAttribute(ATTR_VALUE, p.stringValue());
		
		if (p.getDescription() != null) {
			xsw.writeCharacters("\n");
			xsw.writeStartElement(TAG_DESCRIPTION);
			xsw.writeCharacters("\n");
			xsw.writeCharacters(p.getDescription());
			xsw.writeCharacters("\n");
			xsw.writeEndElement();
			xsw.writeCharacters("\n");
		}
		
		xsw.writeEndElement();
		xsw.writeCharacters("\n");

        
	}

	private void writeParameterDictionaryToXMLStream(ParameterDictionary pd, XMLStreamWriter xsw) throws XMLStreamException {

		xsw.writeStartElement(TAG_PARAMETERS);
		xsw.writeCharacters("\n");
		
		java.util.Set<String> keys = pd.getKeys();

		for (String key : keys) {

			int count = pd.getValueCountForKey(key);
			
			for (int i = 0; i < count; i++) {
				Parameter p = pd.getParameterForKey(key, i);
				this.writeParameterToXMLStream(p, xsw);
			}

		}
	
		xsw.writeEndElement();
		xsw.writeCharacters("\n");

	}

	private void writeImageSetToXMLStream(ImageSet is, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_IMAGES);
		xsw.writeCharacters("\n");
		for (int i = 0; i < is.getImageCount(); i++) {
			xsw.writeStartElement(TAG_IMAGE);
			xsw.writeAttribute(ATTR_FILENAME, is.getFilenameForIndex(i));
			xsw.writeAttribute(ATTR_HASH_ALG, is.getImageHashAlgorithmForIndex(i));
			xsw.writeAttribute(ATTR_HASH, is.getImageHashForIndex(i));
			xsw.writeEndElement();
		}
		xsw.writeEndElement();
	}

	private void writeInputSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_INPUT_STATE);
		xsw.writeCharacters("\n");
		this.writeParameterDictionaryToXMLStream(am.getInputParameters(), xsw);
		this.writeImageSetToXMLStream(am.getInputImages(), xsw);
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}
	
	private void writeOutputSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_OUTPUT_STATE);
		xsw.writeCharacters("\n");
		this.writeParameterDictionaryToXMLStream(am.getOutputParameters(), xsw);
		this.writeImageSetToXMLStream(am.getOutputImages(), xsw);
		this.writeQuantificationSectionToXMLStream(am, xsw);
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}
	
	private void writeQuantificationSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_QUANT);
		xsw.writeCharacters("\n");
		java.util.List<String> outputFilenames = am.getOutputFiles();
		
		for (String f : outputFilenames) {
			xsw.writeStartElement(TAG_DATAFILE);
			xsw.writeAttribute(ATTR_FILENAME, f);
			xsw.writeAttribute(ATTR_HASH_ALG, am.getOutputFileHashAlgorithm(f));
			xsw.writeAttribute(ATTR_HASH, am.getOutputFileHash(f));
			xsw.writeEndElement();
			xsw.writeCharacters("\n");
		}
		
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
		
	}

	private void writeInputSectionToXMLStream(ParameterDictionary pd, XMLStreamWriter xsw) throws XMLStreamException {
		
		
		xsw.writeStartElement(TAG_INPUT_STATE);
		xsw.writeCharacters("\n");
		this.writeParameterDictionaryToXMLStream(pd, xsw);
		xsw.writeEndElement();
		xsw.writeCharacters("\n");

	}

	private void writeAnalysisSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		
		xsw.writeStartElement(TAG_ANALYSIS);
		xsw.writeCharacters("\n");
		this.writeLibrarySectionToXMLStream(am, xsw);
		this.writeTimestampSectionToXMLStream(am, xsw);
		this.writeMethodSectionToXMLStream(am, xsw);
		this.writeScriptSectionToXMLStream(am, xsw);
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}
	
	private void writeLibrarySectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeCharacters(AnalysisMetadata.getLibraryVersionXMLString());
		xsw.writeCharacters("\n");
	}
	
	private void writeTimestampSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_TIMESTAMP);
		xsw.writeAttribute(ATTR_TIME, am.getTime().toString());
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}
	
	private void writeMethodSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		xsw.writeStartElement(TAG_TIMESTAMP);
		xsw.writeAttribute(ATTR_TIME, am.getTime().toString());
		xsw.writeEndElement();
		xsw.writeCharacters("\n");
	}
	
	private void writeScriptSectionToXMLStream(AnalysisMetadata am, XMLStreamWriter xsw) throws XMLStreamException {
		if (am.hasScript()) {
			xsw.writeStartElement(TAG_SCRIPT);
			xsw.writeAttribute(ATTR_FILENAME, am.getScript().getName());
			xsw.writeCData(am.getScript().getScriptString());
			xsw.writeEndElement();
			xsw.writeCharacters("\n");
		}
	}
	

}
