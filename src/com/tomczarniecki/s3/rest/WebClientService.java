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

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import com.tomczarniecki.s3.rest.WebClient.Callback;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class WebClientService implements Service {

    private static final long MAX_SINGLE_REQUEST_FILE_SIZE = 100 * FileUtils.ONE_MB;
    private static final long MULTIPART_CHUNK_SIZE = 10 * FileUtils.ONE_MB;

    private final MimeTypes mimeTypes = new MimeTypes();

    private final S3ObjectParser objectParser = new S3ObjectParser();
    private final S3BucketParser bucketParser = new S3BucketParser();
    private final InitiateUploadParser uploadParser = new InitiateUploadParser();

    private final Configuration credentials;
    private final WebRequestBuilder builder;
    private final WebClient client;

    public WebClientService(Configuration credentials) {
        this.builder = new WebRequestBuilder(credentials);
        this.client = new WebClient(credentials);
        this.credentials = credentials;
    }

    public List<S3Bucket> listAllMyBuckets() {
        WebRequest request = builder.build(Parameters.forAllBuckets());
        return client.process(request, new WebClient.Callback<List<S3Bucket>>() {
            public List<S3Bucket> process(int status, HttpMethod method) throws IOException {
                expect(HttpStatus.SC_OK, status);
                InputStream body = method.getResponseBodyAsStream();
                List<S3Bucket> list = bucketParser.parse(body);
                Collections.sort(list);
                return list;
            }
        });
    }

    public boolean bucketExists(String bucketName) {
        WebRequest request = builder.build(Parameters.forBucket(Method.HEAD, bucketName));
        return client.process(request, new WebClient.Callback<Boolean>() {
            public Boolean process(int status, HttpMethod method) {
                return HttpStatus.SC_NOT_FOUND != status;
            }
        });
    }

    public void createBucket(String bucketName) {
        Parameters parameters = Parameters.forBucket(Method.PUT, bucketName);
        client.process(builder.build(parameters), new Expect(HttpStatus.SC_OK));
    }

    public void deleteBucket(String bucketName) {
        Parameters parameters = Parameters.forBucket(Method.DELETE, bucketName);
        client.process(builder.build(parameters), new Expect(HttpStatus.SC_NO_CONTENT));
    }

    public List<S3Object> listObjectsInBucket(String bucketName) {
        WebRequest request = builder.build(Parameters.forBucket(Method.GET, bucketName));
        return client.process(request, new WebClient.Callback<List<S3Object>>() {
            public List<S3Object> process(int status, HttpMethod method) throws IOException {
                expect(HttpStatus.SC_OK, status);
                InputStream body = method.getResponseBodyAsStream();
                List<S3Object> list = objectParser.parse(body);
                Collections.sort(list);
                return list;
            }
        });
    }

    public boolean objectExists(String bucketName, String objectKey) {
        Parameters parameters = Parameters.forObject(Method.HEAD, bucketName, objectKey);
        return client.process(builder.build(parameters), new WebClient.Callback<Boolean>() {
            public Boolean process(int status, HttpMethod method) {
                return HttpStatus.SC_OK == status;
            }
        });
    }

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        long fileLength = source.length();
        if (fileLength > MAX_SINGLE_REQUEST_FILE_SIZE) {
            multipartFileUpload(bucketName, objectKey, source, listener);
        } else {
            singleRequestFileUpload(bucketName, objectKey, source, listener);
        }
    }

    private void multipartFileUpload(String bucketName, String objectKey, File source, ProgressListener listener) {
        String uploadId = initiateMultipartUpload(bucketName, objectKey, source);
        RuntimeException error = null;
        try {
            uploadParts(uploadId, bucketName, objectKey, source, listener);
        } catch (RuntimeException e) {
            error = e;
        }
        if (error == null) {
            completeMultipartUpload(uploadId);
        } else {
            abortMultipartUpload(uploadId);
        }
    }

    private String initiateMultipartUpload(String bucketName, String objectKey, File source) {
        String contentType = mimeTypes.get(source.getName());

        Headers headers = new Headers();
        headers.add("Content-Type", contentType);
        Parameters parameters = new Parameters(Method.POST, bucketName, objectKey, headers, SubResource.uploads, null);

        WebRequest request = builder.build(parameters);
        return client.process(request, new Callback<String>() {
            public String process(int status, HttpMethod method) throws IOException {
                expect(HttpStatus.SC_OK, status);
                InputStream body = method.getResponseBodyAsStream();
                return uploadParser.parse(body);
            }
        });
    }

    private void uploadParts(String uploadId, String bucketName, String objectKey, File source, ProgressListener listener) {
        throw new UnsupportedOperationException();
    }

    private void completeMultipartUpload(String uploadId) {
        throw new UnsupportedOperationException();
    }

    private void abortMultipartUpload(String uploadId) {
        throw new UnsupportedOperationException();
    }

    private void singleRequestFileUpload(String bucketName, String objectKey, File source, ProgressListener listener) {
        String contentType = mimeTypes.get(source.getName());

        Headers headers = new Headers();
        headers.add("Content-Length", source.length());
        headers.add("Content-Type", contentType);
        headers.add("Content-MD5", Files.computeMD5(source));

        Parameters parameters = Parameters.forObject(Method.PUT, bucketName, objectKey, headers);
        WebRequest request = builder.build(parameters);
        request.setFile(source, contentType, listener);

        client.process(request, new Expect(HttpStatus.SC_OK));
    }

    public String getPublicUrl(String bucketName, String objectKey, DateTime expires) {
        long expirySeconds = expires.getMillis() / 1000;
        Parameters parameters = Parameters.forObject(Method.GET, bucketName, objectKey, expirySeconds);

        QueryString query = new QueryString();
        query.add("AWSAccessKeyId", credentials.getAccessKeyId());
        query.add("Expires", Long.toString(expirySeconds));
        query.add("Signature", credentials.sign(parameters));

        return builder.createURL(parameters, query);
    }

    public void downloadObject(String bucketName, String objectKey, final File target, final ProgressListener listener) {
        Parameters parameters = Parameters.forObject(Method.GET, bucketName, objectKey);
        client.process(builder.build(parameters), new WebClient.Callback<Object>() {
            public Object process(int status, HttpMethod method) throws IOException {
                expect(HttpStatus.SC_OK, status);
                Header header = method.getResponseHeader("Content-Length");
                long length = Long.parseLong(header.getValue());
                InputStream body = method.getResponseBodyAsStream();
                Files.writeToFile(body, target, listener, length);
                return null;
            }
        });
    }

    public void deleteObject(String bucketName, String objectKey) {
        Parameters parameters = Parameters.forObject(Method.DELETE, bucketName, objectKey);
        client.process(builder.build(parameters), new Expect(HttpStatus.SC_NO_CONTENT));
    }

    private static void expect(int expectedStatusCode, int responseStatusCode) {
        if (expectedStatusCode != responseStatusCode) {
            throw new IllegalArgumentException("Unexpected response from AWS - expected ["
                    + expectedStatusCode + "], received [" + responseStatusCode + "]");
        }
    }

    private static class Expect implements WebClient.Callback<Object> {

        private int expected;

        private Expect(int expected) {
            this.expected = expected;
        }

        public Object process(int status, HttpMethod method) {
            expect(expected, status);
            return null;
        }
    }
}
