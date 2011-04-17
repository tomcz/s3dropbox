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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import java.lang.reflect.Field;

public class NtlmFriendlyAmazonS3Client extends AmazonS3Client {

    public NtlmFriendlyAmazonS3Client(AWSCredentials credentials, Configuration configuration) {
        super(credentials, configuration);
        updateProxyAuth(configuration);
    }

    protected void updateProxyAuth(Configuration configuration) {
        if (usingProxyAuth(configuration)) {
            AuthScope authScope = new AuthScope(configuration.getProxyHost(), configuration.getProxyPort());
            getInternalClient().getState().setProxyCredentials(authScope, new NTCredentials(
                    configuration.getProxyUsername(), configuration.getProxyPassword(),
                    configuration.getNtlmHost(), configuration.getNtlmDomain()));
        }
    }

    protected boolean usingProxyAuth(Configuration configuration) {
        return configuration.getProxyHost() != null
                && configuration.getProxyPort() > 0
                && configuration.getProxyUsername() != null
                && configuration.getProxyPassword() != null;
    }

    protected HttpClient getInternalClient() {
        try {
            Field field = client.getClass().getDeclaredField("httpClient");
            field.setAccessible(true);

            return (HttpClient) field.get(client);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
