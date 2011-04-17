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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executor;

class DeleteBucketAction extends AbstractAction {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Controller controller;
    private final Executor executor;
    private final Display display;

    public DeleteBucketAction(Controller controller, Display display, Executor executor) {
        super("Delete Folder");
        this.controller = controller;
        this.executor = executor;
        this.display = display;
    }

    public void actionPerformed(ActionEvent evt) {
        if (controller.isBucketSelected() && confirmDeletion()) {
            deleteBucket();
        }
    }

    private void deleteBucket() {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    controller.deleteCurrentBucket();
                } catch (Exception e) {
                    logger.info("Delete failed", e);
                    deleteError();
                }
            }
        });
    }

    private boolean confirmDeletion() {
        String text = "Are you sure that you want to delete folder %s?\nYou will not be able to undo this action.";
        return display.confirmMessage("Just Checking", String.format(text, controller.getSelectedBucketName()));
    }

    private void deleteError() {
        String text = "Cannot delete folder %s.\nPlease make sure that it is empty and try again.";
        display.showErrorMessage("Delete failed", String.format(text, controller.getSelectedBucketName()));
    }
}
