/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.image.io;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.PixelData;
import edu.stanford.cfuller.imageanalysistools.image.PixelDataFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Hashtable;

/**
 * This class reads an Image from a file on disk.
 * <p>
 * Uses the LOCI bio-formats library to do the reading (http://http://www.loci.wisc.edu/software/bio-formats), so
 * all formats supported by that library are supported in this reader.
 * <p>
 * For image formats that support multiple series per file (that is, multiple (X,Y,Z,C,T) 5D images per file), the reader
 * will return only a single series at a time, keeps track of what series it has last read, and will return the subsequent series
 * on subsequent calls to the read method.
 *
 * @author Colin J. Fuller
 */


public class ImageReader {

	private loci.formats.ImageReader lociReader;
    private java.util.HashMap<String, Integer> seriesCounts;
    private java.util.HashMap<String, Integer> currentSeries;

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
		

		try {
		
			loci.formats.meta.IMetadata meta = (new loci.common.services.ServiceFactory()).getInstance(loci.formats.services.OMEXMLService.class).createOMEXMLMetadata();
			try {
                lociReader.setMetadataStore(meta);
            } catch (IllegalStateException e) {
                lociReader.close();
                lociReader.setMetadataStore(meta);
            }


		} catch (loci.common.services.ServiceException e) {
			e.printStackTrace();
		} catch (loci.common.services.DependencyException e) {
			e.printStackTrace();
		}

        try {
            lock(filename);
        } catch (InterruptedException e) {
            LoggingUtilities.getLogger().warning("interrupted while attempting to lock image file");
            return null;
        }
		
		try {
            try {
			    lociReader.setId(filename);
            } catch (IllegalStateException e) {
                lociReader.close();
                lociReader.setId(filename);
            }
		} catch (loci.formats.FormatException e) {
			throw new java.io.IOException(e);
		}
		
		loci.formats.MetadataTools.populatePixels(lociReader.getMetadataStore(), lociReader);
		
		PixelData p = (new PixelDataFactory()).createPixelData(lociReader.getSizeX(), lociReader.getSizeY(), lociReader.getSizeZ(), lociReader.getSizeC(), lociReader.getSizeT(), lociReader.getPixelType(), lociReader.getDimensionOrder());
		
		int datasize_bytes = 0;
		
        byte[] pixel_bytes = null;

		int byte_position= 0;
		
		try {
            if (!seriesCounts.containsKey(filename) || !hasMoreSeries(filename)) {
                seriesCounts.put(filename, lociReader.getSeriesCount());
                currentSeries.put(filename, 0);
            }
            lociReader.setSeries(currentSeries.get(filename));

            pixel_bytes = new byte[lociReader.getSizeX()*lociReader.getSizeY()*lociReader.getSizeZ()*lociReader.getSizeC()*lociReader.getSizeT()*((int) Math.ceil(lociReader.getBitsPerPixel()*1.0/8))];

			for (int i = 0; i < lociReader.getImageCount(); i++) {
				byte[] currPlane = lociReader.openBytes(i);
                System.arraycopy(currPlane, 0, pixel_bytes,byte_position,currPlane.length);
				byte_position += currPlane.length;
			}
		} catch (loci.formats.FormatException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		}
		
		if (!((loci.formats.meta.IMetadata) lociReader.getMetadataStore()).getPixelsBinDataBigEndian(0, 0)) {
			p.setByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN);
		}
		
		p.setBytes(pixel_bytes);
		
				
		Image toReturn = new Image((loci.formats.meta.IMetadata) lociReader.getMetadataStore(), p);
		
		lociReader.close();

        currentSeries.put(filename, currentSeries.get(filename) + 1);

        release(filename, Thread.currentThread());

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
