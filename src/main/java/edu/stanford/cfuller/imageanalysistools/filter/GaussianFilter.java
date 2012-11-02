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

package edu.stanford.cfuller.imageanalysistools.filter;

import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import net.imglib2.img.ImgPlus;
import net.imglib2.algorithm.gauss.GaussFloat;
import net.imglib2.type.numeric.real.FloatType;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImgLibPixelData;
import edu.stanford.cfuller.imageanalysistools.image.ImagePlusPixelData;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;

/**
 * A Filter that applies a gaussian blur to a 2D Image.
 * <p>
 * This Filter does not use a reference Image.
 *<p>
 * The argument passed to the apply method is the Image to be blurred.
 * 
 *@author Colin J. Fuller
 *
 */


public class GaussianFilter extends Filter {
	
	//TODO: deal with more than 2 dimensional blur. (Or make that be the job of other filters and rename this one -2D?)
	
	int width;
   // boolean precalculatedFFT;
    //Complex[] kernelFFT;

    /**
     * Constructs a GaussianFilter with a default size blur.
     */
	public GaussianFilter() {
		this.width = 5;
       // this.precalculatedFFT = false;
        //this.kernelFFT = null;
	}

    /**
     * Applies the GaussianFilter to the specified Image, blurring it by convolution with a Gaussian function.
     * @param im    The Image to process, which will be blurred.
     */
	@Override
	public void apply(WritableImage im) {

		int kernelSize = this.width;


        final int halfKernelSizeCutoff = 8;
        int halfKernelSize = (kernelSize - 1)/2;


        if (halfKernelSize > halfKernelSizeCutoff) {
            GaussianFilter gf2 = new GaussianFilter();
            gf2.setWidth(halfKernelSize);
            gf2.apply(im);
            gf2.apply(im);
            return;
        }

		//if we're dealing with an ImgLib image, use the ImgLib gaussian filtering to avoid duplication of image data.
		
		if (im.getPixelData() instanceof ImgLibPixelData) {
		
			ImgLibPixelData pd = (ImgLibPixelData) im.getPixelData();
			
			ImgPlus<FloatType> imP = pd.getImg();
			
			int numDim = imP.numDimensions();
			
			double [] sigmas = new double[numDim];
			
			java.util.Arrays.fill(sigmas, 0.0);

			sigmas[0] = this.width;
			sigmas[1] = this.width; // only filter in x-y
			
			new GaussFloat(sigmas, imP);
			
			return;
			
		}


        ImagePlus imP = im.toImagePlus();
        
        GaussianBlur gb = new GaussianBlur();
                
        for (int i = 0; i < imP.getImageStackSize(); i++) {
        	imP.setSliceWithoutUpdate(i+1);
            gb.blur(imP.getProcessor(), width);
        }
        
		//only recopy if not an ImagePlusPixelData underneath, which would make duplicating unnecessary

		if (! (im.getPixelData() instanceof ImagePlusPixelData)) {
			im.copy(ImageFactory.create(imP));
		}

   
	}

    /**
     * Sets the width of the Gaussian filter.  This is the standard deviation of the Gaussian function in units of pixels.
     * @param width     The width of the Gaussian to be used for filtering, in pixels.
     */
	public void setWidth(int width) {
       
		this.width = width;
		
		if (this.width % 2 == 0) {this.width+=1;}
		
	}

	
}
