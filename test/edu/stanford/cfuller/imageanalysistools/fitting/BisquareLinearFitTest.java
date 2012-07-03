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
package edu.stanford.cfuller.imageanalysistools.fitting;

import org.junit.Test;
import org.junit.Before;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import static org.junit.Assert.*;


public class BisquareLinearFitTest {
	
	RealVector x;
	RealVector y;
	RealVector w;
	RealVector expected;
	BisquareLinearFit bslf;
	RealVector result;
	boolean success;
	double eps;
	
	@Before public void init() {
		this.success = true;
		this.eps = 1e-3;
		
		this.x = new ArrayRealVector(5,0.0);
		this.y = new ArrayRealVector(5,0.0);
		this.w = new ArrayRealVector(5,1.0);
		this.expected = new ArrayRealVector(2,0.0);
		this.result = null;
		this.bslf = new BisquareLinearFit();
	}
	
	//tests a straight line, slope 1, no y offset
	@Test public void testwls0() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i);
			this.w.setEntry(i, 1);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,0);
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, no y offset, y-int disabled
	@Test public void testwls1() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i);
			this.w.setEntry(i, 1);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,0);
				
		this.bslf.disableIntercept();
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);

	}

	//tests a straight line, slope 1, with y offset
	@Test public void testwls2() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
			this.w.setEntry(i, 1);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,1);
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, with y offset, y-int disabled
	@Test public void testwls3() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
			this.w.setEntry(i, 1);
		}
		
		this.expected.setEntry(0,1.333333333333);
		this.expected.setEntry(1,0);
				
		this.bslf.disableIntercept();
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, with y offset, uneven weights
	@Test public void testwls4() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
			this.w.setEntry(i, i);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,1);
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, with y offset, y-int disabled, uneven weights
	@Test public void testwls5() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
			this.w.setEntry(i, i);
		}
		
		this.expected.setEntry(0,1.3);
		this.expected.setEntry(1,0);
				
		this.bslf.disableIntercept();
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}

	//tests some scattered points with y offset, even weights
	@Test public void testwls6() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.w.setEntry(i, 1);
		}
		
		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);
		
		this.expected.setEntry(0,-2.3);
		this.expected.setEntry(1,11.2);
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests some scattered points with y offset, y-int disabled, even weights
	@Test public void testwls7() {
	
		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.w.setEntry(i, 1);
		}
		
		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);

		this.expected.setEntry(0,1.433);
		this.expected.setEntry(1,0);

		this.bslf.disableIntercept();

		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);

		assertTrue(success);

	}
	
	//tests some scattered points with y offset, uneven weights
	@Test public void testwls8() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.w.setEntry(i, i);
		}
		
		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);
		
		
		this.expected.setEntry(0,-4.6);
		this.expected.setEntry(1,18.1);
				
		this.result = this.bslf.wlsFit(x,y,w);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests some scattered points with y offset, y-int disabled, uneven weights
	@Test public void testwls9() {
	
		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.w.setEntry(i, i);
		}

		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);

		this.expected.setEntry(0,0.83);
		this.expected.setEntry(1,0);

		this.bslf.disableIntercept();

		this.result = this.bslf.wlsFit(x,y,w);

		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);

		assertTrue(success);

	}

	//tests a straight line, slope 1, no y offset
	@Test public void testbis0() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,0);
				
		this.result = this.bslf.fit(x,y);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, no y offset, y-int disabled
	@Test public void testbis1() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,0);
				
		this.bslf.disableIntercept();
				
		this.result = this.bslf.fit(x,y);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);

	}

	//tests a straight line, slope 1, with y offset
	@Test public void testbis2() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
		}
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,1);
				
		this.result = this.bslf.fit(x,y);
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests a straight line, slope 1, with y offset, y-int disabled
	@Test public void testbis3() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i, i+1);
		}
		
		this.expected.setEntry(0,1.3329);
		this.expected.setEntry(1,0);
				
		this.bslf.disableIntercept();
				
		this.result = this.bslf.fit(x,y);		
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests some scattered points with y offset
	@Test public void testbis6() {

		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
		}
		
		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);
		
		this.expected.setEntry(0,-2.349);
		this.expected.setEntry(1,11.2717);
								
		this.result = this.bslf.fit(x,y);
				
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);
		
		assertTrue(success);
		
	}
	
	//tests some scattered points with y offset, y-int disabled
	@Test public void testbis7() {
	
		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
		}
		
		double[] testY = {5,17,8,2,1};
		
		this.y = new ArrayRealVector(testY);

		this.expected.setEntry(0,1.2939);
		this.expected.setEntry(1,0);

		this.bslf.disableIntercept();

		this.result = this.bslf.fit(x,y);		
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);

		assertTrue(success);

	}
	
	//tests some linear points with outlier, with y offset, y-int enabled
	@Test public void testbis10() {
	
		int test_size = 18;
	
		this.x = new ArrayRealVector(test_size,0.0);
		this.y = new ArrayRealVector(test_size,0.0);
		
	
		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i,i+1);
		}
		this.x.setEntry(test_size-1,92);
		this.y.setEntry(test_size-1,343);
		
		this.expected.setEntry(0,1);
		this.expected.setEntry(1,1);

		this.result = this.bslf.fit(x,y);		
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);

		assertTrue(success);

	}
	
	//tests some linear points with outlier, with y offset, y-int disabled
	@Test public void testbis11() {
	
		int test_size = 18;
	
		this.x = new ArrayRealVector(test_size,0.0);
		this.y = new ArrayRealVector(test_size,0.0);
		
	
		for (int i = 0; i < this.x.getDimension(); i++) {
			this.x.setEntry(i, i);
			this.y.setEntry(i,i+1);
		}
		this.x.setEntry(test_size-1,92);
		this.y.setEntry(test_size-1,343);

		this.expected.setEntry(0,1.0903);
		this.expected.setEntry(1,0);

		this.bslf.disableIntercept();

		this.result = this.bslf.fit(x,y);		
		
		this.success &= (this.result.subtract(this.expected).getNorm() < this.eps);

		assertTrue(success);

	}
	

}


