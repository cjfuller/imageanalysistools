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
