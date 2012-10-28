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

import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;

import javax.swing.JMenu;
import java.util.List;

import static com.tomczarniecki.s3.gui.Constants.ALL_FOLDERS;
import static com.tomczarniecki.s3.gui.Constants.FOLDER_NAME;

class MenuSwitcher implements ControllerListener {

    private final Display display;
    private final JMenu bucketMenu;
    private final JMenu objectMenu;
    private final Worker worker;

    public MenuSwitcher(Display display, JMenu bucketMenu, JMenu objectMenu, Worker worker) {
        this.bucketMenu = bucketMenu;
        this.objectMenu = objectMenu;
        this.display = display;
        this.worker = worker;
    }

    public void updatedBuckets(List<S3Bucket> buckets) {
        worker.executeOnEventLoop(new Runnable() {
            public void run() {
                display.setTitle(String.format(FOLDER_NAME, ALL_FOLDERS));
                bucketMenu.setVisible(true);
                objectMenu.setVisible(false);
            }
        });
    }

    public void updatedObjects(final String bucketName, List<S3Object> objects) {
        worker.executeOnEventLoop(new Runnable() {
            public void run() {
                display.setTitle(String.format(FOLDER_NAME, bucketName));
                bucketMenu.setVisible(false);
                objectMenu.setVisible(true);
            }
        });
    }
}
