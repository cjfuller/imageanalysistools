/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */


package edu.stanford.cfuller.analysistoolsinterface

import java.util.logging.Handler
import java.util.logging.Logger

/**

 * @author cfuller
 */
object LoggingUtilities {

    internal val LOGGER_NAME = "edu.stanford.cfuller.analysistoolsinterface"

    internal var loggerRef: Logger

    init {
        loggerRef = java.util.logging.Logger.getLogger(LOGGER_NAME)
    }

    fun log(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message)

    }

    fun info(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message)

    }

    fun warning(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).warning(message)

    }

    fun severe(message: String) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).severe(message)

    }

    fun addHandler(h: Handler) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).addHandler(h)
    }

}
