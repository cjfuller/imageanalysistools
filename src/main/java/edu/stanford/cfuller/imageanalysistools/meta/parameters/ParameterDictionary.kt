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

package edu.stanford.cfuller.imageanalysistools.meta.parameters

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities

import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory


import java.io.*
import java.util.Hashtable
import java.util.Vector

/**
 * Represents a set of String keys, and associated parameter objects that specify
 * various parameters for the analysis and their values.

 * @author Colin J. Fuller
 */

class ParameterDictionary : Serializable {

    private var parameters: Hashtable<String, List<Parameter>>? = null

    private constructor() {}


    /**
     * Copy constructor.

     * This will create a new ParameterDictionary using the key, value pairs in another ParameterDictionary.

     * This will make a deep copy.

     * @param toCopy    The ParameterDictionary whose keys/values are to be copied.
     */
    constructor(toCopy: ParameterDictionary) {

        this.parameters = Hashtable<String, List<Parameter>>()

        for (key in toCopy.parameters!!.keys) {

            val pl = toCopy.parameters!![key]

            val newPl = java.util.ArrayList<Parameter>()

            for (p in pl) {
                newPl.add(Parameter(p))

            }

            this.parameters!!.put(key, newPl)

        }

    }

    //public instance methods

    /**
     * Gets the value stored for a given key.  Returns null, as per the specification in java.util.Hashtable if there is
     * no value for key stored.

     * @param key       The key whose associated value is to be returned.
     * *
     * @return          The corresponding value, in String format.
     */
    fun getValueForKey(key: String): String {

        return this.getValueForKey(key, 0)

    }

    /**
     * Gets the ith value stored for a given key.  Does not check bounds, so check the number of
     * values stored using [.getValueCountForKey] beforehand.
     * @param key    The key whose associated value is to be returned.
     * *
     * @param i        The index of the value associated with this key to return.
     * *
     * @return        The value of the parameter as a String, or null if there is no value stored.
     */
    fun getValueForKey(key: String, i: Int): String? {
        val pl = this.parameters!![key] ?: return null

        return pl[i].stringValue()
    }

    /**
     * Gets the ith value stored for a given key.  Does not check bounds, so check the number of
     * values stored using [.getValueCountForKey] beforehand.
     * @param key    The key whose associated value is to be returned.
     * *
     * @param i        The index of the value associated with this key to return.
     * *
     * @return        The Parameter object associated with the key, or null if there is no value stored.
     */
    fun getParameterForKey(key: String, i: Int): Parameter? {
        val pl = this.parameters!![key] ?: return null

        return pl[i]
    }

    /**
     * Gets the number of parameters associated with a given key.
     * @param key    The key whose associated value count is to be returned.
     * *
     * @return        The Parameter object associated with the key, or null if there is no value stored.
     */
    fun getValueCountForKey(key: String): Int {
        val pl = this.parameters!![key] ?: return 0

        return pl.size
    }


    /**
     * Gets the value stored for a given key as an int.

     * Does not perform type-checking on the conversion from String to int.

     * @param key       The key whose associated value is to be returned.
     * *
     * @return          The corresponding value, as an int.
     */
    fun getIntValueForKey(key: String): Int {
        return Integer.parseInt(getValueForKey(key))
    }

    /**
     * Gets the value stored for a given key as a boolean.

     * Does not perform type-checking on the conversion from String to boolean.

     * @param key       The key whose associated value is to be returned.
     * *
     * @return          The corresponding value, as a boolean.
     */
    fun getBooleanValueForKey(key: String): Boolean {
        return java.lang.Boolean.parseBoolean(getValueForKey(key))
    }

    /**
     * Gets the value stored for a given key as a double.

     * Does not perform type-checking on the conversion from String to double.

     * @param key       The key whose associated value is to be returned.
     * *
     * @return          The corresponding value, as a double.
     */
    fun getDoubleValueForKey(key: String): Double {
        return java.lang.Double.parseDouble(getValueForKey(key))
    }

    /**
     * Convenience method for checking if the ParameterDictionary has a key and that the corresponding value is true.

     * @param key       The key to be checked.
     * *
     * @return          The logical and of hasKey(key) and getBooleanValueForKey(key).
     */
    fun hasKeyAndTrue(key: String): Boolean {
        return this.hasKey(key) && this.getBooleanValueForKey(key)
    }


    /**
     * Adds a key, value pair to the ParameterDictionary.  Does nothing if the specified key already has an associated
     * value.

     * This method is useful for setting up default values after parsing user-specified options, as the defaults will
     * not overwrite the existing values.

     * @param key       The key to add to the ParameterDictionary if it is not present.
     * *
     * @param value     The key's associated String value.  A new Parameter object will be constructed.
     */
    fun addIfNotSet(key: String, value: String) {

        this.addIfNotSet(key, this.parameterForStringValue(key, value))

        return

    }

    /**
     * Adds a key, value pair to the ParameterDictionary.  Does nothing if the specified key already has an associated
     * value.

     * This method is useful for setting up default values after parsing user-specified options, as the defaults will
     * not overwrite the existing values.

     * @param key       The key to add to the ParameterDictionary if it is not present.
     * *
     * @param value     The key's associated Parameter.
     */
    fun addIfNotSet(key: String, value: Parameter) {

        if (!this.parameters!!.containsKey(key)) {

            this.addValueForKey(key, value)

        }

        return

    }


    /**
     * Removes all key,value pairs from the ParameterDictionary that are not listed in a specified array of valid keys.

     * @param legalArguments    A String array containing the valid keys; any key, value pair not matching one of these keys will be discarded.
     */
    fun discardIllegalArguments(legalArguments: Array<String>) {

        val toRemove = Vector<String>()

        for (key in this.parameters!!.keys) {

            var legal = false

            for (i in legalArguments.indices) {

                if (legalArguments[i] == key) {

                    legal = true
                    break

                }

            }

            if (!legal) {

                toRemove.add(key)

            }

        }

        for (key in toRemove) {

            this.parameters!!.remove(key)

        }

        return

    }

    /**
     * Checks if the ParameterDictionary contains a specified key.

     * @param key   A String that is the key to check for in this ParameterDictionary.
     * *
     * @return      A boolean corresponding to whether the specified key has an associated value.
     */
    fun hasKey(key: String): Boolean {

        return this.parameters!!.containsKey(key)

    }


    /**
     * Adds a key, value pair to the ParameterDictionary.

     * This is distinct from [.addIfNotSet] in that this method will overwrite any existing value.

     * @param key   The key to add to the ParameterDictionary.
     * *
     * @param value The value associated with the key, which will overwrite any existing value.
     */
    fun setValueForKey(key: String, value: String) {

        this.setValueForKey(key, this.parameterForStringValue(key, value))

    }

    /**
     * Adds a key, value pair to the ParameterDictionary.

     * This is distinct from [.addIfNotSet] in that this method will add an additional value for a key if there is one already present.

     * @param key   The key to add to the ParameterDictionary.
     * *
     * @param value The Parameter associated with the key
     */
    fun addValueForKey(key: String, value: Parameter) {

        if (this.parameters!![key] == null) {
            this.parameters!!.put(key, java.util.ArrayList<Parameter>())
        }

        this.parameters!![key].add(value)

    }

    /**
     * Sets a key, value pair in the ParameterDictionary.

     * This is distinct from [.addIfNotSet] in that this method will overwrite existing values.

     * @param key   The key to add to the ParameterDictionary.
     * *
     * @param value The Parameter associated with the key
     */
    fun setValueForKey(key: String, value: Parameter) {

        val pl = java.util.ArrayList<Parameter>()

        pl.add(value)

        this.parameters!!.put(key, pl)

    }

    /**
     * Writes the ParameterDictionary's contents to a specified file in plaintext format.

     * @param filename  The full filename (including path); any existing file with this name will be overwritten without confirmation.
     * *
     * @throws java.io.IOException  If an IOException occurs while opening or writing to the file.
     * *
     */
    @Deprecated("")
    @Deprecated("use an AnalysisMetadata object and write that instead")
    @Throws(java.io.IOException::class)
    fun writeParametersToFile(filename: String) {

        AnalysisMetadataXMLWriter().writeParameterDictionaryToXMLFile(this, filename)

        println(filename)

        return

    }

    /**
     * Writes the ParameterDictionary's contents to a String as XML suitable for inclusion as a section in a file.

     * @return a String containing the XML representation of the ParameterDictionary.
     */
    fun writeParametersToXMLString(): String {
        return AnalysisMetadataXMLWriter().writeParameterDictionaryToXMLString(this)
    }


    /**
     * Adds a Parameter object to the parameter dictionary.
     * @param p     The Parameter to add.
     */
    fun addParameter(p: Parameter) {

        this.addValueForKey(p.name, p)
    }

    /**
     * Gets a set containing the names of all the parameters in the ParameterDictionary.
     * @return  The Set containing the names.
     */
    val keys: Set<String>
        get() = this.parameters!!.keys

    /**
     * Gets the Parameter type for a given parameter name as one of the values of the static fields in the Parameter class.
     * @param key   The name of the parameter.
     * *
     * @return      A ParameterType corresponding to the type of the parameter.
     */
    fun getTypeForKey(key: String): ParameterType {
        return this.getTypeForKey(key, 0)
    }

    /**
     * Gets the Parameter type for a given parameter name as one of the values of the static fields in the Parameter class.
     * @param key   The name of the parameter.
     * *
     * @param i    The index of the value associated with this key whose type will be retrieved.
     * *
     * @return      A ParameterType corresponding to the type of the parameter.
     */
    fun getTypeForKey(key: String, i: Int): ParameterType {
        return this.parameters!![key].get(i).type
    }

    private fun parameterForStringValue(key: String, value: String): Parameter {
        return Parameter(key, key, ParameterType.STRING_T, value, null)
    }

    companion object {

        const val serialVersionUID = 1495412129L


        /**
         * Factory method to create a new empty dictionary.

         * @return  A new ParameterDictionary whose internal storage has been initialized but contains no entries.
         */
        fun emptyDictionary(): ParameterDictionary {

            val parameters = ParameterDictionary()

            parameters.parameters = Hashtable<String, List<Parameter>>()

            return parameters
        }


        private fun parseSingleParameter(parameters: ParameterDictionary, currArg: String) {

            if (currArg.isEmpty() || currArg.startsWith("#")) {
                return
            }

            if (!currArg.matches("[A-Za-z0-9_\\-\\.\\/\\\\: ]+=[A-Za-z0-9_\\-\\.\\/\\\\:\\, ]*".toRegex())) {

                LoggingUtilities.logger.warning("Malformed argument: $currArg.  Ignoring.")

            } else {

                val splitArg = currArg.split("=".toRegex()).toTypedArray()

                if (splitArg[1].toLowerCase() == "true") {

                    splitArg[1] = "true"

                } else if (splitArg[1].toLowerCase() == "false") {

                    splitArg[1] = "false"

                }

                if (parameters.hasKey(splitArg[0])) {
                    var temp = parameters.getValueForKey(splitArg[0])
                    temp += " " + splitArg[1]
                    splitArg[1] = temp
                }

                parameters.setValueForKey(splitArg[0], splitArg[1])

            }
        }

        /**
         * Reads a set of parameters from an XML file.
         * @param filename  The XML file containing the parameter information.
         * *
         * @return          A ParameterDictionary containing the parameters parsed from the specified file.
         * *
         */
        @Deprecated("")
        @Deprecated("use an AnalysisMetadata object instead")
        fun readParametersFromFile(filename: String): ParameterDictionary {
            return AnalysisMetadataParserFactory.createParserForFile(filename).parseFileToParameterDictionary(filename)
        }
    }


}
	

