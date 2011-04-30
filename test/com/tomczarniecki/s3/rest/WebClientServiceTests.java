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

import com.amazonaws.services.s3.model.Region;
import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

public class WebClientServiceTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static Configuration credentials;

    @BeforeClass
    public static void setUp() throws Exception {
        assumeThat(Boolean.getBoolean("ignore.integration.tests"), equalTo(false));
        credentials = new ConfigurationFactory(null).load();
    }

    @Test
    public void shouldListAllMyBuckets() throws Exception {
        Service service = new WebClientService(credentials);
        List<S3Bucket> list = service.listAllMyBuckets();
        assertFalse("Should have some buckets", list.isEmpty());
    }

    @Test
    public void shouldNotFindRandomBucket() throws Exception {
        String bucketName = "test-" + UUID.randomUUID();
        Service service = new WebClientService(credentials);
        assertFalse("Random bucket should not exist", service.bucketExists(bucketName));
    }

    @Test
    public void shouldExpectToFindPublicBucket() throws Exception {
        Service service = new WebClientService(credentials);
        assertTrue("'Public' bucket should exist", service.bucketExists("Public"));
        assertTrue("'public' bucket should exist", service.bucketExists("public"));
    }

    @Test
    public void shouldCreateAndDeleteBucket() throws Exception {
        Service service = new WebClientService(credentials);
        String bucketName = "test-" + UUID.randomUUID();

        service.createBucket(bucketName, Region.AP_Singapore.name());
        assertTrue("Bucket should exist", service.bucketExists(bucketName));
        assertThat(service.listAllMyBuckets(), hasItem(bucket(bucketName)));

        service.deleteBucket(bucketName);
        assertFalse("Bucket should not exist after deletion", service.bucketExists(bucketName));
        assertThat(service.listAllMyBuckets(), not(hasItem(bucket(bucketName))));
    }

    @Test
    public void shouldCreateDownloadAndDeleteObject() throws Exception {
        ProgressListener listener = mock(ProgressListener.class);
        String fileContents = UUID.randomUUID().toString();

        String bucketName = "test-" + UUID.randomUUID();

        Service service = new WebClientService(credentials);
        service.createBucket(bucketName, null);

        File file = folder.newFile("foo.txt");
        FileUtils.writeStringToFile(file, fileContents);

        assertFalse("Object should not exist", service.objectExists(bucketName, file.getName()));
        service.createObject(bucketName, file.getName(), file, listener);

        assertTrue("Object should exist", service.objectExists(bucketName, file.getName()));
        assertThat(service.listObjectsInBucket(bucketName), hasItem(object(file.getName())));

        List<S3Object> objects = service.listObjectsInBucket(bucketName);
        assertThat("Bucket should not be empty after object creation", objects.size(), equalTo(1));

        S3Object object = objects.get(0);
        assertThat("Bad object key", object.getKey(), equalTo(file.getName()));
        assertThat("Bad object size", object.getSize(), equalTo(file.length()));

        File saved = folder.newFile("saved.txt");
        service.downloadObject(bucketName, file.getName(), saved, listener);
        assertThat("Corrupted download", FileUtils.readFileToString(saved), equalTo(fileContents));

        service.deleteObject(bucketName, file.getName());

        objects = service.listObjectsInBucket(bucketName);
        assertThat("Bucket should be empty after object deletion", objects.size(), equalTo(0));
        assertThat(service.listObjectsInBucket(bucketName), not(hasItem(object(file.getName()))));

        service.deleteBucket(bucketName);
    }

    @Test
    public void shouldDownloadFileUsingPublicLink() throws Exception {
        ProgressListener listener = mock(ProgressListener.class);
        String fileContents = UUID.randomUUID().toString();

        String bucketName = "test-" + UUID.randomUUID();

        Service service = new WebClientService(credentials);
        service.createBucket(bucketName, null);

        File file = folder.newFile("foo.txt");
        FileUtils.writeStringToFile(file, fileContents);

        service.createObject(bucketName, file.getName(), file, listener);

        String publicUrl = service.getPublicUrl(bucketName, file.getName(), new DateTime().plusDays(5));

        File saved = folder.newFile("saved.txt");

        downloadToFile(publicUrl, saved);

        assertThat("Corrupted download", FileUtils.readFileToString(saved), equalTo(fileContents));

        service.deleteObject(bucketName, file.getName());

        service.deleteBucket(bucketName);
    }

    private Matcher<S3Bucket> bucket(String bucketName) {
        return hasProperty("name", equalTo(bucketName));
    }

    private Matcher<S3Object> object(String key) {
        return hasProperty("key", equalTo(key));
    }

    private void downloadToFile(String publicUrl, File saved) throws Exception {
        InputStream input = null;
        FileOutputStream output = null;
        try {
            input = new URL(publicUrl).openStream();
            output = new FileOutputStream(saved);
            IOUtils.copy(input, output);

        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }
    }
}
