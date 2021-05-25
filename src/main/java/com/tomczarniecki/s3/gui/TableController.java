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

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.S3ObjectList;
import com.tomczarniecki.s3.Service;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

class TableController implements Controller {

    private final Service service;
    private final DropBoxTableModel model;
    private final Announcer<ControllerListener> announcer;

    private boolean showingObjects;
    private String selectedBucketName;
    private String selectedObjectKey;
    private String nextMarker = "";

    public TableController(Service service, Worker worker) {
        this.announcer = Announcer.createFor(ControllerListener.class);
        this.model = new DropBoxTableModel(worker);
        this.service = service;
    }

    public DropBoxTableModel getModel() {
        return model;
    }

    @Override
    public void addControllerListener(ControllerListener listener) {
        announcer.add(listener);
    }

    public void refreshBuckets() {
        List<S3Bucket> buckets = service.listAllMyBuckets();
        model.updatedBuckets(buckets);
        selectedBucketName = null;
        selectedObjectKey = null;
        showingObjects = false;
        announcer.announce().showingBuckets();
    }

    public void refreshObjects() {
        showObjects(false);
    }

    public void showObjects(boolean useNextMarker) {
        if (!useNextMarker) {
            nextMarker = "";
        }
        S3ObjectList objects = service.listObjectsInBucket(selectedBucketName, nextMarker);
        model.updatedObjects(objects);
        nextMarker = objects.getNextMarker();
        selectedObjectKey = null;
        showingObjects = true;
        announcer.announce().showingObjects(selectedBucketName);
    }

    public void updateSelectedName(int index) {
        String name = model.getNameAt(index);
        if (showingObjects) {
            selectedObjectKey = name;
        } else {
            selectedBucketName = name;
        }
    }

    public String getSelectedBucketName() {
        return selectedBucketName;
    }

    public String getSelectedObjectKey() {
        return selectedObjectKey;
    }

    public List<String> bucketRegions() {
        return service.bucketRegions();
    }

    public boolean bucketExists(String bucketName) {
        return service.bucketExists(bucketName);
    }

    public void createBucket(String bucketName, String region) {
        service.createBucket(bucketName, region);
        refreshBuckets();
    }

    public void deleteCurrentBucket() {
        service.deleteBucket(selectedBucketName);
        refreshBuckets();
    }

    public boolean objectExists(String bucketName, String objectKey) {
        return service.objectExists(bucketName, objectKey);
    }

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        service.createObject(bucketName, objectKey, source, listener);
        showObjects(false);
    }

    public void deleteCurrentObject() {
        service.deleteObject(selectedBucketName, selectedObjectKey);
        showObjects(false);
    }

    public void downloadCurrentObject(File target, ProgressListener listener) {
        service.downloadObject(selectedBucketName, selectedObjectKey, target, listener);
    }

    public String getPublicUrlForCurrentObject(DateTime expires) {
        return service.getPublicUrl(selectedBucketName, selectedObjectKey, expires);
    }

    public boolean isBucketSelected() {
        return selectedBucketName != null;
    }

    public boolean isObjectSelected() {
        return (selectedObjectKey != null)
                && !Constants.BACK_LINK.equals(selectedObjectKey)
                && !Constants.MORE_LINK.equals(selectedObjectKey);
    }

    public boolean canShowBuckets() {
        return showingObjects && Constants.BACK_LINK.equals(selectedObjectKey);
    }

    public boolean canShowObjects() {
        return !showingObjects && isBucketSelected();
    }

    public boolean canShowMoreObjects() {
        return showingObjects && Constants.MORE_LINK.equals(selectedObjectKey);
    }

    public List<S3Bucket> listAllMyBuckets() {
        return service.listAllMyBuckets();
    }

    public void removeFailedUploads(String bucketName) {
        service.removeFailedUploads(bucketName);
    }

    public void getSelectedObject(Callback callback) {
        S3Object object = service.getObject(selectedBucketName, selectedObjectKey);
        callback.selectedObject(object);
    }
}
