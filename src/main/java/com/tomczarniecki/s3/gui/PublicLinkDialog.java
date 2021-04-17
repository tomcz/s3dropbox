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

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.IntRange;
import org.joda.time.DateTime;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PublicLinkDialog extends JDialog {

    private enum Duration {

        Seconds, Minutes, Hours, Days;

        public DateTime with(int offset) {
            DateTime now = new DateTime();
            switch (this) {
                case Days:
                    return now.plusDays(offset);
                case Hours:
                    return now.plusHours(offset);
                case Minutes:
                    return now.plusMinutes(offset);
                default:
                    return now.plusSeconds(offset);
            }
        }

        public IntRange offsets() {
            switch (this) {
                case Days:
                    return new IntRange(1, 7);
                case Hours:
                    return new IntRange(1, 24);
                default:
                    return new IntRange(1, 60);
            }
        }
    }

    private final Controller controller;
    private final JTextArea display;
    private final JComboBox<Integer> offsetSelect;
    private final JComboBox<Duration> durationSelect;

    public PublicLinkDialog(JFrame parent, Controller controller) {
        super(parent, true);

        this.controller = controller;

        display = new JTextArea();
        display.setEditable(false);
        display.setLineWrap(true);

        Duration initial = Duration.Days;
        offsetSelect = new JComboBox<>(ArrayUtils.toObject(initial.offsets().toArray()));
        offsetSelect.setSelectedItem(5);
        offsetSelect.addActionListener(new CreatePublicLinkAction());

        durationSelect = new JComboBox<>(Duration.values());
        durationSelect.setSelectedItem(initial);
        durationSelect.addActionListener(new UpdateOffsetAction());

        getContentPane().add(createDisplayPanel());
        setResizable(false);
        pack();
    }

    public void display() {
        setLocationRelativeTo(getOwner());
        setTitle("Public link for " + controller.getSelectedObjectKey());
        createPublicLink();
        setVisible(true);
    }

    private JPanel createDisplayPanel() {
        JButton createButton = new JButton("Create");
        createButton.addActionListener(new CreatePublicLinkAction());

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(new CopyToClipboardAction());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new CloseAction());

        String cols = "pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref";
        String rows = "pref,5dlu,50dlu,5dlu,pref";

        return FormBuilder.create()
                .layout(new FormLayout(cols, rows))
                .addLabel("Link expires in").xy(1, 1)
                .add(offsetSelect).xy(3, 1)
                .add(durationSelect).xy(5, 1)
                .add(createButton).xy(7, 1)
                .add(copyButton).xy(9, 1)
                .add(new JScrollPane(display)).xyw(1, 3, 9, "fill,fill")
                .add(closeButton).xy(9, 5)
                .padding(Paddings.DIALOG)
                .build();
    }

    private void copyToClipboard() {
        StringSelection selection = new StringSelection(display.getText());
        Clipboard clipboard = getOwner().getToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @SuppressWarnings("ConstantConditions")
    private void createPublicLink() {
        Integer offset = (Integer) offsetSelect.getSelectedItem();
        Duration duration = (Duration) durationSelect.getSelectedItem();
        display.setText(controller.getPublicUrlForCurrentObject(duration.with(offset)));
        display.setCaretPosition(0);
    }

    @SuppressWarnings("ConstantConditions")
    private void updateOffset() {
        Integer offset = (Integer) offsetSelect.getSelectedItem();
        Duration duration = (Duration) durationSelect.getSelectedItem();
        IntRange offsets = duration.offsets();
        if (!offsets.containsInteger(offset)) {
            offset = offsets.getMinimumInteger();
        }
        offsetSelect.setModel(new DefaultComboBoxModel<>(ArrayUtils.toObject(offsets.toArray())));
        offsetSelect.setSelectedItem(offset);
    }

    private class UpdateOffsetAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            updateOffset();
        }
    }

    private class CreatePublicLinkAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            createPublicLink();
        }
    }

    private class CopyToClipboardAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            copyToClipboard();
        }
    }

    private class CloseAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
}
