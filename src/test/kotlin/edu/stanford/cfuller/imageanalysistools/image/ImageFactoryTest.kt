package edu.stanford.cfuller.imageanalysistools.image

import io.kotlintest.specs.FlatSpec
import java.awt.image.BufferedImage

class ImageFactoryTest : FlatSpec() {
    init {
        "An ImageFactory" should "be able to initialize an image given dimension sizes and an initial value" {
            val im = ImageFactory.createWritable(ImageCoordinate.createCoordXYZCT(2, 2, 2, 2, 2), 3.0f)
            im.dimensionSizes.forEach { c -> im.dimensionSizes[c] shouldBe 2 }
            im.forEach { ic -> im.getValue(ic) shouldBe 3.0f }
        }

        "An ImageFactory" should "be able to initialize an image from a BufferedImage" {
            val buf_im = BufferedImage(10, 10, BufferedImage.TYPE_USHORT_GRAY)
            buf_im.raster.setSample(3, 3, 0, 2)
            val im = ImageFactory.create(buf_im)
            im.getValue(ImageCoordinate.createCoordXYZCT(3, 3, 0, 0, 0)) shouldBe 2.0f
        }

        "An ImageFactory" should "be able to initialize an image from an ImagePlus" {
            val imp = ImageFactory.create(
                    ImageCoordinate.createCoordXYZCT(10, 10, 10, 10, 10), 3.0f)
                    .toImagePlus()
            val im = ImageFactory.create(imp)
            im.getValue(ImageCoordinate.createCoordXYZCT(1, 1, 1, 1, 1)) shouldBe 3.0f
        }

        "An ImageFactory" should "be able to initialize an image given pixel data and metadata" {
            val im = ImageFactory.create(ImageCoordinate.createCoordXYZCT(10, 10, 10, 10, 10), 3.0f)
            val im_2 = ImageFactory.create(im.metadata, im.pixelData)
            im_2.getValue(ImageCoordinate.createCoordXYZCT(1, 1, 1, 1, 1)) shouldBe 3.0f
        }
    }
}

class ImageFactoryCopyingTest: FlatSpec() {
    var im = ImageFactory.createWritable(ImageCoordinate.createCoordXYZCT(64,64,10,3,10), 0.0f)
    var coord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0)
    override fun beforeEach() {
        im = ImageFactory.createWritable(ImageCoordinate.createCoordXYZCT(64,64,10,3,10), 0.0f)
        coord = ImageCoordinate.createCoordXYZCT(0,0,0,0,0)
    }
    init {
        "An ImageFactory" should " be able to deep copy an image" {
            val imCp = ImageFactory.create(im)
            im.setValue(coord, 1.0f)
            imCp.getValue(coord) shouldBe 0.0f
        }

        "An ImageFactory" should " be able to deep copy an image as a writable image" {
            val imCp = ImageFactory.createWritable(im)
            imCp.setValue(coord, 1.0f)
            im.getValue(coord) shouldBe 0.0f
        }

        "An ImageFactory" should " be able to shallow copy an image" {
            val imCp = ImageFactory.createShallow(im)
            im.setValue(coord, 1.0f)
            imCp.getValue(coord) shouldBe 1.0f
        }

        "An ImageFactory" should " copy a non-writable image when getting a writable instance" {
            val imRO = ImageFactory.create(im)
            val imWrit = ImageFactory.writableImageInstance(imRO)
            imWrit.setValue(coord, 1.0f)
            imRO.getValue(coord) shouldBe 0.0f
        }
    }
}