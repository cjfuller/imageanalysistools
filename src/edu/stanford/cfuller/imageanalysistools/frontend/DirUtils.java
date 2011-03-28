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

import edu.stanford.cfuller.imageanalysistools.image.io.OmeroServerImageReader;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;


/**
 * Utilities for getting files from a directory or OMERO data source and matching them together into sets of the different channels of the same image.
 */
public class DirUtils {

    private DirUtils() {}

    
    /**
     * Gets filenames of the images to be processed (either from a directory or OMERO data source), along with a display name (i.e. name without a full path) for each.
     * @param params    The ParameterDictionary containing the parameters to be used for the analysis; the data directory or OMERO data source will be pulled from here.
     * @return          A List containing String arrays, one per file, each with two entries: first, a fully qualified filename, and second a display name (or name without path).
     */
    public static java.util.List<String[]> makeMultiwavelengthFileSets(ParameterDictionary params) {


        java.util.Vector<String[]> outputSets = new java.util.Vector<String[]>();

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");


            for (String id : imageIds) {
                
                OmeroServerImageReader ir = new OmeroServerImageReader();

                String[] names = ir.loadImageFromOmeroServer(Long.parseLong(id), params.getValueForKey("omero_hostname"), params.getValueForKey("omero_username"), params.getValueForKey("omero_password"));
                String[] set = new String[2];
                set[0] = names[1];
                set[1] = names[0];
                outputSets.add(set);
            }


        } else {

            java.io.File directory = new java.io.File(params.getValueForKey("local_directory"));

            String commonFilenameTag = "";
            String imageExtension = "";

            if (params.hasKey("image_extension")) {
                imageExtension = params.getValueForKey("image_extension");
            }

            if (params.hasKey("common_filename_tag")) {
                commonFilenameTag = params.getValueForKey("common_filename_tag");
            }

            for (java.io.File f : directory.listFiles()) {



                if ((f.getName().matches(".*Thumb.*")) || (! f.getName().toLowerCase().matches(".*" + imageExtension.toLowerCase() + "$")) || (! f.getName().matches(".*" + commonFilenameTag + ".*"))) {
                    continue;
                }

                String[] set = new String[2];

                set[0] = f.getAbsolutePath();
                set[1] = f.getName();
                outputSets.add(set);

            }
        }


        return outputSets;

    }


    
    /**
     * Gets a list of String arrays each containing a set of filenames that correspond to image files for the color channels of a split channel image.
     *
     * @param params    The ParameterDictionary containing the parameters for the analysis.  The directory or OMERO source and channel names will be taken from here.
     * @return          A List containing String arrays of filenames.  Each array contains the filenames for the channels of an image.
     */
    public static java.util.List<String[]> makeSetsOfMatchingFiles(ParameterDictionary params) {

        String[] setTags = params.getValueForKey("channel_name").split(" ");

        int numPerSet = 0;
        if (params.hasKey("number_of_channels")) {
            numPerSet = Integer.parseInt(params.getValueForKey("number_of_channels"));
        } else {
            numPerSet = setTags.length;
            params.addIfNotSet("number_of_channels", Integer.toString(numPerSet));
        }

        java.util.Vector<String[]> outputSets = new java.util.Vector<String[]>();




        java.io.File directory = new java.io.File(params.getValueForKey("local_directory"));

        java.util.Hashtable<String, String> filenameLookupByName = new java.util.Hashtable<String, String>();

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");

            for (String id : imageIds) {

                OmeroServerImageReader ir = new OmeroServerImageReader();
                
                String[] names = ir.loadImageFromOmeroServer(Long.parseLong(id), params.getValueForKey("omero_hostname"), params.getValueForKey("omero_username"), params.getValueForKey("omero_password"));

                filenameLookupByName.put(names[0], names[1]);

            }


        } else {


            for (java.io.File f : directory.listFiles()) {

                //added hack here for case insensitive extension
                if ((! f.getName().matches(".*" + setTags[0] + ".*")) || (f.getName().matches(".*Thumb.*")) || (! f.getName().toLowerCase().matches(".*" + params.getValueForKey("image_extension").toLowerCase() + "$"))) {
                    continue;
                }


                filenameLookupByName.put(f.getName(), f.getAbsolutePath());
            }

        }

        for (String name : filenameLookupByName.keySet()) {

            String[] tempSet = new String[numPerSet*2];

            for (int i = 0; i < numPerSet; i++) {

                //tempSet[i] = directory.getAbsolutePath() + java.io.File.separator + f.getName().replaceAll(setTags[0], setTags[i]);
                tempSet[i] = filenameLookupByName.get(name.replaceAll(setTags[0], setTags[i]));
                tempSet[numPerSet+i] = name.replaceAll(setTags[0], setTags[i]);
            }

            outputSets.add(tempSet);

        }

        return outputSets;

    }

}
