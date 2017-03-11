package edu.stanford.cfuller.imageanalysistools.util

import javax.xml.bind.annotation.adapters.XmlAdapter

/**
 * This class is an adapter to enable writing binary data to XML
 * (or other string representations) encoded as base 64.
 *
 * @author Colin J. Fuller
 */
class Base64BinaryAdapter : XmlAdapter<String, ByteArray>() {
    // TODO(colin): java now has a base64 encoder built-in; use it
    /**
     * Encodes an array of bytes as a base-64 string.
     * @param bytes the byte array containing the data to encode.
     * *
     * @return a String containing the encoded data
     */
    override fun marshal(bytes: ByteArray): String {
        val n_pad = (3 - bytes.size % 3) % 3
        val paddedBytesLength = bytes.size + n_pad
        val sb = StringBuilder()

        run {
            var i = 0
            while (i < paddedBytesLength) {
                val b0 = bytes[i].toInt()
                var b1: Int = 0
                var b2: Int = 0
                if (i + 1 < bytes.size) {
                    b1 = bytes[i + 1].toInt()
                }
                if (i + 2 < bytes.size) {
                    b2 = bytes[i + 2].toInt()
                }

                val sixBits0 = (b0 and 0x00FF).ushr(2)
                val sixBits1 = (b0 and 0x03 shl 4) + (b1 and 0x00FF).ushr(4)
                val sixBits2 = (b1 and 0x0F shl 2) + (b2 and 0x00FF).ushr(6)
                val sixBits3 = b2 and 0x3F

                val lastSet = i + 3 >= bytes.size
                sb.append(base64Lookup[sixBits0])
                sb.append(base64Lookup[sixBits1])

                if (!lastSet || n_pad < 2) {
                    sb.append(base64Lookup[sixBits2])
                }

                if (!lastSet || n_pad < 1) {
                    sb.append(base64Lookup[sixBits3])
                }
                i += 3
            }
        }

        for (i in 0..n_pad - 1) {
            sb.append(base64Pad)
        }
        return sb.toString()
    }

    /**
     * Decodes a base-64 encoded string to a byte array.
     * @param s the base-64 encoded String
     * *
     * @return a byte array containing the decoded data
     */
    override fun unmarshal(s: String): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        var n_pad = 0
        if (s.isNotEmpty() && s.endsWith("==")) {
            n_pad = 2
        } else if (s.isNotEmpty() && s.endsWith("=")) {
            n_pad = 1
        }

        val shift_max = 18
        var shift = shift_max
        val shift_inc = 6
        var endLength = 0
        if (n_pad > 0) {
            endLength = 4
        }

        var threeBytes: Long = 0
        for (i in 0..s.length - endLength - 1) {
            val currChar = s.substring(i, i + 1)
            val lookupVal = revLookup[currChar]!!
            threeBytes += (lookupVal shl shift).toLong()
            shift -= shift_inc

            if (shift < 0) {
                shift = shift_max
                out.write((threeBytes and 0xFF0000).ushr(16).toByte().toInt())
                out.write((threeBytes and 0x00FF00).ushr(8).toByte().toInt())
                out.write((threeBytes and 0x0000FF).toByte().toInt())
                threeBytes = 0
            }
        }

        threeBytes = 0
        if (n_pad > 0) {
            shift = shift_max
            for (i in 0..3) {
                val currChar = s.substring(s.length - endLength + i, s.length - endLength + i + 1)
                if (currChar == "=") {
                    continue
                }
                val lookupVal = revLookup[currChar]!!
                threeBytes += (lookupVal shl shift).toLong()
                shift -= shift_inc
            }
            out.write((threeBytes and 0xFF0000 shr 16).toByte().toInt())

            if (n_pad == 1) {
                out.write((threeBytes and 0x00FF00 shr 8).toByte().toInt())
            }
        }
        return out.toByteArray()
    }

    companion object {
        internal val base64Lookup = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/")
        internal val base64Pad = "="
        internal var revLookup: MutableMap<String, Int> = java.util.HashMap<String, Int>()
        init {
            for (i in base64Lookup.indices) {
                revLookup.put(base64Lookup[i], i)
            }
        }
    }
}
