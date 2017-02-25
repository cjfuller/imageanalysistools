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

package edu.stanford.cfuller.imageanalysistools.image.io

import javax.swing.JFileChooser

import edu.stanford.cfuller.imageanalysistools.image.Image

/**
 * A type of ImageReader that has the conventional functionality, but also adds
 * the ability to prompt a user with a file chooser dialog to get a file to read.

 * @author Colin J. Fuller
 */
class PromptingImageReader : ImageReader() {

    internal var chooser: JFileChooser

    /**
     * Constructs a new default PromptingImageReader.
     */
    init {

        val path = java.util.prefs.Preferences.userNodeForPackage(this.javaClass).get(PromptingImageReader.PROMPT_DEFAULT_PATH_STRING, "")

        this.chooser = JFileChooser(path)


    }

    /**
     * Reads an Image from a location on disk.  A file chooser dialog will appear and
     * ask the user to select the Image to read.

     * @return the Image read from disk, or null if the user failed to select an Image.
     * *
     * @throws java.io.IOException    if there is a problem reading the Image.
     */
    @Throws(java.io.IOException::class)
    fun promptingRead(): Image? {

        val returnState = chooser.showOpenDialog(null)

        if (returnState == JFileChooser.APPROVE_OPTION) {
            val selected = chooser.selectedFile.absolutePath
            if (selected != null) {
                java.util.prefs.Preferences.userNodeForPackage(this.javaClass).put(PromptingImageReader.PROMPT_DEFAULT_PATH_STRING, selected)
                return this.read(selected)
            }
        }

        return null

    }

    companion object {

        internal val PROMPT_DEFAULT_PATH_STRING = "prompting_image_reader_path"
    }

}
