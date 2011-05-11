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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilities for processing analysis parameters from suitably formatted XML files.
 *
 * @author cfuller
 */
public class ParameterXMLParser {

    final static String PARAMETER_TAG_NAME = "parameter";

    final static String TYPE_ATTR_NAME = "type";
    final static String NAME_ATTR_NAME = "name";
    final static String DISPLAY_ATTR_NAME = "displayname";
    final static String VALUE_ATTR_NAME = "value";
    final static String DEFAULT_ATTR_NAME = "defaultvalue";
    final static String DESCRIPTION_NODE_NAME = "description";

    final static String BOOL_TYPE_NAME = "boolean";
    final static String INT_TYPE_NAME = "integer";
    final static String FLOAT_TYPE_NAME = "floating";
    final static String STRING_TYPE_NAME = "string";

    final static String KNOWN_PARAMETER_XML_FILE = "resources/knownparameters.xml";

    /**
     * Parses the list of some of the known Parameters from an XML file to a List of Parameters; useful for allowing users to interacitively
     * select from known parameters.
     * @return      A List containing a Parameter object for each Parameter described in the known parameters file.
     */
    public java.util.List<Parameter> parseKnownParametersToParameterList() {
        return parseXMLFileToParameterList(this.getClass().getClassLoader().getResource(KNOWN_PARAMETER_XML_FILE).toString());
    }

    /**
     * Parses an XML file to a ParameterDictionary.
     * @param filename  The XML file to parse.
     * @return          A ParameterDictionary with an entry for each parameter described in the XML file.
     */
    public ParameterDictionary parseXMLFileToParameterDictionary(String filename) {
        return convertParameterListToParameterDictionary(parseXMLFileToParameterList(filename));
    }

    /**
     * Collects a List of Parameters (as might be generated by {@link #parseXMLFileToParameterList(String)}) into a ParameterDictionary.
     * @param pl    The List of Parameters to consolidate.
     * @return      A ParameterDictionary containing an entry for each supplied Parameter.
     */
    public ParameterDictionary convertParameterListToParameterDictionary(java.util.List<Parameter> pl) {
        ParameterDictionary pd = ParameterDictionary.emptyDictionary();
        for (Parameter p : pl) {
            pd.addParameter(p);

        }

        return pd;
    }


    /**
     * Parses an XML file to a list of Parameters, suitable for direct use or for conversion to a ParameterDictionary.
     * @param filename      The filename of the XML file describing the parameters.
     * @return              A List containing one Parameter object for each parameter described by the XML file.
     */
    public java.util.List<Parameter> parseXMLFileToParameterList(String filename) {

        java.util.LinkedList<Parameter> output = new java.util.LinkedList<Parameter>();

        Document taskDoc = null;

        try {
            taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename);
        } catch (SAXException e) {
            LoggingUtilities.getLogger().severe("Encountered exception while parsing tasks xml file.");
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            LoggingUtilities.getLogger().severe("Incorrectly configured xml parser.");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            LoggingUtilities.getLogger().severe("Exception while reading xml file.");
            e.printStackTrace();
            return null;
        }

        NodeList tasks = taskDoc.getElementsByTagName(PARAMETER_TAG_NAME);

        for (int i = 0; i < tasks.getLength(); i++) {

            Node n = tasks.item(i);

            Parameter p = this.parameterWithXMLNode(n);

            output.add(p);

        }

        return output;

    }


    /**
     * Parses an XML node from an XML parameter file to a Parameter object.
     * @param node  The node to parse.
     * @return      The Parameter described by the supplied node.
     */
    public Parameter parameterWithXMLNode(Node node) {

        NamedNodeMap nnm = node.getAttributes();

        String typeString = null;
        String name = "";
        String displayName = "";
        int type = -1;
        String descriptionString = "";
        Object value = null;
        Object defaultValue = null;

        if (nnm.getNamedItem(TYPE_ATTR_NAME) != null) {
            typeString = nnm.getNamedItem(TYPE_ATTR_NAME).getNodeValue();
        }

        if (typeString != null) {
            if (typeString.equals(BOOL_TYPE_NAME)) type = Parameter.TYPE_BOOLEAN;
            else if(typeString.equals(INT_TYPE_NAME)) type = Parameter.TYPE_INTEGER;
            else if (typeString.equals(FLOAT_TYPE_NAME)) type = Parameter.TYPE_FLOATING;
            else type = Parameter.TYPE_STRING;

        }

        if (nnm.getNamedItem(NAME_ATTR_NAME) == null && nnm.getNamedItem(DISPLAY_ATTR_NAME) == null) {
            LoggingUtilities.getLogger().severe("parameter specified without name or display name");
            throw new IllegalArgumentException("parameter specified without name or display name");
        }

        if (nnm.getNamedItem(NAME_ATTR_NAME) != null) {
            name = nnm.getNamedItem(NAME_ATTR_NAME).getNodeValue();
        } else {
            name = nnm.getNamedItem(DISPLAY_ATTR_NAME).getNodeValue();
        }

        if (nnm.getNamedItem(DISPLAY_ATTR_NAME) != null ) {
            displayName = nnm.getNamedItem(DISPLAY_ATTR_NAME).getNodeValue();
        } else {
            displayName = nnm.getNamedItem(NAME_ATTR_NAME).getNodeValue();
        }

        if (node.getChildNodes().getLength() > 0) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);

                if (n.getNodeName() == DESCRIPTION_NODE_NAME) {
                    descriptionString += n.getTextContent();
                }
            }
        }


        if (nnm.getNamedItem(DEFAULT_ATTR_NAME) != null) {
            String defaultString = nnm.getNamedItem(DEFAULT_ATTR_NAME).getNodeValue();

            try {

                switch(type) {
                    case Parameter.TYPE_BOOLEAN:

                        defaultValue = Boolean.valueOf(defaultString);

                        break;

                    case Parameter.TYPE_INTEGER:

                        defaultValue = Integer.valueOf(defaultString);

                        break;

                    case Parameter.TYPE_FLOATING:

                        defaultValue = Double.valueOf(defaultString);

                        break;

                    case Parameter.TYPE_STRING:

                        defaultValue = defaultString;

                        break;

                }

            } catch (NumberFormatException e) {
                LoggingUtilities.getLogger().warning("Exception encountered while parsing default value for parameter named: " + name);
                defaultValue = null;
            }
        }

        if (nnm.getNamedItem(VALUE_ATTR_NAME) != null) {
            String valueString = nnm.getNamedItem(VALUE_ATTR_NAME).getNodeValue();

            try {

                switch(type) {
                    case Parameter.TYPE_BOOLEAN:

                        value = Boolean.valueOf(valueString);

                        break;

                    case Parameter.TYPE_INTEGER:

                        value = Integer.valueOf(valueString);

                        break;

                    case Parameter.TYPE_FLOATING:

                        value = Double.valueOf(valueString);

                        break;

                    case Parameter.TYPE_STRING:

                        value = valueString;

                        break;

                }

            } catch (NumberFormatException e) {
                LoggingUtilities.getLogger().warning("Exception encountered while parsing value for parameter named: " + name);
                value = null;
            }
        }

        if (value == null) value = defaultValue;

        Parameter p = new Parameter(name, displayName, type, defaultValue, value, descriptionString);

        return p;

    }

}