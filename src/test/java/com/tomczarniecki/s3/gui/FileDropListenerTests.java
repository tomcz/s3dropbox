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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.mockito.BDDMockito.anyListOf;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FileDropListenerTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    Display display;
    @Mock
    DropBoxModel model;
    @Mock
    Controller controller;

    @Test
    public void shouldPassDroppedFilesToService() throws IOException {
        DirectWorker worker = new DirectWorker();
        ProgressDialog dialog = mock(ProgressDialog.class);

        given(display.createProgressDialog("Upload Progress", worker)).willReturn(dialog);
        given(controller.isShowingObjects()).willReturn(true);
        given(controller.getSelectedBucketName()).willReturn("bucket");

        File file1 = folder.newFile("file1.jpg");
        File file2 = folder.newFile("file2.doc");

        UploadWorker uploader = new UploadWorker(controller, display, worker);
        FileDropListener listener = new FileDropListener(controller, model, display, worker, uploader);
        listener.filesDropped(new File[]{file1, file2});

        verify(controller).createObject("bucket", "file1.jpg", file1, dialog);
        verify(controller).createObject("bucket", "file2.doc", file2, dialog);
    }

    @Test
    public void shouldSelectBucketAndThenPassDroppedFilesToService() throws IOException {
        DirectWorker worker = new DirectWorker();
        ProgressDialog dialog = mock(ProgressDialog.class);

        given(display.createProgressDialog("Upload Progress", worker)).willReturn(dialog);
        given(controller.isShowingObjects()).willReturn(false);
        given(display.selectOption(eq("Select Folder"), anyString(), anyListOf(String.class))).willReturn("bucket");
        given(controller.getSelectedBucketName()).willReturn("bucket");

        File file1 = folder.newFile("file1.jpg");
        File file2 = folder.newFile("file2.doc");

        UploadWorker uploader = new UploadWorker(controller, display, worker);
        FileDropListener listener = new FileDropListener(controller, model, display, worker, uploader);
        listener.filesDropped(new File[]{file1, file2});

        verify(controller).selectBucket("bucket");
        verify(controller).createObject("bucket", "file1.jpg", file1, dialog);
        verify(controller).createObject("bucket", "file2.doc", file2, dialog);
    }

    @Test
    public void shouldUploadFilesInDirectory() throws IOException {
        DirectWorker worker = new DirectWorker();
        ProgressDialog dialog = mock(ProgressDialog.class);

        given(display.createProgressDialog("Upload Progress", worker)).willReturn(dialog);
        given(controller.isShowingObjects()).willReturn(true);
        given(controller.getSelectedBucketName()).willReturn("bucket");

        File directory = folder.newFolder("folder");
        File file1 = new File(directory, "file1.jpg");
        File file2 = new File(directory, "file2.doc");

        FileUtils.touch(file1);
        FileUtils.touch(file2);

        UploadWorker uploader = new UploadWorker(controller, display, worker);
        FileDropListener listener = new FileDropListener(controller, model, display, worker, uploader);
        listener.filesDropped(new File[]{directory});

        verify(controller).createObject("bucket", "folder/file1.jpg", file1, dialog);
        verify(controller).createObject("bucket", "folder/file2.doc", file2, dialog);
    }
}
