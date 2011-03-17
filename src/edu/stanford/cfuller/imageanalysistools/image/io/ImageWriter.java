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

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.PixelData;

/**
 * This class writes an Image to disk as one of several possible formats.
 */

public class ImageWriter {
	
	Image toWrite;

    private ImageWriter(){}

    /**
     * Constructs an ImageWriter for a given Image.
     * <p>
     * The writer retains a reference to the Image, so it can be used multiple times for writing the same Image to disk.  Only the Image's metadata will be used;
     * pixel data is passed in separately, allowing several Images that share the same metadata to be written with the same writer.
     * @param im    The Image whose metadata will be used to write Images to disk.
     */
	public ImageWriter(Image im) {
		toWrite = im;
	}

    /**
     * Writes the specified pixels to disk with the ImageWriter's Image's metadata.
     * <p>
     * The output format will be specified by the filename's extenstion and must be a valid writable extension for the LOCI bioformats library.
     * The recommended format is ome-tiff, with extension ".ome.tif".
     *
     * @param filename  The absolute path of the file to which the image will be written.  The parent directory should exist.
     * @param imagePixels   The PixelData containing the values to write to disk.
     * @throws java.io.IOException  if there is an exception while opening or writing the file.
     */
	public void write(String filename, PixelData imagePixels) throws java.io.IOException {
		loci.formats.IFormatWriter writer = new loci.formats.ImageWriter();
		writer.setMetadataRetrieve(toWrite.getMetadata());
		//the next line is a hack... when writing the same image multiple times, there seems to be some sort of issue with 
		//the pixels data being deleted, but not having a dummy object put in to read the endianness off of.
		toWrite.getMetadata().setPixelsBinDataBigEndian(imagePixels.getByteOrder() == java.nio.ByteOrder.BIG_ENDIAN, 0, 0);
		
		
		try {
			writer.setId(filename);
						
			for (int i = 0; i < imagePixels.getNumPlanes(); i++) {
				
				writer.savePlane(i, imagePixels.getPlane(i));
				
			}
			
		} catch (loci.formats.FormatException e) {
			LoggingUtilities.getLogger().severe("Encountered exception " + e.toString() + " while writing " + filename + " skipping write and proceeding.");
			e.printStackTrace();
			return;
		} finally {
		
		    writer.close();

        }
		
	}
	
}
