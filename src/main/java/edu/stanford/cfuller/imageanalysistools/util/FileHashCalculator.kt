package edu.stanford.cfuller.imageanalysistools.util

import java.security.DigestInputStream
import java.security.MessageDigest

object FileHashCalculator {
    val ALG_SHA1 = "SHA1"
    val ALG_DEFAULT = ALG_SHA1

    @Throws(java.io.IOException::class)
    fun calculateHash(algorithm: String, filename: String): String? {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance(algorithm)
        } catch (e: java.security.NoSuchAlgorithmException) {
            edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities.logger.warning("Unable to hash with algorithm " + algorithm + ": " + e.message)
        }
        if (md == null) {
            return null
        }

        val dis = DigestInputStream(java.io.FileInputStream(filename), md)
        val b = ByteArray(1048576)
        while (dis.available() > 0) {
            dis.read(b)
        }
        md = dis.messageDigest
        val dig = md!!.digest()
        val hex = javax.xml.bind.annotation.adapters.HexBinaryAdapter().marshal(dig)
        return hex
    }
}


