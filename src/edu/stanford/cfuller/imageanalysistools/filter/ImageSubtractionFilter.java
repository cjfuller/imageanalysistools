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
package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

public class ImageSubtractionFilter extends Filter {
	
	boolean subtractPlanarImage;
	
	public ImageSubtractionFilter() {
		this.subtractPlanarImage = false;
	}
	
	public void setSubtractPlanarImage(boolean subPlanar) {
		this.subtractPlanarImage = subPlanar;
	}
	
	public void apply(Image im) {
		
		if (this.referenceImage == null) {
			throw new ReferenceImageRequiredException("ImageSubtractionFilter requires a reference image.");
		}
		
		if (this.subtractPlanarImage) {
			ImageCoordinate ic2 = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
			
			for (ImageCoordinate ic : im) {
				ic2.set(ImageCoordinate.X, ic.get(ImageCoordinate.X));
				ic2.set(ImageCoordinate.Y, ic.get(ImageCoordinate.Y));
				im.setValue(ic, im.getValue(ic) - this.referenceImage.getValue(ic2));

			}
			
			ic2.recycle();
			
		} else {
		
			for (ImageCoordinate ic : im) {
				im.setValue(ic, im.getValue(ic) - this.referenceImage.getValue(ic));
			}
			
		}
		
	}
	
}