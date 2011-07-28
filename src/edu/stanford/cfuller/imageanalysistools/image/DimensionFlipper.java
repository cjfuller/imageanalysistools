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

package edu.stanford.cfuller.imageanalysistools.image;

/**
 * A Class that implements methods to flip the Z- and T- dimensions of an Image (these can get swapped, e.g. while reading
 * metamorph stacks).
 * 
 * @author Colin J. Fuller
 * 
 */
public class DimensionFlipper {


    public static Image flipZT(Image flipped) {
        
        ImageCoordinate sizes = ImageCoordinate.cloneCoord(flipped.getDimensionSizes());


        int temp_t = sizes.get("t");
        sizes.set("t",sizes.get("z"));
        sizes.set("z",temp_t);

        Image newImage = new Image(sizes, 0.0);

        ImageCoordinate flipCoord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

        for (ImageCoordinate ic : flipped) {
            flipCoord.setCoord(ic);
            flipCoord.set("z",ic.get("t"));
            flipCoord.set("t",ic.get("z"));

            newImage.setValue(flipCoord, flipped.getValue(ic));
        }

        flipCoord.recycle();
        sizes.recycle();

        return newImage;

    }


}
