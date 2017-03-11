package edu.stanford.cfuller.analysistoolsinterface

import edu.stanford.cfuller.imageanalysistools.frontend.AnalysisController
import edu.stanford.cfuller.imageanalysistools.frontend.DataSummary
import edu.stanford.cfuller.imageanalysistools.method.Method
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory
import java.io.File
import java.io.IOException
import java.util.prefs.Preferences
import javax.swing.DefaultComboBoxModel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

/**

 * @author cfuller
 */
class SegmentationController : TaskController(), OmeroListener {
    internal var sw: SegmentationWindow = SegmentationWindow(this)
    internal var omeroImageIds: List<Long> = listOf()
    internal var omeroBrowser: OmeroBrowsingWindowController? = null

    override fun startTask() {
        this.omeroBrowser = null
        sw = SegmentationWindow(this)
        sw.addWindowListener(this)
        initializeMethods()
        val imageFilename = Preferences.userNodeForPackage(this::class.java).get("imageFile", "")
        val parameterFilename = Preferences.userNodeForPackage(this::class.java).get("parameterFile", "")
        val selectedMethodIndex = Preferences.userNodeForPackage(this::class.java).getInt("selectedIndex", 0)
        sw.imageFilename = imageFilename
        sw.parameterFilename = parameterFilename
        sw.selectedMethodIndex = selectedMethodIndex
        LoggingUtilities.addHandler(sw.logHandler)
        this.sw.status = STATUS_READY
        sw.isVisible = true
    }

    override fun imageIdsHaveBeenSelected(ids: List<Long>) {
        this.omeroImageIds = ids
        this.sw.useOmeroServer = true
    }

    fun browseForParameterFile() {
        val path = this.sw.parameterFilename
        var fc: JFileChooser? = null
        if (File(path).exists()) {
            fc = JFileChooser(path)
        } else {
            fc = JFileChooser()
        }
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.addChoosableFileFilter(FileNameExtensionFilter("Parameter files (.xml, .rb)", "xml", "rb"))
        val retVal = fc.showOpenDialog(this.sw)


        if (retVal == JFileChooser.APPROVE_OPTION) {

            val selected = fc.selectedFile.absolutePath


            sw.parameterFilename = selected
        }
    }

    fun browseForImageFileOrDirectory() {
        val path = this.sw.imageFilename
        var fc: JFileChooser? = null
        if (File(path).exists()) {
            fc = JFileChooser(path)
        } else {
            fc = JFileChooser()
        }
        fc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES

        val retVal = fc.showOpenDialog(this.sw)

        if (retVal == JFileChooser.APPROVE_OPTION) {

            val selected = fc.selectedFile.absolutePath


            sw.imageFilename = selected
        }
    }


    fun onMethodSelect() {
        val index = this.sw.selectedMethodIndex

        val mi = this.sw.methodComboBoxModel.getElementAt(index) as MethodInfo

        if (mi.methodClass == null) {
            this.sw.setCustomMethodFieldEnabled(true)
        } else {
            this.sw.setCustomMethodFieldEnabled(false)
        }

    }

    fun useOmeroDataSource() {
        try {
            Class.forName("omero.api.GatewayPrx")
        } catch (e: ClassNotFoundException) {
            LoggingUtilities.warning("Could not open OMERO data source; OMERO client plugin missing.")
            this.sw.status = STATUS_OMERO_ERR + STATUS_READY
            this.sw.disableOmero()
            return
        }

        this.omeroBrowser = OmeroBrowsingWindowController()
        omeroBrowser!!.openNewBrowsingWindow(this)
    }

    fun runButtonPressed() {

        if (!(this.sw.status == STATUS_READY || this.sw.status == STATUS_OMERO_ERR + STATUS_READY)) return


        val parameterFilename = this.sw.parameterFilename
        val imageFilename = this.sw.imageFilename

        Preferences.userNodeForPackage(this.javaClass).put("imageFile", imageFilename)
        Preferences.userNodeForPackage(this.javaClass).put("parameterFile", parameterFilename)
        Preferences.userNodeForPackage(this.javaClass).putInt("selectedIndex", this.sw.selectedMethodIndex)

        this.sw.status = STATUS_PROCESSING

        java.awt.EventQueue.invokeLater(Runnable {
            val mi = sw.methodComboBoxModel.getElementAt(sw.selectedMethodIndex) as MethodInfo

            var c: Class<*>? = mi.methodClass

            val am = AnalysisMetadataParserFactory.createParserForFile(parameterFilename).parseFileToAnalysisMetadata(parameterFilename)

            val pd = am.inputParameters

            if (c == null && !pd!!.hasKey("method_name")) {
                try {
                    c = Class.forName(sw.customClassName)
                } catch (e: ClassNotFoundException) {
                    LoggingUtilities.warning("Custom class not found with name: " + sw.customClassName)
                    sw.status = STATUS_READY
                    return@Runnable
                }

            }

            pd!!.addIfNotSet("method_name", c!!.name)
            if (File(imageFilename).isDirectory) {
                pd.addIfNotSet("local_directory", imageFilename)
            } else {
                val wholeFilename = File(imageFilename)
                val name = wholeFilename.name
                val dir = wholeFilename.parent
                if (!pd.hasKey("common_filename_tag")) {
                    pd.addIfNotSet("common_filename_tag", name)
                } else {
                    pd.setValueForKey("image_extension", name)
                }
                pd.addIfNotSet("local_directory", dir)
            }

            pd.addIfNotSet("max_threads", Integer.toString(Runtime.getRuntime().availableProcessors()))

            if (sw.useOmeroServer) {
                pd.setValueForKey("use_omero", "true")
                pd.addIfNotSet("omero_hostname", omeroBrowser!!.hostname)
                pd.addIfNotSet("omero_username", omeroBrowser!!.username)
                pd.addIfNotSet("omero_password", omeroBrowser!!.getPassword())
                var omeroImageIdString: String? = null
                for (l in omeroImageIds) {
                    if (omeroImageIdString == null) {
                        omeroImageIdString = ""
                    } else {
                        omeroImageIdString += " "
                    }
                    omeroImageIdString += java.lang.Long.toString(l)
                }

                pd.addIfNotSet("omero_image_ids", omeroImageIdString!!)
            }


            Thread(Runnable {
                if (!sw.summarizeDataOnly()) {
                    val ac = AnalysisController()
                    ac.addAnalysisLoggingHandler(sw.logHandler)
                    ac.runLocal(am)
                }

                sw.status = STATUS_SUMMARY

                try {
                    DataSummary.SummarizeData(pd.getValueForKey("local_directory") + File.separator + AnalysisController.DATA_OUTPUT_DIR, pd.getValueForKey("local_directory") + File.separator + AnalysisController.PARAMETER_OUTPUT_DIR)
                } catch (e: IOException) {
                    LoggingUtilities.severe("Encountered error while summarizing data.")
                }

                sw.status = STATUS_READY
            }).start()
        })
    }

    private class MethodInfo(displayName: String, className: String?) {
        var displayName: String
            internal set
        var methodClass: Class<*>? = null
            internal set

        init {
            this.displayName = displayName
            if (className != null) {
                try {
                    this.methodClass = Class.forName(className)
                } catch (e: ClassNotFoundException) {
                    LoggingUtilities.warning("Valid class not found for method with name: " + displayName)
                    this.methodClass = null
                }

            } else {
                this.methodClass = null
            }
        }

        override fun toString(): String {
            return this.displayName
        }

    }

    private fun initializeMethods() {
        val model = sw.methodComboBoxModel
        val methodResourceLocation = this.javaClass.classLoader.getResource(METHOD_XML_FILENAME)!!.toString()
        var methodNodes: NodeList? = null

        try {
            methodNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(methodResourceLocation).getElementsByTagName(METHOD_XML_TAG)
        } catch (e: SAXException) {
            LoggingUtilities.severe("Encountered exception while parsing tasks xml file.")
            return
        } catch (e: ParserConfigurationException) {
            LoggingUtilities.severe("Incorrectly configured xml parser.")
            return
        } catch (e: IOException) {
            LoggingUtilities.severe("Exception while reading xml file.")
            return
        }

        for (i in 0..methodNodes!!.length - 1) {
            val n = methodNodes.item(i)
            val displayName = n.attributes.getNamedItem(DISPLAY_ATTR_NAME)
            val className = n.attributes.getNamedItem(CLASS_ATTR_NAME)
            model.addElement(MethodInfo(displayName?.nodeValue!!, className?.nodeValue).toString())
        }
    }

    companion object {
        internal val METHOD_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/methods.xml"
        internal val METHOD_XML_TAG = "method"
        internal val DISPLAY_ATTR_NAME = "displayname"
        internal val CLASS_ATTR_NAME = "class"
        internal val STATUS_PROCESSING = "Processing"
        internal val STATUS_READY = "Ready"
        internal val STATUS_SUMMARY = "Making data summary"
        internal val STATUS_OMERO_ERR = "OMERO missing; "
    }
}
