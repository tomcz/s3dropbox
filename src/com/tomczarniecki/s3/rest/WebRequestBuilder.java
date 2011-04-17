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

public class WebRequestBuilder {

    public static final String DEFAULT_HOST = "s3.amazonaws.com";
    public static final String INSECURE_PROTOCOL = "http";
    public static final String SECURE_PROTOCOL = "https";

    private final S3Dates dates = new S3Dates();

    private final Credentials credentials;
    private final String protocol;

    public WebRequestBuilder(Credentials credentials) {
        this.protocol = credentials.shouldUseSecureProtocol() ? SECURE_PROTOCOL : INSECURE_PROTOCOL;
        this.credentials = credentials;
    }

    public WebRequest build(Parameters parameters) {
        WebRequest request = new WebRequest(parameters.getMethod(), createURL(parameters));
        setHeaders(request, parameters);
        return request;
    }

    public void setHeaders(WebRequest request, Parameters parameters) {
        ensureDateHeaderExists(parameters);
        request.addHeaders(parameters.getHeaders());
        setAuthHeader(request, parameters);
    }

    public String createURL(Parameters parameters) {
        return createURL(parameters, new QueryString());
    }

    public String createURL(Parameters parameters, QueryString query) {
        StringBuilder buf = new StringBuilder();
        buf.append(protocol);
        buf.append("://");
        buf.append(DEFAULT_HOST);
        buf.append(createPath(parameters, query));
        return buf.toString();
    }

    private String createPath(Parameters parameters, QueryString query) {
        String path = parameters.toPath();
        if (query.isEmpty()) {
            return path;
        }
        String join = path.contains("?") ? "&" : "?";
        return path + join + query;
    }

    private void ensureDateHeaderExists(Parameters parameters) {
        Headers headers = parameters.getHeaders();
        if (!headers.containsKey("Date")) {
            headers.add("Date", dates.now());
        }
    }

    private void setAuthHeader(WebRequest method, Parameters parameters) {
        String value = "AWS " + credentials.getAccessKeyId() + ":" + credentials.sign(parameters);
        method.addHeader("Authorization", value);
    }
}
