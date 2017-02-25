/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

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


