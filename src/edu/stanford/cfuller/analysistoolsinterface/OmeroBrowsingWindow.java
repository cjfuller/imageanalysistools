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

import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.JTree;
import javax.swing.LayoutStyle;

/**
 *
 * @author cfuller
 */
public class OmeroBrowsingWindow extends javax.swing.JFrame {

	final static long serialVersionUID= 1L;

    OmeroBrowsingWindowController controller;

    /** Creates new form OmeroBrowsingWindow */
    public OmeroBrowsingWindow(OmeroBrowsingWindowController c) {
        this.controller = c;
        initComponents();
        this.serverTextField.setText(Preferences.userNodeForPackage(this.getClass()).get("omeroserver_hostname", this.serverTextField.getText()));
    }

    public String getHost() {
        return this.serverTextField.getText();
    }

    public String getUsername() {
        return this.usernameTextField.getText();
    }

    public char[] getPassword() {
        return this.passwordField.getPassword();
    }
    
    public JTree getOmeroBrowsingTree() {
        return this.omeroBrowsingTree;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        omeroBrowsingTree = new javax.swing.JTree();
        usernameTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        serverTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        loginButton = new javax.swing.JButton();
        imageButton = new javax.swing.JButton();
        loadImagesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(omeroBrowsingTree);

        serverTextField.setText("171.65.20.207");

        jLabel1.setText("Omero Server:");

        jLabel2.setText("Username:");

        jLabel3.setText("Password:");

        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        imageButton.setText("Use selected");
        imageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageButtonActionPerformed(evt);
            }
        });

        loadImagesButton.setText("Load Images");
        loadImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImagesButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(passwordField, GroupLayout.Alignment.TRAILING)
                                .addComponent(usernameTextField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                .addComponent(loginButton, GroupLayout.Alignment.TRAILING)))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(loadImagesButton)
                            .addComponent(imageButton)))
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(8, 8, 8)
                        .addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loginButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                        .addComponent(loadImagesButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(imageButton))
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed

        Preferences.userNodeForPackage(this.getClass()).put("omeroserver_hostname", this.serverTextField.getText());

        this.controller.loginButtonPressed();
    }//GEN-LAST:event_loginButtonActionPerformed

    private void loadImagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImagesButtonActionPerformed
        this.controller.loadImages();
    }//GEN-LAST:event_loadImagesButtonActionPerformed

    private void imageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageButtonActionPerformed
        this.controller.doneButtonPressed();
    }//GEN-LAST:event_imageButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton imageButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadImagesButton;
    private javax.swing.JButton loginButton;
    private javax.swing.JTree omeroBrowsingTree;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField serverTextField;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

}