package edu.stanford.cfuller.imageanalysistools.frontend

/**
 * @author cfuller
 */
class ImageJStatusUpdater : StatusUpdater {
    /* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.frontend.StatusUpdater#update(int, int, java.lang.String)
	 */
    override fun update(currentProgress: Int, maxProgress: Int, message: String?) {
        if (message != null) {
            ij.IJ.showStatus(message)
        }
        ij.IJ.showProgress(currentProgress, maxProgress)
    }
}
