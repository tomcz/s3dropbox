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

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BucketNameValidatorTests {

    @Mock
    Controller controller;

    @Test
    public void shouldNotPermitShortBucketNames() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("oh"));
    }

    @Test
    public void shouldNotPermitVeryLongBucketNames() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate(RandomStringUtils.randomAlphanumeric(64)));
    }

    @Test
    public void shouldNotPermitBucketNamesWithIllegalCharacters() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("ca#h"));
    }

    @Test
    public void shouldNotPermitExistingBucketNames() {
        given(controller.bucketExists("bucket")).willReturn(true);
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("bucket"));
    }

    @Test
    public void shouldPermitValidBucketNameThatDoesNotYetExist() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNull(validator.validate("bucket"));
        verify(controller).bucketExists("bucket");
    }

    @Test
    public void shouldNotPermitNamesThatStartWithADash() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("-hello"));
    }

    @Test
    public void shouldNotPermitNamesThatEndInADash() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("hello-"));
    }

    @Test
    public void shouldNotPermitNamesInIPAddressFormat() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("192.168.0.1"));
    }

    @Test
    public void shouldNotPermitNamesWithPeriodAndDashSideBySide() {
        BucketNameValidator validator = new BucketNameValidator(controller);
        assertNotNull(validator.validate("hello.-there"));
        assertNotNull(validator.validate("hello-.there"));
    }
}
