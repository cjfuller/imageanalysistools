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

package edu.stanford.cfuller.imageanalysistools.image.io;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.PixelData;


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


public class ImageReader {

	protected loci.formats.ImageReader lociReader;
    protected java.util.HashMap<String, Integer> seriesCounts;
    protected java.util.HashMap<String, Integer> currentSeries;

    protected static java.util.Hashtable<String, Thread> fileLocks;

    protected static long LOCK_WAIT_TIME_MS = 10000;

    static {
        fileLocks = new Hashtable<String, Thread>();
    }

    /**
     * Constructs a new default ImageReader.
     */
	public ImageReader() {
		lociReader = new loci.formats.ImageReader();
        currentSeries = new java.util.HashMap<String, Integer>();
        seriesCounts = new java.util.HashMap<String, Integer>();
	}

    /**
     * Checks if the file specified in the supplied filename has more 5D image series that have not yet been read by this reader.
     * @param filename  The full absolute filename of the image file to check.
     * @return          true if there are more series that have not yet been read; false otherwise
     */
    public boolean hasMoreSeries(String filename) {
        if (!currentSeries.containsKey(filename)) return true;
        return (currentSeries.get(filename) < seriesCounts.get(filename));
    }

    /**
     * Gets the number of 5D image series in the file specified in the supplied filename.
     * This can only be called after the read method has been called on this filename at least once; otherwise it
     * will cause an exception.
     * @param filename  The full absolute filename of the image file to check.
     * @return          The number of 5D image series in the specified file.
     */
    public int getSeriesCount(String filename) {
        return (seriesCounts.get(filename));
    }


    /**
     * Gets the index (starting at zero) of the next 5D image series that would be read by a call to the read method.
     * This method can only be called after the read method has been called on this filename at least once; otherwise it
     * will cause an exception.
     * <p>
     * Note that the value returned by this method will always be between 1 and the result of getSeriesCount(filename), inclusive.
     * Since the read method must have been called once before calling this method, the 0th series will always have been read already,
     * so at minimum the next series will be the first series.  If there are no more series remaining in the file, the next that
     * would be read would be one more than the maximal series index (which is getSeriesCount(filename) - 1).
     *
     * @param filename  The full absolute filename of the image file to check.
     * @return          The series that will be the next to be read by this reader.
     */
    public int getCurrentSeries(String filename) {
        return (currentSeries.get(filename));
    }

    /**
     * Reads an image file from disk to an Image.
     * @param filename  The full absolute filename of the image file to read.
     * @return          An Image containing the pixel data and (if supported by the format) metadata from the image file.
     * @throws java.io.IOException  if there is an error reading the Image file from disk.
     */
	public synchronized Image read(String filename) throws java.io.IOException {
		
		loci.plugins.in.ImporterOptions opts = new loci.plugins.in.ImporterOptions();
		
		opts.setId(filename);
		opts.setWindowless(true);
		
		loci.plugins.in.ImportProcess imp = new loci.plugins.in.ImportProcess(opts);
		
		ij.ImagePlus implus = null;
		
		try {
		
			imp.execute();
			
			loci.plugins.in.ImagePlusReader impr = new loci.plugins.in.ImagePlusReader(imp);

			implus = (impr.openImagePlus())[0];
			
		} catch (loci.formats.FormatException e) {
			throw new java.io.IOException(e);
		}		
		
		PixelData p = new edu.stanford.cfuller.imageanalysistools.image.ImagePlusPixelData(implus);
		
		Image toReturn = new Image((loci.formats.meta.IMetadata) imp.getBaseReader().getMetadataStore(), p);
		
		return toReturn;
		
	}

    /**
     * Locks the file at the specified filename for reading by an ImageReader.
     * 
     * @param filename  The file to lock.
     * @return          The Thread that holds the lock.  This should always be the current thread on normal return.
     * @throws InterruptedException
     */
    protected static Thread lock(String filename) throws InterruptedException {


        synchronized (ImageReader.class) {

            if (!fileLocks.containsKey(filename)) {
                fileLocks.put(filename, Thread.currentThread());
            }

        }

        while(!Thread.currentThread().equals(fileLocks.get(filename))) {

            Thread.sleep(LOCK_WAIT_TIME_MS);

            synchronized (ImageReader.class) {

                if (!fileLocks.containsKey(filename)) {
                    fileLocks.put(filename, Thread.currentThread());
                }

            }



        }

        return Thread.currentThread();


    }


    /**
     * Locks the file at the specified filename for reading by a specified Thread.
     *
     * The current Thread must still be the holder of the lock, or the file must be unlocked, or this method will block.
     *
     * @param filename  The file to lock.
     * @param toHoldLock    The Thread that should hold the lock. 
     * @return              The Thread that holds the lock.  This should always be the specified thread on normal return.
     * @throws InterruptedException
     */
    protected static Thread lockWithThread(String filename, Thread toHoldLock) throws InterruptedException {


        boolean didLock = false;

        synchronized (ImageReader.class) {

            if (!fileLocks.containsKey(filename)) {
                didLock = true;
                fileLocks.put(filename, toHoldLock);
            }

        }

        while(!didLock && !Thread.currentThread().equals(fileLocks.get(filename))) {

            Thread.sleep(LOCK_WAIT_TIME_MS);

            synchronized (ImageReader.class) {

                if (!fileLocks.containsKey(filename)) {
                    didLock = true;
                    fileLocks.put(filename, toHoldLock);
                }

            }



        }

        return toHoldLock;


    }


    /**
     * Releases the lock on the specified file, with the specified Thread that holds the lock.
     * @param filename      The file to unlock.
     * @param lockObject    The Thread that holds the lock.
     */
    protected void release(String filename, Thread lockObject) {

        synchronized (ImageReader.class) {

            if(lockObject.equals(fileLocks.get(filename))) {
                fileLocks.remove(filename);
            }

        }
        
    }
	
	
	
}
