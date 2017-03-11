package edu.stanford.cfuller.imageanalysistools.metric

import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageSet
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import io.kotlintest.specs.FlatSpec

class AreaAndPerimeterMetricTest : FlatSpec() {
    init {
        val ic = ImageCoordinate.createCoordXYZCT(8,8,1,1,1)
        val im = ImageFactory.createWritable(ic, 0.0f)

        // set up a 2x2 region, upper left
        (1..2).forEach { x ->
            (1..2).forEach { y ->
                ic.setCoordXYZCT(x,y,0,0,0)
                im.setValue(ic, 1.0f)
            }
        }

        // set up a 3x3 region, lower right
        (4..6).forEach { x ->
            (4..6).forEach { y ->
                ic.setCoordXYZCT(x,y,0,0,0)
                im.setValue(ic, 2.0f)
            }
        }
        val m = AreaAndPerimeterMetric()
        val imSet = ImageSet(ParameterDictionary.emptyDictionary())
        imSet.addImageWithImageAndName(im, "test image")
        val q = m.quantify(im, imSet)

        "The AreaAndPerimeterMetric" should "correctly calculate the area and perimeter of a 2x2 region" {
            val meas = q.getAllMeasurementsForRegion(1)

            meas.forEach { m ->
                if (m.measurementName == "perimeter") {
                    m.measurement shouldBe 4.0
                }
                if (m.measurementName == "area") {
                    m.measurement shouldBe 4.0
                }
            }
        }

        "The AreaAndPerimeterMetric" should "correctly calculate the area and perimeter of a 3x3 region" {
            val meas = q.getAllMeasurementsForRegion(2)
            meas.forEach { m ->
                if (m.measurementName == "perimeter") {
                    m.measurement shouldBe 8.0
                }
                if (m.measurementName == "area") {
                    m.measurement shouldBe 9.0
                }
            }
        }
    }
}