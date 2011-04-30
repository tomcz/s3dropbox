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
import com.tomczarniecki.s3.Service;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

class Controller {

    private final Announcer<ControllerListener> announcer;
    private final Service service;

    private boolean showingObjects;
    private String selectedBucketName;
    private String selectedObjectKey;

    public Controller(Service service) {
        this.announcer = Announcer.createFor(ControllerListener.class);
        this.service = service;
    }

    public void addListener(ControllerListener listener) {
        announcer.add(listener);
    }

    public void showBuckets() {
        List<S3Bucket> buckets = service.listAllMyBuckets();
        selectedBucketName = null;
        selectedObjectKey = null;
        showingObjects = false;
        announcer.announce().updatedBuckets(buckets);
    }

    public void showObjects() {
        List<S3Object> objects = service.listObjectsInBucket(selectedBucketName);
        selectedObjectKey = null;
        showingObjects = true;
        announcer.announce().updatedObjects(selectedBucketName, objects);
    }

    public void updateSelectedName(String name) {
        if (showingObjects) {
            selectedObjectKey = name;
        } else {
            selectedBucketName = name;
        }
    }

    public void selectBucket(String bucketName) {
        selectedBucketName = bucketName;
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
        showBuckets();
    }

    public void deleteCurrentBucket() {
        service.deleteBucket(selectedBucketName);
        showBuckets();
    }

    public boolean objectExists(String bucketName, String objectKey) {
        return service.objectExists(bucketName, objectKey);
    }

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        service.createObject(bucketName, objectKey, source, listener);
        showObjects();
    }

    public void deleteCurrentObject() {
        service.deleteObject(selectedBucketName, selectedObjectKey);
        showObjects();
    }

    public void downloadCurrentObject(File target, ProgressListener listener) {
        service.downloadObject(selectedBucketName, selectedObjectKey, target, listener);
    }

    public String getPublicUrlForCurrentObject(DateTime expires) {
        return service.getPublicUrl(selectedBucketName, selectedObjectKey, expires);
    }

    public boolean isShowingObjects() {
        return showingObjects;
    }

    public boolean isBucketSelected() {
        return selectedBucketName != null;
    }

    public boolean isObjectSelected() {
        return (selectedObjectKey != null) && !isBackLinkSelected();
    }

    public boolean canShowBuckets() {
        return showingObjects && isBackLinkSelected();
    }

    public boolean canShowObjects() {
        return !showingObjects && isBucketSelected();
    }

    private boolean isBackLinkSelected() {
        return Constants.BACK_LINK.equals(selectedObjectKey);
    }
}
