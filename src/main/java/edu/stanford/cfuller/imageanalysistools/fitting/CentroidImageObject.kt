package edu.stanford.cfuller.imageanalysistools.fitting

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary

/**
 * An image object of arbitrary shape whose position is determined by the
 * (intensity-weighted) centroid over its region in a mask.

 * @author Colin J. Fuller
 */
class CentroidImageObject
/**
 * Creates a GaussianImageObject from the specified masked region in an Image.
 * @param label     The greylevel of the object in the Image mask.
 * @param mask      The mask of objects in the Image, with a unique greylevel assigned to each object.
 * @param parent    The Image that the object occurs in and that is masked by mask.
 * @param p         The parameters associated with this analysis.
 */
(label: Int, mask: Image, parent: Image, p: ParameterDictionary) : ImageObject() {
    init {
        this.init(label, mask, parent, p)
    }

    /**
     * "Fits" this object to a position by finding its intensity-weighted
     * centroid.  Does not compute error estimates, numbers of photons, etc.
     * @param p     The parameters for the current analysis.
     */
    override fun fitPosition(p: ParameterDictionary) {
        if (this.sizeInPixels == 0) {
            this.nullifyImages()
            return
        }
        this.fitParametersByChannel = java.util.ArrayList<FitParameters>()
        this.fitR2ByChannel = java.util.ArrayList<Double>()
        this.fitErrorByChannel = java.util.ArrayList<Double>()
        this.nPhotonsByChannel = java.util.ArrayList<Double>()
        var numChannels = 0

        if (p.hasKey("num_wavelengths")) {
            numChannels = p.getIntValueForKey("num_wavelengths")
        } else {
            numChannels = this.parent!!.dimensionSizes.get(ImageCoordinate.C)
        }

        for (channelIndex in 0..numChannels - 1) {
            this.parentBoxMin!![ImageCoordinate.C] = channelIndex
            this.parentBoxMax!![ImageCoordinate.C] = channelIndex + 1
            this.boxImages()
            val x = java.util.Vector<Double>()
            val y = java.util.Vector<Double>()
            val z = java.util.Vector<Double>()
            val f = java.util.Vector<Float>()

            for (ic in this.parent!!) {
                x.add(ic[ImageCoordinate.X].toDouble())
                y.add(ic[ImageCoordinate.Y].toDouble())
                z.add(ic[ImageCoordinate.Z].toDouble())
                if (this.mask!!.getValue(ic).toInt() == this.label) {
                    f.add(parent!!.getValue(ic))
                } else {
                    f.add(0.0f)
                }
            }

            xValues = DoubleArray(x.size)
            yValues = DoubleArray(y.size)
            zValues = DoubleArray(z.size)
            functionValues = DoubleArray(f.size)

            var xCentroid = 0.0
            var yCentroid = 0.0
            var zCentroid = 0.0
            var totalCounts = 0.0

            for (i in x.indices) {
                xValues!![i] = x[i]
                yValues!![i] = y[i]
                zValues!![i] = z[i]
                functionValues!![i] = f[i].toDouble()
                xCentroid += xValues!![i] * functionValues!![i]
                yCentroid += yValues!![i] * functionValues!![i]
                zCentroid += zValues!![i] * functionValues!![i]
                totalCounts += functionValues!![i]
            }

            xCentroid /= totalCounts
            yCentroid /= totalCounts
            zCentroid /= totalCounts

            val position = ArrayRealVector(3, 0.0)
            position.setEntry(0, xCentroid)
            position.setEntry(1, yCentroid)
            position.setEntry(2, zCentroid)
            this.positionsByChannel.add(position)
        }

        this.hadFittingError = false
        this.nullifyImages()
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
