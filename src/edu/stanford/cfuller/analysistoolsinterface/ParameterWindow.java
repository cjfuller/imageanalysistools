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


package edu.stanford.cfuller.analysistoolsinterface;

import edu.stanford.cfuller.imageanalysistools.parameters.Parameter;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;

/**
 *
 * @author cfuller
 */
public class ParameterWindow extends javax.swing.JFrame {

	static final long serialVersionUID = 1L;

    ParameterSetupController controller;
    DefaultListModel inUseParametersModel;
    Object currentlyBeingEdited;

    /** Creates new form ParameterWindow */
    public ParameterWindow(ParameterSetupController controller) {
        this.controller = controller;
        this.inUseParametersModel = new DefaultListModel();
        initComponents();
        this.usedParameterList.setModel(this.inUseParametersModel);
        this.availableParameterList.setDragEnabled(true);
        this.usedParameterList.setDropMode(DropMode.ON_OR_INSERT);
        this.availableParameterList.setTransferHandler(new ParameterTransferHandler(this.controller));
        this.usedParameterList.setTransferHandler(new ParameterTransferHandler(this.controller));
    }

    public void setAvailableParameters(java.util.List<Object> parameters) {
        DefaultListModel dlm = new DefaultListModel();
        for (Object o : parameters) {
            dlm.addElement(o);
        }
        this.availableParameterList.setModel(dlm);
    }

    public DefaultListModel getInUseParametersModel() {return this.inUseParametersModel;}

    public int getCurrentlySelectedInUseParameterIndex() {
        return this.usedParameterList.getSelectedIndex();
    }

    public void setCurrentlySelectedInUseParamter(int index) {
        this.usedParameterList.setSelectedIndex(index);
    }

    public void setCurrentlySelectedName(String name) {
        this.parameterNameField.setText(name);
    }
    public void setCurrentlySelectedValue(String value) {
        this.parameterValueField.setText(value);
    }
    public String getCurrentlySelectedName() {
        return this.parameterNameField.getText();
    }
    public String getCurrentlySelectedValue() {
        return this.parameterValueField.getText();
    }

    public String getParameterFile() {
        return this.parameterFileField.getText();
    }
    public void setParameterFile(String f) {
        this.parameterFileField.setText(f);
    }

    public void selectMostRecentlyAddedInUseParameter() {
        this.usedParameterList.setSelectedIndex(this.usedParameterList.getModel().getSize()-1);
        this.usedParameterListMouseClicked(null);
    }

    public void setCurrentlySelectedType(int type, boolean enabled) {
        this.booleanRadioButton.setSelected(type == Parameter.TYPE_BOOLEAN);
        this.integerRadioButton.setSelected(type == Parameter.TYPE_INTEGER);
        this.floatingPointRadioButton.setSelected(type == Parameter.TYPE_FLOATING);
        this.textRadioButton.setSelected(type == Parameter.TYPE_STRING);
        this.booleanRadioButton.setEnabled(enabled);
        this.integerRadioButton.setEnabled(enabled);
        this.floatingPointRadioButton.setEnabled(enabled);
        this.textRadioButton.setEnabled(enabled);
    }
    
    public int getCurrentlySelectedType() {
        if (this.booleanRadioButton.isSelected()) return Parameter.TYPE_BOOLEAN;
        if (this.integerRadioButton.isSelected()) return Parameter.TYPE_INTEGER;
        if (this.floatingPointRadioButton.isSelected()) return Parameter.TYPE_FLOATING;
        if (this.textRadioButton.isSelected()) return Parameter.TYPE_STRING;
        return Parameter.TYPE_STRING;
    }

    public void setNameFieldEnabled(boolean enabled) {
        this.parameterNameField.setEnabled(enabled);
    }

    public Object getObjectBeingEdited() {return this.currentlyBeingEdited;}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        parameterTypeButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        availableParameterList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        usedParameterList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        parameterNameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        parameterValueField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        textRadioButton = new javax.swing.JRadioButton();
        booleanRadioButton = new javax.swing.JRadioButton();
        integerRadioButton = new javax.swing.JRadioButton();
        floatingPointRadioButton = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        parameterFileField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Specify Analysis Parameters");

        jScrollPane1.setHorizontalScrollBar(null);

        availableParameterList.setModel(new javax.swing.AbstractListModel() {
			final static long serialVersionUID = 1L;
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        availableParameterList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        availableParameterList.setDropMode(javax.swing.DropMode.INSERT);
        jScrollPane1.setViewportView(availableParameterList);

        jScrollPane2.setHorizontalScrollBar(null);

        usedParameterList.setModel(new javax.swing.AbstractListModel() {
			final static long serialVersionUID = 1L;
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        usedParameterList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usedParameterList.setDropMode(javax.swing.DropMode.INSERT);
        usedParameterList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usedParameterListMouseClicked(evt);
            }
        });
        usedParameterList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                usedParameterListKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(usedParameterList);

        jLabel1.setText("Available parameters:");

        jLabel2.setText("Parameters in use:");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        upButton.setText("Up");
        upButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setText("Down");
        downButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Parameter Name");

        parameterNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterNameFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterNameFieldFocusLost(evt);
            }
        });

        jLabel4.setText("Parameter Value");

        parameterValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                parameterValueFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                parameterValueFieldFocusLost(evt);
            }
        });

        jLabel5.setText("Parameter Type");

        parameterTypeButtonGroup.add(textRadioButton);
        textRadioButton.setText("Text");
        textRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                textRadioButtonFocusLost(evt);
            }
        });

        parameterTypeButtonGroup.add(booleanRadioButton);
        booleanRadioButton.setText("Boolean");
        booleanRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                booleanRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                booleanRadioButtonFocusLost(evt);
            }
        });

        parameterTypeButtonGroup.add(integerRadioButton);
        integerRadioButton.setText("Integer");
        integerRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                integerRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                integerRadioButtonFocusLost(evt);
            }
        });

        parameterTypeButtonGroup.add(floatingPointRadioButton);
        floatingPointRadioButton.setText("Decimal Number");

        floatingPointRadioButton.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                floatingPointRadioButtonFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                floatingPointRadioButtonFocusLost(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                        .addComponent(parameterValueField, GroupLayout.Alignment.LEADING)
                        .addComponent(parameterNameField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(textRadioButton)
                            .addComponent(integerRadioButton))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(floatingPointRadioButton)
                            .addComponent(booleanRadioButton))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parameterNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parameterValueField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(textRadioButton)
                    .addComponent(booleanRadioButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(integerRadioButton)
                    .addComponent(floatingPointRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel7.setText("Parameter file:");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(upButton, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(downButton, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeButton))
                        .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                        .addComponent(browseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(doneButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(jPanel1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(parameterFileField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7)
                    .addComponent(browseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(parameterFileField, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(upButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(downButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton)
                    .addComponent(cancelButton)
                    .addComponent(doneButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        this.controller.useParameter(this.availableParameterList.getSelectedValue());
    }//GEN-LAST:event_addButtonActionPerformed

    private void parameterNameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterNameFieldFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_parameterNameFieldFocusLost

    private void parameterValueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterValueFieldFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_parameterValueFieldFocusLost

    private void usedParameterListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usedParameterListMouseClicked
        controller.processNewParameterSelection(this.usedParameterList.getSelectedValue());
    }//GEN-LAST:event_usedParameterListMouseClicked

    private void usedParameterListKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usedParameterListKeyTyped
        controller.processNewParameterSelection(this.usedParameterList.getSelectedValue());
    }//GEN-LAST:event_usedParameterListKeyTyped

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        controller.upButtonPressed();
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        controller.downButtonPressed();
    }//GEN-LAST:event_downButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        controller.removeButtonPressed();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void parameterNameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterNameFieldFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_parameterNameFieldFocusGained

    private void parameterValueFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_parameterValueFieldFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_parameterValueFieldFocusGained

    private void textRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textRadioButtonFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_textRadioButtonFocusGained

    private void textRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textRadioButtonFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_textRadioButtonFocusLost

    private void booleanRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_booleanRadioButtonFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_booleanRadioButtonFocusGained

    private void booleanRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_booleanRadioButtonFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_booleanRadioButtonFocusLost

    private void integerRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_integerRadioButtonFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_integerRadioButtonFocusGained

    private void integerRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_integerRadioButtonFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_integerRadioButtonFocusLost

    private void floatingPointRadioButtonFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_floatingPointRadioButtonFocusGained
        this.currentlyBeingEdited = this.usedParameterList.getSelectedValue();
    }//GEN-LAST:event_floatingPointRadioButtonFocusGained

    private void floatingPointRadioButtonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_floatingPointRadioButtonFocusLost
        controller.shouldUpdateCurrentlyUsedParameter(this.currentlyBeingEdited);
    }//GEN-LAST:event_floatingPointRadioButtonFocusLost

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        this.controller.browseButtonPressed();
    }//GEN-LAST:event_browseButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        this.controller.doneButtonPressed();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.controller.cancelButtonPressed();
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList availableParameterList;
    private javax.swing.JRadioButton booleanRadioButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JButton downButton;
    private javax.swing.JRadioButton floatingPointRadioButton;
    private javax.swing.JRadioButton integerRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField parameterFileField;
    private javax.swing.JTextField parameterNameField;
    private javax.swing.ButtonGroup parameterTypeButtonGroup;
    private javax.swing.JTextField parameterValueField;
    private javax.swing.JButton removeButton;
    private javax.swing.JRadioButton textRadioButton;
    private javax.swing.JButton upButton;
    private javax.swing.JList usedParameterList;
    // End of variables declaration//GEN-END:variables

}
