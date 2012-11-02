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

package edu.stanford.cfuller.imageanalysistools.meta.parameters;

/**
 * Represents a single parameter used for data analysis, with a name, a display name, a type, a value, and optionally a description.
 * 
 * @author cfuller
 */
public class Parameter {


    String name;
    String displayName;
    ParameterType type;
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
        this.value = p.value;
        this.description = p.description;
    }

    /**
     * Constructs a new parameter with the supplied fields.
     * @param name              The name of the parameter.
     * @param displayName       A string containing a name for display purposes, in a GUI field, for instance.
     * @param type              An ParameterType designating the type of the parameter's value.
     * @param value             The value of the parameter.  This must be a String, Boolean, Double, or Integer in agreement with the specified type parameter.
     * @param description       An optional description of the parameter's use.  (Supply null or an empty string in order to not use this.)
     */
    public Parameter(String name, String displayName, ParameterType type, Object value, String description) {
        this.name = name;
        this.displayName = displayName;
        this.type= type;
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
     * @param type  The ParameterType corresponding to the new type of the parameter.
     */
    public void setType(ParameterType type) {this.type = type;}

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
     * Gets the value of the parameter as a String.
     * @return  The value of the parameter converted to a String using the object's toString method.
     */
	public String stringValue() {
		return this.value.toString();
	}

    /**
     * Gets the description of the parameter as a String.
     * @return  The description of the parameter.
     */
    public String getDescription() {return this.description;}

    /**
     * Gets the type of the parameter.
     * @return  The type of the parameter as an integer, corresponding to one of the static fields.
     */
    public ParameterType getType() {return this.type;}

    /**
     * Gets a string representation of the parameter.
     * @return  A string representation of the parameter.
     */
    public String toString() {return this.getDisplayName();}

    private Object convertValue(Object originalValue) {
        if (this.type == ParameterType.STRING_T) return originalValue.toString();
        if (this.type == ParameterType.BOOLEAN_T) return Boolean.valueOf(originalValue.toString());
        if (this.type == ParameterType.FLOATING_T) return Double.valueOf(originalValue.toString());
        if (this.type == ParameterType.INTEGER_T) return Integer.valueOf(originalValue.toString());
        return null;
    }

}
