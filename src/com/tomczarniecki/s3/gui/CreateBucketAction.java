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
package com.tomczarniecki.s3.gui;

import org.apache.commons.lang.StringUtils;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.Executor;

class CreateBucketAction extends AbstractAction {

    private final BucketNameValidator validator;
    private final Controller controller;
    private final Executor executor;
    private final Display display;

    public CreateBucketAction(Controller controller, Display display, Executor executor) {
        super("Create Folder");
        this.validator = new BucketNameValidator(controller);
        this.controller = controller;
        this.executor = executor;
        this.display = display;
    }

    public void actionPerformed(ActionEvent e) {
        String bucketName = getBucketName();
        if (bucketName != null) {
            String region = getRegion();
            createBucket(bucketName, region);
        }
    }

    private String getBucketName() {
        while (true) {
            String bucketName = display.getInput((String) getValue(NAME), "Please enter a name for the new folder");

            if (bucketName == null) {
                return null;
            }

            bucketName = StringUtils.trimToEmpty(bucketName);
            String errorMessage = validator.validate(bucketName);

            if (errorMessage == null) {
                return bucketName;
            }

            if (!display.confirmMessage("Oops", errorMessage + "\nDo you want to try again?")) {
                return null;
            }
        }
    }

    private String getRegion() {
        List<String> regions = controller.bucketRegions();
        return display.selectOption("Bucket Region", "Please select an S3 region for your folder", regions);
    }

    private void createBucket(final String bucketName, final String region) {
        executor.execute(new Runnable() {
            public void run() {
                controller.createBucket(bucketName, region);
            }
        });
    }
}
