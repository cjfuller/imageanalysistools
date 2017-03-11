package edu.stanford.cfuller.imageanalysistools.image.io.omero

/**
 * @author cfuller
 */
class OmeroServerInfo
/**
 * Constructs a new OmeroServerInfo object given the information needed
 * to connect to the server.
 * @param hostname                The IP address or resolvable hostname of the OMERO server.
 * @param username              The username to use to connect.
 * @param password              The password for the provided username.
 */
(val hostname: String, val username: String, val password: CharArray) {
    @Throws(Throwable::class)
    protected fun Finalize() {
        for (i in this.password.indices) {
            password[i] = '\u0000'
        }
    }
}
