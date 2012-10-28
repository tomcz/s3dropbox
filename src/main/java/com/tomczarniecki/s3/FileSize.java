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
package com.tomczarniecki.s3;

import org.apache.commons.io.FileUtils;

import java.text.DecimalFormat;
import java.util.List;

import static com.tomczarniecki.s3.Generics.newArrayList;
import static com.tomczarniecki.s3.Pair.pair;

public class FileSize {

    private final List<Pair<String, Long>> sizes;
    private final DecimalFormat format;

    public FileSize() {
        sizes = newArrayList();
        sizes.add(pair("GB", FileUtils.ONE_GB));
        sizes.add(pair("MB", FileUtils.ONE_MB));
        sizes.add(pair("KB", FileUtils.ONE_KB));
        format = new DecimalFormat(",##0.0");
    }

    public String format(long size) {
        for (Pair<String, Long> entry : sizes) {
            if (size / entry.getValue() > 0) {
                double value = ((double) size) / ((double) entry.getValue());
                return format.format(value) + " " + entry.getKey();
            }
        }
        return size + " bytes";
    }
}
