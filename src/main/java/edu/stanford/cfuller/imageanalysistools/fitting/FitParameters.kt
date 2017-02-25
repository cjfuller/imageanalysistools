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

package edu.stanford.cfuller.imageanalysistools.fitting

/**
 * A class that holds the parameters that are the result of a fit of an [ImageObject].

 * @author Colin J. Fuller
 */
class FitParameters : java.io.Serializable {

    internal var positionParameters: MutableMap<Int, Double>
    internal var sizeParameters: MutableMap<Int, Double>
    /**
     * Gets the amplitude of the object associated with these parameters.

     * @return the amplitude
     */
    /**
     * Sets the amplitude of the object associated with these parameters.

     * @param a the amplitude
     */
    var amplitude: Double = 0.toDouble()
    /**
     * Gets the background of the object associated with these parameters.

     * @return the background
     */
    /**
     * Sets the background of the object associated with these parameters.

     * @param b the background
     */
    var background: Double = 0.toDouble()

    internal var otherParameters: MutableMap<String, Double>

    /**
     * Creates a new fit parameters object with no parameter values.
     */
    init {

        this.positionParameters = java.util.HashMap<Int, Double>()
        this.sizeParameters = java.util.HashMap<Int, Double>()
        this.amplitude = 0.0
        this.background = 0.0
        this.otherParameters = java.util.HashMap<String, Double>()

    }

    /**
     * Gets the position of the object associated with these parameters in the supplied dimension.

     * @param dim an int that specifies the dimension; this should be one of the dimension constants from [ImageCoordinate][edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate] or a user-defined dimension.
     * *
     * @return the position in that dimension.
     */
    fun getPosition(dim: Int): Double {
        return this.positionParameters[dim]
    }

    /**
     * Sets the position of the object associated with these parameters in the supplied dimension.

     * @param dim an int that specifies the dimension; this should be one of the dimension constants from [ImageCoordinate][edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate] or a user-defined dimension.
     * *
     * @param value the position in the supplied dimension
     */
    fun setPosition(dim: Int, value: Double) {
        this.positionParameters.put(dim, value)
    }

    /**
     * Gets the size of the object associated with these parameters in the supplied dimension.

     * @param dim an int that specifies the dimension; this should be one of the dimension constants from [ImageCoordinate][edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate] or a user-defined dimension.
     * *
     * @return the size in that dimension.
     */
    fun getSize(dim: Int): Double {
        return this.sizeParameters[dim]
    }

    /**
     * Sets the size of the object associated with these parameters in the supplied dimension.

     * @param dim an int that specifies the dimension; this should be one of the dimension constants from [ImageCoordinate][edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate] or a user-defined dimension.
     * *
     * @param value the size in the supplied dimension
     */
    fun setSize(dim: Int, value: Double) {
        this.sizeParameters.put(dim, value)
    }

    /**
     * Gets the value of a named parameter associated with the fit object.
     * @param parameter a String naming the parameter
     * *
     * @return the value of the named parameter as a double
     */
    fun getOtherParameters(parameter: String): Double {
        return this.otherParameters[parameter]
    }

    /**
     * Sets the value of a named parameter associated with the fit object.
     * @param parameter a String naming the parameter
     * *
     * @param value the value of the named parameter as a double
     */
    fun setOtherParameter(parameter: String, value: Double) {
        this.otherParameters.put(parameter, value)
    }

    /**
     * Creates a String representation of the parameters.
     * Format is amplitude, background, pos in each channel, size in each channel, other parameters.
     * @return a String containing the parameter information.
     */
    override fun toString(): String {
        var s = "amp=" + this.amplitude
        s += ";bkg=" + this.background
        for (i in this.positionParameters.keys) {
            s += ";pos" + i + "=" + this.positionParameters[i]
        }
        for (i in this.sizeParameters.keys) {
            s += ";size" + i + "=" + this.sizeParameters[i]
        }
        for (sk in this.otherParameters.keys) {
            s += ";" + sk + "=" + this.otherParameters[sk]
        }
        return s
    }

    companion object {

        internal const val serialVersionUID = 1L
    }

}



