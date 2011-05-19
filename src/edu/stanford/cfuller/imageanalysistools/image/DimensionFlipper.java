package edu.stanford.cfuller.imageanalysistools.image;

/**
 * A Class that implements methods to flip the Z- and T- dimensions of an Image (these can get swapped, e.g. while reading
 * metamorph stacks).
 * 
 * @author Colin J. Fuller
 * 
 */
public class DimensionFlipper {


    public static Image flipZT(Image flipped) {
        
        ImageCoordinate sizes = ImageCoordinate.cloneCoord(flipped.getDimensionSizes());


        int temp_t = sizes.getT();
        sizes.setT(sizes.getZ());
        sizes.setZ(temp_t);

        Image newImage = new Image(sizes, 0.0);

        ImageCoordinate flipCoord = ImageCoordinate.createCoord(0,0,0,0,0);

        for (ImageCoordinate ic : flipped) {
            flipCoord.setCoord(ic);
            flipCoord.setZ(ic.getT());
            flipCoord.setT(ic.getZ());

            newImage.setValue(flipCoord, flipped.getValue(ic));
        }

        flipCoord.recycle();
        sizes.recycle();

        return newImage;

    }


}
