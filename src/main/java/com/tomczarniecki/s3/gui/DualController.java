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

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DualController implements Controller {

    private final Controller tree;
    private final Controller table;
    private final AtomicReference<Controller> active;

    public DualController(Controller tree, Controller table) {
        this.tree = tree;
        this.table = table;
        this.active = new AtomicReference<>(table);
    }

    public void activateTree() {
        active.set(tree);
    }

    public void activateTable() {
        active.set(table);
    }

    @Override
    public void addControllerListener(ControllerListener listener) {
        tree.addControllerListener(listener);
        table.addControllerListener(listener);
    }

    @Override
    public void refreshBuckets() {
        active.get().refreshBuckets();
    }

    @Override
    public List<String> bucketRegions() {
        return active.get().bucketRegions();
    }

    @Override
    public List<S3Bucket> listAllMyBuckets() {
        return active.get().listAllMyBuckets();
    }

    @Override
    public void removeFailedUploads(String bucketName) {
        active.get().removeFailedUploads(bucketName);
    }

    @Override
    public void createBucket(String bucketName, String region) {
        active.get().createBucket(bucketName, region);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return active.get().bucketExists(bucketName);
    }

    @Override
    public boolean isBucketSelected() {
        return active.get().isBucketSelected();
    }

    @Override
    public String getSelectedBucketName() {
        return active.get().getSelectedBucketName();
    }

    @Override
    public String getCurrentPrefix() {
        return active.get().getCurrentPrefix();
    }

    @Override
    public void deleteCurrentBucket() {
        active.get().deleteCurrentBucket();
    }

    @Override
    public void refreshObjects() {
        active.get().refreshObjects();
    }

    @Override
    public boolean isObjectSelected() {
        return active.get().isObjectSelected();
    }

    @Override
    public String getSelectedObjectKey() {
        return active.get().getSelectedObjectKey();
    }

    @Override
    public void getSelectedObject(Callback callback) {
        active.get().getSelectedObject(callback);
    }

    @Override
    public boolean objectExists(String bucketName, String objectKey) {
        return active.get().objectExists(bucketName, objectKey);
    }

    @Override
    public void createObject(String bucketName, String objectKey, File sourceFile, ProgressListener listener) {
        active.get().createObject(bucketName, objectKey, sourceFile, listener);
    }

    @Override
    public void downloadCurrentObject(File targetFile, ProgressListener listener) {
        active.get().downloadCurrentObject(targetFile, listener);
    }

    @Override
    public String getPublicUrlForCurrentObject(DateTime expires) {
        return active.get().getPublicUrlForCurrentObject(expires);
    }

    @Override
    public void deleteCurrentObject() {
        active.get().deleteCurrentObject();
    }
}
