package edu.stanford.cfuller.imageanalysistools.frontend

/**
 * @author cfuller
 */
class LoggingStatusUpdater : StatusUpdater {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.frontend.StatusUpdater#update(int, int, java.lang.String)
	 */
    override fun update(currentProgress: Int, maxProgress: Int, message: String?) {
        LoggingUtilities.logger.info("Progress: $currentProgress of $maxProgress; $message")
    }
}
