package edu.stanford.cfuller.imageanalysistools.frontend

import java.io.OutputStream
import java.util.logging.*

/**
 * Wraps a logging system to provide simple methods for all classes in the imageanalysistools package to get a uniform logger, control its
 * level of logging detail, and add handlers.
 * @author Colin J. Fuller
 */
object LoggingUtilities {
    internal var addedHandlers: MutableList<Handler> = java.util.Vector<Handler>()
    val LOGGER_NAME = "edu.stanford.cfuller.imageanalysistools"

    /**
     * Modifies the logging detail level of the handlers associated with this package.
     * @param l     The new level of logging detail.
     */
    fun setHandlerLevels(l: Level) {
        for (h in addedHandlers) {
            h.level = l
        }
    }

    /**
     * Adds a handler that will log all messages to a given Output Stream.
     * @param o     The OutputStream to which to log.
     */
    fun logToStream(o: OutputStream) {
        val sh = StreamHandler(o, SimpleFormatter())
        addedHandlers.add(sh)
        logger.addHandler(sh)
    }

    /**
     * Adds a handler to the logger for this package.
     * @param h The handler to add.
     */
    fun addHandler(h: Handler) {
        if (!addedHandlers.contains(h)) {
            addedHandlers.add(h)
            logger.addHandler(h)
        }
    }

    /**
     * Causes all messages to be logged.
     */
    fun logDebugMessages() {
        logger.level = Level.ALL
        setHandlerLevels(Level.ALL)
    }

    /**
     * Causes only informational messages and errors/warnings to be logged.
     */
    fun hideDebugMessages() {
        logger.level = Level.INFO
        setHandlerLevels(Level.INFO)
    }

    /**
     * Causes informational and debugging messages to be hidden.
     */
    fun logWarningsAndErrorsOnly() {
        logger.level = Level.WARNING
        setHandlerLevels(Level.WARNING)
    }

    /**
     * Remove all log handlers that have been added.
     */
    fun removeAllAddedLoggingStreams() {
        for (h in addedHandlers) {
            logger.removeHandler(h)
        }
        addedHandlers.clear()
    }

    /**
     * Get a reference to the underlying logger used by this class.
     * @return  A reference to the logger.
     */
    val logger: Logger
        get() = java.util.logging.Logger.getLogger(LOGGER_NAME)
}
