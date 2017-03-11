package edu.stanford.cfuller.imageanalysistools.meta

import org.jruby.embed.ScriptingContainer

import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary
import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter
import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType


/**
 * Utilities for processing analysis parameters from suitably formatted ruby script files.

 * Currently this just sets up an empty dictionary with a parameter pointing to the
 * name of the script file, and a parameter directing it to run the script method.

 * @author Colin J. Fuller
 */
class AnalysisMetadataRubyParser : AnalysisMetadataParser() {
    /**
     * Parses a ruby file to a list of Parameters; this just sets up the script name parameter
     * and leaves everything else to the script itself, which is run once during this method.
     * @param filename      The filename of the ruby source file.
     * @return              A ParameterDictionary containing one Parameter object for each parameter described by the XML file.
     */
    override fun parseFileToAnalysisMetadata(filename: String): AnalysisMetadata {
        val am = AnalysisMetadata()
        val rs = RubyScript(filename)
        am.script = rs
        val pd = ParameterDictionary.emptyDictionary()
        var p = Parameter(SCRIPT_FILENAME_PARAM, SCRIPT_FILENAME_PARAM, ParameterType.STRING_T, filename, "")
        pd.addParameter(p)
        am.inputParameters = pd

        val sc = ScriptingContainer(org.jruby.embed.LocalContextScope.SINGLETHREAD, org.jruby.embed.LocalVariableBehavior.PERSISTENT)
        sc.classLoader = ij.IJ.getClassLoader()
        sc.put("parameters", pd)
        sc.compatVersion = org.jruby.CompatVersion.RUBY1_9
        sc.runScriptlet(this.javaClass.classLoader.getResourceAsStream(SCRIPT_FUNCTIONS_FILE), SCRIPT_FUNCTIONS_FILE)
        sc.runScriptlet(rs.scriptString)
        p = Parameter("method_name", "method_name", ParameterType.STRING_T, "ScriptMethod", "")
        pd.addIfNotSet("method_name", p)
        am.outputParameters = ParameterDictionary(pd)
        return am
    }

    companion object {
        val SCRIPT_FILENAME_PARAM = "script_filename"
        internal val SCRIPT_FUNCTIONS_FILE = "edu/stanford/cfuller/imageanalysistools/resources/parameter_methods.rb"
    }
}
