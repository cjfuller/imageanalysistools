package edu.stanford.cfuller.analysistoolsinterface

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import javax.swing.JLabel

/**

 * @author cfuller
 */
class ImageDisplayPanel : javax.swing.JPanel() {
    internal var currentMaskedImage: BufferedImage? = null
    internal var originalImage: BufferedImage? = null
    internal var originalRescaledImage: BufferedImage? = null
    internal var currentDisplayImage: BufferedImage? = null
    internal var selectionBox: JLabel = JLabel()
    internal var currentImageCoordinateTopLeft: Point? = null
    internal var currentDimensions: Dimension? = null
    internal var isMaskingEnabled: Boolean = false
    internal var currentImageCenter: Point

    /** Creates new form ImageDisplayPanel  */
    init {
        selectionBox.isVisible = false
        selectionBox.border = javax.swing.border.LineBorder(java.awt.Color.GREEN)
        this.add(selectionBox)
        this.currentImageCenter = Point(0, 0)
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(this.currentDisplayImage, 0, 0, this)
    }

    val currentMousePositionInImageCoordinates: Point?
        get() = this.translateDisplayPointToImagePoint(this.mousePosition)

    fun translateDisplayPointToImagePoint(p: Point?): Point? {

        if (p == null) return p

        val widthFrac = p.getX() * 1.0 / this.size.getWidth()
        val heightFrac = p.getY() * 1.0 / this.size.getHeight()

        val imageX = widthFrac * (this.currentDimensions?.getWidth() ?: 0.0) +
                (this.currentImageCoordinateTopLeft?.getX() ?: 0.0)
        val imageY = heightFrac * (this.currentDimensions?.getHeight() ?: 0.0) +
                (this.currentImageCoordinateTopLeft?.getY() ?: 0.0)

        return Point(imageX.toInt(), imageY.toInt())
    }

    fun compositeWithImage(b: BufferedImage, x0: Int, y0: Int, xf: Int, yf: Int) {

        val ar = b.alphaRaster
        val mask = b.raster
        val source = this.originalRescaledImage?.raster
        val dest = this.currentMaskedImage?.raster

        if (source == null || dest == null) { return }

        for (x in x0..xf - 1) {
            for (y in y0..yf - 1) {

                //double alpha = ar.getSample(x,y,0)*1.0/MAX_RGB;


                //double c0 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);
                //double c1 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);
                //double c2 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);


                dest.setSample(x, y, 0, source.getSample(x, y, 0))
                dest.setSample(x, y, 1, source.getSample(x, y, 0))
                dest.setSample(x, y, 2, source.getSample(x, y, 0))

            }
        }


        for (x in x0..xf - 1) {
            for (y in y0..yf - 1) {


                val alpha = ar.getSample(x, y, 0) * 1.0 / MAX_RGB


                val c0 = alpha * mask.getSample(x, y, 0) + (1 - alpha) * dest.getSample(x, y, 0)
                val c1 = alpha * 0 + (1 - alpha) * dest.getSample(x, y, 0)
                val c2 = alpha * 0 + (1 - alpha) * dest.getSample(x, y, 0)



                dest.setSample(x, y, 0, c0)
                dest.setSample(x, y, 1, c1)
                dest.setSample(x, y, 2, c2)
            }
        }
        // a hack to get it to update the zoomed Image...


        this.zoomImage(1.0, this.currentImageCenter)

    }


    fun setImage(b: BufferedImage, willBeMasked: Boolean) {

        this.originalImage = b

        var currMasked = BufferedImage(b.width, b.height, BufferedImage.TYPE_INT_ARGB)
        if (willBeMasked) {
            val originalRescaled = BufferedImage(b.width, b.height, BufferedImage.TYPE_INT_ARGB)
            this.currentMaskedImage = currMasked
            this.originalRescaledImage = originalRescaled

            for (x in 0..b.width - 1) {
                for (y in 0..b.height - 1) {
                    originalRescaled.raster.setSample(x, y, 0, b.raster.getSample(x, y, 0))
                    originalRescaled.raster.setSample(x, y, 1, b.raster.getSample(x, y, 0))
                    originalRescaled.raster.setSample(x, y, 2, b.raster.getSample(x, y, 0))
                    originalRescaled.alphaRaster.setSample(x, y, 0, MAX_RGB)
                    currMasked.raster.setSample(x, y, 0, b.raster.getSample(x, y, 0))
                    currMasked.raster.setSample(x, y, 1, b.raster.getSample(x, y, 0))
                    currMasked.raster.setSample(x, y, 2, b.raster.getSample(x, y, 0))
                    currMasked.alphaRaster.setSample(x, y, 0, MAX_RGB)
                }

            }

            this.rescaleImageIntensity(originalRescaled)

        } else {
            currMasked = this.originalImage ?: currMasked
            this.currentMaskedImage = this.originalImage
            this.originalRescaledImage = this.originalImage
        }

        this.isMaskingEnabled = willBeMasked


        this.rescaleImageIntensity(currMasked)

        this.currentImageCoordinateTopLeft = Point(0, 0)
        val dims = Dimension(b.width, b.height)
        this.currentDimensions = dims

        val screenSize = java.awt.Toolkit.getDefaultToolkit().screenSize

        val widthScaleFactor = screenSize.getWidth() * 0.5 / dims.getWidth()
        val heightScaleFactor = screenSize.getHeight() * 0.5 / dims.getHeight()

        var scaleFactor = if (widthScaleFactor < heightScaleFactor) widthScaleFactor else heightScaleFactor
        if (scaleFactor > 1) scaleFactor = 1.0

        val newImageSize = Dimension((dims.getWidth() * scaleFactor).toInt(), (dims.getHeight() * scaleFactor).toInt())
        this.currentDisplayImage = this.resizeImage(currMasked, newImageSize)
        this.size = newImageSize
        this.repaint()
    }

    fun zoomImage(factor: Double, center: Point) {
        val originalImage = this.originalImage
        val currentDimensions = this.currentDimensions
        if (originalImage == null || currentDimensions == null) { return }
        this.currentImageCenter = center
        val nextDimensions = Dimension((currentDimensions.getWidth() / factor).toInt(), (currentDimensions.getHeight() / factor).toInt())
        if (nextDimensions.getWidth() > originalImage.width) nextDimensions.setSize(originalImage.width.toDouble(), nextDimensions.getHeight())
        if (nextDimensions.getHeight() > originalImage.height) nextDimensions.setSize(nextDimensions.getWidth(), originalImage.height.toDouble())

        val nextUpperLeft = Point((-1.0 * nextDimensions.getWidth() / 2.0 + center.getX()).toInt(), (-1.0 * nextDimensions.getHeight() / 2.0 + center.getY()).toInt())
        nextUpperLeft.setLocation(if (nextUpperLeft.getX() < 0) 0.0 else nextUpperLeft.getX(), if (nextUpperLeft.getY() < 0) 0.0 else nextUpperLeft.getY())

        var x0 = nextUpperLeft.getX().toInt()
        var y0 = nextUpperLeft.getY().toInt()
        var width = nextDimensions.getWidth().toInt()
        var height = nextDimensions.getHeight().toInt()

        if (width < 1 || height < 1) {
            if (originalImage.width < originalImage.height) {
                width = 1
                height = (width.toDouble() * originalImage.height.toDouble() * 1.0 / originalImage.width).toInt()
            } else {
                height = 1
                width = (height.toDouble() * originalImage.width.toDouble() * 1.0 / originalImage.height).toInt()
            }
        }

        width = (height.toDouble() * originalImage.width.toDouble() * 1.0 / originalImage.height).toInt()

        if (x0 + width > originalImage.width) {
            x0 = originalImage.width - width
        }
        if (y0 + height > originalImage.height) {
            y0 = originalImage.height - height
        }
        val nextImage = currentMaskedImage?.getSubimage(x0, y0, width, height) ?: return
        this.currentDisplayImage = this.resizeImage(nextImage, this.size)
        this.currentDimensions = Dimension(width, height)
        this.currentImageCoordinateTopLeft = Point(x0, y0)
        this.repaint()
    }

    fun resetZoom() {
        //this.setImage(this.originalImage, this.isMaskingEnabled);
        val extremelySmallSoomFactor = 1e-6
        this.zoomImage(extremelySmallSoomFactor, Point(0, 0))
    }

    private fun resizeImage(toResize: BufferedImage, newSize: Dimension): BufferedImage {
        val scaleFactorWidth = newSize.getWidth() * 1.0 / toResize.width
        val scaleFactorHeight = newSize.getHeight() * 1.0 / toResize.height
        val trans = AffineTransformOp(AffineTransform.getScaleInstance(scaleFactorWidth, scaleFactorHeight), AffineTransformOp.TYPE_BICUBIC)
        return trans.filter(toResize, null)
    }

    fun drawSelectionBox(start: Point, end: Point) {
        val x_lower = (if (start.getX() < end.getX()) start.getX() else end.getX()).toInt()
        val y_lower = (if (start.getY() < end.getY()) start.getY() else end.getY()).toInt()
        val width = Math.abs(start.getX() - end.getX()).toInt()
        val height = Math.abs(start.getY() - end.getY()).toInt()

        selectionBox.setBounds(x_lower, y_lower, width, height)
        selectionBox.isVisible = true
        this.repaint(0, 0, 0, this.width, this.height)
    }

    fun clearSelectionBox() {
        selectionBox.isVisible = false
        this.repaint()
    }

    fun rescaleImageIntensity(b: BufferedImage): BufferedImage {
        var minPossibleValue = 0
        var maxPossibleValue = 0

        if (b.type == BufferedImage.TYPE_USHORT_GRAY) {
            minPossibleValue = (MAX_GREY + 1) / 8
            maxPossibleValue = MAX_GREY - minPossibleValue
        } else {
            minPossibleValue = (MAX_RGB + 1) / 8
            maxPossibleValue = MAX_RGB - minPossibleValue
        }

        val wr = b.raster
        val max = IntArray(wr.numBands)
        java.util.Arrays.fill(max, 1)

        for (x in 0..b.width - 1) {
            for (y in 0..b.height - 1) {
                for (c in 0..wr.numBands - 1) {
                    if (wr.getSample(x, y, c) > max[c]) {
                        max[c] = wr.getSample(x, y, c)
                    }
                }
            }
        }

        for (x in 0..b.width - 1) {
            for (y in 0..b.height - 1) {
                for (c in 0..wr.numBands - 1) {
                    val offset = if (wr.getSample(x, y, c) > 0) minPossibleValue else 0
                    var newValue = maxPossibleValue * 1.0 / max[c] * wr.getSample(x, y, c) + offset
                    if (newValue > maxPossibleValue + minPossibleValue) {
                        newValue = (maxPossibleValue + minPossibleValue).toDouble()
                    }
                    wr.setSample(x, y, c, newValue)
                }
            }
        }

        return b
    }

    companion object {
        internal val serialVersionUID = 1L
        val MAX_RGB = 255
        val MAX_GREY = 65535
    }
}
