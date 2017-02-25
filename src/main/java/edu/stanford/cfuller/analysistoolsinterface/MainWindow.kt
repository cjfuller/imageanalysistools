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

import javax.swing.GroupLayout
import javax.swing.LayoutStyle

/**

 * @author cfuller
 */
class MainWindow
/** Creates new form MainWindow  */
(controller: MainWindowController) : javax.swing.JFrame() {

    internal var controller: MainWindowController

    init {
        this.setController(controller)
        initComponents()
    }

    fun setController(controller: MainWindowController) {
        this.controller = controller
        controller.setMainWindow(this)
    }

    val taskSelectorSelectedItem: String
        get() = this.taskSelectorComboBox!!.selectedItem as String

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    fun initComponents() {

        jLabel1 = javax.swing.JLabel()
        jLabel2 = javax.swing.JLabel()
        taskSelectorComboBox = javax.swing.JComboBox()
        goButton = javax.swing.JButton()

        defaultCloseOperation = javax.swing.WindowConstants.EXIT_ON_CLOSE
        title = "Straight Lab Image Analysis Tools"

        jLabel1!!.font = java.awt.Font("Lucida Grande", 0, 16) // NOI18N
        jLabel1!!.text = "Straight Lab Image Analysis Tools"

        jLabel2!!.text = "Select a task:"

        taskSelectorComboBox!!.setModel(this.controller.populateTaskComboBoxModel())

        goButton!!.text = "Go"
        goButton!!.addActionListener { evt -> goButtonActionPerformed(evt) }

        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(jLabel1))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel2))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(taskSelectorComboBox!!, 0, 270, java.lang.Short.MAX_VALUE.toInt()))
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addContainerGap(212, java.lang.Short.MAX_VALUE.toInt())
                                                .addComponent(goButton)))
                                .addContainerGap())
        )
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(taskSelectorComboBox!!, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(goButton)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt()))
        )

        pack()
    }// </editor-fold>//GEN-END:initComponents

    private fun goButtonActionPerformed(evt: java.awt.event.ActionEvent) {//GEN-FIRST:event_goButtonActionPerformed
        controller.onGoButtonClick(evt)
    }//GEN-LAST:event_goButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private var goButton: javax.swing.JButton? = null
    private var jLabel1: javax.swing.JLabel? = null
    private var jLabel2: javax.swing.JLabel? = null
    private var taskSelectorComboBox: javax.swing.JComboBox<*>? = null

    companion object {

        internal val serialVersionUID = 1L
    }
    // End of variables declaration//GEN-END:variables

}
