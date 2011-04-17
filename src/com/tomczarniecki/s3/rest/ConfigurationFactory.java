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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.util.Properties;

public class ConfigurationFactory {

    private enum Keys {
        AMAZON_ACCESS_KEY_ID, AMAZON_SECRET_ACCESS_KEY,
        PROXY_HOST, PROXY_PORT,
        PROXY_USERNAME, PROXY_PASSWORD,
        NTLM_HOST, NTLM_DOMAIN,
        USE_SSL, HOSTED_STYLE
    }

    private final File source;

    public ConfigurationFactory(File source) {
        this.source = (source != null) ? source : new File(SystemUtils.USER_HOME, ".s3dropbox");
    }

    public Configuration load() {
        Properties props = Files.loadProperties(source);
        return new Configuration(
                getRequired(props, Keys.AMAZON_ACCESS_KEY_ID),
                getRequired(props, Keys.AMAZON_SECRET_ACCESS_KEY),
                getOptional(props, Keys.PROXY_HOST),
                getOptional(props, Keys.PROXY_PORT),
                getOptional(props, Keys.PROXY_USERNAME),
                getOptional(props, Keys.PROXY_PASSWORD),
                getOptional(props, Keys.NTLM_HOST),
                getOptional(props, Keys.NTLM_DOMAIN),
                props.getProperty(Keys.USE_SSL.name(), "true"),
                props.getProperty(Keys.HOSTED_STYLE.name(), "true"));
    }

    public void save(Configuration credentials) {
        Properties props = new Properties();
        props.setProperty(Keys.AMAZON_ACCESS_KEY_ID.name(), credentials.getAccessKeyId());
        props.setProperty(Keys.AMAZON_SECRET_ACCESS_KEY.name(), credentials.getSecretAccessKey());
        props.setProperty(Keys.PROXY_HOST.name(), credentials.getProxyHost());
        props.setProperty(Keys.PROXY_PORT.name(), credentials.getProxyPort());
        props.setProperty(Keys.PROXY_USERNAME.name(), credentials.getProxyUserName());
        props.setProperty(Keys.PROXY_PASSWORD.name(), credentials.getProxyPassword());
        props.setProperty(Keys.NTLM_HOST.name(), credentials.getNtlmHost());
        props.setProperty(Keys.NTLM_DOMAIN.name(), credentials.getNtlmDomain());
        props.setProperty(Keys.USE_SSL.name(), credentials.getUseSecureProtocol());
        props.setProperty(Keys.HOSTED_STYLE.name(), credentials.getUseHostedBucketStyle());
        Files.saveProperties(source, props);
    }

    private String getRequired(Properties props, Keys key) {
        String value = props.getProperty(key.name());
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(key + " not found");
        }
        return value;
    }

    private String getOptional(Properties props, Keys key) {
        return props.getProperty(key.name(), "");
    }
}
