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

package edu.stanford.cfuller.imageanalysistools.frontend;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

import ij.ImagePlus;
import ij.plugin.PlugIn;

/**
 * An implementation of the ImageJ PlugIn interface that allows the user to select
 * a parameters file and run analysis on the currently displayed ImagePlus.
 * 
 * 
 * @author Colin J. Fuller
 *
 */
public class ImageAnalysisToolsPlugin_ implements PlugIn {
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(String arg) {
		
		if (ij.WindowManager.getImageCount() == 0) {ij.IJ.noImage(); return;}

		
		ImagePlus imPl = ij.IJ.getImage();
		
		
		IJAnalysis ija = new IJAnalysis();
		
		ija.setImagePlus(imPl);
		
		//get the filename for the parameters
		
		String dir = Preferences.userNodeForPackage(this.getClass()).get("parameters_dir", "");
		
		JFileChooser parameterChooser = null;
		
		
		if (dir != "") {
			parameterChooser = new JFileChooser(dir);
		} else {
			parameterChooser = new JFileChooser();
		}
		
		parameterChooser.setMultiSelectionEnabled(false);
		parameterChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int result = parameterChooser.showOpenDialog(null);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			
			File f = parameterChooser.getSelectedFile();
			
			Preferences.userNodeForPackage(this.getClass()).put("parameters_dir", f.getParent());

			ParameterDictionary p = ParameterDictionary.readParametersFromFile(f.getAbsolutePath());
			
			if (p!= null) {
				
				
				ImagePlus output = ija.run(p);
				
				if (output!=null) {
				
					output.show();
					
				}
				
			}
			
		}
		
	}


}
