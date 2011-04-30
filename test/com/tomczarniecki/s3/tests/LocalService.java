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
package com.tomczarniecki.s3.tests;

import com.tomczarniecki.s3.ProgressListener;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.tomczarniecki.s3.Lists.newArrayList;

public class LocalService implements Service {

    private final File root;

    public LocalService() {
        root = new File(SystemUtils.USER_DIR, "build");
        root.mkdirs();
    }

    public List<S3Bucket> listAllMyBuckets() {
        List<S3Bucket> buckets = newArrayList();
        FileFilter filter = DirectoryFileFilter.INSTANCE;
        for (File dir : root.listFiles(filter)) {
            buckets.add(new S3Bucket(dir.getName()));
        }
        return buckets;
    }

    public boolean bucketExists(String bucketName) {
        return bucketFile(bucketName).isDirectory();
    }

    public List<String> bucketRegions() {
        return Collections.singletonList("Local");
    }

    public void createBucket(String bucketName, String region) {
        bucketFile(bucketName).mkdirs();
    }

    public void deleteBucket(String bucketName) {
        File file = bucketFile(bucketName);
        Validate.isTrue(file.delete(), "Cannot delete ", file);
    }

    public List<S3Object> listObjectsInBucket(String bucketName) {
        List<S3Object> objects = newArrayList();
        FileFilter filter = FileFileFilter.FILE;
        for (File file : bucketFile(bucketName).listFiles(filter)) {
            objects.add(new S3Object(file.getName(), file.length(), new LocalDateTime(file.lastModified())));
        }
        return objects;
    }

    public boolean objectExists(String bucketName, String objectKey) {
        return objectFile(bucketName, objectKey).isFile();
    }

    public void createObject(String bucketName, String objectKey, File source, ProgressListener listener) {
        try {
            FileUtils.copyFile(source, objectFile(bucketName, objectKey));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPublicUrl(String bucketName, String objectKey, DateTime expires) {
        LocalDateTime local = new LocalDateTime(expires);
        return objectFile(bucketName, objectKey).toURI() + "?expires=" + local.toString("yyyy-MM-dd-HH-mm-ss");
    }

    public void downloadObject(String bucketName, String objectKey, File target, ProgressListener listener) {
        try {
            FileUtils.copyFile(objectFile(bucketName, objectKey), target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteObject(String bucketName, String objectKey) {
        File file = objectFile(bucketName, objectKey);
        Validate.isTrue(file.delete(), "Cannot delete ", file);
    }

    private File bucketFile(String bucketName) {
        return new File(root, bucketName);
    }

    private File objectFile(String bucketName, String objectKey) {
        return new File(bucketFile(bucketName), objectKey);
    }
}
