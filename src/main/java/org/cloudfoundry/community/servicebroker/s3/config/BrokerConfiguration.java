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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.config.BrokerApiVersionConfig;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.s3.service.BucketGroupPolicy;
import org.springframework.beans.factory.annotation.Value;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * @author David Ehringer
 */
@Configuration
@ComponentScan(basePackages = "org.cloudfoundry.community.servicebroker", excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BrokerApiVersionConfig.class) })
public class BrokerConfiguration {
    
    @Value("${AWS_ACCESS_KEY}")
    private String accessKey;
    
    @Value("${AWS_SECRET_KEY}")
    private String secretKey;

    private AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonIdentityManagement amazonIdentityManagement() {
        return new AmazonIdentityManagementClient(awsCredentials());
    }

    @Bean
    public AmazonS3 amazonS3() {
        return new AmazonS3Client(awsCredentials());
    }

    @Bean
    public BucketGroupPolicy bucketGroupPolicy() throws IOException {
        URL url = new ClassPathResource("default-bucket-policy.json").getURL();
        String policyDocument = Resources.toString(url, Charsets.UTF_8);
        return new BucketGroupPolicy(policyDocument);
    }

    @Bean
    public Catalog catalog() throws JsonParseException, JsonMappingException, IOException {
        ServiceDefinition serviceDefinition = new ServiceDefinition("s3", "amazon-s3",
                "Amazon S3 is storage for the Internet.", true, getPlans(), getTags(), getServiceDefinitionMetadata(),
                null, null);
        return new Catalog(Arrays.asList(serviceDefinition));
    }

    private List<String> getTags() {
        return Arrays.asList("s3", "object-storage");
    }

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<String, Object>();
        sdMetadata.put("displayName", "Amazon S3");
        sdMetadata.put("imageUrl", "http://a1.awsstatic.com/images/logos/aws_logo.png");
        sdMetadata.put("longDescription", "Amazon S3 Service");
        sdMetadata.put("providerDisplayName", "Amazon");
        sdMetadata.put("documentationUrl", "http://aws.amazon.com/s3");
        sdMetadata.put("supportUrl", "http://aws.amazon.com/s3");
        return sdMetadata;
    }

    private List<Plan> getPlans() {
        Plan basic = new Plan("s3-basic-plan", "Basic S3 Plan",
                "An S3 plan providing a single bucket with unlimited storage.", getBasicPlanMetadata());
        return Arrays.asList(basic);
    }

    private Map<String, Object> getBasicPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("bullets", getBasicPlanBullets());
        return planMetadata;
    }

    private List<String> getBasicPlanBullets() {
        return Arrays.asList("Single S3 bucket", "Unlimited storage", "Unlimited number of objects");
    }
}
