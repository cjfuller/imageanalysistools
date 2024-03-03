package edu.stanford.cfuller.imageanalysistools.image.io

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.PixelDataFactory
import java.util.Hashtable

/**
 * This class reads an Image from a file on disk.
 *
 * Uses the LOCI bio-formats library to do the reading
 * (http://www.loci.wisc.edu/software/bio-formats), so all formats supported by that library are
 * supported in this reader.
 *
 * For image formats that support multiple series per file (that is, multiple (X,Y,Z,C,T) 5D images
 * per file), the reader will return only a single series at a time, keeps track of what series it
 * has last read, and will return the subsequent series on subsequent calls to the read method.
 * @author Colin J. Fuller
 */
open class ImageReader {
    protected var lociReader: loci.formats.IFormatReader? = null
    protected var seriesCounts: java.util.HashMap<String, Int> = java.util.HashMap<String, Int>()
    protected var currentSeries: java.util.HashMap<String, Int> = java.util.HashMap<String, Int>()

    /**
     * Checks if the file specified in the supplied filename has more 5D image series that have not
     * yet been read by this reader.
     * @param filename The full absolute filename of the image file to check.
     * @return true if there are more series that have not yet been read; false otherwise
     */
    fun hasMoreSeries(filename: String): Boolean {
        if (!currentSeries.containsKey(filename)) return true
        return currentSeries[filename]!! < seriesCounts[filename]!!
    }

    /**
     * Gets the number of 5D image series in the file specified in the supplied filename. This can
     * only be called after the read method has been called on this filename at least once;
     * otherwise it will cause an exception.
     * @param filename The full absolute filename of the image file to check.
     * @return The number of 5D image series in the specified file.
     */
    fun getSeriesCount(filename: String): Int {
        return seriesCounts[filename]
                ?: throw IllegalArgumentException("Must call .read() on the same filename first.")
    }

    /**
     * Gets the index (starting at zero) of the next 5D image series that would be read by a call to
     * the read method. This method can only be called after the read method has been called on this
     * filename at least once; otherwise it will cause an exception.
     *
     * Note that the value returned by this method will always be between 1 and the result of
     * getSeriesCount(filename), inclusive. Since the read method must have been called once before
     * calling this method, the 0th series will always have been read already, so at minimum the
     * next series will be the first series. If there are no more series remaining in the file, the
     * next that would be read would be one more than the maximal series index (which is
     * getSeriesCount(filename) - 1).
     * @param filename The full absolute filename of the image file to check.
     * @return The series that will be the next to be read by this reader.
     */
    fun getCurrentSeries(filename: String): Int {
        return currentSeries[filename]
                ?: throw IllegalArgumentException("Must call .read() on the same filename first.")
    }

    /**
     * Reads an image file from disk to an Image.
     * @param filename The full absolute filename of the image file to read.
     * @return An Image containing the pixel data and (if supported by the format) metadata from the
     * image file.
     * @throws java.io.IOException if there is an error reading the Image file from disk.
     */
    @Synchronized
    @Throws(java.io.IOException::class)
    open fun read(filename: String): Image? {
        val reader: loci.formats.IFormatReader =
                lociReader ?: loci.formats.ImageReader().getReader(filename)

        lociReader = reader

        try {
            lock(filename)
        } catch (e: InterruptedException) {
            LoggingUtilities.logger.warning("interrupted while attempting to lock image file")
            return null
        }

        try {
            try {
                reader.setId(filename)
            } catch (e: IllegalStateException) {
                reader.close()
                reader.setId(filename)
            }
        } catch (e: loci.formats.FormatException) {
            throw java.io.IOException(e)
        }

        loci.formats.MetadataTools.populatePixels(reader.metadataStore, lociReader, true, true)
        val p =
                PixelDataFactory.createPixelData(
                        reader.sizeX,
                        reader.sizeY,
                        reader.sizeZ,
                        reader.sizeC,
                        reader.sizeT,
                        reader.pixelType,
                        reader.dimensionOrder
                )

        if (!((reader.metadataStore as loci.formats.meta.IMetadata).getPixelsBinDataBigEndian(0, 0)
                        ?: false)
        ) {
            p.setByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN)
        }

        try {
            if (!seriesCounts.containsKey(filename) || !hasMoreSeries(filename)) {
                seriesCounts.put(filename, reader.seriesCount)
                currentSeries.put(filename, 0)
            }
            reader.series = currentSeries[filename]!!

            for (i in 0..reader.imageCount - 1) {
                val currPlane = reader.openBytes(i)
                val zct = reader.getZCTCoords(i)
                p.setPlane(zct[0], zct[1], zct[2], currPlane)
            }
        } catch (e: loci.formats.FormatException) {
            e.printStackTrace()
            return null
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            return null
        }

        val toReturn = ImageFactory.create(reader.metadataStore as loci.formats.meta.IMetadata, p)
        reader.close()
        currentSeries.put(filename, currentSeries[filename]!! + 1)
        release(filename, Thread.currentThread())
        return toReturn
    }

    /**
     * Releases the lock on the specified file, with the specified Thread that holds the lock.
     * @param filename The file to unlock.
     * @param lockObject The Thread that holds the lock.
     */
    protected fun release(filename: String, lockObject: Thread) {
        synchronized(ImageReader::class.java) {
            if (lockObject == fileLocks[filename]) {
                fileLocks.remove(filename)
            }
        }
    }

    companion object {
        protected var fileLocks: java.util.Hashtable<String, Thread> = Hashtable<String, Thread>()
        protected var LOCK_WAIT_TIME_MS: Long = 10000

        /**
         * Locks the file at the specified filename for reading by an ImageReader.
         * @param filename The file to lock.
         * @return The Thread that holds the lock. This should always be the current thread on
         * normal return.
         * @throws InterruptedException TODO(colin): why do we need to lock for reading?
         */
        @Throws(InterruptedException::class)
        fun lock(filename: String): Thread {
            synchronized(ImageReader::class.java) {
                if (!fileLocks.containsKey(filename)) {
                    fileLocks.put(filename, Thread.currentThread())
                }
            }

            while (Thread.currentThread() != fileLocks[filename]) {
                Thread.sleep(LOCK_WAIT_TIME_MS)
                synchronized(ImageReader::class.java) {
                    if (!fileLocks.containsKey(filename)) {
                        fileLocks.put(filename, Thread.currentThread())
                    }
                }
            }
            return Thread.currentThread()
        }

        /**
         * Locks the file at the specified filename for reading by a specified Thread. The current
         * Thread must still be the holder of the lock, or the file must be unlocked, or this method
         * will block.
         * @param filename The file to lock.
         * @param toHoldLock The Thread that should hold the lock.
         * @return The Thread that holds the lock. This should always be the specified thread on
         * normal return.
         * @throws InterruptedException
         */
        @Throws(InterruptedException::class)
        fun lockWithThread(filename: String, toHoldLock: Thread): Thread {
            var didLock = false
            synchronized(ImageReader::class.java) {
                if (!fileLocks.containsKey(filename)) {
                    didLock = true
                    fileLocks.put(filename, toHoldLock)
                }
            }

            while (!didLock && Thread.currentThread() != fileLocks[filename]) {
                Thread.sleep(LOCK_WAIT_TIME_MS)
                synchronized(ImageReader::class.java) {
                    if (!fileLocks.containsKey(filename)) {
                        didLock = true
                        fileLocks.put(filename, toHoldLock)
                    }
                }
            }
            return toHoldLock
        }
    }
}
