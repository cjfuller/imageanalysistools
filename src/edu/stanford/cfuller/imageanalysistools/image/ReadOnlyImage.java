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
 * An Image representation that can only be read, and not written.
 *
 * This class of Image primarily exists to allow sharing of PixelData and metadata among multiple copies of an Image that
 * only need to read the Image data, but still allow them to iterate over the coordinates in the image and box regions of interest
 * independently.
 *
 * @author Colin J. Fuller
 *
 */
public class ReadOnlyImage extends Image {

    
    public ReadOnlyImage(loci.formats.meta.IMetadata m, PixelData p) {
        super(m, p);
    }

    /**
     * Copy constructor.  This makes a shallow copy of the Image to copy for pixel data and metadata, but gives independent
     * iteration and boxing mechanisms.
     * @param toCopy        The Image to copy.
     */
    public ReadOnlyImage(Image toCopy) {
        super();
        this.isBoxed = false;
        this.boxMin = null;
        this.boxMax = null;
        this.coordinateArrayStorage = null;
        this.pixelData = toCopy.pixelData;
        this.dimensionSizes = ImageCoordinate.cloneCoord(toCopy.getDimensionSizes());
        this.metadata = toCopy.metadata;
    }

    /**
     * Overrides the copy function to throw an UnsupportedOperationException.
     *
     * @param other     The Image whose pixel values will (not) be copied.
     */
    @Override
    public void copy(Image other) {
        throw new UnsupportedOperationException("Copy is not supported for read-only Images.");
    }


    /**
     * Overrides the setValue function to throw an UnsupportedOperationException.
     * @deprecated  Use {@link #setValue(ImageCoordinate, double)} instead.
     * @param row   The row (y-coordinate) to set.
     * @param col   The column (x-coordinate) to set.
     * @param value The value that the Image will be set to at the specified location.
     */
    @Override
    public void setValue(int row, int col, double value) {
        throw new UnsupportedOperationException("Setting values is not supported for read-only Images.");
    }

    /**
     * Overrides the setValue function to throw an UnsupportedOperationException.
     * @param coord     The ImageCoordinate at which to set the Image's value.
     * @param value     The value to which to set the Image at the specified coordinate.
     */
    @Override
    public void setValue(ImageCoordinate coord, double value) {
        throw new UnsupportedOperationException("Setting values is not supported for read-only Images.");
    }


    
}
