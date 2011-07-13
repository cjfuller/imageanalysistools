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

import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
import edu.stanford.cfuller.imageanalysistools.image.io.OmeroServerImageReader;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import omero.ServerError;


/**
 * Utilities for getting files from a directory or OMERO data source and matching them together into sets of the different channels of the same image.
 * 
 * @author Colin J. Fuller
 */
public class DirUtils {

    private DirUtils() {}

    
    /**
     * Gets filenames of the images to be processed (either from a directory or OMERO data source), along with a display name (i.e. name without a full path) for each.
     * @param params    The ParameterDictionary containing the parameters to be used for the analysis; the data directory or OMERO data source will be pulled from here.
     * @return          A List containing ImageSets, one per file.
     */
    public static java.util.List<ImageSet> makeMultiwavelengthFileSets(ParameterDictionary params) {


        java.util.Vector<ImageSet> outputSets = new java.util.Vector<ImageSet>();

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");


            for (String id : imageIds) {

                ImageSet currSet = new ImageSet(params);

                currSet.addImageWithOmeroId(Long.parseLong(id));

                outputSets.add(currSet);
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

                ImageSet set = new ImageSet(params);

                set.addImageWithFilename(f.getAbsolutePath());

            
                outputSets.add(set);

            }
        }


        return outputSets;

    }


    
    /**
     * Gets a list of String arrays each containing a set of filenames that correspond to image files for the color channels of a split channel image.
     *
     * @param params    The ParameterDictionary containing the parameters for the analysis.  The directory or OMERO source and channel names will be taken from here.
     * @return          A List containing ImageSets.  Each set can load the Images for the channels of an image.
     */
    public static java.util.List<ImageSet> makeSetsOfMatchingFiles(ParameterDictionary params) {

        String[] setTags = params.getValueForKey("channel_name").split(" ");

        int numPerSet = 0;
        if (params.hasKey("number_of_channels")) {
            numPerSet = Integer.parseInt(params.getValueForKey("number_of_channels"));
        } else {
            numPerSet = setTags.length;
            params.addIfNotSet("number_of_channels", Integer.toString(numPerSet));
        }

        java.util.Vector<ImageSet>  outputSets = new java.util.Vector<ImageSet>();




        java.io.File directory = new java.io.File(params.getValueForKey("local_directory"));

        java.util.HashMap<String, Long> idLookupByName = new java.util.HashMap<String, Long>();

        if (params.hasKey("use_omero") && params.getBooleanValueForKey("use_omero")) {

            String[] imageIds = params.getValueForKey("omero_image_ids").split(" ");

            OmeroServerImageReader ir = new OmeroServerImageReader();

            try {

                for (String id : imageIds) {

                    Long idL = Long.parseLong(id);

                    String name = ir.getImageNameForOmeroId(idL, params.getValueForKey("omero_hostname"), params.getValueForKey("omero_username"), params.getValueForKey("omero_password"));
 
                    idLookupByName.put(name, idL);

                }

            } catch (ServerError e) {

                LoggingUtilities.getLogger().severe("Exception encountered while accessing image on OMERO server: ");
                e.printStackTrace();

            } finally {
                ir.closeConnection();
            }


        } else {


            for (java.io.File f : directory.listFiles()) {

                //added hack here for case insensitive extension

                if ((f.getName().matches(".*Thumb.*")) || (! f.getName().toLowerCase().matches(".*" + params.getValueForKey("image_extension").toLowerCase() + "$"))) {
                    continue;
                }

                idLookupByName.put(f.getAbsolutePath(), null);
            }

        }

        for (String name : idLookupByName.keySet()) {

            if (! name.matches(".*" + setTags[0] + ".*")){
                continue;
            }

            ImageSet tempSet = new ImageSet(params);

            for (int i = 0; i < numPerSet; i++) {

                //tempSet[i] = directory.getAbsolutePath() + java.io.File.separator + f.getName().replaceAll(setTags[0], setTags[i]);

                String subName = name.replaceAll(setTags[0], setTags[i]);

                if (! idLookupByName.containsKey(subName)) {
                    tempSet = null;
                    break;
                }

                Long id = idLookupByName.get(subName);


                if (id != null) {
                    tempSet.addImageWithOmeroIdAndName(id, subName);
                } else {
                	tempSet.addImageWithFilename(subName);
                }
            }

            outputSets.add(tempSet);
        }

        return outputSets;

    }

}
