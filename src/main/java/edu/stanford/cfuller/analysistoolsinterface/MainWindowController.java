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

import java.io.IOException;
import java.util.Hashtable;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author cfuller
 */
public class MainWindowController implements TaskCompletionResponder {

    final static String TASKS_XML_FILENAME="edu/stanford/cfuller/analysistoolsinterface/resources/tasks.xml";
    final static String TASK_TAG_NAME="task";
    final static String NAME_ATTRIBUTE_NAME = "name";
    final static String CONTROLLER_ATTRIBUTE_NAME="controllerclass";

    Hashtable<String, String> taskControllerLookupByName;
    MainWindow mw;

    public MainWindowController() {
        this.taskControllerLookupByName = new Hashtable<String, String>();
    }

    public void setMainWindow(MainWindow mw) {
        this.mw = mw;
    }

    public ComboBoxModel populateTaskComboBoxModel() {

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Document taskDoc = null;

        String taskURLString = this.getClass().getClassLoader().getResource(TASKS_XML_FILENAME).toString();
        
        try {
            taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskURLString);
        } catch (SAXException e) {
            LoggingUtilities.severe("Encountered exception while parsing tasks xml file.");
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            LoggingUtilities.severe("Incorrectly configured xml parser.");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            LoggingUtilities.severe("Exception while reading xml file.");
            e.printStackTrace();
            return null;
        }

        NodeList tasks = taskDoc.getElementsByTagName(TASK_TAG_NAME);

        for (int i = 0; i < tasks.getLength(); i++) {
            
            Node n = tasks.item(i);

            String taskName = n.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue();

            String controllerClass = n.getAttributes().getNamedItem(CONTROLLER_ATTRIBUTE_NAME).getNodeValue();

            taskControllerLookupByName.put(taskName, controllerClass);
            
            model.addElement(taskName);


        }



        return model;
    }

    public void onGoButtonClick(java.awt.event.ActionEvent evt) {

        Class taskControllerClass = null;
        TaskController tc = null;

        try {
            taskControllerClass= Class.forName(taskControllerLookupByName.get(this.mw.getTaskSelectorSelectedItem()));
            tc = (TaskController) taskControllerClass.newInstance();
        } catch (ClassNotFoundException e) {
            LoggingUtilities.severe("Could not find class for controller of task: " + this.mw.getTaskSelectorSelectedItem());
            return;
        } catch (InstantiationException e) {
            LoggingUtilities.severe("Could not instantiate task controller class: " + taskControllerClass.getName());
            return;
        } catch (IllegalAccessException e) {
            LoggingUtilities.severe("Could not access contructor for task controller class: " + taskControllerClass.getName());
        }

        tc.addCompletionResponder(this);

        mw.setVisible(false);

        java.awt.EventQueue.invokeLater(tc);

    }

    public void taskDidComplete(TaskController tc) {
        mw.setVisible(true);
        LoggingUtilities.log("taskCompleted");
    }


}
