package edu.stanford.cfuller.imageanalysistools.image

import ij.ImagePlus
import net.imglib2.img.ImgPlus
import net.imglib2.type.numeric.real.FloatType


/**
 * A factory to construct PixelData objects; the choice of implementation will be made based on the
 * properties of the data.
 * @author Colin J. Fuller
 */
//TODO: reimplement to handle images other than 5D.

/**
 * Constructs a new default PixelDataFactory.
 */
class PixelDataFactory {
    companion object {
        /**
         * Construct a new pixeldata with some sensible default settings (XYZCT ordering, float datatype).
         */
        fun createPixelData(sizes: ImageCoordinate): WritablePixelData {
            return createPixelData(sizes, loci.formats.FormatTools.FLOAT, "XYZCT")
        }
        /**
         * Constructs a new pixeldata object using an [ImageCoordinate] to specify the size of the pixeldata.
         * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
         * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from [loci.formats.FormatTools]
         * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
         * @return              A new PixelData with the specified options.
         */

        fun createPixelData(sizes: ImageCoordinate, data_type: Int, dimensionOrder: String): WritablePixelData {
            return createPixelData(sizes[ImageCoordinate.X], sizes[ImageCoordinate.Y], sizes[ImageCoordinate.Z], sizes[ImageCoordinate.C], sizes[ImageCoordinate.T], data_type, dimensionOrder)
        }

        /**
         * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
         * @param size_x    Size of the pixel data in the X-dimension.
         * @param size_y    Size of the pixel data in the Y-dimension.
         * @param size_z    Size of the pixel data in the Z-dimension.
         * @param size_c    Size of the pixel data in the C-dimension.
         * @param size_t    Size of the pixel data in the T-dimension.
         * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from [loci.formats.FormatTools]
         * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
         * @return          A new PixelData with the specified options.
         */
        fun createPixelData(size_x: Int, size_y: Int, size_z: Int, size_c: Int, size_t: Int, data_type: Int, dimensionOrder: String): WritablePixelData {
            return ImagePlusPixelData(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder)
        }

        /**
         * Creates a new ImagePlusPixelData from an existing ImagePlus.
         * @param imPl    The ImagePlus to use.  This will not be copied, but used and potentially modified in place.
         * @return          A new PixelData with the specified options.
         */
        fun createPixelData(imPl: ImagePlus): WritablePixelData {
            return ImagePlusPixelData(imPl)
        }

        /**
         * Creates a new PixelData from an existing ImgLib2 ImgPlus and a specified dimension order.
         * @param imgpl    The ImgPlus to use.  This may not be copied, but used and potentially modified in place.
         * @param dimensionOrder    a String containing the five characters XYZCT in the order they are in the image (if some dimensions are not present, the can be specified in any order)
         * @return          A new PixelData with the specified options.
         */
        fun createPixelData(imgpl: ImgPlus<FloatType>, dimensionOrder: String): WritablePixelData {
            return ImgLibPixelData(imgpl, dimensionOrder)
        }
    }
}
