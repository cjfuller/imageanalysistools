package edu.stanford.cfuller.imageanalysistools.method

import org.jruby.embed.ScriptingContainer
import edu.stanford.cfuller.imageanalysistools.meta.RubyScript

/**
 * A method that runs a ruby script using jruby, which can be used to script
 * custom analysis methods at runtime.
 *
 * The method gets the name of the script file from the parameter dictionary, and sets
 * the local variables "parameters", "imageset" and "method" to be the parameter dictionary
 * in use for the analysis, the ImageSet object containing the images being analyzed, and
 * this method object.
 *
 * @author Colin J. Fuller
 */

class ScriptMethod : Method() {
    override fun go() {
        val scriptFilename = this.parameters.getValueForKey(SCRIPT_FILENAME_PARAM)
        val sc = ScriptingContainer(org.jruby.embed.LocalContextScope.SINGLETHREAD, org.jruby.embed.LocalVariableBehavior.PERSISTENT)
        sc.classLoader = ij.IJ.getClassLoader()
        sc.put("parameters", this.parameters)
        sc.put("imageset", this.imageSet)
        sc.put("method", this)
        sc.compatVersion = org.jruby.CompatVersion.RUBY1_9
        sc.runScriptlet(this.javaClass.classLoader.getResourceAsStream(SCRIPT_FUNCTIONS_FILE), SCRIPT_FUNCTIONS_FILE)

        val scriptToRun = this.analysisMetadata.script
        sc.runScriptlet(scriptToRun!!.scriptString)

        if (this.parameters.hasKey(PARAM_METHOD_DISPLAY_NAME)) {
            this.displayName = this.parameters.getValueForKey(PARAM_METHOD_DISPLAY_NAME)
        }
    }

    companion object {
        val SCRIPT_FILENAME_PARAM = edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataRubyParser.SCRIPT_FILENAME_PARAM
        internal val SCRIPT_FUNCTIONS_FILE = "edu/stanford/cfuller/imageanalysistools/resources/script_methods.rb"
        internal val PARAM_METHOD_DISPLAY_NAME = "method_display_name"
    }
}

