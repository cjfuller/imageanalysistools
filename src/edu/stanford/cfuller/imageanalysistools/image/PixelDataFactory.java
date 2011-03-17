package edu.stanford.cfuller.imageanalysistools.image;

/**
 * A factory to construct PixelData objects; the choice of implementation will be made based on the
 * properties of the data.
 *
 */
public class PixelDataFactory {

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

        return createPixelData(sizes.getX(), sizes.getY(), sizes.getZ(), sizes.getC(), sizes.getT(), data_type, dimensionOrder);

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
