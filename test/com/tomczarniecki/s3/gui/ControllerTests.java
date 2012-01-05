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

import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;
import com.tomczarniecki.s3.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.tomczarniecki.s3.Generics.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTests {

    @Mock Service service;

    @Test
    public void shouldNotifyListenersWhenBucketsAreUpdated() {
        ControllerListener listener = mock(ControllerListener.class);

        List<S3Bucket> buckets = newArrayList();
        given(service.listAllMyBuckets()).willReturn(buckets);

        Controller controller = new Controller(service);
        controller.addListener(listener);
        controller.showBuckets();

        verify(listener).updatedBuckets(buckets);
    }

    @Test
    public void shouldNotifyListenersWhenObjectsAreLoaded() {
        ControllerListener listener = mock(ControllerListener.class);

        List<S3Object> objects = newArrayList();
        given(service.listObjectsInBucket("bucket")).willReturn(objects);

        Controller controller = new Controller(service);
        controller.addListener(listener);
        controller.selectBucket("bucket");
        controller.showObjects();

        verify(listener).updatedObjects("bucket", objects);
    }

    @Test
    public void shouldShowBucketsWhenObjectsAreVisibleAndBackLinkIsSelected() {
        Controller controller = new Controller(service);
        controller.selectBucket("bucket");
        controller.showObjects();
        controller.updateSelectedName(Constants.BACK_LINK);

        assertThat(controller.canShowBuckets(), equalTo(true));
        assertThat(controller.canShowObjects(), equalTo(false));
    }

    @Test
    public void shouldShowObjectsWhenBucketsAreVisibleAndBucketIsSelected() {
        Controller controller = new Controller(service);
        controller.showBuckets();
        controller.updateSelectedName("bucket");

        assertThat(controller.canShowObjects(), equalTo(true));
        assertThat(controller.canShowBuckets(), equalTo(false));
    }

    @Test
    public void shouldReportObjectAsSelectedWhenSelectedObjectKeyIsNotBackLink() {
        Controller controller = new Controller(service);
        controller.selectBucket("bucket");
        controller.showObjects();
        controller.updateSelectedName("bucket");

        assertThat(controller.isObjectSelected(), equalTo(true));
    }

    @Test
    public void shouldReportObjectAsNotSelectedWhenSelectedObjectKeyIsBackLink() {
        Controller controller = new Controller(service);
        controller.selectBucket("bucket");
        controller.showObjects();
        controller.updateSelectedName(Constants.BACK_LINK);

        assertThat(controller.isObjectSelected(), equalTo(false));
    }
}
