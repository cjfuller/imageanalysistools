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

package edu.stanford.cfuller.imageanalysistools.image;

/**
 * A factory to construct PixelData objects; the choice of implementation will be made based on the
 * properties of the data.
 * 
 * @author Colin J. Fuller
 *
 */
public class PixelDataFactory {

	//TODO: reimplement to handle images other than 5D.
	
    static final int DEFAULT_MAX_BYTE_SIZE = 20000000;
    //static final int DEFAULT_MAX_BYTE_SIZE = 0;

    private int maxByteSize;

    /**
     * Constructs a new default PixelDataFactory.
     */
    public PixelDataFactory(){
        maxByteSize = DEFAULT_MAX_BYTE_SIZE;
    }

    /**
     * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
     * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     * @return              A new PixelData with the specified options.
     */

    public PixelData createPixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {

        return createPixelData(sizes.get("x"), sizes.get("y"), sizes.get("z"), sizes.get("c"), sizes.get("t"), data_type, dimensionOrder);

    }

    /**
     * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
     * @param size_x    Size of the pixel data in the X-dimension.
     * @param size_y    Size of the pixel data in the Y-dimension.
     * @param size_z    Size of the pixel data in the Z-dimension.
     * @param size_c    Size of the pixel data in the C-dimension.
     * @param size_t    Size of the pixel data in the T-dimension.
     * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
     * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
     * @return          A new PixelData with the specified options.
     */
    public PixelData createPixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {

        int sizeInBytes = size_x*size_y*size_z*size_c*size_t*loci.formats.FormatTools.getBytesPerPixel(data_type);

        if (sizeInBytes > maxByteSize) {
            return new LargePixelData(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder);
        } else {
            return new PixelData(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder);
        }
        
        
    }

}
