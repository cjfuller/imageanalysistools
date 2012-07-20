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

package edu.stanford.cfuller.imageanalysistools.meta;

import org.jruby.embed.ScriptingContainer;

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType;



/**
 * Utilities for processing analysis parameters from suitably formatted ruby script files.
 * 
 * Currently this just sets up an empty dictionary with a parameter pointing to the
 * name of the script file, and a parameter directing it to run the script method.
 *
 * @author Colin J. Fuller
 */
public class AnalysisMetadataRubyParser extends AnalysisMetadataParser {
	
	public static final String SCRIPT_FILENAME_PARAM = "script_filename";
	final static String SCRIPT_FUNCTIONS_FILE = "edu/stanford/cfuller/imageanalysistools/resources/parameter_methods.rb";
	
	
	/**
     * Parses a ruby file to a list of Parameters; this just sets up the script name parameter
     * and leaves everything else to the script itself, which is run once during this method.
     * Anything 
     * 
     * @param filename      The filename of the ruby source file.
     * @return              A ParameterDictionary containing one Parameter object for each parameter described by the XML file.
     * 
     */
	public AnalysisMetadata parseFileToAnalysisMetadata(String filename) {
		
		AnalysisMetadata am = new AnalysisMetadata();
		
		RubyScript rs = new RubyScript(filename);
		
		am.setScript(rs);
				
		ParameterDictionary pd = ParameterDictionary.emptyDictionary();
		
		Parameter p = new Parameter(SCRIPT_FILENAME_PARAM, SCRIPT_FILENAME_PARAM, ParameterType.STRING_T, filename, null);
		pd.addParameter(p);
		p = new Parameter("method_name", "method_name", ParameterType.STRING_T, "ScriptMethod", null);
		pd.addParameter(p);
		
		am.setInputParameters(pd);
		
		//should eventually change the way ParameterDictionary is implemented to actually use Parameter objects
		// until then, build the dictionary this way.
		
		ScriptingContainer sc = new ScriptingContainer(org.jruby.embed.LocalContextScope.SINGLETHREAD, org.jruby.embed.LocalVariableBehavior.PERSISTENT);
		sc.setClassLoader(ij.IJ.getClassLoader());
		sc.put("parameters", pd);
				
		sc.setCompatVersion(org.jruby.CompatVersion.RUBY1_9);
		
		sc.runScriptlet(this.getClass().getClassLoader().getResourceAsStream(SCRIPT_FUNCTIONS_FILE), SCRIPT_FUNCTIONS_FILE);
		sc.runScriptlet(rs.getScriptString());
		
		return am;
	}
	
}
