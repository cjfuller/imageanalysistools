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

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import omero.ServerError;
import omero.api.ExporterPrx;
import omero.api.GatewayPrx;
import omero.api.ServiceFactoryPrx;
import omero.client;

import java.io.*;

/**
 * An ImageReader that reads images from an OMERO server instead of files on disk.
 */

//TODO: add functionality to have it remember login information, and redefine the read method to use this information, so this can be passed as an ImageReader and still function.

public class OmeroServerImageReader extends ImageReader {

    ServiceFactoryPrx serviceFactory;
    client readerClient;
    GatewayPrx gateway;

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
        this.serviceFactory = null;
        this.readerClient = null;
        this.gateway = null;
    }


    /**
     * Attempts a connection to the server at the specified address using the provided username and password.
     *
     * Logs any errors in connecting, but returns normally regardless of whether the connection succeeded.
     *
     * @param address       The IP address or resolvable hostname of the OMERO server.
     * @param username      The username to use to connect.
     * @param password      The password for the provided username.
     */
    protected void connectToServer(String address, String username, String password) {

        try {
            this.readerClient = new client(address);
            this.serviceFactory = this.readerClient.createSession(username, password);
            this.gateway = this.serviceFactory.createGateway();
        } catch(ServerError e) {
            LoggingUtilities.getLogger().severe("Exception while connecting to omero server");
        } catch (CannotCreateSessionException e) {
            LoggingUtilities.getLogger().severe("Exception while connecting to omero server");
        } catch (PermissionDeniedException e) {
            LoggingUtilities.getLogger().severe("Invalid username or password.");
        }


    }

    /**
     * Closes the connection to the OMERO server.
     */
    public void closeConnection() {
        if (this.readerClient != null) {
            this.readerClient.closeSession();
        }
        this.serviceFactory = null;
        this.gateway = null;
    }

    /**
     * Reads an Image with the specified Id on the OMERO server using the provided address, username, and password.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param serverAddress         The IP address or resolvable hostname of the OMERO server.
     * @param username              The username to use to connect.
     * @param password              The password for the provided username.
     * @return                      An Image containing the data from the Image identified by ID.  It is not guaranteed how much metadata, if any, will be retrieved.  Null if there is a problem retrieving the Image.
     * @throws IOException          If there is a problem with the temporary disk storage for the retrieved Image.
     */
    public Image readImageFromOmeroServer(long omeroserverImageId, String serverAddress, String username, String password) throws IOException {
        String[] temporaryImageFilename = this.loadImageFromOmeroServer(omeroserverImageId, serverAddress, username, password);
        if (temporaryImageFilename == null || temporaryImageFilename[1] == null) return null;
        return this.read(temporaryImageFilename[1]);
    }


    /**
     * Reads the name of an Image on the OMERO server given its Id.
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param serverAddress         The IP address or resolvable hostname of the OMERO server.
     * @param username              The username to use to connect.
     * @param password              The password for the provided username.
     * @return                      A String containing the name of the Image.
     * @throws ServerError          if the Image cannnot be accessed on the server.
     */
    public String getImageNameForOmeroId(long omeroserverImageId, final String serverAddress, final String username, final String password) throws ServerError {

        if (this.serviceFactory == null) {

            this.connectToServer(serverAddress, username, password);
        }

        return gateway.getImage(omeroserverImageId).getName().getValue();

    }



    /**
     * Asynchronously loads the Image with the specified Id on the OMERO server using the provided address, username, and password, and stores it to a temporary file on disk.
     *
     * @param omeroserverImageId    The ID that the OMERO server as assigned to the desired image; this image will be retrieved.
     * @param serverAddress         The IP address or resolvable hostname of the OMERO server.
     * @param username              The username to use to connect.
     * @param password              The password for the provided username.
     * @return                      An array containing two elements: first, the original name of the image on the OMERO server; second, the temporary filename to which the image is being stored.
     */
    public String[] loadImageFromOmeroServer(final long omeroserverImageId, final String serverAddress, final String username, final String password) {

        File tempfile = null;

        String originalName = null;

        try {

            if (this.serviceFactory == null) {

                this.connectToServer(serverAddress, username, password);
            }

            originalName = this.getImageNameForOmeroId(omeroserverImageId, serverAddress, username, password);

            closeConnection();
            this.serviceFactory = null;

            tempfile = File.createTempFile("omerodownload", ".ome.tif");
            tempfile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempfile);

            final BufferedOutputStream out = new BufferedOutputStream(fos);

            final String lockFilename = tempfile.getAbsolutePath();

//            Thread t = null;
//
//            try {
//                t = lock(lockFilename);
//            } catch (InterruptedException e) {
//                LoggingUtilities.getLogger().severe("interrupted while locking image file");
//            }

            final Thread lockingThread = (new Thread(new Runnable() {

                public void run() {

                    try {

                        if (serviceFactory == null) {
                            try {
                                while (OmeroServerImageReader.getNumberOfConnections() >= MAX_CONNECTIONS) {
                                    Thread.sleep(SLEEP_TIME_MS);
                                }

                            } catch (InterruptedException e) {
                                LoggingUtilities.getLogger().warning("interrupted while waiting on omero server connection");
                                return;
                            }

                            OmeroServerImageReader.incrementConnections();

                            connectToServer(serverAddress, username, password);
                        }


                        ExporterPrx exporter = serviceFactory.createExporter();


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
                        serviceFactory = null;
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





        } catch (ServerError serverError) {
            LoggingUtilities.getLogger().severe("Unable to retrieve image from omero server.");
        } catch (IOException e) {
            LoggingUtilities.getLogger().severe("Exception while writing temporary image file to disk.");
        }

        String[] toReturn = new String[2];

        toReturn[0] = originalName;
        toReturn[1] = tempfile != null ? tempfile.getAbsolutePath() : null;

        return toReturn;



    }


}
