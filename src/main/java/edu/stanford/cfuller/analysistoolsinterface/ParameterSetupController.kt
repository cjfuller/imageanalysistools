package edu.stanford.cfuller.analysistoolsinterface

import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLParser
import edu.stanford.cfuller.imageanalysistools.meta.parameters.LegacyParameterXMLParser
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter
import java.io.File
import java.util.Vector
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**

 * @author cfuller
 */
class ParameterSetupController : TaskController() {
    internal var pw: ParameterWindow = ParameterWindow(this)
    internal val availableParameters: MutableList<Parameter> = Vector<Parameter>()
    internal var parametersInUse: Vector<Parameter> = Vector<Parameter>()

    override fun startTask() {
        this.pw = ParameterWindow(this)
        availableParameters.clear()

        val pdKnown = LegacyParameterXMLParser().parseKnownParametersToParameterList()

        pdKnown.keys.forEach { key ->
            pdKnown.getParameterForKey(key, 0)?.let { availableParameters.add(it) }
        }

        pw.setAvailableParameters(availableParameters.map(Parameter::toString))
        var startingDir = Preferences.userNodeForPackage(this.javaClass).get("parameterFile", "")

        if (startingDir !== "" && File(startingDir).exists()) {
            startingDir = File(startingDir).parent
        } else {
            startingDir = ""
        }
        pw.parameterFile = startingDir
        pw.isVisible = true
        pw.addWindowListener(this)
    }

    fun addSelectedFilter(name: String, classname: String) {
        val p = Parameter(P_FILTER_ALL, name, ParameterType.STRING_T, classname, "")
        this.useParameter(p)
    }

    fun addSelectedMetric(name: String, classname: String) {
        val p = Parameter(P_METRIC, name, ParameterType.STRING_T, classname, "")
        this.useParameter(p)
    }

    fun useParameter(parameter: Any) {
        val p = parameter as Parameter

        if (p.name == P_FILTER_ADD) {
            val sel = FilterSelectionFrame(this)
            sel.isVisible = true
            return
        }

        if (p.name == P_METRIC_ADD) {
            val sel = MetricSelectionFrame(this)
            sel.isVisible = true
            return
        }

        val dlm = pw.inUseParametersModel
        dlm.addElement(Parameter(p).toString())
        pw.selectMostRecentlyAddedInUseParameter()
    }

    fun shouldUpdateCurrentlyUsedParameter(o: Any) {
        val p = o as Parameter
        p.name = pw.currentlySelectedName
        p.type = pw.currentlySelectedType
        p.setValue(pw.currentlySelectedValue)
        if (p.displayName.isEmpty()) p.displayName = p.name
    }

    fun processNewParameterSelection(o: Any) {
        val p = o as Parameter

        pw.currentlySelectedName = p.name
        if (p.getValue() != null) {
            pw.currentlySelectedValue = p.getValue().toString()
        } else {
            pw.currentlySelectedValue = ""
        }

        pw.setNameFieldEnabled(true)

        pw.setCurrentlySelectedType(p.type, true)

    }

    fun upButtonPressed() {
        moveSelectedElement(-1)
    }

    fun downButtonPressed() {
        moveSelectedElement(1)
    }

    private fun moveSelectedElement(offset: Int) {
        val dlm = pw.inUseParametersModel

        val selected = pw.currentlySelectedInUseParameterIndex

        val toMove = dlm.remove(selected)

        var newIndex = selected + offset
        while (newIndex > dlm.size) {
            newIndex--
        }
        while (newIndex < 0) {
            newIndex++
        }

        dlm.add(newIndex, toMove)
        pw.setCurrentlySelectedInUseParamter(newIndex)
    }

    fun browseButtonPressed() {
        val path = this.pw.parameterFile
        var fc: JFileChooser? = null
        if (File(path).exists()) {
            fc = JFileChooser(path)
        } else {
            fc = JFileChooser()
        }
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.addChoosableFileFilter(FileNameExtensionFilter("XML Files", "xml"))
        fc.fileFilter = FileNameExtensionFilter("XML Files", "xml")
        fc.isFileHidingEnabled = true
        val retVal = fc.showDialog(this.pw, "OK")


        if (retVal == JFileChooser.APPROVE_OPTION) {

            var selected = fc.selectedFile.absolutePath

            if (!selected.toLowerCase().endsWith(".xml")) {
                selected += ".xml"
            }

            pw.parameterFile = selected

            if (File(selected).exists()) {

                pw.inUseParametersModel.clear()

                val pd = AnalysisMetadataXMLParser().parseFileToParameterDictionary(selected)
                for (key in pd.keys) {
                    val count = pd.getValueCountForKey(key)
                    for (i in 0..count - 1) {
                        pd.getParameterForKey(key, i)?.let { this.useParameter(it) }
                    }
                }
            }

        }

    }

    fun removeButtonPressed() {
        val dlm = pw.inUseParametersModel

        val selected = pw.currentlySelectedInUseParameterIndex

        val toMove = dlm.remove(selected)
    }

    fun cancelButtonPressed() {
        this.pw.dispose()
    }

    fun doneButtonPressed() {
        val pxw = AnalysisMetadataXMLWriter()

        val parameters = pw.inUseParametersModel.toArray()

        val pd = ParameterDictionary.emptyDictionary()

        for (o in parameters) {
            pd.addParameter(o as Parameter)
        }


        var parameterFile = pw.parameterFile

        if (File(parameterFile).isDirectory) {
            parameterFile += File.separator + DEFAULT_FILENAME
        }

        pxw.writeParameterDictionaryToXMLFile(pd, parameterFile)

        Preferences.userNodeForPackage(this.javaClass).put("parameterFile", parameterFile)

        this.pw.dispose()

    }

    companion object {

        internal val DEFAULT_FILENAME = "default.xml"

        val P_FILTER_ALL = "filter_all"
        val P_FILTER_ADD = "filter_add"
        val P_METRIC = "metric_name"
        val P_METRIC_ADD = "metric_add"
    }


}
