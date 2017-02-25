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

import java.io.IOException
import java.util.Hashtable
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

/**

 * @author cfuller
 */
class MainWindowController : TaskCompletionResponder {

    internal var taskControllerLookupByName: Hashtable<String, String>
    internal var mw: MainWindow

    init {
        this.taskControllerLookupByName = Hashtable<String, String>()
    }

    fun setMainWindow(mw: MainWindow) {
        this.mw = mw
    }

    fun populateTaskComboBoxModel(): ComboBoxModel<*>? {

        val model = DefaultComboBoxModel()

        var taskDoc: Document? = null

        val taskURLString = this.javaClass.classLoader.getResource(TASKS_XML_FILENAME)!!.toString()

        try {
            taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskURLString)
        } catch (e: SAXException) {
            LoggingUtilities.severe("Encountered exception while parsing tasks xml file.")
            e.printStackTrace()
            return null
        } catch (e: ParserConfigurationException) {
            LoggingUtilities.severe("Incorrectly configured xml parser.")
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            LoggingUtilities.severe("Exception while reading xml file.")
            e.printStackTrace()
            return null
        }

        val tasks = taskDoc!!.getElementsByTagName(TASK_TAG_NAME)

        for (i in 0..tasks.length - 1) {

            val n = tasks.item(i)

            val taskName = n.attributes.getNamedItem(NAME_ATTRIBUTE_NAME).nodeValue

            val controllerClass = n.attributes.getNamedItem(CONTROLLER_ATTRIBUTE_NAME).nodeValue

            taskControllerLookupByName.put(taskName, controllerClass)

            model.addElement(taskName)


        }



        return model
    }

    fun onGoButtonClick(evt: java.awt.event.ActionEvent) {

        var taskControllerClass: Class<*>? = null
        var tc: TaskController? = null

        try {
            taskControllerClass = Class.forName(taskControllerLookupByName[this.mw.taskSelectorSelectedItem])
            tc = taskControllerClass!!.newInstance() as TaskController
        } catch (e: ClassNotFoundException) {
            LoggingUtilities.severe("Could not find class for controller of task: " + this.mw.taskSelectorSelectedItem)
            return
        } catch (e: InstantiationException) {
            LoggingUtilities.severe("Could not instantiate task controller class: " + taskControllerClass!!.name)
            return
        } catch (e: IllegalAccessException) {
            LoggingUtilities.severe("Could not access contructor for task controller class: " + taskControllerClass!!.name)
        }

        tc!!.addCompletionResponder(this)

        mw.isVisible = false

        java.awt.EventQueue.invokeLater(tc)

    }

    override fun taskDidComplete(tc: TaskController) {
        mw.isVisible = true
        LoggingUtilities.log("taskCompleted")
    }

    companion object {

        internal val TASKS_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/tasks.xml"
        internal val TASK_TAG_NAME = "task"
        internal val NAME_ATTRIBUTE_NAME = "name"
        internal val CONTROLLER_ATTRIBUTE_NAME = "controllerclass"
    }


}
