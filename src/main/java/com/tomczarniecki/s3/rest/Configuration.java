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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class Configuration {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final boolean useSecureProtocol;

    private final String proxyHost;
    private final String proxyPort;

    private final String proxyUserName;
    private final String proxyPassword;

    private final String ntlmHost;
    private final String ntlmDomain;

    private final boolean useDarkTheme;

    public Configuration(String accessKeyId, String secretAccessKey) {
        this(accessKeyId, secretAccessKey, "", "", "", "", "", "", true, true);
    }

    public Configuration(String accessKeyId, String secretAccessKey,
                         String proxyHost, String proxyPort,
                         String proxyUserName, String proxyPassword,
                         String ntlmHost, String ntlmDomain,
                         boolean useSecureProtocol, boolean useDarkTheme) {

        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.useSecureProtocol = useSecureProtocol;

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;

        this.proxyUserName = proxyUserName;
        this.proxyPassword = proxyPassword;

        this.ntlmHost = ntlmHost;
        this.ntlmDomain = ntlmDomain;

        this.useDarkTheme = useDarkTheme;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public boolean useSecureProtocol() {
        return useSecureProtocol;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public String getNtlmHost() {
        return ntlmHost;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public boolean useDarkTheme() {
        return useDarkTheme;
    }

    public AWSCredentials getAWSCredentials() {
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    public ClientConfiguration getClientConfiguration() {
        ClientConfiguration config = new ClientConfiguration();

        config.setProxyHost(StringUtils.defaultIfEmpty(proxyHost, config.getProxyHost()));
        config.setProxyPort(NumberUtils.toInt(proxyPort, config.getProxyPort()));

        config.setProxyUsername(StringUtils.defaultIfEmpty(proxyUserName, config.getProxyUsername()));
        config.setProxyPassword(StringUtils.defaultIfEmpty(proxyPassword, config.getProxyPassword()));

        config.setProxyDomain(StringUtils.defaultIfEmpty(ntlmDomain, config.getProxyDomain()));
        config.setProxyWorkstation(StringUtils.defaultIfEmpty(ntlmHost, config.getProxyWorkstation()));

        if (!useSecureProtocol) {
            config.setProtocol(Protocol.HTTP);
        }
        return config;
    }
}
