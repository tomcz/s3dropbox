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

import com.tomczarniecki.s3.S3Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.List;

public class RemoveFailedUploadsAction extends AbstractAction {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Controller controller;
    private final ProgressDialog dialog;
    private final Worker worker;

    public RemoveFailedUploadsAction(Controller controller, Display display, Worker worker) {
        super("Remove Failed Uploads");
        this.dialog = display.createProgressDialog("Progress", worker);
        this.controller = controller;
        this.worker = worker;
    }

    public void actionPerformed(ActionEvent e) {
        worker.executeInBackground(new Runnable() {
            public void run() {
                dialog.begin();
                try {
                    dialog.append("Removing failed uploads from:\n");
                    List<S3Bucket> buckets = controller.listAllMyBuckets();
                    for (int i = 0; i < buckets.size(); i++) {
                        S3Bucket bucket = buckets.get(i);
                        dialog.append("%s ... ", bucket.getName());
                        controller.removeFailedUploads(bucket.getName());
                        dialog.processed(i + 1, buckets.size());
                        dialog.append("Done\n");
                    }
                } catch (Exception e) {
                    logger.info("Cleanup failed", e);
                    dialog.append("\n\nERROR - %s", e.toString());

                } finally {
                    dialog.finish();
                }
            }
        });
    }
}
