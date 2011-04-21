/*
 * Copyright (c) 2011, Thomas Czarniecki
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
package com.tomczarniecki.s3.rest;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class NtlmFriendlyAmazonS3ClientTests {

    @Test
    public void shouldSetupNTCredentialsWhenConfiguredForAuthenticatedProxyAccess() {
        Configuration configuration = new Configuration("publicKey", "provateKey",
                "proxyHost", "81", "proxyUser", "proxyPass", "ntHost", "ntDomain", "");

        NtlmFriendlyAmazonS3Client client = new NtlmFriendlyAmazonS3Client(configuration);
        HttpClient internalClient = client.getInternalClient();

        AuthScope authScope = new AuthScope("proxyHost", 81);
        Credentials proxyCredentials = internalClient.getState().getProxyCredentials(authScope);
        assertThat(proxyCredentials, instanceOf(NTCredentials.class));

        NTCredentials ntCredentials = (NTCredentials) proxyCredentials;
        assertThat(ntCredentials.getUserName(), equalTo("proxyUser"));
        assertThat(ntCredentials.getPassword(), equalTo("proxyPass"));
        assertThat(ntCredentials.getHost(), equalTo("ntHost"));
        assertThat(ntCredentials.getDomain(), equalTo("ntDomain"));
    }
}
