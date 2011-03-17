package edu.stanford.cfuller.imageanalysistools.image.io;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import com.sun.deploy.ClientContainer;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import omero.ServerError;
import omero.api.ExporterPrx;
import omero.api.GatewayPrx;
import omero.api.ServiceFactoryPrx;
import omero.client;

import java.io.*;
import java.nio.channels.FileLock;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: 3/4/11
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
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

    protected static void incrementConnections() {
        synchronized (OmeroServerImageReader.class) {

            OmeroServerImageReader.currentConnections += 1;


        }
    }

    protected static void decrementConnections() {
        
        synchronized (OmeroServerImageReader.class) {

            OmeroServerImageReader.currentConnections -= 1;


        }
    }

    protected static int getNumberOfConnections() {
        
        synchronized (OmeroServerImageReader.class) {

            return OmeroServerImageReader.currentConnections;

        }
    }

    public OmeroServerImageReader() {
        this.serviceFactory = null;
        this.readerClient = null;
        this.gateway = null;
    }

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

    protected void closeConnection() {
        this.readerClient.closeSession();
    }

    public Image readImageFromOmeroServer(long omeroserverImageId, String serverAddress, String username, String password) throws IOException {
        String[] temporaryImageFilename = this.loadImageFromOmeroServer(omeroserverImageId, serverAddress, username, password);
        if (temporaryImageFilename == null || temporaryImageFilename[1] == null) return null;
        return this.read(temporaryImageFilename[1]);
    }

    public String[] loadImageFromOmeroServer(final long omeroserverImageId, final String serverAddress, final String username, final String password) {

        File tempfile = null;

        String originalName = null;

        try {

            if (this.serviceFactory == null) {

                this.connectToServer(serverAddress, username, password);
            }

            originalName = gateway.getImage(omeroserverImageId).getName().getValue();

            closeConnection();
            this.serviceFactory = null;

            tempfile = File.createTempFile("omerodownload", ".ome.tif");
            tempfile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempfile);

            final BufferedOutputStream out = new BufferedOutputStream(fos);

            final String lockFilename = tempfile.getAbsolutePath();

            Thread t = null;

            try {
                t = lock(lockFilename);
            } catch (InterruptedException e) {
                LoggingUtilities.getLogger().severe("interrupted while locking image file");
            }


            final Thread lockObject = t;

            (new Thread(new Runnable() {

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

                        release(lockFilename, lockObject);
                        

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

            })).start();



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
