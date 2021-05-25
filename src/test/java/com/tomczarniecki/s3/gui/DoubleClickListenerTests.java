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

import java.awt.event.MouseEvent;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DoubleClickListenerTests {

    @Mock
    TableController controller;

    @Mock
    MouseEvent event;

    @Test
    public void shouldNotTriggerWhenNotDoubleClicked() {
        given(event.getClickCount()).willReturn(1);

        DoubleClickListener listener = new DoubleClickListener(controller, new DirectExecutor());
        listener.mouseClicked(event);

        verifyZeroInteractions(controller);
    }

    @Test
    public void shouldShowBucketsWhenDoubleClicked() {
        given(event.getClickCount()).willReturn(2);
        given(controller.canShowBuckets()).willReturn(true);

        DoubleClickListener listener = new DoubleClickListener(controller, new DirectExecutor());
        listener.mouseClicked(event);

        verify(controller).refreshBuckets();
    }

    @Test
    public void shouldShowObjectsWhenDoubleClicked() {
        given(event.getClickCount()).willReturn(2);
        given(controller.canShowObjects()).willReturn(true);

        DoubleClickListener listener = new DoubleClickListener(controller, new DirectExecutor());
        listener.mouseClicked(event);

        verify(controller).showObjects(false);
    }
}
