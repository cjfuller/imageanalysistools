package edu.stanford.cfuller.imageanalysistools.frontend

import java.util.prefs.Preferences
import javax.swing.JFileChooser

import ij.plugin.PlugIn

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * An implementation of the ImageJ PlugIn interface that allows the user to select
 * a parameters file and run analysis on the currently displayed ImagePlus.
 * @author Colin J. Fuller
 */
class ImageAnalysisToolsPlugin_ : PlugIn {
    /* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
    override fun run(arg: String) {
        if (ij.WindowManager.getImageCount() == 0) {
            ij.IJ.noImage()
            return
        }
        val imPl = ij.IJ.getImage()
        val ija = IJAnalysis()
        ija.setImagePlus(imPl)

        //get the filename for the parameters
        val dir = Preferences.userNodeForPackage(this.javaClass).get("parameters_dir", "")
        var parameterChooser: JFileChooser? = null

        if (dir !== "") {
            parameterChooser = JFileChooser(dir)
        } else {
            parameterChooser = JFileChooser()
        }
        parameterChooser.isMultiSelectionEnabled = false
        parameterChooser.fileSelectionMode = JFileChooser.FILES_ONLY

        val result = parameterChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val f = parameterChooser.selectedFile
            Preferences.userNodeForPackage(this.javaClass).put("parameters_dir", f.parent)
            val p = ParameterDictionary.readParametersFromFile(f.absolutePath)
            val output = ija.run(p)
            output?.show()
        }
    }
}
