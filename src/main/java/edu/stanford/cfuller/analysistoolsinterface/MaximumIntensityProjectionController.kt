package edu.stanford.cfuller.analysistoolsinterface

import edu.stanford.cfuller.imageanalysistools.frontend.MaximumIntensityProjection
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser


/**

 * @author cfuller
 */
class MaximumIntensityProjectionController : TaskController() {
    internal var mipw: MaximumIntensityProjectionWindow = MaximumIntensityProjectionWindow(this)

    fun goButtonPressed() {
        if (this.mipw.status == STATUS_PROCESSING) return

        Preferences.userNodeForPackage(this.javaClass).put("maximumIntensityProjectionDirectory", this.mipw.directoryText)
        Thread(Runnable {
            try {
                mipw.status = STATUS_PROCESSING

                if (File(mipw.directoryText).isDirectory) {
                    MaximumIntensityProjection.projectDirectory(mipw.directoryText)
                } else {
                    MaximumIntensityProjection.project(mipw.directoryText)
                }
            } catch (e: java.io.IOException) {
                LoggingUtilities.severe("Encountered IO exception while making maximum intensity projection of: " + mipw.directoryText)
            } finally {
                mipw.status = STATUS_READY
            }
        }).start()
    }

    fun browseButtonPressed() {
        var chooser: JFileChooser? = null

        if (this.mipw.directoryText.isEmpty()) {
            chooser = JFileChooser()
        } else {
            chooser = JFileChooser(this.mipw.directoryText)
        }

        chooser.isMultiSelectionEnabled = false
        chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES

        val retval = chooser.showOpenDialog(this.mipw)
        if (retval == JFileChooser.APPROVE_OPTION) {
            this.mipw.directoryText = chooser.selectedFile.absolutePath
        }
    }

    override fun startTask() {
        this.mipw = MaximumIntensityProjectionWindow(this)
        val savedDirectory = Preferences.userNodeForPackage(this.javaClass).get("maximumIntensityProjectionDirectory", this.mipw.directoryText)
        this.mipw.directoryText = savedDirectory
        this.mipw.status = STATUS_READY
        this.mipw.addWindowListener(this)
        this.mipw.isVisible = true
    }

    companion object {
        internal val STATUS_PROCESSING = "Processing"
        internal val STATUS_READY = "Ready"
    }
}
