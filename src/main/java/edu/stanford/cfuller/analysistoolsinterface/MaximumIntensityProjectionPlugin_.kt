package edu.stanford.cfuller.analysistoolsinterface

import ij.plugin.PlugIn

/**
 * @author cfuller
 */
class MaximumIntensityProjectionPlugin_ : PlugIn {

    /* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
    override fun run(arg0: String) {

        java.awt.EventQueue.invokeLater(MaximumIntensityProjectionController())

    }

}
