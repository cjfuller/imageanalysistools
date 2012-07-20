/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
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

package edu.stanford.cfuller.imageanalysistools.image.io;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

import edu.stanford.cfuller.imageanalysistools.image.PixelDataFactory;
import edu.stanford.cfuller.imageanalysistools.image.PixelData;
import edu.stanford.cfuller.imageanalysistools.image.ImgLibPixelData;

import net.imglib2.io.ImgOpener;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.img.array.ArrayImgFactory;

import java.util.Hashtable;

/**
 * This class reads an Image from a file on disk.
 * <p>
 * Uses the LOCI bio-formats library to do the reading (http://www.loci.wisc.edu/software/bio-formats), so
 * all formats supported by that library are supported in this reader.
 * <p>
 * For image formats that support multiple series per file (that is, multiple (X,Y,Z,C,T) 5D images per file), the reader
 * will return only a single series at a time, keeps track of what series it has last read, and will return the subsequent series
 * on subsequent calls to the read method.
 *
 * @author Colin J. Fuller
 */


public class ImgLibImageReader extends ImageReader {


    /**
     * Reads an image file from disk to an Image.
     * @param filename  The full absolute filename of the image file to read.
     * @return          An Image containing the pixel data and (if supported by the format) metadata from the image file.
     * @throws java.io.IOException  if there is an error reading the Image file from disk.
     */
	@Override
	public synchronized Image read(String filename) throws java.io.IOException {

		ImgOpener o = new ImgOpener();

		loci.formats.IFormatReader reader = null;

		try {
			lock(filename);
		} catch (InterruptedException e) {
			LoggingUtilities.getLogger().warning("interrupted while attempting to lock image file");
			return null;
		}

		try {
			reader = o.createReader(filename, true);
		} catch (loci.formats.FormatException e) {
			throw new java.io.IOException(e);
		}

		loci.formats.meta.IMetadata meta = (loci.formats.meta.IMetadata) reader.getMetadataStore();

		Img<FloatType> img = null;

		try {
			img = o.openImg(reader, new ArrayImgFactory<FloatType>(), new FloatType());
		} catch (net.imglib2.io.ImgIOException e) {
			throw new java.io.IOException(e);
		}

		PixelData p = PixelDataFactory.createPixelData(new ImgPlus<FloatType>(img), meta.getPixelsDimensionOrder(0).toString());

		Image toReturn = ImageFactory.create(meta, p);

		lociReader.close();
		seriesCounts.put(filename, 1);
		currentSeries.put(filename, 1);

		release(filename, Thread.currentThread());

		return toReturn;

	}


}
