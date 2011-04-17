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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class S3DatesTests {

    @Test
    public void createsTimestampStringInExpectedFormat() throws Exception {
        S3Dates dates = new S3Dates();
        DateTime dt = new DateTime(2008, 5, 19, 21, 49, 25, 0, DateTimeZone.forID("GMT"));
        assertEquals("Mon, 19 May 2008 21:49:25 GMT", dates.format(dt));
    }

    @Test
    public void parsesLastModifiedDateCorrectly() throws Exception {
        S3Dates dates = new S3Dates();
        DateTime actual = dates.parseLastModified("2008-05-21T09:32:42.001Z");
        DateTime expected = new DateTime(2008, 5, 21, 9, 32, 42, 1, DateTimeZone.UTC);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseUTCDateIntoExpectedLocalDateTime() throws Exception {
        DateTimeZone defaultTZ = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("Australia/Sydney"));

            S3Dates dates = new S3Dates();
            LocalDateTime actual = dates.parseLastModifiedLocal("2009-03-11T03:18:06.000Z");
            LocalDateTime expected = new LocalDateTime(2009, 3, 11, 14, 18, 6);

            assertEquals(expected, actual);

        } finally {
            DateTimeZone.setDefault(defaultTZ);
        }
    }
}
