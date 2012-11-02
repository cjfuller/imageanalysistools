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


import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLParser;
import edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser;
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter;
import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author cfuller
 */
public class ParameterSetupController extends TaskController {

    final static String DEFAULT_FILENAME = "default.xml";
    
	public static final String P_FILTER_ALL = "filter_all";
	public static final String P_FILTER_ADD = "filter_add";
	public static final String P_METRIC = "metric_name";
	public static final String P_METRIC_ADD = "metric_add";
	
    ParameterWindow pw;

    List<Parameter> availableParameters;

    Vector<Parameter> parametersInUse;

    public ParameterSetupController() {
        availableParameters = new Vector<Parameter>();
        parametersInUse = new Vector<Parameter>();
    }

    @Override
    public void startTask() {


        this.pw = new ParameterWindow(this);

        availableParameters = new Vector<Parameter>();

		ParameterDictionary pdKnown = (new LegacyParameterXMLParser()).parseKnownParametersToParameterList();

		for (String key: pdKnown.getKeys()) {
			availableParameters.add(pdKnown.getParameterForKey(key, 0));
		}

        java.util.LinkedList<Object> objectParameters = new java.util.LinkedList<Object>();
        objectParameters.addAll(availableParameters);

        pw.setAvailableParameters(objectParameters);

        String startingDir = Preferences.userNodeForPackage(this.getClass()).get("parameterFile", "");

        if (startingDir != "" && (new File(startingDir)).exists()) {
            startingDir = (new File(startingDir)).getParent();
        } else {
        	startingDir = "";
        }

        pw.setParameterFile(startingDir);

        pw.setVisible(true);

        pw.addWindowListener(this);

    }

    public void addSelectedFilter(String name, String classname) {
    	
    	Parameter p = new Parameter(P_FILTER_ALL, name, ParameterType.STRING_T, classname, null);
    	
    	this.useParameter(p);
    	
    }
    
    public void addSelectedMetric(String name, String classname) {
    	
    	Parameter p = new Parameter(P_METRIC, name, ParameterType.STRING_T, classname, null);
    	
    	this.useParameter(p);
    }

    public void useParameter(Object parameter) {

        Parameter p = (Parameter) parameter;
        
		
		if (p.getName().equals(P_FILTER_ADD)) {
			FilterSelectionFrame sel = (new FilterSelectionFrame(this));
			sel.setVisible(true);
			return;
		}
		
		if (p.getName().equals(P_METRIC_ADD)) {
			MetricSelectionFrame sel = (new MetricSelectionFrame(this));
			sel.setVisible(true);
			return;
		}
		
        

        DefaultListModel dlm = pw.getInUseParametersModel();

        dlm.addElement(new Parameter(p));

        pw.selectMostRecentlyAddedInUseParameter();

    }

    public void shouldUpdateCurrentlyUsedParameter(Object o) {
        Parameter p = (Parameter) o;
        p.setName(pw.getCurrentlySelectedName());
        p.setType(pw.getCurrentlySelectedType());
        p.setValue(pw.getCurrentlySelectedValue());
        if (p.getDisplayName().isEmpty()) p.setDisplayName(p.getName());
    }

    public void processNewParameterSelection(Object o) {
        Parameter p = (Parameter) o;

        pw.setCurrentlySelectedName(p.getName());
        if (p.getValue() != null) {
            pw.setCurrentlySelectedValue(p.getValue().toString());
        } else {
            pw.setCurrentlySelectedValue("");
  		}

        pw.setNameFieldEnabled(true);

        pw.setCurrentlySelectedType(p.getType(), true);
        
    }

    public void upButtonPressed() {
        moveSelectedElement(-1);
    }

    public void downButtonPressed() {
        moveSelectedElement(1);
    }

    private void moveSelectedElement(int offset) {
        DefaultListModel dlm = pw.getInUseParametersModel();

        int selected = pw.getCurrentlySelectedInUseParameterIndex();

        Object toMove = dlm.remove(selected);

        int newIndex = selected + offset;
        while(newIndex > dlm.getSize()) {newIndex--;}
        while(newIndex < 0) {newIndex++;}

        dlm.add(newIndex, toMove);
        pw.setCurrentlySelectedInUseParamter(newIndex);
    }

    public void browseButtonPressed() {
        String path = this.pw.getParameterFile();
        JFileChooser fc = null;
        if ((new File(path)).exists()) {
            fc = new JFileChooser(path);
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        fc.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        fc.setFileHidingEnabled(true);
        int retVal = fc.showDialog(this.pw, "OK");


        if (retVal == JFileChooser.APPROVE_OPTION) {

            String selected = fc.getSelectedFile().getAbsolutePath();

            if (!selected.toLowerCase().endsWith(".xml")) {
                selected+= ".xml";
            }

            pw.setParameterFile(selected);

            if ((new File(selected)).exists()) {

                pw.getInUseParametersModel().clear();

                ParameterDictionary pd = (new AnalysisMetadataXMLParser()).parseFileToParameterDictionary(selected);
                for (String key : pd.getKeys()) {
					int count = pd.getValueCountForKey(key);
					for (int i = 0; i < count; i++) {
						this.useParameter(pd.getParameterForKey(key, i));
					}
                }
            }

        }

    }

    public void removeButtonPressed() {
        DefaultListModel dlm = pw.getInUseParametersModel();

        int selected = pw.getCurrentlySelectedInUseParameterIndex();

        Object toMove = dlm.remove(selected);
    }

    public void cancelButtonPressed() {
        this.pw.dispose();
    }

    public void doneButtonPressed() {
        AnalysisMetadataXMLWriter pxw = new AnalysisMetadataXMLWriter();

        Object[] parameters = pw.getInUseParametersModel().toArray();

		ParameterDictionary pd = ParameterDictionary.emptyDictionary();

        for (Object o : parameters) {
            pd.addParameter((Parameter) o);
        }


        String parameterFile = pw.getParameterFile();

        if ((new File(parameterFile)).isDirectory()) {
            parameterFile += File.separator + DEFAULT_FILENAME;
        }

        pxw.writeParameterDictionaryToXMLFile(pd, parameterFile);

        Preferences.userNodeForPackage(this.getClass()).put("parameterFile", parameterFile);

        this.pw.dispose();

    }


}
