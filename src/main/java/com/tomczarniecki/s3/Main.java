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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class Main implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(Main.class);

    private final ConfigurationFactory factory;
    private Configuration configuration;

    public Main(String[] args) {
        File file = (args.length > 0) ? new File(args[0]) : null;
        factory = new ConfigurationFactory(file);
        try {
            configuration = factory.load();
        } catch (Exception e) {
            logger.warn("Cannot load credentials", e);
        }
    }

    public static void main(String[] args) {
        Main app = new Main(args);
        app.start();
    }

    public void start() {
        setupLookAndFeel();
        SwingUtilities.invokeLater(this);
    }

    private void setupLookAndFeel() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            System.setProperty("apple.laf.useScreenMenuBar", "false");
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
        if (configuration != null && configuration.useDarkTheme()) {
            FlatDarkLaf.install();
        } else {
            FlatLightLaf.install();
        }
    }

    public void run() {
        ensureConfigurationExists();
        final Service service = new WebClientService(configuration);
        DropBox box = new DropBox(service);
        box.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        box.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.close();
                System.exit(0);
            }
        });
        box.showBuckets();
    }

    private void ensureConfigurationExists() {
        if (configuration == null) {
            CredentialsDialog dialog = new CredentialsDialog();
            Configuration cfg = dialog.getCredentials();
            if (cfg == null) {
                System.exit(1);
            }
            try {
                factory.save(cfg);
            } catch (Exception e) {
                logger.warn("Failed to save configuration", e);
            }
            configuration = cfg;
        }
    }
}
