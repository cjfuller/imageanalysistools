package edu.stanford.cfuller.imageanalysistools.filter

import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import io.kotlintest.specs.FlatSpec
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.URL
import java.security.MessageDigest
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import kotlin.reflect.full.createInstance

val PLANAR_IMAGE_URL = "https://s3-us-west-1.amazonaws.com/imageanalysistools/planar_test_image_smaller.ome.tif"
val PLANAR_MASK_URL = "https://s3-us-west-1.amazonaws.com/imageanalysistools/planar_test_mask_smaller.ome.tif"

fun readImageFromURL(url: String): Image? {
    val data = URL(url).readBytes()
    println(data.size)
    val f = createTempFile()
    f.deleteOnExit()
    f.writeBytes(data)
    println(f.absolutePath)
    return ImageReader().read(f.absolutePath)
}

fun hashImageContent(im: Image): String {
    val bytes = ByteArrayOutputStream()
    val content = DataOutputStream(bytes)
    im.dimensionSizes.forEach { d ->
        content.writeFloat(im.dimensionSizes[d].toFloat())
    }
    im.forEach { ic ->
        content.writeFloat(im.getValue(ic))
    }
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(bytes.toByteArray())
    return HexBinaryAdapter().marshal(digest.digest()).toLowerCase()
}

val image = readImageFromURL(PLANAR_IMAGE_URL)!!
val mask = readImageFromURL(PLANAR_MASK_URL)!!

class Filter2DRegressionTest : FlatSpec() {
    init {
        val params = ParameterDictionary.emptyDictionary()
        val refIm = ImageFactory.createWritable(image)
        params.setValueForKey("min_size", "20")
        params.setValueForKey("max_size", "40")
        params.setValueForKey("bandpass_filter_low", "3")
        params.setValueForKey("bandpass_filter_high", "10")

        FILTER_REGRESSION_HASHES.forEach { (filterClass, hashes) ->
            (filterClass.simpleName ?: "Unknown class") should "not have regressed" {
                val f = filterClass.createInstance()
                f.referenceImage = refIm
                f.params = params
                hashes["image"]?.let {
                    val im = ImageFactory.createWritable(image)
                    f.apply(im)
                    val hash = hashImageContent(im)
                    hash shouldEqual it
                }
                hashes["mask"]?.let {
                    val im = ImageFactory.createWritable(mask)
                    f.apply(im)
                    val hash = hashImageContent(im)
                    hash shouldEqual it
                }
            }
        }
    }
}