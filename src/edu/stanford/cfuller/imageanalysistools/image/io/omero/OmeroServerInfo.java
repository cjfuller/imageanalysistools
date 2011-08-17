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

package edu.stanford.cfuller.imageanalysistools.image.io.omero;

/**
 * @author cfuller
 *
 */
public class OmeroServerInfo {

	private String hostname;
	private String username;
	private char[] password;
	
	public String getHostname() {return this.hostname;}
	public String getUsername() {return this.username;}
	public char[] getPassword() {return this.password;}
	
	/**
	 * Constructs a new OmeroServerInfo object given the information needed
	 * to connect to the server.
	 *      
	 * @param hostname				The IP address or resolvable hostname of the OMERO server.
	 * @param username              The username to use to connect.
     * @param password              The password for the provided username.
	 */
	public OmeroServerInfo(String hostname, String username, char[] password) {
		this.hostname = hostname; this.username = username; this.password = password;
	}
	
	protected void Finalize() throws Throwable {
		for (int i =0; i < this.password.length; i++) {
			password[i] = '\0';
		}
	}
	
}
