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

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

class BusyCursorExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Display display;
    private final Worker worker;

    public BusyCursorExecutor(Display display, Worker worker) {
        this.display = display;
        this.worker = worker;
    }

    public void execute(final Runnable command) {
        worker.executeInBackground(() -> {
            String error = null;
            setCursor(true);
            try {
                command.run();

            } catch (Exception e) {
                logger.warn("Task failed", e);
                error = e.toString();

            } finally {
                setCursor(false);
            }
            if (error != null) {
                showError(error);
            }
        });
    }

    private void setCursor(final boolean busy) {
        worker.executeOnEventLoop(() -> {
            if (busy) {
                display.showBusyCursor();
            } else {
                display.showNormalCursor();
            }
        });
    }

    private void showError(final String error) {
        worker.executeOnEventLoop(() -> {
            String message = "Something bad has happened.\n" +
                    "Any saves, updates or deletes may be incomplete.\n" +
                    "Error is: " + WordUtils.wrap(error, 80) + "\n" +
                    "Please wait a bit and try again.\n";
            display.showErrorMessage("Oops", message);
        });
    }
}
