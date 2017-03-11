package edu.stanford.cfuller.imageanalysistools.image.io.omero

import omero.ServerError
import omero.client
import omero.api.GatewayPrx
import omero.api.ServiceFactoryPrx
import Glacier2.CannotCreateSessionException
import Glacier2.PermissionDeniedException

/**
 * @author cfuller
 */
class OmeroServerConnection(infoIn: OmeroServerInfo) {
    var serviceFactory: ServiceFactoryPrx? = null
        internal set
    internal var readerClient: client? = null
    var gateway: GatewayPrx? = null
        internal set

    var isConnected: Boolean = false
        internal set

    private val info: OmeroServerInfo = infoIn

    fun connect() {
        try {
            this.readerClient = client(this.info.hostname)
            this.serviceFactory = this.readerClient!!.createSession(this.info.username, String(this.info.password))
            this.gateway = this.serviceFactory!!.createGateway()
            this.isConnected = true
        } catch (e: ServerError) {
            java.util.logging.Logger.getLogger(OmeroServerConnection::class.java.toString()).severe("Exception while connecting to omero server")
        } catch (e: CannotCreateSessionException) {
            java.util.logging.Logger.getLogger(OmeroServerConnection::class.java.toString()).severe("Exception while connecting to omero server")
        } catch (e: PermissionDeniedException) {
            java.util.logging.Logger.getLogger(OmeroServerConnection::class.java.toString()).severe("Invalid username or password.")
        }
    }

    fun disconnect() {
        if (this.readerClient != null) {
            this.readerClient!!.closeSession()
            this.readerClient = null
        }
        this.serviceFactory = null
        this.gateway = null
        this.isConnected = false
    }
}
