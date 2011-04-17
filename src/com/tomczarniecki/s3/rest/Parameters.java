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

import com.tomczarniecki.s3.Lists;
import com.tomczarniecki.s3.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class Parameters {

    private static final String DATE_HEADER = "date";
    private static final String CONTENT_MD5_HEADER = "content-md5";
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private static final String ALTERNATIVE_DATE_HEADER = "x-amz-date";
    private static final String AMAZON_HEADER_PREFIX = "x-amz-";

    private final Method method;
    private final String bucketName;
    private final String objectKey;
    private final Headers headers;
    private final SubResource subResource;
    private final Long expires;

    public Parameters(Method method, String bucketName, String objectKey, Headers headers,
                      SubResource subResource, Long expires) {

        this.method = method;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.headers = headers;
        this.subResource = subResource;
        this.expires = expires;
    }

    public static Parameters forAllBuckets() {
        return new Parameters(Method.GET, "", "", new Headers(), null, null);
    }

    public static Parameters forBucket(Method method, String bucketName) {
        return new Parameters(method, bucketName, "", new Headers(), null, null);
    }

    public static Parameters forObject(Method method, String bucketName, String objectKey) {
        return forObject(method, bucketName, objectKey, new Headers());
    }

    public static Parameters forObject(Method method, String bucketName, String objectKey, Headers headers) {
        return new Parameters(method, bucketName, objectKey, headers, null, null);
    }

    public static Parameters forObject(Method method, String bucketName, String objectKey, Long expires) {
        return new Parameters(method, bucketName, objectKey, new Headers(), null, expires);
    }

    public Method getMethod() {
        return method;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String toPath() {
        StringBuilder buf = new StringBuilder();
        appendPath(buf);
        return buf.toString();
    }

    public String toSign() {
        SortedMap<String, String> canonicalHeaders = getCanonicalHeaders();
        StringBuilder buf = new StringBuilder();
        buf.append(method.name());
        buf.append("\n");
        for (Map.Entry<String, String> entry : canonicalHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(AMAZON_HEADER_PREFIX)) {
                buf.append(key);
                buf.append(":");
                buf.append(value);
            } else {
                buf.append(value);
            }
            buf.append("\n");
        }
        appendPath(buf);
        return buf.toString();
    }

    private SortedMap<String, String> getCanonicalHeaders() {
        SortedMap<String, String> canonicalHeaders = Maps.createSorted();
        for (String key : headers.keys()) {
            String header = key.toLowerCase();
            if (isCanonicalHeader(header)) {
                String value = concatenateHeaderValues(headers.values(key));
                canonicalHeaders.put(header, value);
            }
        }
        if (canonicalHeaders.containsKey(ALTERNATIVE_DATE_HEADER)) {
            canonicalHeaders.put(DATE_HEADER, "");
        }
        // If the expires is non-null, use that for the date field.
        // This trumps the x-amz-date behavior.
        if (expires != null) {
            canonicalHeaders.put(DATE_HEADER, expires.toString());
        }
        if (!canonicalHeaders.containsKey(CONTENT_TYPE_HEADER)) {
            canonicalHeaders.put(CONTENT_TYPE_HEADER, "");
        }
        if (!canonicalHeaders.containsKey(CONTENT_MD5_HEADER)) {
            canonicalHeaders.put(CONTENT_MD5_HEADER, "");
        }
        if (!canonicalHeaders.containsKey(DATE_HEADER)) {
            throw new IllegalStateException("Must have a Date header");
        }
        return canonicalHeaders;
    }

    private boolean isCanonicalHeader(String header) {
        return header.startsWith(AMAZON_HEADER_PREFIX) ||
                header.equals(CONTENT_TYPE_HEADER) ||
                header.equals(CONTENT_MD5_HEADER) ||
                header.equals(DATE_HEADER);
    }

    private String concatenateHeaderValues(Iterable<String> values) {
        List<String> results = Lists.create();
        for (String value : values) {
            results.add(value.replaceAll("\n", "").trim());
        }
        return StringUtils.join(results, ",");
    }

    private void appendPath(StringBuilder buf) {
        buf.append("/");
        if (StringUtils.isNotEmpty(bucketName)) {
            buf.append(bucketName);
            buf.append("/");
        }
        if (StringUtils.isNotEmpty(objectKey)) {
            boolean first = true;
            for (String token : StringUtils.split(objectKey, "/")) {
                if (first) {
                    first = false;
                } else {
                    buf.append("/");
                }
                buf.append(encode(token));
            }
        }
        if (subResource != null) {
            buf.append("?");
            buf.append(subResource.name());
        }
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnhandledException(e);
        }
    }
}
