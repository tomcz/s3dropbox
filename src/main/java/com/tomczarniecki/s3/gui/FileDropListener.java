/*
 * Copyright (c) 2009, Thomas Czarniecki
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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

class FileDropListener implements FileDrop.Listener {

    private final Controller controller;
    private final UploadWorker uploader;
    private final Display display;
    private final Worker worker;

    public FileDropListener(Controller controller, Display display, Worker worker, UploadWorker uploader) {
        this.controller = controller;
        this.uploader = uploader;
        this.display = display;
        this.worker = worker;
    }

    public void filesDropped(final File[] files) {
        if (controller.isBucketSelected()) {
            String bucketName = controller.getSelectedBucketName();
            worker.executeInBackground(() -> uploader.uploadFiles(bucketName, files));
        } else {
            worker.executeInBackground(() -> {
                List<S3Bucket> buckets = controller.listAllMyBuckets();
                List<String> names = buckets.stream().map(S3Bucket::getName).collect(Collectors.toList());
                worker.executeOnEventLoop(() -> selectBucketAndUploadFiles(names, files));
            });
        }
    }

    private void selectBucketAndUploadFiles(List<String> names, File[] files) {
        String bucketName = display.selectOption("Select Folder", "Please choose a folder for your files.", names);
        if (bucketName != null) {
            worker.executeInBackground(() -> uploader.uploadFiles(bucketName, files));
        }
    }
}
