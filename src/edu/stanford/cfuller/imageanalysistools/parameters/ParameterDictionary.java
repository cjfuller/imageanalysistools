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

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents a set of key, value string pairs that specify various parameters for the analysis and their values.
 * <p>
 * This is essentially just a hashtable, but contains convenience methods for getting values as different types, as well
 * as for parsing parameters from command line options.
 *
 * @author Colin J. Fuller
 *
 */

public class ParameterDictionary implements Serializable {

	public static final long serialVersionUID=1L;
		
	private Hashtable<String, String> parameters;


	private ParameterDictionary(){}


    /**
     * Copy constructor.
     *
     * This will create a new ParameterDictionary using the key, value pairs in another ParameterDictionary.
     *
     * This only makes a semi-shallow copy.  The internal storage of the two ParameterDictionaries is distinct, so adding
     * a key, value pair to one will not affect the other.  However, the key and value objects originally present will likely not be copied.
     * Currently, the implementation exclusively uses Strings as the internal keys and values, and these are immutable, so
     * this should not cause a problem.
     *
     * @param toCopy    The ParameterDictionary whose keys/values are to be copied.
     */
	public ParameterDictionary(ParameterDictionary toCopy) {
		
		this.parameters = new Hashtable<String, String>(toCopy.parameters);
	
	}


    /**
     * Factory method to create a new empty dictionary.
     *
     * @return  A new ParameterDictionary whose internal storage has been initialized but contains no entries.
     */
    public static ParameterDictionary emptyDictionary() {

        ParameterDictionary parameters = new ParameterDictionary();

		parameters.parameters = new Hashtable<String, String>();

        return parameters;
    }


    /**
     * Parses a String array containing command line arguments and creates a new ParameterDictionary containing these entries.
     *
     * Each entry in the String array should be a single key-value pair in the format "key=value".  Allowable characters
     * in the keys and values are alphanumeric characters, underscores, hyphens, periods, forward and backslashes, and colons.
     *
     * @param args      The String array containing arguments (can be the string array argument to main(String[])).
     * @return          A new ParameterDictionary containing the parsed arguments.
     */
	public static ParameterDictionary parseArgumentListToParameterDictionary(String[] args) {
	
		ParameterDictionary parameters = new ParameterDictionary();
	
		parameters.parameters = new Hashtable<String, String>();
	
		for (int i = 1; i < args.length; i++) {
		
			String currArg = args[i];
			
            parseSingleParameter(parameters, currArg);
			
		}
	
	
		return parameters;
	
	}

    /**
     * Parses a file containing parameter key-value pairs and creates a new ParameterDictionary containing these entries.
     *<p>
     * The parameters file should contain one key-value pair per line in the format "key=value".  Allowable characters
     * in the keys and values are alphanumeric characters, underscores, hyphens, periods, forward and backslashes, and colons.
     * <p>
     *
     * Empty lines and lines starting with # will be ignored.
     *
     * @deprecated      Use {#link readParametersFromFile} instead with XML-format parameters.
     *
     * @param filename  The name of the file containing the parameters.
     * @return          A new ParameterDictionary containing the parsed arguments.
     * @throws IOException  if an exception is encountered while reading the file.
     */
    public static ParameterDictionary parseParametersFileToParameterDictionary(String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        ParameterDictionary parameters = new ParameterDictionary();
        parameters.parameters = new Hashtable<String, String>();

        String currentLine = br.readLine();

        while(currentLine != null) {
            parseSingleParameter(parameters, currentLine);
            currentLine = br.readLine();
        }

        br.close();

        return parameters;

    }



    private static void parseSingleParameter(ParameterDictionary parameters, String currArg) {

        if (currArg.isEmpty() || currArg.startsWith("#")) {return;}

        if (! (currArg.matches("[A-Za-z0-9_\\-\\.\\/\\\\: ]+=[A-Za-z0-9_\\-\\.\\/\\\\:\\, ]*"))){

				LoggingUtilities.getLogger().warning("Malformed argument: " + currArg + ".  Ignoring.");

			} else {

				String[] splitArg = currArg.split("=", -1);

				if (splitArg[1].toLowerCase().equals("true")) {

					splitArg[1] = "true";

				} else if (splitArg[1].toLowerCase().equals("false")) {

					splitArg[1] = "false";

				}

                if (parameters.hasKey(splitArg[0])) {
                    String temp = parameters.getValueForKey(splitArg[0]);
                    temp += " " + splitArg[1];
                    splitArg[1] = temp;
                }

                parameters.setValueForKey(splitArg[0], splitArg[1]);

			}
    }
	
	//public instance methods

    /**
     * Gets the value stored for a given key.  Returns null, as per the specification in java.util.Hashtable if there is
     * no value for key stored.
     * 
     * @param key       The key whose associated value is to be returned.
     * @return          The corresponding value, in String format.
     */
	public String getValueForKey(String key) {
	
		return this.parameters.get(key);
		
	}

    /**
     * Gets the value stored for a given key as an int.
     *
     * Does not perform type-checking on the conversion from String to int.
     *
     * @param key       The key whose associated value is to be returned.
     * @return          The corresponding value, as an int.
     */
    public int getIntValueForKey(String key) {
        return Integer.parseInt(getValueForKey(key));
    }

    /**
     * Gets the value stored for a given key as a boolean.
     *
     * Does not perform type-checking on the conversion from String to boolean.
     *
     * @param key       The key whose associated value is to be returned.
     * @return          The corresponding value, as a boolean.
     */
    public boolean getBooleanValueForKey(String key) {
        return Boolean.parseBoolean(getValueForKey(key));
    }

    /**
     * Gets the value stored for a given key as a double.
     *
     * Does not perform type-checking on the conversion from String to double.
     *
     * @param key       The key whose associated value is to be returned.
     * @return          The corresponding value, as a double.
     */
    public double getDoubleValueForKey(String key) {
        return Double.parseDouble(getValueForKey(key));
    }

    /**
     * Convenience method for checking if the ParameterDictionary has a key and that the corresponding value is true.
     * 
     * @param key       The key to be checked.
     * @return          The logical and of hasKey(key) and getBooleanValueForKey(key).
     */
    public boolean hasKeyAndTrue(String key) {
        return this.hasKey(key) && this.getBooleanValueForKey(key);
    }


    /**
     * Adds a key, value pair to the ParameterDictionary.  Does nothing if the specified key already has an associated
     * value.
     *
     * This method is useful for setting up default values after parsing user-specified options, as the defaults will
     * not overwrite the existing values.
     *
     * @param key       The key to add to the ParameterDictionary if it is not present.
     * @param value     The key's associated value.
     */
	public void addIfNotSet(String key, String value) {
	
		if (! this.parameters.containsKey(key) ) {
		
			this.parameters.put(key, value);
		
		}
	
		return;
	
	}


    /**
     * Removes all key,value pairs from the ParameterDictionary that are not listed in a specified array of valid keys.
     *
     * @param legalArguments    A String array containing the valid keys; any key, value pair not matching one of these keys will be discarded.
     */
	public void discardIllegalArguments(String[] legalArguments) {
		
		Vector<String> toRemove = new Vector<String>();
		
		for (String key : this.parameters.keySet()) {
		
			boolean legal = false;
		
			for (int i = 0; i < legalArguments.length; i++) {
			
				if (legalArguments[i].equals( key)) {
				
					legal = true;
					break;
				
				}
			
			}
			
			if (!legal) {
			
				toRemove.add(key);
			
			}
		
		}
		
		for (String key : toRemove) {
		
			this.parameters.remove(key);
		
		}
		
		return;
		
	}

    /**
     * Checks if the ParameterDictionary contains a specified key.
     *
     * @param key   A String that is the key to check for in this ParameterDictionary.
     * @return      A boolean corresponding to whether the specified key has an associated value.
     */
	public boolean hasKey(String key) {
	
		return this.parameters.containsKey(key);
	
	}


    /**
     * Adds a key, value pair to the ParameterDictionary.
     *
     * This is distinct from {@link #addIfNotSet(String, String)} in that this method will overwrite any existing value.
     *
     * @param key   The key to add to the ParameterDictionary.
     * @param value The value associated with the key, which will overwrite any existing value.
     */
	public void setValueForKey(String key, String value) {
	
		this.parameters.put(key, value);
	
	}


    /**
     * Writes the ParameterDictionary's contents to a specified file in plaintext format.
     * 
     * @param filename  The full filename (including path); any existing file with this name will be overwritten without confirmation.
     * @throws java.io.IOException  If an IOException occurs while opening or writing to the file.
     */
	public void writeParametersToFile(String filename) throws java.io.IOException {

        (new ParameterXMLWriter()).writeParameterDictionaryToXMLFile(this, filename);

//
//		PrintWriter output = new PrintWriter(new FileWriter(filename));
//
//		for (String key : this.parameters.keySet()) {
//
//			output.println(key + "=" + this.parameters.get(key));
//
//		}
//
//		output.close();
		
		return;

	}

    /**
     * Reads a set of parameters from an XML file.
     * @param filename  The XML file containing the parameter information.
     * @return          A ParameterDictionary containing the parameters parsed from the specified file.
     */
    public static ParameterDictionary readParametersFromFile(String filename) {
        return (new ParameterXMLParser()).parseXMLFileToParameterDictionary(filename);
    }


    /**
     * Adds a Parameter object to the parameter dictionary.
     * @param p     The Parameter to add.
     */
    public void addParameter(Parameter p) {
        parseSingleParameter(this, p.getName()+ "=" + p.getValue().toString());
    }

    /**
     * Gets a set containing the names of all the parameters in the ParameterDictionary.
     * @return  The Set containing the names.
     */
    public java.util.Set<String> getKeys() {
        return this.parameters.keySet();
    }

    /**
     * Gets the Parameter type for a given parameter name as one of the values of the static fields in the Parameter class.
     * @param key   The name of the parameter.
     * @return      An integer corresponding to the type of the parameter.  In the current implementation, which only uses strings, this will always be Parameter.TYPE_STRING, but may change in the future.
     */
    public int getTypeForKey(String key) {
        return Parameter.TYPE_STRING;
    }
	

}
	

