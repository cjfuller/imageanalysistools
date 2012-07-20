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

public class RubyScript {
	
	String name;
	String script;
	
	public RubyScript(String filename) {
		this.name = filename;
		this.script = "";
		
		try {
			StringBuilder sb = new StringBuilder();
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filename));
			String line = br.readLine();
			while(line != null) {
				sb.append(line);
				sb.append("\n");
				line= br.readLine();
			}
			this.script = sb.toString();
		} catch (java.io.IOException e) {
			edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().severe("Exception while reading ruby script for analysis: " + e.getMessage());
		}
				
	}
	
	public RubyScript(String scriptString, String name) {
		this.script = scriptString;
		this.name = name;
	}
	
	public String getScriptString() {
		return this.script;
	}
	
	public String getName() {
		return this.name;
	}
		
	
}


