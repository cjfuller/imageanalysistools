/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.parameters;

import org.jruby.embed.ScriptingContainer;

/**
 * Utilities for processing analysis parameters from suitably formatted ruby script files.
 * 
 * Currently this just sets up an empty dictionary with a parameter pointing to the
 * name of the script file, and a parameter directing it to run the script method.
 *
 * @author Colin J. Fuller
 */
public class ParameterRubyParser extends ParameterParser {
	
	public static final String SCRIPT_FILENAME_PARAM = "script_filename";
	final static String SCRIPT_FUNCTIONS_FILE = "edu/stanford/cfuller/imageanalysistools/resources/parameter_methods.rb";
	
	
	/**
     * Parses a ruby file to a list of Parameters; this just sets up the script name parameter
     * and leaves everything else to the script itself.
     * 
     * @param filename      The filename of the ruby source file.
     * @return              A List containing one Parameter object for each parameter described by the XML file.
     * 
     */
	public java.util.List<Parameter> parseFileToParameterList(String filename) {
		
		java.util.List<Parameter> pl = new java.util.ArrayList<Parameter>();
		Parameter p = new Parameter(SCRIPT_FILENAME_PARAM, SCRIPT_FILENAME_PARAM, Parameter.TYPE_STRING, "", filename, null);
		pl.add(p);
		p = new Parameter("method_name", "method_name", Parameter.TYPE_STRING, "", "ScriptMethod", null);
		pl.add(p);
		
		//should eventually change the way ParameterDictionary is implemented to actually use Parameter objects
		// until then, build the dictionary this way.
		
		ParameterDictionary pd = ParameterDictionary.emptyDictionary();
		
		ScriptingContainer sc = new ScriptingContainer(org.jruby.embed.LocalContextScope.SINGLETHREAD, org.jruby.embed.LocalVariableBehavior.PERSISTENT);
		sc.setClassLoader(ij.IJ.getClassLoader());
		sc.put("parameters", pd);
				
		sc.setCompatVersion(org.jruby.CompatVersion.RUBY1_9);
		
		sc.runScriptlet(this.getClass().getClassLoader().getResourceAsStream(SCRIPT_FUNCTIONS_FILE), SCRIPT_FUNCTIONS_FILE);
		sc.runScriptlet(org.jruby.embed.PathType.ABSOLUTE, filename);
		
		for (String key : pd.getKeys()) {
			pl.add(new Parameter(key, key, pd.getTypeForKey(key), "", pd.getValueForKey(key), null));
		}
		
		return pl;
	}
	
}
