/*
 * Copyright (c) 2011, Thomas Czarniecki
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
 */
package com.tomczarniecki.s3.gui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;
import com.tomczarniecki.s3.Pair;
import org.apache.commons.lang.StringUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static com.tomczarniecki.s3.Pair.pair;

public class CreateBucketDialog extends JDialog {

    private final JTextField bucketName;
    private final JComboBox<String> bucketRegion;

    private boolean createBucket;

    public CreateBucketDialog(List<String> regions) {
        setTitle("Create Folder");
        setModal(true);

        bucketName = new JTextField();
        String[] array = regions.toArray(new String[0]);
        bucketRegion = new JComboBox<>(array);

        getContentPane().add(createDisplayPanel());
        setResizable(false);
        pack();
    }

    public Pair<String, String> get(Pair<String, String> bucketNameAndRegion) {
        setValues(bucketNameAndRegion);
        setLocationRelativeTo(null);
        setVisible(true);
        return result();
    }

    private JPanel createDisplayPanel() {
        String cols = "pref,5dlu,100dlu";
        String rows = "pref,5dlu,pref,5dlu,pref";

        return FormBuilder.create()
                .layout(new FormLayout(cols, rows))
                .addLabel("Folder Name").xy(1, 1)
                .add(bucketName).xy(3, 1)
                .addLabel("S3 Region").xy(1, 3)
                .add(bucketRegion).xy(3, 3)
                .add(createButtonBar()).xyw(1, 5, 3)
                .padding(Paddings.DIALOG)
                .build();
    }

    private JPanel createButtonBar() {
        JButton createButton = new JButton("Create");
        createButton.addActionListener(new CreateAction());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelAction());

        return ButtonBarBuilder.create()
                .addGlue()
                .addButton(createButton)
                .addRelatedGap()
                .addButton(cancelButton)
                .build();
    }

    private void setValues(Pair<String, String> bucketNameAndRegion) {
        createBucket = false;
        if (bucketNameAndRegion != null) {
            this.bucketName.setText(bucketNameAndRegion.getKey());
            this.bucketRegion.setSelectedItem(bucketNameAndRegion.getValue());
        }
    }

    private Pair<String, String> result() {
        if (createBucket) {
            String name = StringUtils.trimToEmpty(bucketName.getText());
            String region = (String) bucketRegion.getSelectedItem();
            return pair(name, region);
        }
        return null;
    }

    private class CreateAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            createBucket = true;
            setVisible(false);
        }
    }

    private class CancelAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            createBucket = false;
            setVisible(false);
        }
    }
}
