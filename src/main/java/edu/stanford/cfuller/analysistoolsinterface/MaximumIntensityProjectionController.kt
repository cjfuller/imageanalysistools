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

package edu.stanford.cfuller.analysistoolsinterface

import edu.stanford.cfuller.imageanalysistools.frontend.MaximumIntensityProjection
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser


/**

 * @author cfuller
 */
class MaximumIntensityProjectionController : TaskController() {

    internal var mipw: MaximumIntensityProjectionWindow

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
