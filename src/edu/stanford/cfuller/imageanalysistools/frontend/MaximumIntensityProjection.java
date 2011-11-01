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

        ImageCoordinate projectionSizes = ImageCoordinate.createCoordXYZCT(im.getDimensionSizes().get(ImageCoordinate.X), im.getDimensionSizes().get(ImageCoordinate.Y), 1, im.getDimensionSizes().get(ImageCoordinate.C), im.getDimensionSizes().get(ImageCoordinate.T));
        
        Image imProj = new Image(projectionSizes, 0.0f);

        projectionSizes.recycle();

        for (ImageCoordinate i : im) {

            ImageCoordinate ic = ImageCoordinate.cloneCoord(i);
            ic.set(ImageCoordinate.Z,0);

            float origValue = im.getValue(i);
            float projValue = imProj.getValue(ic);

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
