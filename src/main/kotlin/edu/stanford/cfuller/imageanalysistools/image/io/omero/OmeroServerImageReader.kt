package edu.stanford.cfuller.imageanalysistools.image.io.omero

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader
import omero.ServerError
import omero.api.ExporterPrx

import java.io.*

/**
 * An ImageReader that reads images from an OMERO server instead of files on disk.

 * @author Colin J. Fuller
 */

//TODO: add functionality to have it remember login information, and redefine the read method to use this information, so this can be passed as an ImageReader and still function.

class OmeroServerImageReader : ImageReader() {
    internal var connection: OmeroServerConnection? = null

    /**
     * Constructs a new OmeroServerImageReader with no specified server.
     */
    init {
        this.connection = null
    }

    /**
     * Attempts a connection to the server at the specified address using the provided username and password.
     * Logs any errors in connecting, but returns normally regardless of whether the connection succeeded.
     * @param info     An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.
     */
    private fun connectToServer(info: OmeroServerInfo) {
        this.connection = OmeroServerConnection(info)
        this.connection!!.connect()
    }

    /**
     * Closes the connection to the OMERO server.
     */
    fun closeConnection() {
        this.connection!!.disconnect()
    }

    /**
     * Reads an Image with the specified Id on the OMERO server using the provided address, username, and password.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * *
     * @param info                    An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.
     * *
     * @return                      An Image containing the data from the Image identified by ID.  It is not guaranteed how much metadata, if any, will be retrieved.  Null if there is a problem retrieving the Image.
     * *
     * @throws IOException          If there is a problem with the temporary disk storage for the retrieved Image.
     */
    @Throws(IOException::class)
    fun readImageFromOmeroServer(omeroserverImageId: Long, info: OmeroServerInfo): Image? {
        val temporaryImageFilename = this.loadImageFromOmeroServer(omeroserverImageId, info) ?: return null
        return this.read(temporaryImageFilename[1]!!)
    }

    /**
     * Reads the name of an Image on the OMERO server given its Id.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param info                    An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.
     * @return                      A String containing the name of the Image.
     * @throws IOException         if the Image cannnot be accessed on the server.
     */
    @Throws(IOException::class)
    fun getImageNameForOmeroId(omeroserverImageId: Long, info: OmeroServerInfo): String {
        try {
            if (this.connection == null || !this.connection!!.isConnected) {
                this.connectToServer(info)
            }
            return this.connection!!.gateway!!.getImage(omeroserverImageId).name.value
        } catch (e: ServerError) {
            throw IOException(e)
        }
    }

    /**
     * Asynchronously loads the Image with the specified Id on the OMERO server using the provided address, username, and password, and stores it to a temporary file on disk.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param info                    An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.
     * @return                      An array containing two elements: first, the original name of the image on the OMERO server; second, the temporary filename to which the image is being stored.
     */
    fun loadImageFromOmeroServer(omeroserverImageId: Long, info: OmeroServerInfo): Array<String?>? {
        var tempfile: File? = null
        var originalName: String? = null
        try {
            if (this.connection == null || !this.connection!!.isConnected) {
                this.connectToServer(info)
            }
            originalName = this.getImageNameForOmeroId(omeroserverImageId, info)
            this.closeConnection()
            tempfile = File.createTempFile("omerodownload", ".ome.tif")
            tempfile!!.deleteOnExit()
            val fos = FileOutputStream(tempfile)
            val out = BufferedOutputStream(fos)
            val lockFilename = tempfile.absolutePath
            val lockingThread = Thread(Runnable {
                try {
                    if (connection == null || !connection!!.isConnected) {
                        try {
                            while (OmeroServerImageReader.numberOfConnections >= MAX_CONNECTIONS) {
                                Thread.sleep(SLEEP_TIME_MS)
                            }
                        } catch (e: InterruptedException) {
                            LoggingUtilities.logger.warning("interrupted while waiting on omero server connection")
                            return@Runnable
                        }
                        OmeroServerImageReader.incrementConnections()
                        connectToServer(info)
                    }

                    val exporter = connection!!.serviceFactory!!.createExporter()
                    exporter.addImage(omeroserverImageId)
                    val lengthOfFile = exporter.generateTiff()
                    var currPos: Long = 0
                    val chunkSize = 524288

                    while (currPos < lengthOfFile) {
                        val data = exporter.read(currPos, chunkSize)
                        currPos += data.size.toLong()
                        out.write(data)
                    }
                    exporter.close()
                    release(lockFilename, Thread.currentThread())
                } catch (serverError: ServerError) {
                    LoggingUtilities.logger.severe("Unable to retrieve image from omero server.  " + serverError.toString())
                    serverError.printStackTrace()
                } catch (e: IOException) {
                    LoggingUtilities.logger.severe("Exception while writing temporary image file to disk.")
                } finally {
                    closeConnection()
                    OmeroServerImageReader.decrementConnections()
                    connection = null
                }
            })

            try {
                ImageReader.lockWithThread(lockFilename, lockingThread)
            } catch (e: InterruptedException) {
                LoggingUtilities.logger.severe("interrupted while locking image file")
                return null
            }
            lockingThread.start()
        } catch (e: IOException) {
            LoggingUtilities.logger.severe("Exception while processing omero server images")
        }
        val toReturn = arrayOfNulls<String>(2)
        toReturn[0] = originalName
        toReturn[1] = if (tempfile != null) tempfile.absolutePath else null
        return toReturn
    }

    companion object {
        internal val MAX_CONNECTIONS = 10
        internal val SLEEP_TIME_MS: Long = 10000
        @Volatile internal var currentConnections: Int = 0

        init {
            currentConnections = 0
        }
        /**
         * Adds a connection to the count of current connections to the OMERO server.
         * Ensures that the server is not overloaded with connection requests.
         */
        private fun incrementConnections() {
            synchronized(OmeroServerImageReader::class.java) {
                OmeroServerImageReader.currentConnections += 1
            }
        }

        /**
         * Removes a connection from the count of current connections to the OMERO server.
         * Ensures that the server is not overloaded with connection requests.
         */
        private fun decrementConnections() {
            synchronized(OmeroServerImageReader::class.java) {
                OmeroServerImageReader.currentConnections -= 1
            }
        }

        /**
         * Gets the number of current connections to the OMERO server.
         * @return  The current number of connections.
         */
        private val numberOfConnections: Int
            get() = synchronized(OmeroServerImageReader::class.java) {
                return OmeroServerImageReader.currentConnections
            }
    }
}
