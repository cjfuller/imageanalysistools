/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.cfuller.imageanalysistools.parameters;

import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;

/**
 *
 * @author cfuller
 */
public class ParameterXMLWriter extends ParameterXMLParser {

    public void writeParameterDictionaryToXMLFile(ParameterDictionary pd, String filename) {

        java.util.List<Parameter> pl = new java.util.LinkedList<Parameter>();

        for (String k : pd.getKeys()) {
            Parameter p = new Parameter(k, k, pd.getTypeForKey(k), pd.getValueForKey(k), pd.getValueForKey(k), null);
            pl.add(p);
        }

        writeParameterListToXMLFile(pl, filename);

    }

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
