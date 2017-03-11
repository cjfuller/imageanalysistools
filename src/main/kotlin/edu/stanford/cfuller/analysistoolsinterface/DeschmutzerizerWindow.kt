package edu.stanford.cfuller.analysistoolsinterface

/**

 * @author cfuller
 */
class DeschmutzerizerWindow
/** Creates new form DeschmutzerizerWindow  */
(internal var controller: DeschmutzerizerController) : javax.swing.JFrame() {
    var imageFilename: String
        get() = this.directoryTextField!!.text
        set(filename) {
            this.directoryTextField!!.text = filename
        }

    fun setDone(done: Boolean) {
        this.doneLabel!!.isVisible = done
    }

    fun setStartButtonEnabled(enabled: Boolean) {
        this.startButton!!.isEnabled = enabled
    }

    fun useExistingMask(): Boolean {
        return this.useExistingMaskCheckBox!!.isSelected
    }

    fun groupSelectionSelected(): Boolean {
        return this.groupButton!!.isSelected
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private fun initComponents() {

        buttonGroup1 = javax.swing.ButtonGroup()
        jCheckBox1 = javax.swing.JCheckBox()
        jLabel1 = javax.swing.JLabel()
        directoryTextField = javax.swing.JTextField()
        jLabel2 = javax.swing.JLabel()
        groupButton = javax.swing.JRadioButton()
        regionButton = javax.swing.JRadioButton()
        useExistingMaskCheckBox = javax.swing.JCheckBox()
        browseButton = javax.swing.JButton()
        startButton = javax.swing.JButton()
        continueButton = javax.swing.JButton()
        doneLabel = javax.swing.JLabel()
        selectAllButton = javax.swing.JButton()

        jCheckBox1!!.text = "jCheckBox1"

        defaultCloseOperation = javax.swing.WindowConstants.DISPOSE_ON_CLOSE

        jLabel1!!.font = java.awt.Font("Lucida Grande", 0, 16)
        jLabel1!!.text = "The Deschmutzerizer"

        jLabel2!!.text = "File or directory to process:"

        buttonGroup1!!.add(groupButton)
        groupButton!!.text = "Toggle entire group"

        buttonGroup1!!.add(regionButton)
        regionButton!!.isSelected = true
        regionButton!!.text = "Toggle individual region"

        useExistingMaskCheckBox!!.text = "Use existing deschmutzerized mask"

        browseButton!!.text = "Browse"
        browseButton!!.addActionListener { evt -> browseButtonActionPerformed(evt) }

        startButton!!.text = "Start"
        startButton!!.addActionListener { evt -> startButtonActionPerformed(evt) }

        continueButton!!.text = "Remove Selected and Continue"
        continueButton!!.isEnabled = false
        continueButton!!.addActionListener { evt -> continueButtonActionPerformed(evt) }

        doneLabel!!.font = java.awt.Font("Lucida Grande", 0, 18) // NOI18N
        doneLabel!!.text = "Done!"

        selectAllButton!!.text = "Toggle All"
        selectAllButton!!.isEnabled = false
        selectAllButton!!.addActionListener { evt -> selectAllButtonActionPerformed(evt) }

        val layout = javax.swing.GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel2))
                                        .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(directoryTextField!!, javax.swing.GroupLayout.DEFAULT_SIZE, 535, java.lang.Short.MAX_VALUE.toInt()))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(regionButton)
                                                                .addGap(36, 36, 36)
                                                                .addComponent(useExistingMaskCheckBox))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(groupButton)
                                                                .addGap(282, 282, 282)
                                                                .addComponent(browseButton))))
                                        .addComponent(jLabel1))
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(continueButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(selectAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, java.lang.Short.MAX_VALUE.toInt())
                                .addComponent(doneLabel)
                                .addGap(18, 18, 18)
                                .addComponent(startButton)
                                .addGap(23, 23, 23))
        )
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel1)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2)
                                .addGap(3, 3, 3)
                                .addComponent(directoryTextField!!, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(browseButton)
                                        .addComponent(groupButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(regionButton)
                                        .addComponent(useExistingMaskCheckBox))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startButton)
                                        .addComponent(continueButton)
                                        .addComponent(doneLabel)
                                        .addComponent(selectAllButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt()))
        )

        pack()
    }// </editor-fold>//GEN-END:initComponents

    private fun browseButtonActionPerformed(evt: java.awt.event.ActionEvent) {//GEN-FIRST:event_browseButtonActionPerformed
        this.controller.browseButtonPressed()
    }//GEN-LAST:event_browseButtonActionPerformed

    private fun startButtonActionPerformed(evt: java.awt.event.ActionEvent) {//GEN-FIRST:event_startButtonActionPerformed
        this.selectAllButton!!.isEnabled = true
        this.continueButton!!.isEnabled = true
        this.controller.startButtonPressed()
    }//GEN-LAST:event_startButtonActionPerformed

    private fun continueButtonActionPerformed(evt: java.awt.event.ActionEvent) {//GEN-FIRST:event_continueButtonActionPerformed
        this.controller.continueButtonPressed()
    }//GEN-LAST:event_continueButtonActionPerformed

    private fun selectAllButtonActionPerformed(evt: java.awt.event.ActionEvent) {//GEN-FIRST:event_selectAllButtonActionPerformed
        this.controller.selectAllButtonPressed()
    }//GEN-LAST:event_selectAllButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private var browseButton: javax.swing.JButton? = null
    private var buttonGroup1: javax.swing.ButtonGroup? = null
    private var continueButton: javax.swing.JButton? = null
    private var directoryTextField: javax.swing.JTextField? = null
    private var doneLabel: javax.swing.JLabel? = null
    private var groupButton: javax.swing.JRadioButton? = null
    private var jCheckBox1: javax.swing.JCheckBox? = null
    private var jLabel1: javax.swing.JLabel? = null
    private var jLabel2: javax.swing.JLabel? = null
    private var regionButton: javax.swing.JRadioButton? = null
    private var selectAllButton: javax.swing.JButton? = null
    private var startButton: javax.swing.JButton? = null
    private var useExistingMaskCheckBox: javax.swing.JCheckBox? = null

    companion object {

        /**

         */
        private val serialVersionUID = -114308959777145758L
    }
    // End of variables declaration//GEN-END:variables

    init {
        initComponents()
        this.doneLabel!!.isVisible = false
    }
}