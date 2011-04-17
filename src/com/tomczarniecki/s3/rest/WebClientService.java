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
package com.tomczarniecki.s3.rest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.List;

import static com.tomczarniecki.s3.Lists.newArrayList;

public class WebClientService implements Service {

    private final AmazonS3 client;

    public WebClientService(Configuration configuration) {
        this.client = new NtlmFriendlyAmazonS3Client(configuration);
    }

    public List<S3Bucket> listAllMyBuckets() {
        List<S3Bucket> buckets = newArrayList();
        for (Bucket bucket : client.listBuckets()) {
            buckets.add(new S3Bucket(bucket.getName()));
        }
        return buckets;
    }

    public boolean bucketExists(String bucketName) {
        return client.doesBucketExist(bucketName);
    }

    public void createBucket(String bucketName) {
        client.createBucket(bucketName);
    }

    public void deleteBucket(String bucketName) {
        client.deleteBucket(bucketName);
    }

    public List<S3Object> listObjectsInBucket(String bucketName) {
        List<S3Object> objects = newArrayList();
        ObjectListing objectListing = client.listObjects(bucketName);
        for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
            DateTime lastModified = new DateTime(summary.getLastModified());
            lastModified = lastModified.toDateTime(DateTimeZone.getDefault());
            objects.add(new S3Object(summary.getKey(), summary.getSize(), lastModified.toLocalDateTime()));
        }
        return objects;
    }

    public boolean objectExists(String bucketName, String objectKey) {
        ObjectListing objectListing = client.listObjects(bucketName, objectKey);
        for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
            if (summary.getKey().equals(objectKey)) {
                return true;
            }
        }
        return false;
    }

    public String getPublicUrl(String bucketName, String objectKey, DateTime expires) {
        return client.generatePresignedUrl(bucketName, objectKey, expires.toDate()).toExternalForm();
    }

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        ProgressListenerAdaptor adaptor = new ProgressListenerAdaptor(listener, source.length());
        client.putObject(new PutObjectRequest(bucketName, objectKey, source).withProgressListener(adaptor));
    }

    public void downloadObject(String bucketName, String objectKey, File target, ProgressListener listener) {
        com.amazonaws.services.s3.model.S3Object object = client.getObject(bucketName, objectKey);
        Files.writeToFile(object.getObjectContent(), target, listener, object.getObjectMetadata().getContentLength());
    }

    public void deleteObject(String bucketName, String objectKey) {
        client.deleteObject(bucketName, objectKey);
    }
}
