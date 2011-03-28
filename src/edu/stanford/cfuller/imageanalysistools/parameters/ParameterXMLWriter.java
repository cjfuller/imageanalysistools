/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
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
