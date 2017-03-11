package edu.stanford.cfuller.analysistoolsinterface

import java.awt.event.WindowEvent
import java.awt.event.WindowListener

/**

 * @author cfuller
 */
abstract class TaskController : Thread(), WindowListener {

    internal var responders: java.util.Vector<TaskCompletionResponder> = java.util.Vector<TaskCompletionResponder>()

    fun addCompletionResponder(tcr: TaskCompletionResponder) {
        this.responders.add(tcr)
    }

    abstract fun startTask()

    fun onFinish() {
        for (tcr in responders) {
            tcr.taskDidComplete(this)
        }
    }
    override fun windowActivated(e: WindowEvent) { }

    override fun windowClosed(e: WindowEvent) {
        this.onFinish()
    }

    override fun windowClosing(e: WindowEvent) { }
    override fun windowDeactivated(e: WindowEvent) { }
    override fun windowDeiconified(e: WindowEvent) { }
    override fun windowIconified(e: WindowEvent) { }
    override fun windowOpened(e: WindowEvent) { }

    override fun run() {
        this.startTask()
    }
}
