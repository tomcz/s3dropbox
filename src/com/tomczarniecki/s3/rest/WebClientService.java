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
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static com.tomczarniecki.s3.Lists.newArrayList;

public class WebClientService implements Service {

    private static final long MAX_SINGLE_UPLOAD_FILE_SIZE = 64 * FileUtils.ONE_MB;
    private static final long MULTIPART_CHUNK_SIZE = 5 * FileUtils.ONE_MB;

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        if (source.length() <= MAX_SINGLE_UPLOAD_FILE_SIZE) {
            singleRequestUpload(bucketName, objectKey, source, listener);
        } else {
            multipartFileUpload(bucketName, objectKey, source, listener);
        }
    }

    private void singleRequestUpload(String bucketName, String objectKey, File source, ProgressListener listener) {
        ProgressListenerAdaptor adaptor = new ProgressListenerAdaptor(listener, source.length());
        client.putObject(new PutObjectRequest(bucketName, objectKey, source).withProgressListener(adaptor));
    }

    private void multipartFileUpload(String bucketName, String objectKey, File source, ProgressListener listener) {
        String uploadId = initiateMultipartUpload(bucketName, objectKey);
        try {
            List<PartETag> uploadETags = uploadParts(uploadId, bucketName, objectKey, source, listener);
            completeUpload(bucketName, objectKey, uploadId, uploadETags);

        } catch (RuntimeException e) {
            abortUpload(bucketName, objectKey, uploadId);
            throw e;
        }
    }

    private String initiateMultipartUpload(String bucketName, String objectKey) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
        InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    private List<PartETag> uploadParts(String uploadId, String bucketName, String objectKey, File source,
                                       ProgressListener listener) {

        List<PartETag> uploadETags = newArrayList();

        long filePosition = 0;
        long fileLength = source.length();

        ProgressListenerAdaptor adaptor = new ProgressListenerAdaptor(listener, fileLength);

        for (int i = 1; filePosition < fileLength; i++) {
            long partSize = Math.min(MULTIPART_CHUNK_SIZE, fileLength - filePosition);

            UploadPartRequest request = new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(objectKey)
                    .withUploadId(uploadId)
                    .withPartNumber(i)
                    .withFile(source)
                    .withFileOffset(filePosition)
                    .withPartSize(partSize)
                    .withProgressListener(adaptor);

            UploadPartResult result = client.uploadPart(request);
            uploadETags.add(result.getPartETag());
        }
        return uploadETags;
    }

    private void completeUpload(String bucketName, String objectKey, String uploadId, List<PartETag> uploadETags) {
        client.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, uploadETags));
    }

    private void abortUpload(String bucketName, String objectKey, String uploadId) {
        try {
            client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, objectKey, uploadId));
        } catch (RuntimeException e) {
            logger.warn("Unable to abort upload for bucket [" + bucketName + "], object ["
                    + objectKey + "] and upload id [" + uploadId + "]", e);
        }
    }

    public void downloadObject(String bucketName, String objectKey, File target, ProgressListener listener) {
        com.amazonaws.services.s3.model.S3Object object = client.getObject(bucketName, objectKey);
        Files.writeToFile(object.getObjectContent(), target, listener, object.getObjectMetadata().getContentLength());
    }

    public String getPublicUrl(String bucketName, String objectKey, DateTime expires) {
        return client.generatePresignedUrl(bucketName, objectKey, expires.toDate()).toExternalForm();
    }

    public void deleteObject(String bucketName, String objectKey) {
        client.deleteObject(bucketName, objectKey);
    }
}
