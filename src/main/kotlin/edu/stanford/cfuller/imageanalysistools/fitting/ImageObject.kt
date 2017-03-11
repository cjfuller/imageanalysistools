package edu.stanford.cfuller.imageanalysistools.fitting

import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.linear.RealVector

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.io.StringWriter

import edu.stanford.cfuller.imageanalysistools.util.Base64BinaryAdapter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamWriter

/**
 * An object in an image with some spatial extent that can be fit to some function.

 * Classes that extend ImageObject will supply methods that perform the fit to a specific functional form.

 * @author Colin J. Fuller
 */
abstract class ImageObject : Serializable {
    internal var centroidInMask: Vector3D = Vector3D(Double.NaN, Double.NaN, Double.NaN)
    internal var sizeInPixels: Int = 0

    /**
     * Gets the label of this object; this corresponds to its greylevel value in the original image mask.
     * @return  The label of this object.
     */
    var label: Int = 0
        internal set

    internal var maxIntensityZCoordByChannel: IntArray? = null
    internal var xValues: DoubleArray? = null
    internal var yValues: DoubleArray? = null
    internal var zValues: DoubleArray? = null
    /**
     * Gets the internal array containing the greylevel values of the pixels in the box containing this object.

     * No specific order is guaranteed beyond that it must be the same order as the x-, y-, and z- coordinates.

     * @return  An array containing the values of each pixel in this object's box.
     */
    var functionValues: DoubleArray? = null
        internal set

    internal var numberOfChannels: Int = 0

    /**
     * Gets the fitted parameters, one set per channel in the original image of the object.
     * @return  A List containing FitParameters objects, one for each channel in the original object.
     */
    var fitParametersByChannel: MutableList<FitParameters>? = null
        internal set
    /**
     * Gets the R^2 values for the fit in each channel.
     * @return  a List containing one R^2 value for the fit in each channel.
     */
    var fitR2ByChannel: MutableList<Double>? = null
        internal set
    /**
     * Gets an error estimate for the fitting of the position of the object in each channel.
     * @return  a List containing one fit error estimate for each channel.
     */
    var fitErrorByChannel: MutableList<Double>? = null
        internal set

    /**
     * Gets the number of photons above background from the object in each channel.
     * @return    a List containing the number of photons collected in each channel.
     */
    var nPhotonsByChannel: MutableList<Double>? = null
        internal set

    internal var positionsByChannel: MutableList<RealVector> = mutableListOf()
    internal var correctedPositionsByChannel: MutableMap<Int, RealVector> = mutableMapOf()
    internal var vectorDifferencesBetweenChannels: MutableMap<Int, RealVector> = mutableMapOf()
    internal var scalarDifferencesBetweenChannels: MutableMap<Int, Double> = mutableMapOf()
    internal var parentBoxMin: ImageCoordinate? = null
    internal var parentBoxMax: ImageCoordinate? = null

    /**
     * Returns a reference to the parent Image of this ImageObject (that is, the image used to create it).

     * @return  A reference to the parent Image.
     */
    var parent: Image? = null
        internal set
    /**
     * Returns a reference to the mask of the original image that was used to create this ImageObject.

     * @return  A reference to the mask Image.
     */
    var mask: Image? = null
        internal set

    /**
     * Gets the ID of the image from which the object was taken (this could, for instance, be the filename of the original image).

     * @return  A string that is the ID of the original image.
     */
    /**
     * Sets the ID of the image from which the object was taken (this could, for instance, be the filename of the original image).
     * @param imageID   A string to which to set the ID of the image.
     */
    var imageID: String? = null

    internal var hadFittingError: Boolean = false
    /**
     * Gets information on whether this ImageObject had its position corrected successfully.
     * @return    true if the position was corrected successfully, false if it failed or was not corrected at all.
     */
    /**
     * Sets whether this ImageObject had its position corrected successfully.
     * @param success    Whether the position has been corrected successfully.
     */
    var correctionSuccessful: Boolean = false


    /**
     * Initializes the fields of an ImageObject to default or null values.
     */
    protected fun init() {
        this.parentBoxMin = null
        this.parentBoxMax = null
        this.parent = null
        this.sizeInPixels = 0
        this.fitParametersByChannel = null
        this.maxIntensityZCoordByChannel = null
        this.imageID = null
        this.hadFittingError = true
        this.fitR2ByChannel = null
        this.fitErrorByChannel = null
        this.nPhotonsByChannel = null
        this.label = 0
        this.vectorDifferencesBetweenChannels = java.util.HashMap<Int, RealVector>()
        this.scalarDifferencesBetweenChannels = java.util.HashMap<Int, Double>()
        this.numberOfChannels = 0
        this.correctionSuccessful = false
    }


    /**
     * Initializes the fields of an ImageObject based on supplied image and parameter data.

     * Both the mask and the parent will be unmodified, except for boxing a region of interest, so no other thread should
     * be using these images at the same time.

     * @param label     The numerical label of the object in the mask.
     * *
     * @param mask      An image mask, containing a unique greylevel for each object; the greylevel of the object being initialized should correspond to the parameter [.label].
     * *
     * @param parent    The original imgae that the mask corresponds to.
     * *
     * @param p         A [ParameterDictionary] containing the parameters for the current analysis.  In particular, this routine makes use of the various box size parameters.
     */
    protected fun init(label: Int, mask: Image, parent: Image, p: ParameterDictionary) {
        this.fitParametersByChannel = null
        this.fitR2ByChannel = null
        this.fitErrorByChannel = null
        this.nPhotonsByChannel = null
        this.positionsByChannel = java.util.ArrayList<RealVector>()
        this.correctedPositionsByChannel = java.util.HashMap<Int, RealVector>()
        this.vectorDifferencesBetweenChannels = java.util.HashMap<Int, RealVector>()
        this.scalarDifferencesBetweenChannels = java.util.HashMap<Int, Double>()
        this.parent = parent
        this.mask = mask
        this.label = label
        this.sizeInPixels = 0
        this.centroidInMask = Vector3D(0.0, 0.0, 0.0)
        this.imageID = null
        this.hadFittingError = true
        this.numberOfChannels = p.getIntValueForKey("num_wavelengths") // use this so that if there's extra wavelengths not to be quantified at the end, these won't skew the initial guess

        for (i in mask) {
            if (mask.getValue(i) == label.toFloat()) {
                sizeInPixels++
                this.centroidInMask = this.centroidInMask.add(
                        Vector3D(i[ImageCoordinate.X].toDouble(), i[ImageCoordinate.Y].toDouble(), i[ImageCoordinate.Z].toDouble()))
            }
        }

        if (this.sizeInPixels == 0) {
            return
        }

        this.centroidInMask = this.centroidInMask.scalarMultiply(1.0 / sizeInPixels)

        var xcoord = Math.round(this.centroidInMask.x - p.getIntValueForKey("half_box_size")).toInt()
        var ycoord = Math.round(this.centroidInMask.y - p.getIntValueForKey("half_box_size")).toInt()
        if (xcoord < 0) {
            xcoord = 0
        }
        if (ycoord < 0) {
            ycoord = 0
        }
        this.parentBoxMin = ImageCoordinate.createCoordXYZCT(xcoord, ycoord, 0, 0, 0)

        xcoord = Math.round(this.centroidInMask.x + p.getIntValueForKey("half_box_size")).toInt() + 1
        ycoord = Math.round(this.centroidInMask.y + p.getIntValueForKey("half_box_size")).toInt() + 1
        if (xcoord > mask.dimensionSizes[ImageCoordinate.X]) {
            xcoord = mask.dimensionSizes[ImageCoordinate.X]
        }
        if (ycoord > mask.dimensionSizes[ImageCoordinate.Y]) {
            ycoord = mask.dimensionSizes[ImageCoordinate.Y]
        }

        this.parentBoxMax = ImageCoordinate.createCoordXYZCT(
                xcoord, ycoord,
                parent.dimensionSizes[ImageCoordinate.Z],
                parent.dimensionSizes[ImageCoordinate.C],
                parent.dimensionSizes[ImageCoordinate.T])

        //handle either 2D or 3D masks

        //2D case:
        if (mask.dimensionSizes[ImageCoordinate.Z] == 1) {
            //find the max intensity pixel in each channel and use this to refine the box
            this.maxIntensityZCoordByChannel = IntArray(parent.dimensionSizes[ImageCoordinate.C])
            var minZOverall = parent.dimensionSizes[ImageCoordinate.Z]
            var maxZOverall = 0
            for (c in 0..this.numberOfChannels - 1) {
                this.parentBoxMin!![ImageCoordinate.C] = c
                this.parentBoxMax!![ImageCoordinate.C] = c + 1
                parent.setBoxOfInterest(this.parentBoxMin!!, this.parentBoxMax!!)
                var maxValue = 0.0
                var maxCoord: ImageCoordinate? = null
                for (ic in parent) {
                    if (ic[ImageCoordinate.X] != Math.round(this.centroidInMask.x).toInt() ||
                            ic.get(dimensionConstant = ImageCoordinate.Y) != Math.round(this.centroidInMask.y).toInt()) {
                        continue
                    }
                    if (parent.getValue(ic) > maxValue) {
                        maxValue = parent.getValue(ic).toDouble()
                        if (maxCoord != null) maxCoord.recycle()
                        maxCoord = ImageCoordinate.cloneCoord(ic)
                    }
                }

                if (maxCoord == null) continue
                if (maxCoord[ImageCoordinate.Z] > maxZOverall) maxZOverall = maxCoord[ImageCoordinate.Z]
                if (maxCoord[ImageCoordinate.Z] < minZOverall) minZOverall = maxCoord[ImageCoordinate.Z]

                this.maxIntensityZCoordByChannel!![c] = maxCoord[ImageCoordinate.Z]
                maxCoord.recycle()
                parent.clearBoxOfInterest()
            }

            if (minZOverall > maxZOverall) {
                minZOverall = 0
                maxZOverall = 0
                java.util.logging.Logger.getLogger("edu.stanford.cfuller.colocalization3d").warning("Problem when calculating Z range of image stack.")
            }

            val zAverage = (minZOverall + maxZOverall) / 2
            var zcoord = 0

            this.parentBoxMin!![ImageCoordinate.C] = 0
            zcoord = zAverage - p.getIntValueForKey("half_z_size")
            if (zcoord < 0) zcoord = 0
            this.parentBoxMin!![ImageCoordinate.Z] = zcoord

            this.parentBoxMax!![ImageCoordinate.C] = parent.dimensionSizes[ImageCoordinate.C]
            zcoord = zAverage + p.getIntValueForKey("half_z_size") + 1
            if (zcoord > parent.dimensionSizes[ImageCoordinate.Z]) zcoord = parent.dimensionSizes[ImageCoordinate.Z]
            this.parentBoxMax!![ImageCoordinate.Z] = zcoord

        } else { //3D mask
            var zcoord = Math.round(this.centroidInMask.z - p.getIntValueForKey("half_z_size")).toInt()
            if (zcoord < 0) {
                zcoord = 0
            }
            this.parentBoxMin!![ImageCoordinate.Z] = zcoord
            zcoord = Math.round(this.centroidInMask.z + p.getIntValueForKey("half_z_size").toDouble() + 1.0).toInt()
            this.parentBoxMax!![ImageCoordinate.Z] = zcoord
        }
    }


    /**
     * Fits the object to the ImageObject's functional form in order to determine its position.
     * @param p     The parameters for the current analysis.
     */
    abstract fun fitPosition(p: ParameterDictionary)

    /**
     * Nullifies the references to the parent and mask image, and the internal storage of the image data and coordinates to free them for garbage collection.

     * This should only be called after fitting has been completed, as fitting will no longer be possible without the original image data.
     */
    fun nullifyImages() {
        //this.parent.dispose();
        this.parent = null
        //this.mask.dispose();
        this.mask = null
        this.xValues = null
        this.yValues = null
        this.zValues = null
        this.functionValues = null
    }

    /**
     * Cleans up the ImageCoordinates used internally by ImageCoordinates and recycles them for future use.
     * @throws Throwable
     */
    @Throws(Throwable::class)
    protected fun finalize() {
        if (this.parentBoxMin != null) {
            this.parentBoxMin!!.recycle()
            this.parentBoxMin = null
        }
        if (this.parentBoxMax != null) {
            this.parentBoxMax!!.recycle()
            this.parentBoxMax = null
        }
        // super.finalize() // TODO(colin): why can't I call super.finalize()?
    }


    /**
     * Sets the relevant region of interest in the parent image to be the region that boxes this object.
     */
    fun boxImages() {
        this.parent!!.setBoxOfInterest(this.parentBoxMin!!, this.parentBoxMax!!)
        this.mask!!.setBoxOfInterest(this.parentBoxMin!!, this.parentBoxMax!!)
    }

    /**
     * Clears the region of interest in the parent image.
     */
    fun unboxImages() {
        this.parent!!.clearBoxOfInterest()
        this.mask!!.clearBoxOfInterest()
    }

    /**
     * Gets the internal array containing the x-coordinates of the pixels in the box containing this object.
     * No specific order is guaranteed beyond that it must be the same order as the y- and z- coordinates and function values.
     * @return  An array containing the x-coordinates of each pixel in this object's box.
     */
    fun getxValues(): DoubleArray {
        return xValues!!
    }

    /**
     * Gets the internal array containing the y-coordinates of the pixels in the box containing this object.
     * No specific order is guaranteed beyond that it must be the same order as the x- and z- coordinates and function values.
     * @return  An array containing the x-coordinates of each pixel in this object's box.
     */
    fun getyValues(): DoubleArray {
        return yValues!!
    }

    /**
     * Gets the internal array containing the z-coordinates of the pixels in the box containing this object.
     * No specific order is guaranteed beyond that it must be the same order as the x- and y- coordinates and function values.
     * @return  An array containing the x-coordinates of each pixel in this object's box.
     */
    fun getzValues(): DoubleArray {
        return zValues!!
    }

    /**
     * Gets information on whether this object has finished fitting with no errors.
     * @return  true if fitting finished normally, false if there was an error or the object was not yet fit.
     */
    fun finishedFitting(): Boolean {
        return !this.hadFittingError
    }

    /**
     * Gets the position of this object in the specified channel.
     * Do not modify the returned RealVector, as it is a reference to the internally stored vector.
     * @param channel    The index of the channel, either by order in the original multiwavelength image, or in the order specified for split wavelength images.
     * @return    A RealVector containing the x,y,and z coordinates of the position, or null if it has not yet been determined, or the channel is out of range.
     */
    fun getPositionForChannel(channel: Int): RealVector? {
        if (channel >= this.positionsByChannel.size) {
            return null
        }
        return this.positionsByChannel[channel]
    }

    /**
     * Gets the corrected position of this object in the specified channel (if there is a corrected position).
     * Do not modify the returned RealVector, as it is a reference to the internally stored vector.
     * @param channel    The index of the channel, either by order in the original multiwavelength image, or in the order specified for split wavelength images.
     * @return    A RealVector containing the x,y,and z coordinates of the position, or null if it has not yet been determined, or the channel is out of range.
     */
    fun getCorrectedPositionForChannel(channel: Int): RealVector? {
        if (!this.correctedPositionsByChannel.containsKey(channel)) {
            return null
        }
        return this.correctedPositionsByChannel[channel]
    }

    /**
     * Applies the specified correction vector to the position of the object to generate a corrected position that can be accessed using [.getCorrectedPositionForChannel].
     * @param channel    The index of the channel, either by order in the original multiwavelength image, or in the order specified for split wavelength images.
     * @param correction a RealVector containing a correction that will be subtracted from the position of the object in the specified channel.
     */
    fun applyCorrectionVectorToChannel(channel: Int, correction: RealVector) {
        this.correctedPositionsByChannel.put(channel, this.getPositionForChannel(channel)!!.subtract(correction))
    }

    /**
     * Gets the vector difference between the position of the object in two channels.
     * Note that there is no unit conversion here, and the distance is returned in image units of pixels or sections.
     * @param channel0    The index of one channel to use for the difference.
     * @param channel1    The index of the other channel to use for the difference.
     * @return            The vector difference between the two channels, as channel1 - channel0, or null if either channel is out of range or has not yet been fit.
     */
    fun getVectorDifferenceBetweenChannels(channel0: Int, channel1: Int): RealVector? {
        val key = this.numberOfChannels * channel0 + channel1

        if (!this.vectorDifferencesBetweenChannels.containsKey(key)) {
            val ch0 = this.getPositionForChannel(channel0)
            val ch1 = this.getPositionForChannel(channel1)
            if (ch0 == null || ch1 == null) {
                return null
            }
            this.vectorDifferencesBetweenChannels.put(key, ch1.subtract(ch0))
        }
        return this.vectorDifferencesBetweenChannels[key]
    }

    /**
     * Gets the vector difference between the corrected positions of the object in two channels.
     * If there is no corrected position for a channel, its uncorrected position is used.
     * Note that there is no unit conversion here, and the distance is returned in image units of pixels or sections.
     * @param channel0    The index of one channel to use for the difference.
     * @param channel1    The index of the other channel to use for the difference.
     * @return            The vector difference between the two channels, as channel1 - channel0, or null if either channel is out of range or has not yet been fit.
     */
    fun getCorrectedVectorDifferenceBetweenChannels(channel0: Int, channel1: Int): RealVector {
        var c0: RealVector? = this.correctedPositionsByChannel[channel0]
        var c1: RealVector? = this.correctedPositionsByChannel[channel1]
        if (c0 == null) c0 = this.positionsByChannel[channel0]
        if (c1 == null) c1 = this.positionsByChannel[channel1]
        return c1.subtract(c0)
    }

    /**
     * Gets the scalar difference between the position of the object in two channels.
     * Units are converted from image units to real units using the supplied vector of conversions.
     * @param channel0    The index of one channel to use for the difference.
     * @param channel1    The index of the other channel to use for the difference.
     * @param pixelToDistanceConversions    A vector containing the number of realspace distance units per pixel or section, one element per dimension.
     * @return            The scalar distance between the position of the object in each channel (that is, the length of the vector representing the vector distance), or null if either channel is out of range or has not yet been fit.
     */
    fun getScalarDifferenceBetweenChannels(channel0: Int, channel1: Int, pixelToDistanceConversions: RealVector): Double? {
        val key = this.numberOfChannels * channel0 + channel1
        if (!this.scalarDifferencesBetweenChannels.containsKey(key)) {
            val vecDifference = this.getVectorDifferenceBetweenChannels(channel0, channel1) ?: return null
            this.scalarDifferencesBetweenChannels.put(key, vecDifference.ebeMultiply(pixelToDistanceConversions).norm)
        }
        return this.scalarDifferencesBetweenChannels[key]
    }

    fun writeToXMLString(): String {
        val sw = StringWriter()
        try {
            val xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw)
            this.writeToXML(xsw)
        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception encountered while writing XML correction output: " + e.message)
        }

        return sw.toString()
    }

    fun writeToXML(xsw: XMLStreamWriter) {
        try {
            xsw.writeStartElement(OBJECT_ELEMENT)
            xsw.writeAttribute(LABEL_ATTR, Integer.toString(this.label))
            xsw.writeAttribute(IMAGE_ATTR, this.imageID)
            xsw.writeCharacters("\n")
            for (i in 0..this.numberOfChannels - 1) {
                xsw.writeStartElement(CHANNEL_ELEMENT)
                xsw.writeAttribute(CH_ID_ATTR, Integer.toString(i))
                xsw.writeCharacters("\n")
                xsw.writeStartElement(FIT_ELEMENT)
                xsw.writeAttribute(R2_ATTR, java.lang.Double.toString(this.fitR2ByChannel!![i]))
                xsw.writeAttribute(ERROR_ATTR, java.lang.Double.toString(this.fitErrorByChannel!![i]))
                xsw.writeAttribute(N_PHOTONS_ATTR, java.lang.Double.toString(this.nPhotonsByChannel!![i]))
                xsw.writeCharacters("\n")
                xsw.writeStartElement(PARAMETERS_ELEMENT)
                xsw.writeCharacters(this.fitParametersByChannel!![i].toString().replace(";", ",").replace("}", "").replace("{", ""))
                xsw.writeEndElement() //parameters
                xsw.writeCharacters("\n")
                xsw.writeStartElement(POSITION_ELEMENT)
                xsw.writeCharacters(this.getPositionForChannel(i)!!.toString().replace(";", ",").replace("}", "").replace("{", ""))
                xsw.writeEndElement() //position
                xsw.writeCharacters("\n")
                xsw.writeEndElement() //fit
                xsw.writeCharacters("\n")
                xsw.writeEndElement() //channel
                xsw.writeCharacters("\n")
            }
            xsw.writeStartElement(SERIAL_ELEMENT)
            xsw.writeAttribute(ENCODING_ATTR, ENCODING_NAME)
            val bytesOutput = ByteArrayOutputStream()
            try {
                val oos = ObjectOutputStream(bytesOutput)
                oos.writeObject(this)
            } catch (e: java.io.IOException) {
                LoggingUtilities.logger.severe("Exception encountered while serializing ImageObject: " + e.message)
            }
            val adapter = Base64BinaryAdapter()
            xsw.writeCharacters(adapter.marshal(bytesOutput.toByteArray()))
            xsw.writeEndElement() //serial
            xsw.writeCharacters("\n")
            xsw.writeEndElement() //object
            xsw.writeCharacters("\n")
        } catch (e: XMLStreamException) {
            LoggingUtilities.logger.severe("Exception encountered while writing XML correction output: " + e.message)
        }
    }

    companion object {
        //TODO: maintain the notion that the ImageObject has some location in real space, but reduce
        //dependence on 5D images.

        const val serialVersionUID = 5L
        val OBJECT_ELEMENT = "image_object"
        val CHANNEL_ELEMENT = "channel"
        val LABEL_ATTR = "label"
        val IMAGE_ATTR = "image_id"
        val CH_ID_ATTR = "channel_id"
        val FIT_ELEMENT = "fit"
        val PARAMETERS_ELEMENT = "parameters"
        val R2_ATTR = "r_squared"
        val ERROR_ATTR = "fit_error"
        val N_PHOTONS_ATTR = "photon_count"
        val POSITION_ELEMENT = "position"
        val SERIAL_ELEMENT = "serialized_form"
        val ENCODING_ATTR = "encoding"
        val ENCODING_NAME = "base64"
    }
}
