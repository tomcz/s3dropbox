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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CreateBucketActionTests {

    @Mock Display display;
    @Mock Controller controller;

    @Test
    public void shouldCreateBucketWhenValidNameIsProvided() {
        given(display.getInput(eq("Create Folder"), anyString())).willReturn("bucket");

        CreateBucketAction action = new CreateBucketAction(controller, display, new DirectExecutor());
        action.actionPerformed(null);

        verify(controller).createBucket("bucket");
    }

    @Test
    public void shouldNotAttemptToCreateBucketWhenUserDoesNotProvideBucketName() {
        CreateBucketAction action = new CreateBucketAction(controller, display, new DirectExecutor());
        action.actionPerformed(null);
        verifyZeroInteractions(controller);
    }

    @Test
    public void shouldNotAttemptToCreateBucketWhenInvalidBucketNameIsProvided() {
        given(display.getInput(eq("Create Folder"), anyString())).willReturn("bucket");
        given(controller.bucketExists("bucket")).willReturn(true);

        CreateBucketAction action = new CreateBucketAction(controller, display, new DirectExecutor());
        action.actionPerformed(null);

        verify(controller, never()).createBucket("bucket");
    }

    @Test
    public void shouldCreateBucketFollowingRetryOnBucketName() {
        given(display.getInput(eq("Create Folder"), anyString())).willReturn("bucket");
        given(controller.bucketExists("bucket")).willReturn(true);
        given(display.confirmMessage(eq("Oops"), anyString())).willReturn(true);
        given(display.getInput(eq("Create Folder"), anyString())).willReturn("foo");

        CreateBucketAction action = new CreateBucketAction(controller, display, new DirectExecutor());
        action.actionPerformed(null);

        verify(controller).createBucket("foo");
    }
}
