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
package com.tomczarniecki.s3;

import com.tomczarniecki.s3.gui.CredentialsDialog;
import com.tomczarniecki.s3.gui.DropBox;
import com.tomczarniecki.s3.rest.Configuration;
import com.tomczarniecki.s3.rest.ConfigurationFactory;
import com.tomczarniecki.s3.rest.WebClientService;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(final String... args) {
        setupLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                File file = (args.length > 0) ? new File(args[0]) : null;
                Configuration credentials = getCredentials(file);
                DropBox box = new DropBox(new WebClientService(credentials));
                box.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                box.addWindowListener(new WindowCloseListener());
                box.showBuckets();
            }
        });
    }

    private static void setupLookAndFeel() {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            } catch (Exception e) {
                // not to worry, can still use platform default L&F
            }
        }
    }

    private static Configuration getCredentials(File file) {
        ConfigurationFactory factory = new ConfigurationFactory(file);
        try {
            return factory.load();
        } catch (Exception e) {
            logger.warn("Cannot load credentials", e);
            return createCredentials(factory);
        }
    }

    private static Configuration createCredentials(ConfigurationFactory factory) {
        CredentialsDialog dialog = new CredentialsDialog();
        Configuration credentials = dialog.getCredentials();
        if (credentials == null) {
            System.exit(1);
        }
        factory.save(credentials);
        return credentials;
    }

    private static class WindowCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent windowEvent) {
            System.exit(0);
        }
    }
}
