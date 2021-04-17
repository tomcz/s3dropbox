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
import com.tomczarniecki.s3.ProgressListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ProgressDialog extends JDialog implements ProgressListener {

    private final JTextArea display;
    private final JProgressBar progress;
    private final JButton closeButton;
    private final Worker worker;

    public ProgressDialog(JFrame parent, String title, Worker worker) {
        super(parent, title, true);
        this.worker = worker;

        this.display = new JTextArea();
        this.display.setEditable(false);
        this.display.setLineWrap(true);

        progress = new JProgressBar();
        closeButton = new JButton("Close");
        closeButton.addActionListener(new CloseButtonClick());

        JPanel panel = FormBuilder.create()
                .layout(new FormLayout("225dlu,5dlu,70dlu", "100dlu,5dlu,pref"))
                .add(new JScrollPane(this.display)).xyw(1, 1, 3, "fill,fill")
                .add(progress).xy(1, 3, "fill,fill")
                .add(closeButton).xy(3, 3, "fill,fill")
                .padding(Paddings.DIALOG)
                .build();

        getContentPane().add(panel);
        setResizable(false);
        pack();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void begin() {
        worker.executeOnEventLoop(() -> {
            setLocationRelativeTo(getOwner());
            closeButton.setEnabled(false);
            display.setText("");
            setVisible(true);
        });
        next();
    }

    public void append(String text) {
        append(text, new Object[0]);
    }

    public void append(final String text, final Object... args) {
        worker.executeOnEventLoop(() -> {
            display.append(String.format(text, args));
            int length = display.getDocument().getLength();
            display.setCaretPosition(length);
        });
    }

    public void finish() {
        worker.executeOnEventLoop(() -> {
            closeButton.setEnabled(true);
            if (progress.isIndeterminate()) {
                progress.setMinimum(0);
                progress.setMaximum(100);
                progress.setStringPainted(true);
                progress.setIndeterminate(false);
            }
            int max = progress.getMaximum();
            progress.setValue(max);
        });
    }

    public void next() {
        worker.executeOnEventLoop(() -> {
            progress.setValue(0);
            progress.setIndeterminate(true);
        });
    }

    public void processed(final long count, final long length) {
        worker.executeOnEventLoop(() -> {
            if (progress.isIndeterminate()) {
                progress.setMinimum(0);
                progress.setMaximum((int) length);
                progress.setStringPainted(true);
                progress.setIndeterminate(false);
            }
            progress.setValue((int) count);
        });
    }

    private class CloseButtonClick implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
}
