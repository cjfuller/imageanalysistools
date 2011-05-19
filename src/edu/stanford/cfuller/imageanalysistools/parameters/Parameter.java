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

/**
 * Represents a single parameter used for data analysis, with a name, a display name, a type, a value, and optionally a default value and description.
 * 
 * @author cfuller
 */
public class Parameter {

    public final static int TYPE_BOOLEAN = 0;
    public final static int TYPE_INTEGER = 1;
    public final static int TYPE_FLOATING = 2;
    public final static int TYPE_STRING = 3;

    String name;
    String displayName;
    int type;
    Object defaultValue;
    Object value;
    String description;

    protected Parameter(){}

    /**
     * Constructs a new parameter that is a copy of another parameter.
     * @param p     The Parameter to copy.
     */
    public Parameter(Parameter p) {
        this.name = p.name;
        this.displayName = p.displayName;
        this.type = p.type;
        this.defaultValue = p.defaultValue;
        this.value = p.value;
        this.description = p.description;
    }

    /**
     * Constructs a new parameter with the supplied fields.
     * @param name              The name of the parameter.
     * @param displayName       A string containing a name for display purposes, in a GUI field, for instance.
     * @param type              An integer designating the type of the parameter's value; selected from one of the static fields.
     * @param defaultValue      The default value for the parameter (used in case the value is invalid / not supplied, or in order to use this class to provide defaults for user input).
     * @param value             The value of the parameter.  This must be a String, Boolean, Double, or Integer in agreement with the specified type parameter.
     * @param description       An optional description of the parameter's use.  (Supply null or an empty string in order to not use this.)
     */
    public Parameter(String name, String displayName, int type, Object defaultValue, Object value, String description) {
        this.name = name;
        this.displayName = displayName;
        this.type= type;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
    }

    /**
     * Sets the value of the parameter to the specified Object.  This can be the correct type (String, Boolean, Double, or Integer), or a String that can be parsed to one of these types.
     * @param value     The value to assign to the Parameter; this may or may not be copied, so do not rely on either behavior.
     */
    public void setValue(Object value) {
        this.value = convertValue(value);
    }

    /**
     * Sets the name of the parameter to the specified String.
     * @param name      The name to assign to this parameter.
     */
    public void setName(String name) {this.name = name;}

    /**
     * Sets the display name of the parameter to the specified String.
     * @param displayName   The display name to assign to the parameter.
     */
    public void setDisplayName(String displayName) {this.displayName = displayName;}

    /**
     * Sets the type of the parameter to a new type.
     * @param type  The integer corresponding to the new type of the parameter.
     */
    public void setType(int type) {this.type = type;}

    /**
     * Gets the name of the parameter.
     * @return  The name of the parameter as a String.
     */
    public String getName() {return this.name;}

    /**
     * Gets the display name of the parameter.
     * @return  The display name of the parameter as a String.
     */
    public String getDisplayName() {return this.displayName;}

    /**
     * Gets the value of the parameter as an Object.
     * @return  The value of the parameter; the appropriate one of a String, Boolean, Double, or Integer, cast to an Object.
     */
    public Object getValue() {return this.value;}

    /**
     * Gets the default value of the parameter as an Object.
     * @return  The default value of the parameter;  the appropriate one of a String, Boolean, Double, or Integer, cast to an Object.
     */
    public Object getDefaultValue() {return this.defaultValue;}

    /**
     * Gets the description of the parameter as a String.
     * @return  The description of the parameter.
     */
    public String getDescription() {return this.description;}

    /**
     * Gets the type of the parameter.
     * @return  The type of the parameter as an integer, corresponding to one of the static fields.
     */
    public int getType() {return this.type;}

    /**
     * Gets a string representation of the parameter.
     * @return  A string representation of the parameter.
     */
    public String toString() {return this.getDisplayName();}

    private Object convertValue(Object originalValue) {
        if (this.type == TYPE_STRING) return originalValue.toString();
        if (this.type == TYPE_BOOLEAN) return Boolean.valueOf(originalValue.toString());
        if (this.type == TYPE_FLOATING) return Double.valueOf(originalValue.toString());
        if (this.type == TYPE_INTEGER) return Integer.valueOf(originalValue.toString());
        return null;
    }

}
