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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * @author cfuller
 *
 */
public class TimepointSplittingMethod extends Method {

	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.method.Method#go()
	 */
	@Override
	public void go() {


        //first create the reference Image

        ImageCoordinate dimSizes = ImageCoordinate.cloneCoord(this.images.get(0).getDimensionSizes());

        dimSizes.set(ImageCoordinate.C,this.images.size());

        WritableImage reference = ImageFactory.createWritable(dimSizes, 0.0f);

        for (ImageCoordinate ic : reference) {
            ImageCoordinate ic_c = ImageCoordinate.cloneCoord(ic);
            ic_c.set(ImageCoordinate.C,0);
            reference.setValue(ic, this.imageSet.getImageForIndex(ic.get(ImageCoordinate.C)).getValue(ic_c));
            ic_c.recycle();
        }

        //now create the output images

        java.util.List<Image> split = reference.split(ImageCoordinate.T);
        
        for (Image singleT : split) {
        	this.storeImageOutput(singleT);
        }


        dimSizes.recycle();
		
	}

}
