/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */


package edu.stanford.cfuller.analysistoolsinterface

import java.awt.event.WindowEvent
import java.awt.event.WindowListener

/**

 * @author cfuller
 */
abstract class TaskController : Thread(), WindowListener {

    internal var responders: java.util.Vector<TaskCompletionResponder>

    init {
        this.responders = java.util.Vector<TaskCompletionResponder>()
    }

    fun addCompletionResponder(tcr: TaskCompletionResponder) {
        this.responders.add(tcr)
    }

    abstract fun startTask()

    fun onFinish() {
        for (tcr in responders) {
            tcr.taskDidComplete(this)
        }
    }

    override fun windowActivated(e: WindowEvent) {

    }

    override fun windowClosed(e: WindowEvent) {
        this.onFinish()
    }

    override fun windowClosing(e: WindowEvent) {

    }

    override fun windowDeactivated(e: WindowEvent) {

    }

    override fun windowDeiconified(e: WindowEvent) {

    }

    override fun windowIconified(e: WindowEvent) {

    }

    override fun windowOpened(e: WindowEvent) {

    }

    override fun run() {
        this.startTask()
    }

}
