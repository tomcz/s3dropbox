/*
 * Copyright (c) 2009, Thomas Czarniecki
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.UnhandledException;

import java.io.IOException;

public class WebClient {

    private final HttpClient client;

    public WebClient(Configuration credentials) {
        client = createClient(credentials);
    }

    public <T> T process(WebRequest request, Callback<T> callback) {
        HttpMethod method = createMethod(request);
        try {
            int code = client.executeMethod(method);
            return callback.process(code, method);

        } catch (IOException e) {
            throw new UnhandledException(e);

        } finally {
            method.releaseConnection();
        }
    }

    private HttpMethod createMethod(WebRequest request) {
        HttpMethod method = newMethod(request);
        setHeaders(method, request);
        setFile(method, request);
        return method;
    }

    private HttpMethod newMethod(WebRequest request) {
        String url = request.getUrl();
        System.out.println("url = " + url);
        switch (request.getMethod()) {
            case HEAD:
                return new HeadMethod(url);
            case GET:
                return new GetMethod(url);
            case PUT:
                return new PutMethod(url);
            case DELETE:
                return new DeleteMethod(url);
            default:
                throw new IllegalArgumentException("Unexpected method: " + request.getMethod());
        }
    }

    private void setHeaders(HttpMethod method, WebRequest request) {
        Headers headers = request.getHeaders();
        for (String key : headers.keys()) {
            boolean isFirst = true;
            for (String value : headers.values(key)) {
                if (isFirst) {
                    method.setRequestHeader(key, value);
                    isFirst = false;
                } else {
                    method.addRequestHeader(key, value);
                }
            }
        }
    }

    private void setFile(HttpMethod method, WebRequest request) {
        if (method instanceof EntityEnclosingMethod && request.hasFile()) {
            ((EntityEnclosingMethod) method).setRequestEntity(new CountingFileRequestEntity(
                    request.getFile(), request.getContentType(), request.getListener()));
        }
    }

    private static HttpClient createClient(Configuration credentials) {
        HttpClient client = new HttpClient();
        if (credentials.shouldUseProxy()) {
            String proxyHost = credentials.getProxyHost();
            int proxyPort = Integer.parseInt(credentials.getProxyPort());
            client.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }
        if (credentials.shouldUserProxyAuth()) {
            String proxyUserName = credentials.getProxyUserName();
            String proxyPass = credentials.getProxyPassword();
            String ntlmHost = credentials.getNtlmHost();
            String ntlmDomain = credentials.getNtlmDomain();
            NTCredentials proxyAuth = new NTCredentials(proxyUserName, proxyPass, ntlmHost, ntlmDomain);
            client.getState().setProxyCredentials(AuthScope.ANY, proxyAuth);
        }
        HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(10000);
        params.setSoTimeout(10000);
        return client;
    }

    public interface Callback<T> {
        T process(int status, HttpMethod method) throws IOException;
    }
}
