/*
 * Copyright (c) 2010, Thomas Czarniecki
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

import org.apache.commons.lang.SystemUtils;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import java.awt.Cursor;
import java.io.File;
import java.util.List;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

class Display {

    private final JFrame frame;

    public Display(JFrame frame) {
        this.frame = frame;
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public void showBusyCursor() {
        setCursor(Cursor.WAIT_CURSOR);
    }

    public void showNormalCursor() {
        setCursor(Cursor.DEFAULT_CURSOR);
    }

    private void setCursor(int cursor) {
        frame.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    public JPopupMenu createPopupMenu() {
        return new JPopupMenu();
    }

    public PublicLinkDialog createPublicLinkDialog(Controller controller) {
        return new PublicLinkDialog(frame, controller);
    }

    public ProgressDialog createProgressDialog(String title, Worker worker) {
        return new ProgressDialog(frame, title, worker);
    }

    public void showErrorMessage(String title, String message) {
        showMessageDialog(frame, message, title, ERROR_MESSAGE);
    }

    public boolean confirmMessage(String tilte, String message) {
        int result = showConfirmDialog(frame, message, tilte, YES_NO_OPTION, WARNING_MESSAGE);
        return result == YES_OPTION;
    }

    public String getInput(String title, String message) {
        return showInputDialog(frame, message, title, QUESTION_MESSAGE);
    }

    public String selectOption(String title, String message, List<String> options) {
        return (String) showInputDialog(frame, message, title, QUESTION_MESSAGE, null, options.toArray(), null);
    }

    public File selectDirectory(String title, String buttonText) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(SystemUtils.USER_HOME, "Desktop"));
        chooser.setDialogTitle(title);

        int result = chooser.showDialog(frame, buttonText);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    public File[] selectFiles(String title, String buttonText) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setSelectedFile(new File(SystemUtils.USER_HOME, "Desktop"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(title);

        int result = chooser.showDialog(frame, buttonText);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFiles();
        }
        return null;
    }
}
