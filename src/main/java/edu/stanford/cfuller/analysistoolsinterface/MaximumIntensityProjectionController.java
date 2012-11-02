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

import edu.stanford.cfuller.imageanalysistools.frontend.MaximumIntensityProjection;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;

/**
 *
 * @author cfuller
 */
public class MaximumIntensityProjectionController extends TaskController {

    MaximumIntensityProjectionWindow mipw;

    final static String STATUS_PROCESSING = "Processing";
    final static String STATUS_READY = "Ready";


    public MaximumIntensityProjectionController() {


    }

    public void goButtonPressed() {

        if (this.mipw.getStatus().equals(STATUS_PROCESSING)) return;

        Preferences.userNodeForPackage(this.getClass()).put("maximumIntensityProjectionDirectory", this.mipw.getDirectoryText());



            new Thread(new Runnable() {

                public void run() {
                    try {

                        mipw.setStatus(STATUS_PROCESSING);

                        if ((new File(mipw.getDirectoryText())).isDirectory()) {
                            MaximumIntensityProjection.projectDirectory(mipw.getDirectoryText());

                        } else {
                            MaximumIntensityProjection.project(mipw.getDirectoryText());
                        }

                    } catch (java.io.IOException e) {
                        LoggingUtilities.severe("Encountered IO exception while making maximum intensity projection of: " + mipw.getDirectoryText());
                    } finally {
                        mipw.setStatus(STATUS_READY);
                    }

                }}).start();

        


    }

    public void browseButtonPressed() {

        JFileChooser chooser = null;

        if (this.mipw.getDirectoryText().isEmpty()) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(this.mipw.getDirectoryText());
        }

        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        

        int retval = chooser.showOpenDialog(this.mipw);

        if (retval == JFileChooser.APPROVE_OPTION) {
            this.mipw.setDirectoryText(chooser.getSelectedFile().getAbsolutePath());
        }
        

    }

    public void startTask() {

        this.mipw = new MaximumIntensityProjectionWindow(this);
        String savedDirectory = Preferences.userNodeForPackage(this.getClass()).get("maximumIntensityProjectionDirectory", this.mipw.getDirectoryText());

        this.mipw.setDirectoryText(savedDirectory);

        this.mipw.setStatus(STATUS_READY);

        this.mipw.addWindowListener(this);

        this.mipw.setVisible(true);

    }



}
