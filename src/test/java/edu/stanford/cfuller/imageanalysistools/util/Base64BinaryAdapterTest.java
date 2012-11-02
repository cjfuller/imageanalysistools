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

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;


public class Base64BinaryAdapterTest {
	
	private Base64BinaryAdapter adapter;
	
	
	@Before public void init() {
		this.adapter = new Base64BinaryAdapter();
	}
	
	//test a short string
	@Test public void test0() {
		String t = "This is a test.";
		
		byte[] b = t.getBytes();
				
		String encoded = this.adapter.marshal(b);
		
		byte[] out = this.adapter.unmarshal(encoded);
		
		assertTrue(java.util.Arrays.equals(b, out));
		
		
		
	}
		
	//test a long string
	@Test public void test1() {
		
		String merchantOfVenice = "How sweet the moonlight sleeps upon this bank!\n"
		+ "Here will we sit and let the sounds of music\n"
		+ "Creep in our ears: soft stillness and the night\n"
		+ "Become the touches of sweet harmony.\n"
		+ "Sit, Jessica. Look how the floor of heaven\n"
		+ "Is thick inlaid with patines of bright gold:\n"
		+ "There's not the smallest orb which thou behold'st\n"
		+ "But in his motion like an angel sings,\n"
		+ "Still quiring to the young-eyed cherubins;\n"
		+ "Such harmony is in immortal souls;\n"
		+ "But whilst this muddy vesture of decay\n"
		+ "Doth grossly close it in, we cannot hear it.\n";
		
		byte[] b = merchantOfVenice.getBytes();
		
		String encoded = this.adapter.marshal(b);
		
		byte[] out = this.adapter.unmarshal(encoded);
		
		assertTrue(java.util.Arrays.equals(b, out));

	}
	
	//test 100 streams of random byte values
	@Test public void test2() {
		
		java.util.Random rng = new java.util.Random(System.currentTimeMillis());
		
		int minLength = 100;
		int maxLength = 1 << 22;
		
		int n_tests = 100;
		
		for (int i = 0; i < n_tests; i++) {
								
			int length = rng.nextInt();
		
			if (length < 0) length *= -1;
		
			if (length < minLength) length = minLength;
			
			if (length > maxLength) length = length >>> 10;
				
			byte[] testBytes = new byte[length];
		
			rng.nextBytes(testBytes);
		
			String encoded = this.adapter.marshal(testBytes);
		
			byte[] out = this.adapter.unmarshal(encoded);
		
			assertTrue(java.util.Arrays.equals(testBytes, out));
			
		}
		
	}
	
		
}

