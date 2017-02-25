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

package edu.stanford.cfuller.imageanalysistools.frontend

import java.io.File
import java.util.prefs.Preferences

import javax.swing.JFileChooser

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

import ij.ImagePlus
import ij.plugin.PlugIn

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

            if (p != null) {


                val output = ija.run(p)

                output?.show()

            }

        }

    }


}
