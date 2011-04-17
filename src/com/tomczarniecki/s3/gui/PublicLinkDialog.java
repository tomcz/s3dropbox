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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.IntRange;
import org.joda.time.DateTime;

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

        Seconds, Minutes, Hours, Days, Weeks, Months;

        public DateTime with(int offset) {
            DateTime now = new DateTime();
            switch (this) {
                case Minutes:
                    return now.plusMinutes(offset);
                case Hours:
                    return now.plusHours(offset);
                case Days:
                    return now.plusDays(offset);
                case Weeks:
                    return now.plusWeeks(offset);
                case Months:
                    return now.plusMonths(offset);
                default:
                    return now.plusSeconds(offset);
            }
        }
    }

    private final Controller controller;
    private final JTextArea display;
    private final JComboBox offsetSelect;
    private final JComboBox durationSelect;

    public PublicLinkDialog(JFrame parent, Controller controller) {
        super(parent, true);

        this.controller = controller;

        display = new JTextArea();
        display.setEditable(false);
        display.setLineWrap(true);

        IntRange offsetRange = new IntRange(1, 100);
        offsetSelect = new JComboBox(ArrayUtils.toObject(offsetRange.toArray()));
        offsetSelect.setSelectedItem(5);
        offsetSelect.addActionListener(new CreatePublicLinkAction());

        durationSelect = new JComboBox(Duration.values());
        durationSelect.setSelectedItem(Duration.Days);
        durationSelect.addActionListener(new CreatePublicLinkAction());

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

        CellConstraints cc = new CellConstraints();
        String cols = "pref,2dlu,pref,2dlu,pref,2dlu,pref,2dlu,pref";
        String rows = "pref,5dlu,50dlu,5dlu,pref";

        PanelBuilder builder = new PanelBuilder(new FormLayout(cols, rows));
        builder.setDefaultDialogBorder();
        builder.addLabel("Link expires in", cc.xy(1, 1));
        builder.add(offsetSelect, cc.xy(3, 1));
        builder.add(durationSelect, cc.xy(5, 1));
        builder.add(createButton, cc.xy(7, 1));
        builder.add(copyButton, cc.xy(9, 1));
        builder.add(new JScrollPane(display), cc.xyw(1, 3, 9, "fill,fill"));
        builder.add(closeButton, cc.xy(9, 5));

        return builder.getPanel();
    }

    private void copyToClipboard() {
        StringSelection selection = new StringSelection(display.getText());
        Clipboard clipboard = getOwner().getToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    private void createPublicLink() {
        Integer offset = (Integer) offsetSelect.getSelectedItem();
        Duration duration = (Duration) durationSelect.getSelectedItem();
        display.setText(controller.getPublicUrlForCurrentObject(duration.with(offset)));
        display.setCaretPosition(0);
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
