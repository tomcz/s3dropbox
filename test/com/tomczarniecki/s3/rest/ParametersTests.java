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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParametersTests {

    @Test
    public void createsExpectedStringForObjectGet() throws Exception {
        Headers headers = new Headers();
        headers.add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        Parameters parameters = Parameters.forObject(Method.GET, "johnsmith", "puppy.jpg", headers);

        String expected = "GET\n\n\nTue, 27 Mar 2007 19:36:42 +0000\n/johnsmith/puppy.jpg";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForObjectGetWithPath() throws Exception {
        Headers headers = new Headers();
        headers.add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        Parameters parameters = Parameters.forObject(Method.GET, "johnsmith", "/cute/puppy.jpg", headers);

        String expected = "GET\n\n\nTue, 27 Mar 2007 19:36:42 +0000\n/johnsmith/cute/puppy.jpg";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForObjectPut() throws Exception {
        Headers headers = new Headers();
        headers.add("Date", "Tue, 27 Mar 2007 21:15:45 +0000");
        headers.add("Content-MD5", "4gJE4saaMU4BqNR0kLY+lw==");
        headers.add("Content-Type", "image/jpeg");
        headers.add("Content-Length", "94328");

        Parameters parameters = Parameters.forObject(Method.PUT, "johnsmith", "puppy.jpg", headers);

        String expected = "PUT\n4gJE4saaMU4BqNR0kLY+lw==\nimage/jpeg\nTue, 27 Mar 2007 21:15:45 +0000\n/johnsmith/puppy.jpg";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForBucketList() throws Exception {
        Parameters parameters = Parameters.forBucket(Method.GET, "johnsmith");
        parameters.getHeaders().add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        String expected = "GET\n\n\nTue, 27 Mar 2007 19:36:42 +0000\n/johnsmith/";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForAclFetch() throws Exception {
        Headers headers = new Headers();
        headers.add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        Parameters parameters = new Parameters(Method.GET, "johnsmith", "", headers, SubResource.acl, null);

        String expected = "GET\n\n\nTue, 27 Mar 2007 19:36:42 +0000\n/johnsmith/?acl";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForObjectDeleteWithDateAlternative() throws Exception {
        Headers headers = new Headers();
        headers.add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");
        headers.add("x-amz-date", "Tue, 27 Mar 2007 21:20:26 +0000");

        Parameters parameters = Parameters.forObject(Method.DELETE, "johnsmith", "puppy.jpg", headers);

        String expected = "DELETE\n\n\n\nx-amz-date:Tue, 27 Mar 2007 21:20:26 +0000\n/johnsmith/puppy.jpg";

        assertEquals(expected, parameters.toSign());
    }

    @Test
    public void createsExpectedStringForObjectGetWithExpiryTimestamp() throws Exception {
        Long expires = 1211233765000L;

        Parameters parameters = Parameters.forObject(Method.GET, "johnsmith", "puppy.jpg", expires);
        parameters.getHeaders().add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        String expected = "GET\n\n\n1211233765000\n/johnsmith/puppy.jpg";

        assertEquals(expected, parameters.toSign());
    }
}
