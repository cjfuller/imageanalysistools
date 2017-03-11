package edu.stanford.cfuller.imageanalysistools.frontend

/**
 * @author cfuller
 */
interface StatusUpdater {
    fun update(currentProgress: Int, maxProgress: Int, message: String?)
}
