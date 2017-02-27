package edu.stanford.cfuller.imageanalysistools.meta.parameters

/**
 * Parameters for storage in a ParameterDictionary can be one of four types:
 * boolean, integer, floating-point, or String.  ParameterType specifies these
 * and handles converting their names to strings.

 * @author Colin J. Fuller
 */
enum class ParameterType(private val stringRep: String) {
    BOOLEAN_T("boolean"),
    INTEGER_T("integer"),
    FLOATING_T("floating"),
    STRING_T("string");

    /**
     * Gets the name of the ParameterType as a String
     * @return the String naming the parameter type: one of boolean, integer, floating, or string.
     */
    override fun toString(): String {
        return this.stringRep
    }
}