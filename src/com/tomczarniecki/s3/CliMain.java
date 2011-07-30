/*
 * Copyright (c) 2011, Thomas Czarniecki
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

import com.tomczarniecki.s3.rest.Configuration;
import com.tomczarniecki.s3.rest.ConfigurationFactory;
import com.tomczarniecki.s3.rest.WebClientService;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;

public class CliMain {

    private final String[] args;

    public CliMain(String[] args) {
        this.args = args;
    }

    public void main() {
        boolean success = mainWithoutExit();
        System.exit(success ? 0 : 1);
    }

    public boolean mainWithoutExit() {
        Options options = createOptions();
        Service service = null;
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                showHelp(options);
            } else {
                service = new WebClientService(loadConfiguration(cmd));
                invokeService(service, cmd);
            }
            return true;

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            showHelp(options);
            return false;

        } finally {
            if (service != null) {
                service.close();
            }
        }
    }

    private void invokeService(Service service, CommandLine cmd) throws ParseException {
        if (cmd.hasOption("list")) {
            listAction(service, cmd);
        } else {
            fileAction(service, cmd);
        }
    }

    private void listAction(Service service, CommandLine cmd) {
        String bucketName = cmd.getOptionValue("bucket", "*");
        if (bucketName.equals("*")) {
            List<S3Bucket> buckets = service.listAllMyBuckets();
            System.out.println("\nListing " + buckets.size() + " buckets:");
            for (S3Bucket bucket : buckets) {
                System.out.println(bucket.getName());
            }
        } else {
            FileSize size = new FileSize();
            List<S3Object> objects = service.listObjectsInBucket(bucketName);
            System.out.println("\nListing " + objects.size() + " objects in " + bucketName + ":");
            for (S3Object object : objects) {
                String objectSize = StringUtils.rightPad(size.format(object.getSize()), 10);
                System.out.println(objectSize + " " + object.getKey());
            }
        }
    }

    private void fileAction(Service service, CommandLine cmd) throws ParseException {
        String bucket = getRequiredOption(cmd, "bucket");
        String object = getRequiredOption(cmd, "object");
        File file = new File(getRequiredOption(cmd, "file"));

        if (cmd.hasOption("get")) {
            System.out.printf("Downloading %s from bucket %s to %s ...\n", object, bucket, file);
            service.downloadObject(bucket, object, file, new NullProgressListener());

        } else if (cmd.hasOption("put")) {
            System.out.printf("Uploading %s to bucket %s as %s ...\n", file, bucket, object);
            service.createObject(bucket, object, file, new NullProgressListener());
        }
        System.out.println("... Done");
    }

    private String getRequiredOption(CommandLine cmd, String key) throws ParseException {
        String value = cmd.getOptionValue(key);
        if (value == null) {
            throw new ParseException(key + " is required!");
        }
        return value;
    }

    private Configuration loadConfiguration(CommandLine cmd) {
        File confFile = cmd.hasOption("conf") ? new File(cmd.getOptionValue("conf")) : null;
        ConfigurationFactory factory = new ConfigurationFactory(confFile);
        return factory.load();
    }

    private void showHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("s3dropbox", options);
    }

    private Options createOptions() {
        Option bucket = new Option("bucket", true, "Name of S3 bucket");
        bucket.setArgName("NAME");

        Option object = new Option("object", true, "Name of S3 object");
        object.setArgName("NAME");

        Option file = new Option("file", true, "File to upload or download");
        file.setArgName("FILE");

        Option config = new Option("conf", true, "Configuration properties file");
        config.setArgName("FILE");

        OptionGroup group = new OptionGroup();
        group.addOption(new Option("put", "Upload file to S3"));
        group.addOption(new Option("get", "Download file from S3"));
        group.addOption(new Option("list", "List buckets or objects in a bucket"));
        group.setRequired(true);

        Options options = new Options();
        options.addOption("help", false, "Print this message");
        options.addOption("cli", false, "Run in command line mode");
        options.addOptionGroup(group);
        options.addOption(config);
        options.addOption(bucket);
        options.addOption(object);
        options.addOption(file);
        return options;
    }

    private static class NullProgressListener implements ProgressListener {
        public void processed(long count, long length) {
        }
    }
}
