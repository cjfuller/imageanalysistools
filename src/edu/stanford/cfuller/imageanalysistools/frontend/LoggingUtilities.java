/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.frontend;

import java.io.OutputStream;
import java.util.logging.*;

/**
 * Wraps a logging system to provide simple methods for all classes in the imageanalysistools package to get a uniform logger, control its
 * level of logging detail, and add handlers.
 *
 * @author Colin J. Fuller
 */
public class LoggingUtilities {

    static java.util.List<Handler> addedHandlers;

    static Logger loggerReference = null;

    public static final String LOGGER_NAME = "edu.stanford.cfuller.imageanalysistools";

    static {
        addedHandlers = new java.util.Vector<Handler>();
        loggerReference = java.util.logging.Logger.getLogger(LOGGER_NAME);
    }

    /**
     * Modifies the logging detail level of the handlers associated with this package.
     * @param l     The new level of logging detail.
     */
    protected static void setHandlerLevels(Level l) {
        for (Handler h : addedHandlers) {h.setLevel(l);}
    }

    /**
     * Adds a handler that will log all messages to a given Output Stream.
     * @param o     The OutputStream to which to log.
     */
    public static void logToStream(OutputStream o) {

        StreamHandler sh = new StreamHandler(o, new SimpleFormatter());
        addedHandlers.add(sh);

        getLogger().addHandler(sh);
        
    }

    /**
     * Adds a handler to the logger for this package.
     * @param h The handler to add.
     */
    public static void addHandler(Handler h) {

        addedHandlers.add(h);
        getLogger().addHandler(h);

        
    }

    /**
     * Causes all messages to
     */
    public static void logDebugMessages() {

        getLogger().setLevel(Level.ALL);
        setHandlerLevels(Level.ALL);
    }

    public static void hideDebugMessages() {

        getLogger().setLevel(Level.INFO);
        setHandlerLevels(Level.INFO);


    }

    public static void logWarningsAndErrorsOnly() {

        getLogger().setLevel(Level.WARNING);
        setHandlerLevels(Level.WARNING);

    }

    public static void removeAllAddedLoggingStreams() {

        for (Handler h : addedHandlers) {

            getLogger().removeHandler(h);

            
        }
        
        addedHandlers.clear();

    }

    public static Logger getLogger() {
        return java.util.logging.Logger.getLogger(LOGGER_NAME);
    }


}
