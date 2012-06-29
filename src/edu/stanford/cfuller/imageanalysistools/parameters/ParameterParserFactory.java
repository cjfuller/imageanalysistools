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

/**
 * A factory for generating an appropriate parameter parser for a given file.
 * Currently, this just looks at the extension.  A .rb file is treated as a 
 * ruby script, and a .xml file (or any other extension) is treated as an xml file.
 * 
 * @author Colin J. Fuller
 */
public class ParameterParserFactory {
	
	/**
	* Generate a ParameterParser appropriate for the supplied file.
	* 
	* @param filename	The filename of the parameter file
	* @return 	a ParameterParser object appropriate for reading parameters from the file.
	*/
	public static ParameterParser createParameterParserForFile(String filename) {
		
		if (filename.endsWith(".rb")) {
			return new ParameterRubyParser();
		} else {
			return new ParameterXMLParser();
		}
		
	}
	
}

