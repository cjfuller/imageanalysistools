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

import omero.ServerError;
import omero.client;
import omero.api.GatewayPrx;
import omero.api.ServiceFactoryPrx;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * @author cfuller
 *
 */
public class OmeroServerConnection {

	ServiceFactoryPrx serviceFactory;
    client readerClient;
    GatewayPrx gateway;
    
    boolean isConnected;
	
	private OmeroServerInfo info;
	
	public OmeroServerConnection(OmeroServerInfo info) {
		this.info = info;
		this.isConnected = false;
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	protected OmeroServerConnection() {}
	
	public void connect() {

        try {
            this.readerClient = new client(this.info.getHostname());
            this.serviceFactory = this.readerClient.createSession(this.info.getUsername(), new String(this.info.getPassword()));
            this.gateway = this.serviceFactory.createGateway();
            this.isConnected = true;
        } catch(ServerError e) {
            java.util.logging.Logger.getLogger(OmeroServerConnection.class.toString()).severe("Exception while connecting to omero server");
        } catch (CannotCreateSessionException e) {
        	java.util.logging.Logger.getLogger(OmeroServerConnection.class.toString()).severe("Exception while connecting to omero server");
        } catch (PermissionDeniedException e) {
        	java.util.logging.Logger.getLogger(OmeroServerConnection.class.toString()).severe("Invalid username or password.");
        }
	}
	
	public void disconnect() {
		if (this.readerClient != null) {
            this.readerClient.closeSession();
            this.readerClient = null;
        }
        this.serviceFactory = null;
        this.gateway = null;
        this.isConnected = false;
	}
	
	public GatewayPrx getGateway() {
		return this.gateway;
	}
	
	public ServiceFactoryPrx getServiceFactory() {
		return this.serviceFactory;
	}
	
}
