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
import java.util.ArrayList
import java.util.HashMap
import java.util.prefs.Preferences

import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.GroupLayout
import javax.swing.GroupLayout.Alignment
import javax.swing.JScrollPane
import javax.swing.JList
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Rectangle
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.LayoutStyle.ComponentPlacement
import javax.swing.JButton
import java.awt.event.ActionListener
import java.awt.event.ActionEvent

/**
 * @author cfuller
 */
class FilterSelectionFrame(internal var psc: ParameterSetupController) : JFrame() {
    internal var parameterList: JList<*>

    protected fun addAdditionalFilters() {
        val jfc = JFileChooser(Preferences.userNodeForPackage(this.javaClass).get("additional_filters_filename", ""))
        jfc.isMultiSelectionEnabled = false
        jfc.fileSelectionMode = JFileChooser.FILES_ONLY
        val result = jfc.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            val filename = jfc.selectedFile.absolutePath
            Preferences.userNodeForPackage(this.javaClass).put("additional_filters_filename", filename)
            populateFilterList(filename, false)
            parameterList.setListData(filters.toTypedArray())
        }
    }

    init {
        preferredSize = Dimension(400, 300)
        bounds = Rectangle(0, 22, 400, 300)

        this.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        val scrollPane = JScrollPane()

        val lblLoadAdditionalFilters = JLabel("Load additional filters from an XML file:")

        val btnLoad = JButton("Load")
        btnLoad.addActionListener { addAdditionalFilters() }
        val groupLayout = GroupLayout(contentPane)
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 400, java.lang.Short.MAX_VALUE.toInt())
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblLoadAdditionalFilters)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnLoad)
                                .addContainerGap(64, java.lang.Short.MAX_VALUE.toInt()))
        )
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblLoadAdditionalFilters)
                                        .addComponent(btnLoad))
                                .addContainerGap(11, java.lang.Short.MAX_VALUE.toInt()))
        )

        parameterList = JList()
        parameterList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(arg0: MouseEvent?) {
                val selected = parameterList.selectedValue as String
                psc.addSelectedFilter(selected, filterLookupByName[selected])
                isVisible = false
                dispose()
            }
        })
        scrollPane.setViewportView(parameterList)
        contentPane.layout = groupLayout
        parameterList.setListData(filters.toTypedArray())
    }

    companion object {

        private val serialVersionUID = 1L

        internal var filters: MutableList<String>
        internal var filterLookupByName: MutableMap<String, String>

        internal val FILTERS_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/filters.xml"
        internal val FILTER_TAG_NAME = "filter"
        internal val NAME_ATTR = "displayname"
        internal val CLASS_ATTR = "class"

        init {

            filters = ArrayList<String>()
            filterLookupByName = HashMap<String, String>()

            populateFilterList(FILTERS_XML_FILENAME, true)

        }

        protected fun populateFilterList(filename: String, isResource: Boolean) {

            var taskDoc: Document? = null

            var taskURLString: String? = null

            if (isResource) {
                if (ij.IJ.getInstance() != null) {
                    taskURLString = ij.IJ.getClassLoader().getResource(filename)!!.toString()
                } else {
                    taskURLString = ClassLoader.getSystemClassLoader().getResource(filename)!!.toString()
                }
            } else {
                taskURLString = filename
            }

            try {
                taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskURLString)
            } catch (e: SAXException) {
                LoggingUtilities.severe("Encountered exception while parsing filters xml file.")
                e.printStackTrace()
                return
            } catch (e: ParserConfigurationException) {
                LoggingUtilities.severe("Incorrectly configured xml parser.")
                e.printStackTrace()
                return
            } catch (e: IOException) {
                LoggingUtilities.severe("Exception while reading xml file.")
                e.printStackTrace()
                return
            }

            val tasks = taskDoc!!.getElementsByTagName(FILTER_TAG_NAME)

            for (i in 0..tasks.length - 1) {

                val n = tasks.item(i)

                val filterName = n.attributes.getNamedItem(NAME_ATTR).nodeValue

                val filterClass = n.attributes.getNamedItem(CLASS_ATTR).nodeValue

                filterLookupByName.put(filterName, filterClass)

                filters.add(filterName)


            }
        }
    }
}
