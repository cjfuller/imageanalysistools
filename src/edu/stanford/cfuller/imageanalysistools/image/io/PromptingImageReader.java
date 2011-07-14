/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.imageanalysistools.image.io;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import edu.stanford.cfuller.imageanalysistools.image.Image;

/**
 * A type of ImageReader that has the conventional functionality, but also adds
 * the ability to prompt a user with a file chooser dialog to get a file to read.
 * 
 * @author cfuller
 *
 */
public class PromptingImageReader extends ImageReader {

	JFileChooser chooser;
	
	static final String PROMPT_DEFAULT_PATH_STRING = "prompting_image_reader_path";

	/**
	 * Constructs a new default PromptingImageReader.
	 */
	public PromptingImageReader() {

		String path = java.util.prefs.Preferences.userNodeForPackage(this.getClass()).get(PromptingImageReader.PROMPT_DEFAULT_PATH_STRING, "");

		this.chooser = new JFileChooser(path);


	}

	/**
	 * Reads an Image from a location on disk.  A file chooser dialog will appear and 
	 * ask the user to select the Image to read.
	 * 
	 * @return the Image read from disk, or null if the user failed to select an Image.
	 * @throws java.io.IOException	if there is a problem reading the Image.
	 */
	public Image promptingRead() throws java.io.IOException {

		int returnState = chooser.showOpenDialog(null);

		if (returnState == JFileChooser.APPROVE_OPTION) {
			String selected = chooser.getSelectedFile().getAbsolutePath();
			if (selected != null) {
				java.util.prefs.Preferences.userNodeForPackage(this.getClass()).put(PromptingImageReader.PROMPT_DEFAULT_PATH_STRING, selected);
				return this.read(selected);
			}
		}
		
		return null;

	}

}
