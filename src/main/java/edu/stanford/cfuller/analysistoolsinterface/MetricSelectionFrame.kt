package edu.stanford.cfuller.analysistoolsinterface

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
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

/**
 * @author cfuller
 */
class MetricSelectionFrame(internal var psc: ParameterSetupController) : JFrame() {
    internal var parameterList: JList<String> = JList<String>()

    init {
        preferredSize = Dimension(400, 300)
        bounds = Rectangle(0, 22, 400, 300)

        this.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        val scrollPane = JScrollPane()
        val groupLayout = GroupLayout(contentPane)
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 450, java.lang.Short.MAX_VALUE.toInt())
        )
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 278, java.lang.Short.MAX_VALUE.toInt())
        )

        parameterList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(arg0: MouseEvent?) {
                val selected = parameterList.selectedValue
                psc.addSelectedMetric(selected, metricLookupByName[selected] ?: "Metric not found")
                isVisible = false
                dispose()
            }
        })
        scrollPane.setViewportView(parameterList)
        contentPane.layout = groupLayout
        parameterList.setListData(metrics.toTypedArray())
    }

    companion object {

        private val serialVersionUID = 1L

        internal var metrics: MutableList<String>
        internal var metricLookupByName: MutableMap<String, String>

        internal val METRICS_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/metrics.xml"
        internal val METRIC_TAG_NAME = "metric"
        internal val NAME_ATTR = "displayname"
        internal val CLASS_ATTR = "class"

        init {

            metrics = ArrayList<String>()
            metricLookupByName = HashMap<String, String>()

            populateFilterList()

        }

        protected fun populateFilterList() {

            var taskDoc: Document? = null

            var taskURLString: String? = null

            if (ij.IJ.getInstance() != null) {
                taskURLString = ij.IJ.getClassLoader().getResource(METRICS_XML_FILENAME)!!.toString()
            } else {
                taskURLString = ClassLoader.getSystemClassLoader().getResource(METRICS_XML_FILENAME)!!.toString()
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

            val tasks = taskDoc!!.getElementsByTagName(METRIC_TAG_NAME)

            for (i in 0..tasks.length - 1) {

                val n = tasks.item(i)

                val filterName = n.attributes.getNamedItem(NAME_ATTR).nodeValue

                val filterClass = n.attributes.getNamedItem(CLASS_ATTR).nodeValue

                metricLookupByName.put(filterName, filterClass)

                metrics.add(filterName)

            }
        }
    }

}
