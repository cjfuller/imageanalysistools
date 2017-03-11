package edu.stanford.cfuller.analysistoolsinterface

/**

 * @author cfuller
 */
object Main {

    /**
     * @param args the command line arguments
     */
    @JvmStatic fun main(args: Array<String>) {
        java.awt.EventQueue.invokeLater {
            val mw = MainWindow(MainWindowController())
            mw.isVisible = true
        }
    }


}
