/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter

import java.awt.Rectangle

import ij.IJ
import ij.ImagePlus
import ij.measure.Measurements
import ij.plugin.ContrastEnhancer
import ij.plugin.filter.PlugInFilter
import ij.process.Blitter
import ij.process.ColorProcessor
import ij.process.FHT
import ij.process.FloatProcessor
import ij.process.ImageProcessor
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImagePlusPixelData
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate

/**
 * A filter that retains only a section of frequency space from an Image's Fourier transform.

 *
 *
 * This filter does not use a reference Image.
 *
 *
 * The argument to the apply method should be the Image to be bandpass filtered.

 * @author Colin J. Fuller
 */
class BandpassFilter : Filter() {

    internal var bandLow: Double = 0.toDouble()
    internal var bandHigh: Double = 0.toDouble()

    internal var shouldRescale: Boolean = false

    /**
     * Default constructor; creates a filter that is effectively a no-op until the setBand method is called, unless parameters
     * setting the low and high band limit were specified, which when the apply method is called will override any set or default
     * values.
     */
    init {
        bandLow = 0.0
        bandHigh = 0.0
        this.shouldRescale = false

    }

    /**
     * Sets whether the image should be rescaled to its original range after bandpass filtering.

     * @param shouldRescale        A boolean specifying whether the image will be rescaled.
     */
    fun setShouldRescale(shouldRescale: Boolean) {
        this.shouldRescale = shouldRescale
    }


    /**
     * Applies the bandpass filter to an Image, removing the range of frequency space specified by the setBand method.
     * @param im    The Image to be bandpass filtered; it will be replaced by its filtered version.
     */
    override fun apply(im: WritableImage) {

        im.clearBoxOfInterest() //just in case

        var oldMin = java.lang.Float.MAX_VALUE
        var oldMax = -1.0f * java.lang.Float.MAX_VALUE

        for (ic in im) {
            if (im.getValue(ic) < oldMin) {
                oldMin = im.getValue(ic)
            }

            if (im.getValue(ic) > oldMax) {
                oldMax = im.getValue(ic)
            }
        }



        if (this.params!!.hasKey(PARAM_BAND_LOW)) {
            this.bandLow = this.params!!.getDoubleValueForKey(PARAM_BAND_LOW)
        }

        if (this.params!!.hasKey(PARAM_BAND_HIGH)) {
            this.bandHigh = this.params!!.getDoubleValueForKey(PARAM_BAND_HIGH)
        }

        val imp = im.toImagePlus()

        val ijf = IJFFTFilter()

        IJFFTFilter.setFilterLargeDia(this.bandHigh)
        IJFFTFilter.setFilterSmallDia(this.bandLow)

        ijf.setup("", imp)

        for (i in 0..imp.stackSize - 1) {

            imp.setSliceWithoutUpdate(i + 1)

            val proc = imp.processor

            ijf.run(proc)

        }

        //only copy if this wasn't filtered in place.
        if (im.pixelData !is ImagePlusPixelData) {
            im.copy(ImageFactory.create(imp))
        }

        var newMin = java.lang.Float.MAX_VALUE
        var newMax = -1.0f * java.lang.Float.MAX_VALUE

        for (ic in im) {
            if (im.getValue(ic) < newMin) {
                newMin = im.getValue(ic)
            }

            if (im.getValue(ic) > newMax) {
                newMax = im.getValue(ic)
            }
        }


        val oldRange = oldMax - oldMin
        val newRange = newMax - newMin

        if (this.shouldRescale) {

            for (ic in im) {
                im.setValue(ic, (im.getValue(ic) - newMin) / newRange * oldRange + oldMin)
            }

        }


    }


    /**
     * Sets the band in frequency space to be retained in the Fourier transformed Image.
     *
     *
     * The two arguments specify the lower and upper bounds of the range to be retained, in terms of size in pixels, rather than frequency.
     * So for the low parameter, specify the lower size (and therefore the higher frequency); likewise, specify the larger size (and smaller frequency) for
     * the high parameter.


     * @param low   The lower bound of the sizes in pixels to be filtered.
     * *
     * @param high  The upper bound of the sizes in pixels to be filtered.
     */
    fun setBand(low: Double, high: Double) {
        this.bandLow = low
        this.bandHigh = high
    }


    /*
     * This has been taken directly from the ImageJ class FFTFilter and modified for
     * non-interactive use of the filter.
     */
    protected class IJFFTFilter : PlugInFilter, Measurements {

        private var imp: ImagePlus? = null
        private var fht: FHT? = null
        private var slice: Int = 0
        private var stackSize = 1

        override fun setup(arg: String, imp: ImagePlus?): Int {
            this.imp = imp
            if (imp == null) {
                IJ.noImage()
                return PlugInFilter.DONE
            }
            stackSize = imp.stackSize
            fht = imp.getProperty("FHT") as FHT
            if (fht != null) {
                IJ.error("FFT Filter", "Spatial domain image required")
                return PlugInFilter.DONE
            }
            if (!showBandpassDialog(imp))
                return PlugInFilter.DONE
            else
                return if (processStack) PlugInFilter.DOES_ALL + PlugInFilter.DOES_STACKS + PlugInFilter.PARALLELIZE_STACKS else PlugInFilter.DOES_ALL
        }

        override fun run(ip: ImageProcessor) {
            slice++
            filter(ip)
        }

        internal fun filter(ip: ImageProcessor) {
            var ip2 = ip
            if (ip2 is ColorProcessor) {
                showStatus("Extracting brightness")
                ip2 = ip2.brightness
            }
            val roiRect = ip2.roi
            val maxN = Math.max(roiRect.width, roiRect.height)
            val sharpness = (100.0 - toleranceDia) / 100.0
            val doScaling = doScalingDia
            val saturate = saturateDia

            IJ.showProgress(1, 20)

            /* 	tile mirrored image to power of 2 size
    			first determine smallest power 2 >= 1.5 * image width/height
    		  	factor of 1.5 to avoid wrap-around effects of Fourier Trafo */

            var i = 2
            while (i < 1.5 * maxN) i *= 2

            // Calculate the inverse of the 1/e frequencies for large and small structures.
            val filterLarge = 2.0 * filterLargeDia / i.toDouble()
            val filterSmall = 2.0 * filterSmallDia / i.toDouble()

            // fit image into power of 2 size
            val fitRect = Rectangle()
            fitRect.x = Math.round((i - roiRect.width) / 2.0).toInt()
            fitRect.y = Math.round((i - roiRect.height) / 2.0).toInt()
            fitRect.width = roiRect.width
            fitRect.height = roiRect.height

            // put image (ROI) into power 2 size image
            // mirroring to avoid wrap around effects
            showStatus("Pad to " + i + "x" + i)
            ip2 = tileMirror(ip2, i, i, fitRect.x, fitRect.y)
            IJ.showProgress(2, 20)

            // transform forward
            showStatus(i.toString() + "x" + i + " forward transform")
            val fht = FHT(ip2)
            fht.setShowProgress(false)
            fht.transform()
            IJ.showProgress(9, 20)
            //new ImagePlus("after fht",ip2.crop()).show();

            // filter out large and small structures
            showStatus("Filter in frequency domain")
            filterLargeSmall(fht, filterLarge, filterSmall, choiceIndex, sharpness)
            //new ImagePlus("filter",ip2.crop()).show();
            IJ.showProgress(11, 20)

            // transform backward
            showStatus("Inverse transform")
            fht.inverseTransform()
            IJ.showProgress(19, 20)
            //new ImagePlus("after inverse",ip2).show();

            // crop to original size and do scaling if selected
            showStatus("Crop and convert to original type")
            fht.roi = fitRect
            ip2 = fht.crop()
            if (doScaling) {
                val imp2 = ImagePlus(imp!!.title + "-filtered", ip2)
                ContrastEnhancer().stretchHistogram(imp2, if (saturate) 1.0 else 0.0)
                ip2 = imp2.processor
            }

            // convert back to original data type
            val bitDepth = imp!!.bitDepth
            when (bitDepth) {
                8 -> ip2 = ip2.convertToByte(doScaling)
                16 -> ip2 = ip2.convertToShort(doScaling)
                24 -> {
                    ip.snapshot()
                    showStatus("Setting brightness")
                    (ip as ColorProcessor).brightness = ip2 as FloatProcessor
                }
                32 -> {
                }
            }

            // copy filtered image back into original image
            if (bitDepth != 24) {
                ip.snapshot()
                ip.copyBits(ip2, roiRect.x, roiRect.y, Blitter.COPY)
            }
            ip.resetMinAndMax()
            IJ.showProgress(20, 20)
        }

        internal fun showStatus(msg: String) {
            if (stackSize > 1 && processStack)
                IJ.showStatus("FFT Filter: $slice/$stackSize")
            else
                IJ.showStatus(msg)
        }

        /** Puts ImageProcessor (ROI) into a new ImageProcessor of size width x height y at position (x,y).
         * The image is mirrored around its edges to avoid wrap around effects of the FFT.  */
        fun tileMirror(ip: ImageProcessor, width: Int, height: Int, x: Int, y: Int): ImageProcessor? {
            if (IJ.debugMode) IJ.log("FFT.tileMirror: " + width + "x" + height + " " + ip)
            if (x < 0 || x > width - 1 || y < 0 || y > height - 1) {
                IJ.error("Image to be tiled is out of bounds.")
                return null
            }

            val ipout = ip.createProcessor(width, height)

            val ip2 = ip.crop()
            val w2 = ip2.width
            val h2 = ip2.height

            //how many times does ip2 fit into ipout?
            val i1 = Math.ceil(x / w2.toDouble()).toInt()
            val i2 = Math.ceil((width - x) / w2.toDouble()).toInt()
            val j1 = Math.ceil(y / h2.toDouble()).toInt()
            val j2 = Math.ceil((height - y) / h2.toDouble()).toInt()

            //tile
            if (i1 % 2 > 0.5)
                ip2.flipHorizontal()
            if (j1 % 2 > 0.5)
                ip2.flipVertical()

            run {
                var i = -i1
                while (i < i2) {
                    var j = -j1
                    while (j < j2) {
                        ipout.insert(ip2, x - i * w2, y - j * h2)
                        j += 2
                    }
                    i += 2
                }
            }

            ip2.flipHorizontal()
            run {
                var i = -i1 + 1
                while (i < i2) {
                    var j = -j1
                    while (j < j2) {
                        ipout.insert(ip2, x - i * w2, y - j * h2)
                        j += 2
                    }
                    i += 2
                }
            }

            ip2.flipVertical()
            run {
                var i = -i1 + 1
                while (i < i2) {
                    var j = -j1 + 1
                    while (j < j2) {
                        ipout.insert(ip2, x - i * w2, y - j * h2)
                        j += 2
                    }
                    i += 2
                }
            }

            ip2.flipHorizontal()
            var i = -i1
            while (i < i2) {
                var j = -j1 + 1
                while (j < j2) {
                    ipout.insert(ip2, x - i * w2, y - j * h2)
                    j += 2
                }
                i += 2
            }

            return ipout
        }


        /*
    	filterLarge: down to which size are large structures suppressed?
    	filterSmall: up to which size are small structures suppressed?
    	filterLarge and filterSmall are given as fraction of the image size
    				in the original (untransformed) image.
    	stripesHorVert: filter out: 0) nothing more  1) horizontal  2) vertical stripes
    				(i.e. frequencies with x=0 / y=0)
    	scaleStripes: width of the stripe filter, same unit as filterLarge
    	*/
        internal fun filterLargeSmall(ip: ImageProcessor, filterLarge: Double, filterSmall: Double, stripesHorVert: Int, scaleStripes: Double) {
            var scaleStripes = scaleStripes

            val maxN = ip.width

            val fht = ip.pixels as FloatArray
            val filter = FloatArray(maxN * maxN)
            for (i in 0..maxN * maxN - 1)
                filter[i] = 1f

            var row: Int
            var backrow: Int
            var rowFactLarge: Float
            var rowFactSmall: Float

            var col: Int
            var backcol: Int
            var factor: Float
            var colFactLarge: Float
            var colFactSmall: Float

            var factStripes: Float

            // calculate factor in exponent of Gaussian from filterLarge / filterSmall

            val scaleLarge = filterLarge * filterLarge
            val scaleSmall = filterSmall * filterSmall
            scaleStripes = scaleStripes * scaleStripes
            //float FactStripes;

            // loop over rows
            for (j in 1..maxN / 2 - 1) {
                row = j * maxN
                backrow = (maxN - j) * maxN
                rowFactLarge = Math.exp(-(j * j) * scaleLarge).toFloat()
                rowFactSmall = Math.exp(-(j * j) * scaleSmall).toFloat()


                // loop over columns
                col = 1
                while (col < maxN / 2) {
                    backcol = maxN - col
                    colFactLarge = Math.exp(-(col * col) * scaleLarge).toFloat()
                    colFactSmall = Math.exp(-(col * col) * scaleSmall).toFloat()
                    factor = (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall
                    when (stripesHorVert) {
                        1 -> factor *= 1 - Math.exp(-(col * col) * scaleStripes).toFloat()
                        2 -> factor *= 1 - Math.exp(-(j * j) * scaleStripes).toFloat() // vert stripes
                    }// hor stripes

                    fht[col + row] *= factor
                    fht[col + backrow] *= factor
                    fht[backcol + row] *= factor
                    fht[backcol + backrow] *= factor
                    filter[col + row] *= factor
                    filter[col + backrow] *= factor
                    filter[backcol + row] *= factor
                    filter[backcol + backrow] *= factor
                    col++
                }
            }

            //process meeting points (maxN/2,0) , (0,maxN/2), and (maxN/2,maxN/2)
            val rowmid = maxN * (maxN / 2)
            rowFactLarge = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleLarge).toFloat()
            rowFactSmall = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleSmall).toFloat()
            factStripes = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleStripes).toFloat()

            fht[maxN / 2] *= (1 - rowFactLarge) * rowFactSmall // (maxN/2,0)
            fht[rowmid] *= (1 - rowFactLarge) * rowFactSmall // (0,maxN/2)
            fht[maxN / 2 + rowmid] *= (1 - rowFactLarge * rowFactLarge) * rowFactSmall * rowFactSmall // (maxN/2,maxN/2)
            filter[maxN / 2] *= (1 - rowFactLarge) * rowFactSmall // (maxN/2,0)
            filter[rowmid] *= (1 - rowFactLarge) * rowFactSmall // (0,maxN/2)
            filter[maxN / 2 + rowmid] *= (1 - rowFactLarge * rowFactLarge) * rowFactSmall * rowFactSmall // (maxN/2,maxN/2)

            when (stripesHorVert) {
                1 -> {
                    fht[maxN / 2] *= 1 - factStripes
                    fht[rowmid] = 0f
                    fht[maxN / 2 + rowmid] *= 1 - factStripes
                    filter[maxN / 2] *= 1 - factStripes
                    filter[rowmid] = 0f
                    filter[maxN / 2 + rowmid] *= 1 - factStripes
                }
                2 -> {
                    fht[maxN / 2] = 0f
                    fht[rowmid] *= 1 - factStripes
                    fht[maxN / 2 + rowmid] *= 1 - factStripes
                    filter[maxN / 2] = 0f
                    filter[rowmid] *= 1 - factStripes
                    filter[maxN / 2 + rowmid] *= 1 - factStripes
                }
            }// hor stripes
            // vert stripes

            //loop along row 0 and maxN/2
            rowFactLarge = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleLarge).toFloat()
            rowFactSmall = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleSmall).toFloat()
            col = 1
            while (col < maxN / 2) {
                backcol = maxN - col
                colFactLarge = Math.exp(-(col * col) * scaleLarge).toFloat()
                colFactSmall = Math.exp(-(col * col) * scaleSmall).toFloat()

                when (stripesHorVert) {
                    0 -> {
                        fht[col] *= (1 - colFactLarge) * colFactSmall
                        fht[backcol] *= (1 - colFactLarge) * colFactSmall
                        fht[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall
                        fht[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall
                        filter[col] *= (1 - colFactLarge) * colFactSmall
                        filter[backcol] *= (1 - colFactLarge) * colFactSmall
                        filter[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall
                        filter[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall
                    }
                    1 -> {
                        factStripes = Math.exp(-(col * col) * scaleStripes).toFloat()
                        fht[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes)
                        fht[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes)
                        fht[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        fht[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        filter[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes)
                        filter[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes)
                        filter[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        filter[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                    }
                    2 -> {
                        factStripes = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleStripes).toFloat()
                        fht[col] = 0f
                        fht[backcol] = 0f
                        fht[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        fht[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        filter[col] = 0f
                        filter[backcol] = 0f
                        filter[col + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                        filter[backcol + rowmid] *= (1 - colFactLarge * rowFactLarge) * colFactSmall * rowFactSmall * (1 - factStripes)
                    }
                }
                col++
            }

            // loop along column 0 and maxN/2
            colFactLarge = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleLarge).toFloat()
            colFactSmall = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleSmall).toFloat()
            for (j in 1..maxN / 2 - 1) {
                row = j * maxN
                backrow = (maxN - j) * maxN
                rowFactLarge = Math.exp(-(j * j) * scaleLarge).toFloat()
                rowFactSmall = Math.exp(-(j * j) * scaleSmall).toFloat()

                when (stripesHorVert) {
                    0 -> {
                        fht[row] *= (1 - rowFactLarge) * rowFactSmall
                        fht[backrow] *= (1 - rowFactLarge) * rowFactSmall
                        fht[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall
                        fht[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall
                        filter[row] *= (1 - rowFactLarge) * rowFactSmall
                        filter[backrow] *= (1 - rowFactLarge) * rowFactSmall
                        filter[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall
                        filter[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall
                    }
                    1 -> {
                        factStripes = Math.exp((-(maxN / 2)).toDouble() * (maxN / 2).toDouble() * scaleStripes).toFloat()
                        fht[row] = 0f
                        fht[backrow] = 0f
                        fht[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        fht[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        filter[row] = 0f
                        filter[backrow] = 0f
                        filter[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        filter[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                    }
                    2 -> {
                        factStripes = Math.exp(-(j * j) * scaleStripes).toFloat()
                        fht[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes)
                        fht[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes)
                        fht[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        fht[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        filter[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes)
                        filter[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes)
                        filter[row + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                        filter[backrow + maxN / 2] *= (1 - rowFactLarge * colFactLarge) * rowFactSmall * colFactSmall * (1 - factStripes)
                    }
                }
            }
            if (displayFilter && slice == 1) {
                val f = FHT(FloatProcessor(maxN, maxN, filter, null))
                f.swapQuadrants()
                ImagePlus("Filter", f).show()
            }
        }

        internal fun showBandpassDialog(imp: ImagePlus): Boolean {
            return true
        }

        companion object {

            private var filterLargeDia = 40.0
            private var filterSmallDia = 3.0
            private var choiceIndex = 0
            private var toleranceDia = 5.0
            private val doScalingDia = false
            private var saturateDia = false
            private var displayFilter = false
            private var processStack = false

            fun setFilterLargeDia(large: Double) {
                filterLargeDia = large
            }

            fun setFilterSmallDia(small: Double) {
                filterSmallDia = small
            }

            //0 = none, 1 = horizontal, 2 = vertical
            fun setChoice(choice: Int) {
                choiceIndex = choice
            }

            fun setTolerance(tol: Double) {
                toleranceDia = tol
            }

            fun setSaturate(sat: Boolean) {
                saturateDia = sat
            }

            fun setDisplay(display: Boolean) {
                displayFilter = display
            }

            fun setProcessStack(process: Boolean) {
                processStack = process
            }
        }

    }

    companion object {


        internal val PARAM_BAND_LOW = "bandpass_filter_low"
        internal val PARAM_BAND_HIGH = "bandpass_filter_high"
    }


}
