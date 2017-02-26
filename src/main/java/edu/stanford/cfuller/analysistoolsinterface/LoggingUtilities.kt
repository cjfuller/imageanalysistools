package edu.stanford.cfuller.analysistoolsinterface

import java.util.logging.Handler
import java.util.logging.Logger

/**

 * @author cfuller
 */
object LoggingUtilities {

    internal val LOGGER_NAME = "edu.stanford.cfuller.analysistoolsinterface"

    internal var loggerRef: Logger

    init {
        loggerRef = java.util.logging.Logger.getLogger(LOGGER_NAME)
    }

    fun log(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message)

    }

    fun info(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message)

    }

    fun warning(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).warning(message)

    }

    fun severe(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).severe(message)

    }

    fun addHandler(h: Handler) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).addHandler(h)
    }

}
