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

    public static ParameterDictionary readParametersFromFile(String filename) {
        return (new ParameterXMLParser()).parseXMLFileToParameterDictionary(filename);
    }

    public void addParameter(Parameter p) {
        parseSingleParameter(this, p.getName()+ "=" + p.getValue().toString());
    }

    public java.util.Set<String> getKeys() {
        return this.parameters.keySet();
    }

    public int getTypeForKey(String key) {
        return Parameter.TYPE_STRING;
    }
	

}
	

