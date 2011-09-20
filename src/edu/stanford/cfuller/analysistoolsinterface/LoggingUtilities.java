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


package edu.stanford.cfuller.analysistoolsinterface;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 *
 * @author cfuller
 */
public class LoggingUtilities {

    static final String LOGGER_NAME="edu.stanford.cfuller.analysistoolsinterface";

    static Logger loggerRef;
    
    static {
        loggerRef = java.util.logging.Logger.getLogger(LOGGER_NAME);
    }

    public static void log(String message) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message);

    }

    public static void info(String message) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).info(message);

    }

    public static void warning(String message) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).warning(message);

    }

    public static void severe(String message) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).severe(message);

    }

    public static void addHandler(Handler h) {
        java.util.logging.Logger.getLogger(LOGGER_NAME).addHandler(h);
    }

}
