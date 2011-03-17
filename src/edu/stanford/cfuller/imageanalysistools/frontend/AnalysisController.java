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

package edu.stanford.cfuller.imageanalysistools.frontend;

import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

import java.util.logging.Handler;

/**
 * Master controller of analysis routines.  Sets up critical default parameters if they have not been set up and runs
 * the analysis method.
 */

public class AnalysisController {


    public static final String DATA_OUTPUT_DIR="output";
    public static final String IMAGE_OUTPUT_DIR="output_mask";
    public static final String PARAMETER_OUTPUT_DIR="parameters";
    static final String PARAMETER_EXTENSION=".xml";

    /**
     * Run analysis on the local machine.
     * @param params    The {@link ParameterDictionary} specifying the options for this analysis run.
     */
	public void runLocal(ParameterDictionary params) {
		String[] allowableArgs = {"compile", "localDirectory", "generateMasks", "generateValues", "numberOfChannels", "channelName", "methodName", "tempDir", "imageExtension", "minSize", "maxSize", "useClustering", "maxClusters"};
		
		//params.discardIllegalArguments(allowableArgs);
/*
		params.addIfNotSet("compile", "false");
		params.addIfNotSet("localDirectory", System.getProperty("user.dir"));
		params.addIfNotSet("generateMasks", "true");
		params.addIfNotSet("generateValues", "true");
		params.addIfNotSet("numberOfChannels", "1");
		params.addIfNotSet("channelName", "dapi");
		params.addIfNotSet("methodName", "RecursiveThreshMethod");
*/
		params.addIfNotSet("temp_dir", System.getProperty("user.dir") + java.io.File.separator + "temp");
		params.addIfNotSet("image_extension", "");
		params.addIfNotSet("DEBUG", "false");
/*
		int numChannels = params.getValueForKey("channelName").split(" ").length;
		
		if (Integer.parseInt(params.getValueForKey("numberOfChannels")) != numChannels) {
					
			throw new IllegalArgumentException("Error:  number of channel names listed, " + numChannels + ", does not match the specified number of channels, " + Integer.parseInt(params.getValueForKey("numberOfChannels")));
		
		}
*/
		LocalAnalysis.run(params);	
		
	}


    public void addAnalysisLoggingHandler(Handler h) {
        LoggingUtilities.addHandler(h);
    }
}
