/* ===================================================================================
 * Copyright (c) 2008, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ===================================================================================
 */
package com.tomczarniecki.s3.gui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.tomczarniecki.s3.rest.Configuration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class CredentialsDialog extends JDialog {

    private final JTextField accessKeyId;
    private final JTextField secretAccessKey;

    private Configuration credentials;

    public CredentialsDialog() {
        setTitle("Amazon S3 Credentials");
        setModal(true);

        accessKeyId = new JTextField();
        secretAccessKey = new JTextField();

        getContentPane().add(createDisplayPanel());
        setResizable(false);
        pack();
    }

    public Configuration getCredentials() {
        setLocationRelativeTo(null);
        secretAccessKey.setText("");
        accessKeyId.setText("");
        credentials = null;
        setVisible(true);
        return credentials;
    }

    private JPanel createDisplayPanel() {
        CellConstraints cc = new CellConstraints();
        String cols = "pref,5dlu,200dlu";
        String rows = "pref,5dlu,pref,5dlu,pref";

        PanelBuilder builder = new PanelBuilder(new FormLayout(cols, rows));
        builder.setDefaultDialogBorder();
        builder.addLabel("Access Key ID", cc.xy(1, 1));
        builder.add(accessKeyId, cc.xy(3, 1));
        builder.addLabel("Secret Access Key", cc.xy(1, 3));
        builder.add(secretAccessKey, cc.xy(3, 3));
        builder.add(createButtonBar(), cc.xyw(1, 5, 3));

        return builder.getPanel();
    }

    private JPanel createButtonBar() {
        JButton createButton = new JButton("Save");
        createButton.addActionListener(new SaveAction());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelAction());

        ButtonBarBuilder buttonBar = ButtonBarBuilder.createLeftToRightBuilder();
        buttonBar.addGlue();
        buttonBar.addGridded(createButton);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(cancelButton);
        return buttonBar.getPanel();
    }

    private void createCredentials() {
        String key = trimToEmpty(accessKeyId.getText());
        String secret = trimToEmpty(secretAccessKey.getText());
        if (isNotEmpty(key) && isNotEmpty(secret)) {
            credentials = new Configuration(key, secret);
        }
    }

    private class SaveAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            createCredentials();
            setVisible(false);
        }
    }

    private class CancelAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
}
