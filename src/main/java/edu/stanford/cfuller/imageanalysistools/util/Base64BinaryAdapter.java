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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
* This class is an adapter to enable writing binary data to XML 
* (or other string representations) encoded as base 64.
* 
* @author Colin J. Fuller
*/
public class Base64BinaryAdapter extends XmlAdapter<String, byte[]> {
	
	final static String[] base64Lookup = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
										  "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d",
										  "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
										  "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7",
										  "8", "9", "+", "/"};
										
	final static String base64Pad = "=";
	
	static java.util.Map<String, Integer> revLookup;
	
	
	static {
		revLookup = new java.util.HashMap<String,Integer>();
		
		for (int i = 0; i < base64Lookup.length; i++) {
			revLookup.put(base64Lookup[i], i);
		}
	}
	
	/**
	* Encodes an array of bytes as a base-64 string.
	* @param bytes the byte array containing the data to encode.
	* @return a String containing the encoded data
	*/
	public String marshal(byte[] bytes) {
		int n_pad = ((3 - (bytes.length % 3)) % 3);
		
		int paddedBytesLength = bytes.length + n_pad;
			
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < paddedBytesLength; i+=3) {
			
			byte b0 = bytes[i];
			
			byte b1 = 0;
			byte b2 = 0;
			
			if (i+1 < bytes.length) {
				b1 = bytes[i+1];
			}
			
			if (i+2 < bytes.length) {
				b2 = bytes[i+2];
			}
						
			int sixBits0 = (b0 & 0x00FF) >>> 2;
			int sixBits1 = ((b0 & 0x03) << 4) + ((b1 & 0x00FF) >>> 4);
			int sixBits2 = ((b1 & 0x0F) << 2) + ((b2 & 0x00FF) >>> 6);
			int sixBits3 = b2 & 0x3F;
			
			boolean lastSet = (i + 3 >= bytes.length);
			
			sb.append(base64Lookup[sixBits0]);
			sb.append(base64Lookup[sixBits1]);
			
			if (!lastSet || n_pad < 2) {
			
				sb.append(base64Lookup[sixBits2]);
				
			}
			
			if (!lastSet || n_pad < 1) {
			
				sb.append(base64Lookup[sixBits3]);
			}
			
			
			
		}
		
		for (int i = 0; i < n_pad; i++) {
			sb.append(base64Pad);
		}
		
		return sb.toString();
		
	}
	
	/**
	* Decodes a base-64 encoded string to a byte array.
	* @param s the base-64 encoded String
	* @return a byte array containing the decoded data
	*/
	public byte[] unmarshal(String s) {
		
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		
		int n_pad = 0;
		
		if (s.length() > 0 && s.endsWith("==")) {
			n_pad = 2;
		} else if (s.length() > 0 && s.endsWith("=")) {
			n_pad = 1;
		}
				
		int shift_max = 18;
		int shift = shift_max;
		int shift_inc = 6;
		
		int endLength = 0;
		
		if (n_pad > 0 ) {
			endLength = 4;
		}
		
		long threeBytes = 0;
		
		for (int i = 0; i < s.length() - endLength; i++) {
			
			String currChar = s.substring(i, i+1);
			
			int lookupVal = revLookup.get(currChar);
			
			threeBytes += (lookupVal << shift);
			
			shift -= shift_inc;
			
			if (shift < 0) {
				
				shift = shift_max;
				
				out.write((byte) ((threeBytes & 0xFF0000) >>> 16));
				out.write((byte) ((threeBytes & 0x00FF00) >>> 8));
				out.write((byte) ((threeBytes & 0x0000FF)));
				
				threeBytes = 0;
			}
			
		}
		
		threeBytes = 0;
		
		if (n_pad > 0) {
			shift = shift_max;
			
			for (int i = 0; i < 4; i++) {
				
				String currChar = s.substring(s.length() - endLength + i, s.length() - endLength + i+1);
				
				if (currChar.equals("=")) {
					continue;
				}

				int lookupVal = revLookup.get(currChar);

				threeBytes += (lookupVal << shift);

				shift -= shift_inc;

				
			}
			
			out.write((byte) ((threeBytes & 0xFF0000) >> 16));
			
			if (n_pad == 1) {
				out.write((byte) ((threeBytes & 0x00FF00) >> 8));
			}
				
		}
		
		return out.toByteArray();

	}
	
}
