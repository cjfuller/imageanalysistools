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

package edu.stanford.cfuller.imageanalysistools.util;

import java.security.DigestInputStream;
import java.security.MessageDigest;


public class FileHashCalculator {
	
	public static final String ALG_SHA1 = "SHA1";
	
	public static final String ALG_DEFAULT = ALG_SHA1;
	
	public static String calculateHash(String algorithm, String filename) throws java.io.IOException {
		
		MessageDigest md = null;
		
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (java.security.NoSuchAlgorithmException e) {
			edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.getLogger().warning("Unable to hash with algorithm " + algorithm + ": " + e.getMessage());
		}
		
		if (md == null) {return null;}
		
		DigestInputStream dis = new DigestInputStream(new java.io.FileInputStream(filename), md);
		
		byte[] b = new byte[1048576];
		
		while (dis.available() > 0) {
			dis.read(b);
		}
		
		md = dis.getMessageDigest();
		
		byte[] dig = md.digest();
		
		String hex = (new javax.xml.bind.annotation.adapters.HexBinaryAdapter()).marshal(dig);
	
		return hex;
		
	}
	
}


