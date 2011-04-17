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
package com.tomczarniecki.s3.rest;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mortbay.jetty.Server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class WebClientTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private int freePort;
    private Server server;

    private WebClient client;

    @Before
    public void setup() throws Exception {
        client = new WebClient(new Credentials("accessKey", "secretAccessKey"));

        freePort = findFreePort();

        server = new Server(freePort);
        server.start();
    }

    private int findFreePort() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    @After
    public void teardown() throws Exception {
        server.stop();
    }

    @Test
    public void shouldProcessGetResponse() {
        String text = RandomStringUtils.randomAlphanumeric(100);

        TestHandler handler = new TestHandler();
        handler.statusCode = 201;
        handler.responseBody = text;

        server.addHandler(handler);

        WebRequest request = new WebRequest(Method.GET, "http://localhost:" + freePort + "/test");
        request.addHeader("foo", "bar");

        String responseBody = client.process(request, new WebClient.Callback<String>() {
            public String process(int status, HttpMethod method) throws IOException {
                InputStream body = method.getResponseBodyAsStream();
                assertThat(status, equalTo(201));
                return IOUtils.toString(body);
            }
        });

        assertThat(handler.requestURI, equalTo("/test"));
        assertThat(handler.requestMethod, equalTo("GET"));
        assertThat(handler.requestHeaders.get("foo"), equalTo("bar"));
        assertThat(responseBody, equalTo(text));
    }

    @Test
    public void shouldProcessPutResponse() throws Exception {
        File file = folder.newFile("foo.txt");
        String text = RandomStringUtils.randomAlphanumeric(100);

        FileUtils.writeStringToFile(file, text);

        TestHandler handler = new TestHandler();

        server.addHandler(handler);

        WebRequest request = new WebRequest(Method.PUT, "http://localhost:" + freePort + "/test");
        request.setFile(file, "text/plain", new NullProgressListener());

        int status = client.process(request, new WebClient.Callback<Integer>() {
            public Integer process(int status, HttpMethod method) throws IOException {
                return status;
            }
        });

        assertThat(handler.requestMethod, equalTo("PUT"));
        assertThat(handler.requestBody, equalTo(text));
        assertThat(status, equalTo(200));
    }

}
