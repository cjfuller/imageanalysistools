package edu.stanford.cfuller.analysistoolsinterface

import ij.plugin.PlugIn

class BatchAnalysisPlugin_ : PlugIn {
    override fun run(arg0: String) {
        java.awt.EventQueue.invokeLater(SegmentationController())
    }
}