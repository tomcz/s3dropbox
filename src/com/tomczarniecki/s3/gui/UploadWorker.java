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

import com.tomczarniecki.s3.FileSize;
import com.tomczarniecki.s3.Lists;
import com.tomczarniecki.s3.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class UploadWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProgressDialog dialog;
    private final FileSize fileSize;

    private final Controller controller;
    private final Display display;

    public UploadWorker(Controller controller, Display display, Worker worker) {
        this.dialog = display.createProgressDialog("Upload Progress", worker);
        this.fileSize = new FileSize();
        this.controller = controller;
        this.display = display;
    }

    public void uploadFiles(final File[] files) {
        dialog.begin();
        try {
            uploadFiles(resolveKeys(files));
            dialog.append("\nDone");

        } catch (Exception e) {
            logger.warn("Upload failed", e);
            dialog.append(" ERROR\n --> %s\n", e.toString());

        } finally {
            dialog.finish();
        }
    }

    private void uploadFiles(List<Pair<String, File>> files) {
        String bucketName = controller.getSelectedBucketName();
        String plural = (files.size() > 1) ? "files" : "file";
        dialog.append("Attempting upload of %d %s to folder %s\n\n", files.size(), plural, bucketName);

        for (Pair<String, File> entry : files) {
            uploadFile(bucketName, entry.getKey(), entry.getValue());
        }
    }

    private void uploadFile(String bucketName, String objectKey, File file) {
        if (canCreateObject(bucketName, objectKey, file)) {
            attemptObjectCreation(bucketName, objectKey, file);
        } else {
            dialog.append("File %s not uploaded\n", file.getAbsolutePath());
        }
    }

    private boolean canCreateObject(String bucketName, String objectKey, File file) {
        if (controller.objectExists(bucketName, objectKey)) {
            String message = "File %s already exists in folder %s.\nDo you want to overwrite?";
            return display.confirmMessage("Oops", String.format(message, file.getName(), bucketName));
        }
        return true;
    }

    private void attemptObjectCreation(String bucketName, String objectKey, File file) {
        dialog.next();
        try {
            dialog.append("File %s (%s) ...", file.getAbsolutePath(), fileSize.format(file.length()));
            controller.createObject(bucketName, objectKey, file, dialog);
            dialog.append(" OK\n");

        } catch (RuntimeException e) {
            logger.warn("Upload failed for " + file, e);
            dialog.append(" ERROR\n --> %s\n", e.toString());
        }
    }

    private List<Pair<String, File>> resolveKeys(File[] files) {
        List<Pair<String, File>> list = Lists.newArrayList();
        resolveFolders(list, files, "");
        return list;
    }

    private void resolveFolders(List<Pair<String, File>> list, File[] folder, String prefix) {
        for (File file : folder) {
            resolveFile(list, file, prefix);
        }
    }

    private void resolveFile(List<Pair<String, File>> list, File file, String prefix) {
        if (file.isFile()) {
            list.add(Pair.create(prefix + file.getName(), file));
        } else if (file.isDirectory()) {
            resolveFolders(list, file.listFiles(), prefix + file.getName() + "/");
        }
    }
}
