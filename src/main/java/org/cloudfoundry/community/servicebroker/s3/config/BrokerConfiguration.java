/*
 * Copyright 2014 the original author or authors.
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

import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.springframework.cloud.servicebroker.config.BrokerApiVersionConfig;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.s3.policy.BucketGroupPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ClassPathResource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * @author David Ehringer
 */
@Configuration
@ComponentScan(basePackages = "org.cloudfoundry.community.servicebroker", excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BrokerApiVersionConfig.class) })
public class BrokerConfiguration {

    @Autowired
    private AwsClientConfiguration awsClientConfiguration;

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(awsClientConfiguration.getAwsAccessKey(), awsClientConfiguration.getAwsSecretKey());
    }

    @Bean
    public AmazonIdentityManagement amazonIdentityManagement() {
        return new AmazonIdentityManagementClient(awsCredentials(), awsClientConfiguration.toClientConfiguration());
    }

    @Bean
    public AmazonS3 amazonS3() {
        return new AmazonS3Client(awsCredentials(), awsClientConfiguration.toClientConfiguration());
    }

    @Bean
    public BucketGroupPolicy bucketGroupPolicy() throws IOException {
        URL url = new ClassPathResource("default-bucket-policy.json").getURL();
        String policyDocument = Resources.toString(url, Charsets.UTF_8);
        return new BucketGroupPolicy(policyDocument);
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() throws IOException {
        return new BrokerApiVersion();
    }

    @Bean
    public Catalog catalog(ServiceDefinition serviceDefinition) throws IOException {
        return new Catalog(Arrays.asList(serviceDefinition));
    }
}
