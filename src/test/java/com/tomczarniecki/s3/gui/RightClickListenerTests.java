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

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseEvent;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RightClickListenerTests {

    @Mock
    Controller controller;
    @Mock
    MouseEvent event;
    @Mock
    JPopupMenu menu;
    @Mock
    Display display;

    @Test
    public void shouldNotShowPopupWhenObjectIsNotSelected() {
        given(display.createPopupMenu()).willReturn(menu);
        given(event.isPopupTrigger()).willReturn(true);
        given(controller.isObjectSelected()).willReturn(false);

        RightClickListener listener = new RightClickListener(controller, display);
        listener.mousePressed(event);

        verifyZeroInteractions(menu);
    }

    @Test
    public void shouldShowPopupWhenObjectIsSelected() {
        given(display.createPopupMenu()).willReturn(menu);
        given(event.isPopupTrigger()).willReturn(true);
        given(controller.isObjectSelected()).willReturn(true);

        RightClickListener listener = new RightClickListener(controller, display);
        listener.mousePressed(event);

        verify(menu).show(any(Component.class), anyInt(), anyInt());
    }
}
