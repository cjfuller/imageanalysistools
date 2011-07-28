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

package edu.stanford.cfuller.imageanalysistools.parameters;

import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;

/**
 * Utilites for writing existing analysis parameters to XML files.
 *
 * @author cfuller
 */
public class ParameterXMLWriter extends ParameterXMLParser {


    /**
     * Writes the parameters in a ParameterDictionary to an XML file that can be read at a later time by a ParameterXMLParser.
     *
     * Note that if this is used to write parameters back to disk that were originally read from an XML file, this may not preserve the type
     * of those parameters, dependent on the implementation of the ParameterDictionary.
     *
     * @param pd            The ParameterDictionary to write to XML.
     * @param filename      The full path and filename to the XML file that will be written.
     */
    public void writeParameterDictionaryToXMLFile(ParameterDictionary pd, String filename) {

        java.util.List<Parameter> pl = new java.util.LinkedList<Parameter>();

        for (String k : pd.getKeys()) {
            Parameter p = new Parameter(k, k, pd.getTypeForKey(k), pd.getValueForKey(k), pd.getValueForKey(k), null);
            pl.add(p);
        }

        writeParameterListToXMLFile(pl, filename);

    }

    /**
     * Writes the parameters in a List of Parameter objects to an XML file that can be read at a later time by a ParameterXMLParser.
     *
     * @param parameters    The List of Parameters to write to XML.
     * @param filename      The full path and filename to the XML file that will be written.
     */
    public void writeParameterListToXMLFile(java.util.List<Parameter> parameters, String filename) {

        try {

            XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(new PrintWriter(filename));

            xsw.writeStartDocument();
            xsw.writeCharacters("\n");

            xsw.writeStartElement("root");
            xsw.writeCharacters("\n");

            for (Parameter p : parameters) {

                xsw.writeStartElement(PARAMETER_TAG_NAME);

                if(p.getName() != null) {
                    xsw.writeAttribute(NAME_ATTR_NAME, p.getName());
                }

                if (p.getDisplayName() != null) {
                    xsw.writeAttribute(DISPLAY_ATTR_NAME, p.getDisplayName());
                }

                if (p.getType() >= 0) {

                    String typeString = STRING_TYPE_NAME;
                    if (p.getType() == Parameter.TYPE_BOOLEAN) typeString = BOOL_TYPE_NAME;
                    else if (p.getType() == Parameter.TYPE_FLOATING) typeString = FLOAT_TYPE_NAME;
                    else if (p.getType() == Parameter.TYPE_INTEGER) typeString = INT_TYPE_NAME;


                    xsw.writeAttribute(TYPE_ATTR_NAME, typeString);
                }

                if (p.getDefaultValue() != null) {
                    xsw.writeAttribute(DEFAULT_ATTR_NAME, p.getDefaultValue().toString());
                }

                if (p.getValue() != null) {
                    xsw.writeAttribute(VALUE_ATTR_NAME, p.getValue().toString());
                }

                if (p.getDescription()!= null && !p.getDescription().isEmpty()) {
                    xsw.writeCharacters("\n");
                    xsw.writeStartElement(DESCRIPTION_NODE_NAME);
                    xsw.writeCharacters("\n");
                    xsw.writeCharacters(p.getDescription());
                    xsw.writeEndElement();
                    xsw.writeCharacters("\n");
                }

                xsw.writeEndElement();
                xsw.writeCharacters("\n");





            }




            xsw.writeEndElement();
            xsw.writeEndDocument();

            xsw.close();


        } catch (XMLStreamException e) {
            LoggingUtilities.getLogger().severe("Exception while writing parameters to XML file.");
            e.printStackTrace();
        } catch (IOException e) {
            LoggingUtilities.getLogger().severe("Exception while writing XML file.");
            e.printStackTrace();
        }


    }


}
