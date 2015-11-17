/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.community.servicebroker.s3.config;

import com.amazonaws.ClientConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author David Ehringer
 */
public class AwsClientConfigurationTest {

    @Test
    public void proxyConfigurationCanBeSet(){
        AwsClientConfiguration brokerConfiguration = new AwsClientConfiguration();
        brokerConfiguration.setPreemptiveBasicProxyAuth(true);
        brokerConfiguration.setProxyHost("myhost.com");
        brokerConfiguration.setProxyPort("81");
        brokerConfiguration.setProxyUsername("user");
        brokerConfiguration.setProxyPassword("secret");

        ClientConfiguration clientConfiguration = brokerConfiguration.toClientConfiguration();
        assertThat(clientConfiguration.getProxyHost(), is("myhost.com"));
        assertThat(clientConfiguration.getProxyPort(), is(81));
        assertThat(clientConfiguration.getProxyUsername(), is("user"));
        assertThat(clientConfiguration.getProxyPassword(), is("secret"));
        assertThat(clientConfiguration.isPreemptiveBasicProxyAuth(), is(true));
    }

    @Test
    public void proxyConfigurationIsNullIfNotSet(){
        AwsClientConfiguration brokerConfiguration = new AwsClientConfiguration();

        ClientConfiguration clientConfiguration = brokerConfiguration.toClientConfiguration();
        assertNull(clientConfiguration.getProxyHost());
        assertThat(clientConfiguration.getProxyPort(), is(-1)); // What ClientConfiguration uses as a default
        assertNull(clientConfiguration.getProxyUsername());
        assertNull(clientConfiguration.getProxyPassword());
        assertFalse(clientConfiguration.isPreemptiveBasicProxyAuth());
    }
}
