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

package edu.stanford.cfuller.imageanalysistools.image.io.omero;

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
import omero.ServerError;
import omero.api.ExporterPrx;

import java.io.*;

/**
 * An ImageReader that reads images from an OMERO server instead of files on disk.
 * 
 * @author Colin J. Fuller
 */

//TODO: add functionality to have it remember login information, and redefine the read method to use this information, so this can be passed as an ImageReader and still function.

public class OmeroServerImageReader extends ImageReader {

//    ServiceFactoryPrx serviceFactory;
//    client readerClient;
//    GatewayPrx gateway;

	OmeroServerConnection connection;
	
    static final int MAX_CONNECTIONS = 10;
    static final long SLEEP_TIME_MS = 10000;
    static volatile int currentConnections;


    static{
        currentConnections =0;
    }

    /**
     * Adds a connection to the count of current connections to the OMERO server.
     *
     * Ensures that the server is not overloaded with connection requests.
     */
    protected static void incrementConnections() {
        synchronized (OmeroServerImageReader.class) {

            OmeroServerImageReader.currentConnections += 1;


        }
    }


    /**
     * Removes a connection from the count of current connections to the OMERO server.
     *
     * Ensures that the server is not overloaded with connection requests.
     */
    protected static void decrementConnections() {

        synchronized (OmeroServerImageReader.class) {

            OmeroServerImageReader.currentConnections -= 1;


        }
    }

    /**
     * Gets the number of current connections to the OMERO server.
     *
     * @return  The current number of connections.
     */
    protected static int getNumberOfConnections() {

        synchronized (OmeroServerImageReader.class) {

            return OmeroServerImageReader.currentConnections;

        }
    }

    /**
     * Constructs a new OmeroServerImageReader with no specified server.
     */
    public OmeroServerImageReader() {
        this.connection = null;
    }


    /**
     * Attempts a connection to the server at the specified address using the provided username and password.
     *
     * Logs any errors in connecting, but returns normally regardless of whether the connection succeeded.
     *
     * @param info     An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.  
     */
    protected void connectToServer(OmeroServerInfo info) {

        	this.connection = new OmeroServerConnection(info);
            this.connection.connect();
      
    }

    /**
     * Closes the connection to the OMERO server.
     */
    public void closeConnection() {
    	this.connection.disconnect();
    }

    /**
     * Reads an Image with the specified Id on the OMERO server using the provided address, username, and password.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param info					An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.  
     * @return                      An Image containing the data from the Image identified by ID.  It is not guaranteed how much metadata, if any, will be retrieved.  Null if there is a problem retrieving the Image.
     * @throws IOException          If there is a problem with the temporary disk storage for the retrieved Image.
     */
    public Image readImageFromOmeroServer(long omeroserverImageId, OmeroServerInfo info) throws IOException {
        String[] temporaryImageFilename = this.loadImageFromOmeroServer(omeroserverImageId, info);
        if (temporaryImageFilename == null || temporaryImageFilename[1] == null) return null;
        return this.read(temporaryImageFilename[1]);
    }


    /**
     * Reads the name of an Image on the OMERO server given its Id.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param info					An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.  
     * @return                      A String containing the name of the Image.
     * @throws IOException         if the Image cannnot be accessed on the server.
     */
    public String getImageNameForOmeroId(long omeroserverImageId, OmeroServerInfo info) throws IOException {
		
		try {
        	if (this.connection == null || ! this.connection.isConnected()) {

	            this.connectToServer(info);
	        }

	        return this.connection.getGateway().getImage(omeroserverImageId).getName().getValue();
		} catch (ServerError e) {
			throw new IOException(e);
		}

    }



    /**
     * Asynchronously loads the Image with the specified Id on the OMERO server using the provided address, username, and password, and stores it to a temporary file on disk.
     *
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param info					An OmeroServerInfo object containing the hostname, usenname, and password to use for the connection.  
     * @return                      An array containing two elements: first, the original name of the image on the OMERO server; second, the temporary filename to which the image is being stored.
     */
    public String[] loadImageFromOmeroServer(final long omeroserverImageId, final OmeroServerInfo info) {

        File tempfile = null;

        String originalName = null;

        try {

            if (this.connection == null || !this.connection.isConnected()) {

                this.connectToServer(info);
            }

            originalName = this.getImageNameForOmeroId(omeroserverImageId, info);

            this.closeConnection();

            tempfile = File.createTempFile("omerodownload", ".ome.tif");
            tempfile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempfile);

            final BufferedOutputStream out = new BufferedOutputStream(fos);

            final String lockFilename = tempfile.getAbsolutePath();

            final Thread lockingThread = (new Thread(new Runnable() {

                public void run() {

                    try {

                        if (connection == null || ! connection.isConnected()) {
                            try {
                                while (OmeroServerImageReader.getNumberOfConnections() >= MAX_CONNECTIONS) {
                                    Thread.sleep(SLEEP_TIME_MS);
                                }

                            } catch (InterruptedException e) {
                                LoggingUtilities.getLogger().warning("interrupted while waiting on omero server connection");
                                return;
                            }

                            OmeroServerImageReader.incrementConnections();

                            connectToServer(info);
                        }


                        ExporterPrx exporter = connection.getServiceFactory().createExporter();


                        exporter.addImage(omeroserverImageId);


                        long lengthOfFile = exporter.generateTiff();

                        long currPos = 0;
                        int chunkSize = 524288;

                        while (currPos < lengthOfFile) {

                            byte[] data = exporter.read(currPos, chunkSize);

                            currPos += data.length;

                            out.write(data);

                        }

                        exporter.close();

                        release(lockFilename, Thread.currentThread());


                    } catch (ServerError serverError) {
                        LoggingUtilities.getLogger().severe("Unable to retrieve image from omero server.  " + serverError.getMessage());
                        serverError.printStackTrace();
                    } catch (IOException e) {
                        LoggingUtilities.getLogger().severe("Exception while writing temporary image file to disk.");
                    } finally {
                        closeConnection();
                        OmeroServerImageReader.decrementConnections();
                        connection = null;
                    }
                }

            }));

            try {
                lockWithThread(lockFilename, lockingThread);
            } catch (InterruptedException e) {
                LoggingUtilities.getLogger().severe("interrupted while locking image file");
                return null;
            }


            lockingThread.start();





        } catch (IOException e) {
            LoggingUtilities.getLogger().severe("Exception while processing omero server images");
        }

        String[] toReturn = new String[2];

        toReturn[0] = originalName;
        toReturn[1] = tempfile != null ? tempfile.getAbsolutePath() : null;

        return toReturn;



    }


}
