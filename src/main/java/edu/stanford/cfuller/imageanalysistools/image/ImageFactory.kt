/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.image

import ij.ImagePlus

import java.awt.image.BufferedImage


/**
 * A Factory for creating new objects implementing the Image and WritableImage interfaces.
 *
 *
 * It's recommended to use this class's methods to create new Images rather than instantiating
 * a particular implementation in order to avoid problems as these implementations change.
 *
 *
 * For the Image creation methods, both a create and createWritable form exist, which will get
 * an object implementing Image and WritableImage, respectively.  The exception is [.createShallow],
 * which makes a read-only shallow copy of another Image so that things like iteration bounds can be set separately, but does
 * not require copying of the pixel data.

 * @author Colin J. Fuller
 */
class ImageFactory : java.io.Serializable {
    companion object {

        internal const val serialVersionUID = 1457193458130580976L

        /**
         * Gets a writable instance of the supplied Image.  If the underlying
         * implementation of that Image is already writable, it is returned (not a copy)
         * as a WritableImage.  If it is read-only, then a writable copy is made and returned.
         *
         *
         * Use this method with caution, as often other code supplying an object using the Image
         * interface expects it to remain unmodified.

         * @param orig    The Image, which is only known to be readable.
         * *
         * @return a WritableImage that contains the same data as orig.  May or may not be a copy.
         */
        fun writableImageInstance(orig: Image): WritableImage {
            if (orig.isWritable) {
                return orig as WritableImage
            }
            return ImageFactory.createWritable(orig)
        }

        /**
         * Constructs a new Image with the specified metadata and PixelData.
         *
         *
         * The metadata and PixelData should already be initialized (and should contain consistent values for things like dimension sizes).
         *
         *
         * This constructor is primarily used by classes that read images in binary format; most users will probably want to use a different constructor.
         * @param m     An object containing the metadata associated with the Image (as a loci.formats.meta.IMetadata, to ease integration with the LOCI bio-formats library).
         * *
         * @param p     A PixelData object containing the actual values at each pixel in the Image.
         * *
         * @return the constructed Image.
         */
        fun create(m: loci.formats.meta.IMetadata, p: PixelData): Image {
            return ReadOnlyImageImpl(m, p)
        }

        /**
         * Constructs a new WritableImage with the specified metadata and PixelData.
         *
         *
         * The metadata and PixelData should already be initialized (and should contain consistent values for things like dimension sizes).
         * <P>
         * This constructor is primarily used by classes that read images in binary format; most users will probably want to use a different constructor.
         * @param m     An object containing the metadata associated with the Image (as a loci.formats.meta.IMetadata, to ease integration with the LOCI bio-formats library).
         * *
         * @param p     A PixelData object containing the actual values at each pixel in the Image.
         * *
         * @return the constructed WritableImage.
        </P> */
        fun createWritable(m: loci.formats.meta.IMetadata, p: WritablePixelData): WritableImage {
            return WritableImageImpl(m, p)
        }

        /**
         * Constructs a new Image that is a deep copy of the specified Image.  The pixel data will be copied exactly, but no guarantee
         * is made that the metadata will be completely copied; only the minimally necessary metadata (like dimension sizes) will definitely be copied.

         * @param toCopy    The Image to copy.
         * *
         * @return the constructed Image.
         */
        fun create(toCopy: Image): Image {
            return ReadOnlyImageImpl(toCopy, false)
        }

        /**
         * Constructs a new Image that is a shallow copy of the specified Image.

         * @param toCopy    The Image to copy.
         * *
         * @return the constructed Image.
         */
        fun createShallow(toCopy: Image): Image {
            return ReadOnlyImageImpl(toCopy, true)
        }

        /**
         * Constructs a new WritableImage that is a deep copy of the specified Image.  The pixel data will be copied exactly, but no guarantee
         * is made that the metadata will be completely copied; only the minimally necessary metadata (like dimension sizes) will definitely be copied.
         *
         *

         * @param toCopy    The Image to copy.
         * *
         * @return the constructed WritableImage.
         */
        fun createWritable(toCopy: Image): WritableImage {
            return WritableImageImpl(toCopy)
        }

        /**
         * Constructs a new Image with specified dimension sizes and all the pixel values set to some specified initial value.  Bare-bones metadata
         * will also be constructed.
         *
         *
         * (Note that even though ImageCoordinates are zero-indexed, the ImageCoordinate's components should be the actual sizes of the dimensions,
         * not the indices of those dimensions.  For instance, for an 512 x by 512 y by 1 z plane by 1 color by 1 timepoint image, the ImageCoordinate
         * specifying sizes should have components = (512, 512, 1, 1, 1), not (511, 511, 0, 0, 0).)

         * @param dimensionSizes    An ImageCoordinate whose components will be the sizes of each dimension of the Image.
         * *
         * @param initialValue      The initial value to which all the pixels will be set.
         * *
         * @return the constructed Image.
         */
        fun create(dimensionSizes: ImageCoordinate, initialValue: Float): Image {
            return ReadOnlyImageImpl(dimensionSizes, initialValue)
        }

        /**
         * Constructs a new WritableImage with specified dimension sizes and all the pixel values set to some specified initial value.  Bare-bones metadata
         * will also be constructed.
         *
         *
         * (Note that even though ImageCoordinates are zero-indexed, the ImageCoordinate's components should be the actual sizes of the dimensions,
         * not the indices of those dimensions.  For instance, for an 512 x by 512 y by 1 z plane by 1 color by 1 timepoint image, the ImageCoordinate
         * specifying sizes should have components = (512, 512, 1, 1, 1), not (511, 511, 0, 0, 0).)

         * @param dimensionSizes    An ImageCoordinate whose components will be the sizes of each dimension of the Image.
         * *
         * @param initialValue      The initial value to which all the pixels will be set.
         * *
         * @return the constructed Image.
         */
        fun createWritable(dimensionSizes: ImageCoordinate, initialValue: Float): WritableImage {
            return WritableImageImpl(dimensionSizes, initialValue)
        }

        /**
         * Constructs a new Image from a java standard BufferedImage.

         * @param bufferedImage        The BufferedImage to convert to an Image.
         * *
         * @return the constructed Image.
         */
        fun create(bufferedImage: BufferedImage): Image {
            return ReadOnlyImageImpl(bufferedImage)
        }

        /**
         * Constructs a new WritableImage from a java standard BufferedImage.

         * @param bufferedImage        The BufferedImage to convert to an Image.
         * *
         * @return the constructed WritableImage.
         */
        fun createWritable(bufferedImage: BufferedImage): Image {
            return WritableImageImpl(bufferedImage)
        }

        /**
         * Constructs a new Image from an ImageJ ImagePlus.

         * @param imPl        The ImagePlus to convert to an Image.
         * *
         * @return the constructed Image.
         */
        fun create(imPl: ImagePlus): Image {
            return ReadOnlyImageImpl(imPl)
        }

        /**
         * Constructs a new Image from an ImageJ ImagePlus.

         * @param imPl        The ImagePlus to convert to an Image.
         * *
         * @return the constructed WritableImage.
         */
        fun createWritable(imPl: ImagePlus): WritableImage {
            return WritableImageImpl(imPl)
        }
    }


}
