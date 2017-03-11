package edu.stanford.cfuller.imageanalysistools.util

import io.kotlintest.specs.FlatSpec
import java.util.*

class Base64BinaryAdapterTest : FlatSpec() {
    private var adapter: Base64BinaryAdapter = Base64BinaryAdapter()
    init {
        "A short string" should "round-trip encode/decode" {
            val t = "This is a test."
            val b = t.toByteArray()
            val encoded = this.adapter.marshal(b)
            val out = this.adapter.unmarshal(encoded)
            b.indices.forEach { out[it] shouldEqual b[it] }
        }
        "A long string" should "round-trip encode/decode" {
            val merchantOfVenice = """
                |How sweet the moonlight sleeps upon this bank!
                |Here will we sit and let the sounds of music
                |Creep in our ears: soft stillness and the night
                |Become the touches of sweet harmony.
                |Sit, Jessica. Look how the floor of heaven
                |Is thick inlaid with patines of bright gold:
                |There's not the smallest orb which thou behold'st
                |But in his motion like an angel sings,
                |Still quiring to the young-eyed cherubins;
                |Such harmony is in immortal souls;
                |But whilst this muddy vesture of decay
                |Doth grossly close it in, we cannot hear it.
            """.trimMargin()
            val b = merchantOfVenice.toByteArray()
            val encoded = this.adapter.marshal(b)
            val out = this.adapter.unmarshal(encoded)
            b.indices.forEach { out[it] shouldEqual b[it] }
        }
        "Streams of random byte values" should "round-trip encode/decode" {
            val rng = Random(System.currentTimeMillis())
            val minLength = 100
            val maxLength = 1 shl 16
            val n_tests = 100

            for (i in 0..n_tests - 1) {
                var length = rng.nextInt()
                if (length < 0) length *= -1
                if (length < minLength) length = minLength
                if (length > maxLength) length = maxLength
                val testBytes = ByteArray(length)
                rng.nextBytes(testBytes)
                val encoded = this.adapter.marshal(testBytes)
                val out = this.adapter.unmarshal(encoded)
                testBytes.indices.forEach { out[it] shouldEqual testBytes[it] }
            }
        }
    }
}

