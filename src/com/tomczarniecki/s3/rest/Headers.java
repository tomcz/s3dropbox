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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Headers {

    private static final String METADATA_PREFIX = "x-amz-meta-";

    private final Map<String, List<String>> map = Maps.create();

    public void add(Headers headers) {
        for (String key : headers.keys()) {
            for (String value : headers.values(key)) {
                add(key, value);
            }
        }
    }

    public void add(String key, Object value) {
        add(key, value.toString());
    }

    public void add(String key, String value) {
        List<String> current = map.get(key);
        if (current == null) {
            current = Lists.create();
            map.put(key, current);
        }
        current.add(value);
    }

    public void addMetaData(String key, Object value) {
        add(METADATA_PREFIX + key, value);
    }

    public void addMetaData(String key, String value) {
        add(METADATA_PREFIX + key, value);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean containsMetaDataKey(String key) {
        return map.containsKey(METADATA_PREFIX + key);
    }

    public Iterable<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public Iterable<String> values(String key) {
        List<String> values = map.get(key);
        return (values != null) ? Collections.unmodifiableList(values) : Collections.<String>emptyList();
    }
}
