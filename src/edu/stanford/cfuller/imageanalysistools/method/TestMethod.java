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

package edu.stanford.cfuller.imageanalysistools.method;


import edu.stanford.cfuller.imageanalysistools.matching.KinetochoreMatcher;

/**
 * A veritable playground of image analysis that will sit in the GUI and allow
 * easy testing of new methods.
 * 
 * @author Colin J. Fuller
 *
 */
public class TestMethod extends Method {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.method.Method#go()
	 */
	@Override
	public void go() {
		
//		Image input = new Image(this.images.get(2));
//		KernelFilterND kf = new KernelFilterND();
//		
//		double[] d = {0.1, 0.2, 0.4, 0.2, 0.1};
//				
//		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.X, d);
//		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Y, d);
//		kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d);
//		java.util.Vector<Filter> filters = new java.util.Vector<Filter>();
//        		
//        Renormalization3DFilter LBE3F = new Renormalization3DFilter();
//       
//        
////        filters.add(LBE3F);
////        LBE3F.setParameters(this.parameters);
////        
////        Image QOSeg = new Image(input);
////        LBE3F.apply(QOSeg);
//        
//        filters.add(kf);
//        
//        LaplacianFilterND lf = new LaplacianFilterND();
//        
//        filters.add(lf);
//        
//        filters.add(new ZeroPointFilter());
//        
//        filters.add(new MaximumSeparabilityThresholdingFilter());
//        filters.add(new Label3DFilter());
//        filters.add(new SizeAbsoluteFilter());
//        filters.add(new RelabelFilter());
//        
//        this.parameters.setValueForKey("min_size", "10");
//        this.parameters.setValueForKey("max_size", "1000000");
//        
////        filters.add(new RecursiveMaximumSeparability3DFilter());
////        filters.add(new RelabelFilter());
////        filters.add(new SizeAbsoluteFilter());
////        filters.add(new RelabelFilter());
//
//        for (Filter i : filters){
//            i.setParameters(this.parameters);
////            i.setReferenceImage(QOSeg);
//            i.setReferenceImage(this.images.get(0));
//        }
//
////        Image toProcess = new Image(QOSeg);
//
//        iterateOnFiltersAndStoreResult(filters, input, new ZeroMetric());
		
        KinetochoreMatcher km = new KinetochoreMatcher();
        
        km.setParameters(this.parameters);
        
        km.makePairs(this.images.get(0), this.images.get(2));
        
       
//        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();
//
//        filters.add(new RenormalizationFilter());
//		
//        for (Filter f : filters) {
//            f.setParameters(this.parameters);
//            f.setReferenceImage(this.images.get(0));
//        }
//
//        Image toProcess = new Image(this.images.get(0));
//        //java.awt.image.BufferedImage buffered = toProcess.toBufferedImage();
//        
//        //Image converted = new Image(buffered);
//
//        
//        iterateOnFiltersAndStoreResult(filters, new Image(toProcess), new edu.stanford.cfuller.imageanalysistools.metric.ZeroMetric());

//		long start = System.currentTimeMillis();
//		
//		double d = 0;
//		
//		Image process = this.images.get(0);
//		
//		for (ImageCoordinate ic : process) {
//			d += process.getValue(ic);
//		}
//		
//		long end = System.currentTimeMillis();
//		
//		System.out.println("time: " + (end-start));
		
	}

}
