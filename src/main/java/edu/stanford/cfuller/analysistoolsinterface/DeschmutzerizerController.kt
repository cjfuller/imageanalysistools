package edu.stanford.cfuller.analysistoolsinterface

import edu.stanford.cfuller.imageanalysistools.frontend.AnalysisController
import edu.stanford.cfuller.imageanalysistools.frontend.DataSummary
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader
import edu.stanford.cfuller.imageanalysistools.metric.Measurement
import edu.stanford.cfuller.imageanalysistools.metric.Quantification
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory
import ij.ImagePlus
import ij.gui.ImageWindow
import ij.gui.Roi
import ij.process.ImageConverter
import ij.process.ImageProcessor

import java.awt.Dimension
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import java.util.HashSet
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.JFrame

/**

 * @author cfuller
 */
class DeschmutzerizerController : TaskController() {

    internal var dw: DeschmutzerizerWindow
    internal var dieh: DeschmutzerizerInputEventHandler

    internal var originalImageWindow: ImageWindow? = null
    internal var maskWindow: ImageWindow? = null

    internal var currentOriginalImage: Image
    internal var currentMaskImage: Image
    internal var currentLabeledMaskImage: Image

    internal var selectedRegions: HashSet<Int>

    internal var colorCodedMaskDisplay: ImagePlus
    internal var originalImageDisplay: ImagePlus

    internal var filenamesToProcess: MutableList<String>

    internal var currentMaskFilename: String? = null
    internal var currentDataFilename: String? = null
    internal var currentLabeledMaskFilename: String? = null
    internal var currentParametersFilename: String? = null
    internal var lastParametersProcessed: String? = null
    internal var lastDataFilenameProcessed: String? = null

    internal var currentParameters: ParameterDictionary? = null

    fun shouldDrawMouseDragBox(shouldDraw: Boolean) {

        //        if (shouldDraw) {
        //            this.originalImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());
        //            this.maskImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());
        //
        //        } else {
        //            this.originalImageDisplay.clearSelectionBox();
        //            this.maskImageDisplay.clearSelectionBox();
        //
        //        }

    }

    fun processBox(start: Point, end: Point) {

        var regionNumberCheck: Image? = null

        if (this.dw.groupSelectionSelected()) {
            regionNumberCheck = this.currentMaskImage
        } else {
            regionNumberCheck = this.currentLabeledMaskImage
        }

        var xLower = (if (start.getX() < end.getX()) start.getX() else end.getX()).toInt()
        var yLower = (if (start.getY() < end.getY()) start.getY() else end.getY()).toInt()

        var width = Math.abs(start.getX() - end.getX()).toInt()
        var height = Math.abs(start.getY() - end.getY()).toInt()

        if (xLower < 0) {
            width += xLower
            if (width < 0) width = 0
            xLower = 0
        }

        if (yLower < 0) {
            height += yLower
            if (height < 0) height = 0
            yLower = 0
        }

        if (xLower + width > regionNumberCheck.dimensionSizes[ImageCoordinate.X]) {
            width = regionNumberCheck.dimensionSizes[ImageCoordinate.X] - xLower
        }

        if (yLower + height > regionNumberCheck.dimensionSizes[ImageCoordinate.Y]) {
            height = regionNumberCheck.dimensionSizes[ImageCoordinate.Y] - yLower
        }


        val posCZT = this.colorCodedMaskDisplay.convertIndexToPosition(this.colorCodedMaskDisplay.slice)

        val startCoord = ImageCoordinate.createCoordXYZCT(xLower, yLower, posCZT[1] - 1, posCZT[0] - 1, posCZT[2] - 1)
        val endCoord = ImageCoordinate.createCoordXYZCT(xLower + width + 1, yLower + height + 1, posCZT[1], posCZT[0], posCZT[2])

        regionNumberCheck.setBoxOfInterest(startCoord, endCoord)

        val tempSelectedRegions = java.util.HashSet<Int>()

        for (ic in regionNumberCheck) {
            if (regionNumberCheck.getValue(ic) > 0) {
                tempSelectedRegions.add(regionNumberCheck.getValue(ic).toInt())
            }
        }


        startCoord.recycle()
        endCoord.recycle()

        regionNumberCheck.clearBoxOfInterest()

        var x0 = regionNumberCheck.dimensionSizes[ImageCoordinate.X]
        var xf = 0
        var y0 = regionNumberCheck.dimensionSizes[ImageCoordinate.Y]
        var yf = 0

        val nextSelectedRegions = java.util.HashSet<Int>()

        nextSelectedRegions.addAll(this.selectedRegions)

        val red = intArrayOf(255, 0, 0)
        val white = intArrayOf(255, 255, 255)

        val currentSlice = this.colorCodedMaskDisplay.slice

        for (ic in regionNumberCheck) {

            val x = ic[ImageCoordinate.X]
            val y = ic[ImageCoordinate.Y]
            val z = ic[ImageCoordinate.Z]
            val c = ic[ImageCoordinate.C]
            val t = ic[ImageCoordinate.T]

            if (tempSelectedRegions.contains(regionNumberCheck.getValue(ic).toInt()) && !this.selectedRegions.contains(this.currentLabeledMaskImage.getValue(ic).toInt())) {

                this.colorCodedMaskDisplay.setPositionWithoutUpdate(c + 1, z + 1, t + 1)

                val ip = this.colorCodedMaskDisplay.processor

                ip.putPixel(x, y, red)

                nextSelectedRegions.add(this.currentLabeledMaskImage.getValue(ic).toInt())

                if (x < x0) x0 = x
                if (x >= xf) xf = x + 1
                if (y < y0) y0 = y
                if (y >= yf) yf = y + 1


            } else if (tempSelectedRegions.contains(regionNumberCheck.getValue(ic).toInt()) && this.selectedRegions.contains(this.currentLabeledMaskImage.getValue(ic).toInt())) {

                this.colorCodedMaskDisplay.setPositionWithoutUpdate(c + 1, z + 1, t + 1)

                val ip = this.colorCodedMaskDisplay.processor

                ip.putPixel(x, y, white)

                nextSelectedRegions.remove(this.currentLabeledMaskImage.getValue(ic).toInt())

                if (x < x0) x0 = x
                if (x >= xf) xf = x + 1
                if (y < y0) y0 = y
                if (y >= yf) yf = y + 1

            }


        }

        this.colorCodedMaskDisplay.slice = currentSlice

        this.colorCodedMaskDisplay.updateAndRepaintWindow()


        this.selectedRegions = nextSelectedRegions


    }

    fun processSelectedBox() {


        var currentRoi: Roi? = this.colorCodedMaskDisplay.roi


        if (currentRoi == null) {

            currentRoi = this.originalImageDisplay.roi

        }

        if (currentRoi == null) return

        val secondCorner = Point(currentRoi.bounds.location)
        secondCorner.translate(currentRoi.bounds.getWidth().toInt(), currentRoi.bounds.getHeight().toInt())


        this.processBox(currentRoi.bounds.location, secondCorner)
        //this.processBox(this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseDownPoint()), this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseUpPoint()));

        this.colorCodedMaskDisplay.killRoi()
        this.originalImageDisplay.killRoi()

    }

    fun processSelectedPoint() {
        this.processBox(this.colorCodedMaskDisplay.canvas.cursorLoc, this.colorCodedMaskDisplay.canvas.cursorLoc)
    }

    override fun startTask() {

        this.currentParameters = null
        this.lastDataFilenameProcessed = null
        this.lastParametersProcessed = null

        this.selectedRegions = java.util.HashSet<Int>()

        this.dw = DeschmutzerizerWindow(this)

        this.dw.addWindowListener(this)

        this.dieh = DeschmutzerizerInputEventHandler(this)

        this.dw.imageFilename = Preferences.userNodeForPackage(this.javaClass).get("deschmutzerizer_filename", this.dw.imageFilename)

        this.dw.isVisible = true

        this.originalImageWindow = null
        this.maskWindow = null


    }

    private fun finish() {

        this.dw.setDone(true)
        this.dw.setStartButtonEnabled(true)
        if (this.lastParametersProcessed != null) {
            try {
                DataSummary.SummarizeData(File(this.getOutputFilename(this.lastDataFilenameProcessed)).parent, File(this.lastParametersProcessed!!).parent)
            } catch (e: java.io.IOException) {
                LoggingUtilities.severe("encountered exception while making data summary")
            }

        }
    }

    private fun createOutputDirectory(outputFilename: String) {
        val dirFile = File(outputFilename).parentFile
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
    }

    private fun finishCurrentImage() {


        this.originalImageWindow!!.isVisible = false
        this.maskWindow!!.isVisible = false


        val outputImage = ImageFactory.createWritable(this.currentMaskImage)
        val outputImageLabeled = ImageFactory.createWritable(this.currentLabeledMaskImage)
        val outputImageFilename = this.getOutputFilename(this.currentMaskFilename)
        val outputImageLabeledFilename = this.getOutputFilename(this.currentLabeledMaskFilename)
        this.createOutputDirectory(outputImageFilename)

        for (ic in outputImage) {
            if (this.selectedRegions.contains(this.currentLabeledMaskImage.getValue(ic).toInt())) {
                outputImage.setValue(ic, 0f)
                outputImageLabeled.setValue(ic, 0f)
            }
        }


        outputImage.writeToFile(outputImageFilename)
        outputImageLabeled.writeToFile(outputImageLabeledFilename)

        val outputDataFilename = this.getOutputFilename(this.currentDataFilename)
        this.createOutputDirectory(outputDataFilename)

        try {
            val input = BufferedReader(FileReader(this.currentDataFilename!!))

            val output = PrintWriter(FileWriter(outputDataFilename))

            var currentLine: String? = input.readLine()

            while (currentLine != null) {

                val split = currentLine.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                var regionNumber = 0

                try {

                    regionNumber = java.lang.Double.parseDouble(split[0]).toInt()

                } catch (e: NumberFormatException) {
                    currentLine = input.readLine()

                    continue
                }

                if (!this.selectedRegions.contains(regionNumber)) {
                    output.println(currentLine)
                }

                currentLine = input.readLine()

            }

            output.close()

            val data = File(this.currentDataFilename!!)


            val ois = ObjectInputStream(FileInputStream(data.parent + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX + File.separator + data.name))

            val q = ois.readObject() as Quantification

            val outputQ = Quantification()

            if (q != null) {

                for (m in q.allMeasurements) {
                    if (!this.selectedRegions.contains(m.featureID.toInt())) {
                        outputQ.addMeasurement(m)
                    }
                }

            }

            val outputData = File(outputDataFilename)

            val outputDir = File(outputData.parent + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX)

            if (!outputDir.exists()) {
                outputDir.mkdir()
            }

            val oos = ObjectOutputStream(FileOutputStream(outputData.parent + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX + File.separator + outputData.name))

            oos.writeObject(outputQ)

            oos.close()
            ois.close()

        } catch (e: java.io.IOException) {
            LoggingUtilities.warning("encountered IO exception while writing deschmutzed data")
        } catch (e: ClassNotFoundException) {
            LoggingUtilities.warning("unable to read quantification while writing deschmutzed data")
        }


    }

    private fun readCurrentMaskAndDataFilenames(nextImageToProcess: String) {

        val nextToProcessFile = File(nextImageToProcess)

        // System.out.println(nextImageToProcess);

        var parametersFilename: String? = null

        val parametersDir = nextToProcessFile.parent + File.separator + PARAMETERS_DIR

        val parametersDirFile = File(parametersDir)

        for (f in parametersDirFile.listFiles()!!) {
            if (f.name.matches((".*" + nextToProcessFile.name + ".*").toRegex())) {
                parametersFilename = f.absolutePath
                break
            }
        }

        if (parametersFilename == null) {
            this.currentParameters = null
            this.currentParametersFilename = null
            this.currentMaskFilename = null
            this.currentLabeledMaskFilename = null
            this.currentDataFilename = null
            return
        }


        val am = AnalysisMetadataParserFactory.createParserForFile(parametersFilename).parseFileToAnalysisMetadata(parametersFilename)


        val p = am.outputParameters

        this.currentParameters = p
        this.currentParametersFilename = parametersFilename

        val outputImages = am.getOutputImages()

        val maskFilename = outputImages.getImageNameForIndex(0)

        this.currentMaskFilename = maskFilename
        this.currentLabeledMaskFilename = maskFilename
        //temporarily disabling selection by cell due to bugs with the background subtraction masks
        // if (outputImages.size() > 1) {
        // 	this.currentLabeledMaskFilename = outputImages.getImageNameForIndex(1);
        // }
        this.currentDataFilename = am.outputFiles[0]

        this.lastParametersProcessed = parametersFilename
        this.lastDataFilenameProcessed = this.currentDataFilename

    }

    private fun getOutputFilename(inputFilename: String): String {

        val inputFile = File(inputFilename)

        val outputFilename = inputFile.parent + File.separator + OUTPUT_DIR + File.separator + inputFile.name
        return outputFilename

    }

    private fun processNextImage() {

        this.selectedRegions.clear()

        if (this.filenamesToProcess.isEmpty()) {
            this.finish()
            return
        }

        val nextToProcess = this.filenamesToProcess.removeAt(0)

        this.readCurrentMaskAndDataFilenames(nextToProcess)

        val maskFilename = this.currentMaskFilename
        val labeledMaskFilename = this.currentLabeledMaskFilename

        val ir = ImageReader()

        var currentImage: Image? = null
        var currentMask: Image? = null
        var currentLabeledMask: Image? = null

        var existingMask: Image? = null

        try {

            if (maskFilename == null || labeledMaskFilename == null) {
                throw java.io.IOException("could not locate mask")
            }

            currentImage = ir.read(nextToProcess)
            currentMask = ir.read(maskFilename)
            currentLabeledMask = ir.read(labeledMaskFilename)

            if (this.dw.useExistingMask()) {
                try {
                    existingMask = ir.read(this.getOutputFilename(maskFilename))
                } catch (e: Exception) {
                    LoggingUtilities.warning("encountered exception while attempting to load deschmutzerized mask, continuing without")
                    existingMask = null
                }

            }

        } catch (e: java.io.IOException) {
            LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing. " + e.message)
            this.processNextImage()
            return
        } catch (e: IllegalArgumentException) {
            LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing. " + e.message)
            this.processNextImage()
            return
        }


        this.currentOriginalImage = currentImage
        this.currentMaskImage = currentMask
        this.currentLabeledMaskImage = currentLabeledMask


        var channel = 0

        if (this.currentParameters != null && this.currentParameters!!.hasKey("marker_channel_index")) {
            channel = this.currentParameters!!.getIntValueForKey("marker_channel_index")
        }

        val display = ImageFactory.createWritable(this.currentMaskImage)

        for (ic in display) {
            if (display.getValue(ic) > 0) {
                display.setValue(ic, java.lang.Float.MAX_VALUE)
            }
        }

        val origMask = ImageFactory.createWritable(this.currentMaskImage).toImagePlus()

        this.colorCodedMaskDisplay = origMask.createHyperStack("mask", this.currentMaskImage.dimensionSizes[ImageCoordinate.C], this.currentMaskImage.dimensionSizes[ImageCoordinate.Z], this.currentMaskImage.dimensionSizes[ImageCoordinate.T], 24) //24 = RGB

        for (i in 0..this.colorCodedMaskDisplay.imageStackSize - 1) {
            this.colorCodedMaskDisplay.slice = i + 1
            origMask.slice = i + 1
            this.colorCodedMaskDisplay.processor = origMask.processor.duplicate().convertToRGB()
        }


        if (existingMask != null) {
            for (ic in existingMask) {
                if (this.currentLabeledMaskImage.getValue(ic) > 0 && existingMask.getValue(ic) == 0f) {

                    this.colorCodedMaskDisplay.setPosition(ic[ImageCoordinate.C] + 1, ic[ImageCoordinate.Z] + 1, ic[ImageCoordinate.T] + 1)
                    val ip = this.colorCodedMaskDisplay.processor
                    val red = intArrayOf(255, 0, 0)
                    ip.putPixel(ic[ImageCoordinate.X], ic[ImageCoordinate.Y], red)
                    this.selectedRegions.add(this.currentLabeledMaskImage.getValue(ic).toInt())
                }
            }
        }

        val orig = this.currentOriginalImage.toImagePlus()
        this.originalImageDisplay = orig

        this.originalImageDisplay.setPosition(channel + 1, 1, 1)

        if (this.maskWindow == null || this.originalImageWindow == null) {
            this.colorCodedMaskDisplay.show()
            maskWindow = this.colorCodedMaskDisplay.window

            orig.show()
            originalImageWindow = orig.window

            this.originalImageWindow!!.setLocationRelativeTo(this.dw)
            this.originalImageWindow!!.setLocationAndSize(this.originalImageWindow!!.location.getX().toInt(), (this.originalImageWindow!!.location.getY() + this.dw.height).toInt(), 500, (500.0 * this.currentMaskImage.dimensionSizes[ImageCoordinate.Y] / this.currentMaskImage.dimensionSizes[ImageCoordinate.X]).toInt())

            this.maskWindow!!.setLocationRelativeTo(this.dw)
            this.maskWindow!!.setLocationAndSize((this.maskWindow!!.location.getX() + this.dw.width * 1.1).toInt(), (this.maskWindow!!.location.getY() + this.dw.height / 2).toInt(), 500, (500.0 * this.currentMaskImage.dimensionSizes[ImageCoordinate.Y] / this.currentMaskImage.dimensionSizes[ImageCoordinate.X]).toInt())

            this.maskWindow!!.addMouseListener(this.dieh)
            this.maskWindow!!.canvas.addMouseListener(this.dieh)

            this.originalImageWindow!!.addMouseListener(this.dieh)
            this.originalImageWindow!!.canvas.addMouseListener(this.dieh)

        } else {

            this.maskWindow!!.setImage(this.colorCodedMaskDisplay)
            this.originalImageWindow!!.setImage(orig)
        }

        this.maskWindow!!.isVisible = true
        this.originalImageWindow!!.isVisible = true

        orig.resetDisplayRange()
        this.colorCodedMaskDisplay.resetDisplayRange()
        orig.updateAndDraw()
        this.originalImageDisplay.updateAndRepaintWindow()

    }

    fun startButtonPressed() {

        Preferences.userNodeForPackage(this.javaClass).put("deschmutzerizer_filename", this.dw.imageFilename)

        this.filenamesToProcess = java.util.LinkedList<String>()

        val toProcess = File(this.dw.imageFilename)

        if (toProcess.isDirectory) {
            for (f in toProcess.listFiles()!!) {
                this.filenamesToProcess.add(f.absolutePath)
            }
        } else {
            this.filenamesToProcess.add(toProcess.absolutePath)
        }

        this.dw.setStartButtonEnabled(false)

        this.processNextImage()


    }

    fun browseButtonPressed() {
        val path = this.dw.imageFilename
        var fc: JFileChooser? = null
        if (File(path).exists()) {
            fc = JFileChooser(path)
        } else {
            fc = JFileChooser()
        }
        fc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES

        val retVal = fc.showOpenDialog(this.dw)

        if (retVal == JFileChooser.APPROVE_OPTION) {

            val selected = fc.selectedFile.absolutePath


            dw.imageFilename = selected
        }
    }

    fun continueButtonPressed() {
        this.finishCurrentImage()
        this.processNextImage()
    }

    fun selectAllButtonPressed() {

        this.processBox(Point(0, 0), Point(this.currentOriginalImage.dimensionSizes[ImageCoordinate.X], this.currentOriginalImage.dimensionSizes[ImageCoordinate.Y]))

    }

    companion object {


        val PARAMETERS_DIR = "parameters"
        val OUTPUT_DIR = "deschmutzed"
    }


}
