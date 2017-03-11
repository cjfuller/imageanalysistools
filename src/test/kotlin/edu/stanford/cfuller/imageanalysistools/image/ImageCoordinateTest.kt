package edu.stanford.cfuller.imageanalysistools.image

import io.kotlintest.specs.FlatSpec

class ImageCoordinateTest : FlatSpec() {
    var coord: ImageCoordinate = ImageCoordinate.createCoord()
    val dims = listOf(
            ImageCoordinate.X, ImageCoordinate.Y, ImageCoordinate.Z, ImageCoordinate.C,
            ImageCoordinate.T)
    var otherCoord: ImageCoordinate = ImageCoordinate.createCoord()

    override fun beforeEach() {
        coord = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)
        otherCoord = ImageCoordinate.createCoordXYZCT(1, 2, 3, 4, 5)
    }

    init {
        "ImageCoordinates" should "provide get access to x, y, z, c, t" {
            dims.forEach { coord[it] shouldBe 0 }
        }

        "ImageCoordinates" should "provide set access to x, y, z, c, t" {
            dims.forEach { coord[it] = 1 }
            dims.forEach { coord[it] shouldBe 1 }
        }

        "ImageCoordinates" should "be able to clone another coordinate" {
            val cloned = ImageCoordinate.cloneCoord(otherCoord)
            cloned.forEach { cloned[it] shouldBe otherCoord[it] }
        }

        "ImageCoordinates" should "be able to become a copy of another coordinate" {
            coord.setCoord(otherCoord)
            coord.forEach { coord[it] shouldBe otherCoord[it] }
        }

        "XYZCT ImageCoordinates" should "have only x, y, z, c, t" {
            dims.forEach { coord should contain(it) }
            coord.forEach { dims should contain(it) }
        }
    }
}