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

import com.amazonaws.services.s3.internal.BucketNameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

class BucketNameValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Controller controller;
    private final Pattern validNamePattern;
    private final Pattern ipAddressPattern;

    public BucketNameValidator(Controller controller) {
        this.validNamePattern = Pattern.compile("^[a-z0-9][a-z0-9\\.\\-]+?[a-z0-9]$");
        this.ipAddressPattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+.\\d+$");
        this.controller = controller;
    }

    public String validate(String bucketName) {
        if (!validNamePattern.matcher(bucketName).matches()) {
            return "Folder name can only contain lowercase letters, digits, periods\n" +
                    "and dashes, and must start and end with a letter or a digit.";
        }
        if (ipAddressPattern.matcher(bucketName).matches()) {
            return "Folder name cannot be in an IP address format.";
        }
        try {
            BucketNameUtils.validateBucketName(bucketName);
        } catch (IllegalArgumentException e) {
            return StringUtils.replace(e.getMessage(), "Bucket", "Folder");
        }
        try {
            if (controller.bucketExists(bucketName)) {
                return "You cannot use this folder name since someone else is already using it.";
            }
        } catch (Exception e) {
            logger.warn("Cannot check if bucket '" + bucketName + "' exists: " + e);
        }
        return null;
    }
}
