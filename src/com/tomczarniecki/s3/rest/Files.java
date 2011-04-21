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

import com.amazonaws.services.s3.model.S3Object;
import com.tomczarniecki.s3.ProgressListener;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Files {

    public static Properties loadProperties(File file) {
        FileInputStream input = null;
        try {
            Properties props = new Properties();
            input = new FileInputStream(file);
            props.load(input);
            return props;

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static void saveProperties(File file, Properties props) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            props.store(out, null);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public static void writeToFile(S3Object object, File target, ProgressListener listener) {
        long fileLength = object.getObjectMetadata().getContentLength();
        writeToFile(object.getObjectContent(), target, listener, fileLength);
    }

    public static void writeToFile(InputStream input, File file, ProgressListener listener, long length) {
        OutputStream output = null;
        try {
            output = new CountingOutputStream(new FileOutputStream(file), listener, length);
            IOUtils.copy(input, output);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }
}
