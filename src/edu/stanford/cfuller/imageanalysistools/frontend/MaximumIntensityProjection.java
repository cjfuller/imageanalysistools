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

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;

import java.io.File;

/**
 * Utilities for creating maximum intensity projections of images.
 * 
 * @author Colin J. Fuller
 */
public class MaximumIntensityProjection {

    final static String directorySuffix = "maxintproj";
    final static String outputSuffix = "_proj.ome.tif";
    
    //TODO: handle projecting Z dimension (or an arbitrary, specified dimension?) in images other than 5D.


    /**
     * Makes a maximum intensity projection of an Image and returns it as an Image.
     * @param im    The Image to be projected.
     * @return      The projection, as an Image.
     */
    public static Image projectImage(Image im) {

        ImageCoordinate projectionSizes = ImageCoordinate.createCoordXYZCT(im.getDimensionSizes().get("x"), im.getDimensionSizes().get("y"), 1, im.getDimensionSizes().get("c"), im.getDimensionSizes().get("t"));
        
        Image imProj = new Image(projectionSizes, 0.0);

        projectionSizes.recycle();

        for (ImageCoordinate i : im) {

            ImageCoordinate ic = ImageCoordinate.cloneCoord(i);
            ic.set("z",0);

            double origValue = im.getValue(i);
            double projValue = imProj.getValue(ic);

            if (origValue > projValue) {imProj.setValue(ic, origValue);}

            ic.recycle();
        }

        return imProj;
    }

    /**
     * Makes a maximum intensity projection of an image designated by a given filename; writes the projection back out to disk in a subdirectory.
     * @param filename                  The filename of the image to be projected.
     * @throws java.io.IOException      if there is a problem reading the image from disk or writing the projection to disk.
     */
    public static void project(String filename) throws java.io.IOException {

        ImageReader imR = new ImageReader();

        Image im = imR.read(filename);

        //do the projection

        Image imProj = projectImage(im);

        //write the output

        File f = new File(filename);
        String directory = f.getParent();

        File outputDirectory = new File(directory + File.separator + directorySuffix);

        if (!outputDirectory.exists()) {outputDirectory.mkdir();}

        String output = outputDirectory.getAbsolutePath() + File.separator + f.getName();

        if (!imR.hasMoreSeries(filename)) {
            imProj.writeToFile(output + outputSuffix);
        } else {
            int imCounter = 0;
            imProj.writeToFile(output + "_" + imCounter++ + outputSuffix);
            do {
                im = imR.read(filename);
                imProj = projectImage(im);
                imProj.writeToFile(output + "_" + imCounter++ + outputSuffix);
            } while (imR.hasMoreSeries(filename));

        }
        
    }

    /**
     * Makes maximum intensity projections of all the images in a single directory.
     * @param directory                 The directory containing the images to project.
     * @throws java.io.IOException      if there is a problem reading the image from disk or writing the projection to disk.
     */
    public static void projectDirectory(String directory) throws java.io.IOException {

        File dirFile = new File(directory);

        
        System.out.println("attempting to project: " + dirFile.getAbsolutePath());
        
        for (File eachFile : dirFile.listFiles()) {

        	System.out.println("current file: " + eachFile.getAbsolutePath());
        	
            try {
                project(eachFile.getAbsolutePath());
            } catch (java.io.IOException e) {
                LoggingUtilities.getLogger().warning("Exception encountered while projecting: " + eachFile.getAbsolutePath() + "\n" + e.getMessage());
            }

        }

        
    }


}
