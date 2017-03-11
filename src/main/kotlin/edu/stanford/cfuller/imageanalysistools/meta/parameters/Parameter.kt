package edu.stanford.cfuller.imageanalysistools.meta.parameters

/**
 * Represents a single parameter used for data analysis, with a name, a display name, a type, a value, and optionally a description.
 * @author cfuller
 */
class Parameter {
    /**
     * Gets the name of the parameter.
     * @return  The name of the parameter as a String.
     */
    /**
     * Sets the name of the parameter to the specified String.
     * @param name      The name to assign to this parameter.
     */
    var name: String
    /**
     * Gets the display name of the parameter.
     * @return  The display name of the parameter as a String.
     */
    /**
     * Sets the display name of the parameter to the specified String.
     * @param displayName   The display name to assign to the parameter.
     */
    var displayName: String
    /**
     * Gets the type of the parameter.
     * @return  The type of the parameter as an integer, corresponding to one of the static fields.
     */
    /**
     * Sets the type of the parameter to a new type.
     * @param type  The ParameterType corresponding to the new type of the parameter.
     */
    var type: ParameterType
    internal var value: Any
    /**
     * Gets the description of the parameter as a String.
     * @return  The description of the parameter.
     */
    var description: String
        internal set

    /**
     * Constructs a new parameter that is a copy of another parameter.
     * @param p     The Parameter to copy.
     */
    constructor(p: Parameter) {
        this.name = p.name
        this.displayName = p.displayName
        this.type = p.type
        this.value = p.value
        this.description = p.description
    }

    /**
     * Constructs a new parameter with the supplied fields.
     * @param name              The name of the parameter.
     * @param displayName       A string containing a name for display purposes, in a GUI field, for instance.
     * @param type              An ParameterType designating the type of the parameter's value.
     * @param value             The value of the parameter.  This must be a String, Boolean, Double, or Integer in agreement with the specified type parameter.
     * @param description       An optional description of the parameter's use.  (Supply null or an empty string in order to not use this.)
     */
    constructor(name: String, displayName: String, type: ParameterType, value: Any, description: String) {
        this.name = name
        this.displayName = displayName
        this.type = type
        this.value = value
        this.description = description
    }

    /**
     * Sets the value of the parameter to the specified Object.  This can be the correct type (String, Boolean, Double, or Integer), or a String that can be parsed to one of these types.
     * @param value     The value to assign to the Parameter; this may or may not be copied, so do not rely on either behavior.
     */
    fun setValue(value: Any) {
        this.value = convertValue(value)!!
    }

    /**
     * Gets the value of the parameter as an Object.
     * @return  The value of the parameter; the appropriate one of a String, Boolean, Double, or Integer, cast to an Object.
     */
    fun getValue(): Any {
        return this.value
    }

    /**
     * Gets the value of the parameter as a String.
     * @return  The value of the parameter converted to a String using the object's toString method.
     */
    fun stringValue(): String {
        return this.value.toString()
    }

    /**
     * Gets a string representation of the parameter.
     * @return  A string representation of the parameter.
     */
    override fun toString(): String {
        return this.displayName
    }

    private fun convertValue(originalValue: Any): Any? {
        if (this.type == ParameterType.STRING_T) return originalValue.toString()
        if (this.type == ParameterType.BOOLEAN_T) return java.lang.Boolean.valueOf(originalValue.toString())
        if (this.type == ParameterType.FLOATING_T) return java.lang.Double.valueOf(originalValue.toString())
        if (this.type == ParameterType.INTEGER_T) return Integer.valueOf(originalValue.toString())
        return null
    }
}
