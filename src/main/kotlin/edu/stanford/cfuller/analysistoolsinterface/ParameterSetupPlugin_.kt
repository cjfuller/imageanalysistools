package edu.stanford.cfuller.analysistoolsinterface

import ij.plugin.PlugIn
import edu.stanford.cfuller.analysistoolsinterface.ParameterSetupController

/**
 * @author cfuller
 */
class ParameterSetupPlugin_ : PlugIn {
    /* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
    override fun run(arg0: String) {
        val p = ParameterSetupController()
        java.awt.EventQueue.invokeLater(p)
    }
}
