/*
 * Copyright (c) 2021, Thomas Czarniecki
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

import com.tomczarniecki.s3.S3Object;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executor;

public class ShowDetailsAction extends AbstractAction implements Controller.Callback {

    private final Controller controller;
    private final Display display;
    private final Executor executor;
    private final SwingWorker worker;

    public ShowDetailsAction(Controller controller, Display display, Executor executor, SwingWorker worker) {
        super("Show Details");
        this.controller = controller;
        this.display = display;
        this.executor = executor;
        this.worker = worker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (controller.isObjectSelected()) {
            executor.execute(() -> controller.getSelectedObject(ShowDetailsAction.this));
        }
    }

    @Override
    public void selectedObject(S3Object object) {
        worker.executeOnEventLoop(() -> {
            String msg = String.format(
                    "Key: %s\nSize: %s\nLast Modified: %s",
                    object.getKey(), object.getSize(), object.getLastModified()
            );
            display.showMessage("Details", msg);
        });
    }
}
