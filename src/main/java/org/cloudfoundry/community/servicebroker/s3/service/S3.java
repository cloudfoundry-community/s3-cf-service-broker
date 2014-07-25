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
package org.cloudfoundry.community.servicebroker.s3.service;

import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.TagSet;

/**
 * @author David Ehringer
 */
@Component
public class S3 {

    private static final Logger logger = LoggerFactory.getLogger(S3.class);

    private final AmazonS3 s3;

    // TODO make configurable. Can be empty
    private String bucketNamePrefix = "cloud-foundry-";

    @Autowired
    public S3(AmazonS3 s3) {
        this.s3 = s3;
    }

    public Bucket createBucketForInstance(String instanceId, ServiceDefinition service, String planId,
            String organizationGuid, String spaceGuid) {
        String bucketName = getBucketNameForInstance(instanceId);
        logger.info("Creating bucket '{}' for serviceInstanceId '{}'", bucketName, instanceId);
        Bucket bucket = s3.createBucket(bucketName);

        // TODO additional tagging options
        BucketTaggingConfiguration bucketTaggingConfiguration = new BucketTaggingConfiguration();
        TagSet tagSet = new TagSet();
        tagSet.setTag("serviceDefinitionId", service.getId());
        tagSet.setTag("planId", planId);
        tagSet.setTag("organizationGuid", organizationGuid);
        tagSet.setTag("spaceGuid", spaceGuid);
        bucketTaggingConfiguration.withTagSets(tagSet);
        s3.setBucketTaggingConfiguration(bucket.getName(), bucketTaggingConfiguration);

        return bucket;
    }

    public void deleteBucket(String id) {
        String bucketName = getBucketNameForInstance(id);
        logger.info("Deleting bucket '{}' for serviceInstanceId '{}'", bucketName, id);
        s3.deleteBucket(bucketName);
    }

    public String getBucketNameForInstance(String instanceId) {
        return bucketNamePrefix + instanceId;
    }

    public ServiceInstance findServiceInstance(String instanceId) {
        String bucketName = getBucketNameForInstance(instanceId);
        if (s3.doesBucketExist(bucketName)) {
            BucketTaggingConfiguration bucketTaggingConfiguration = s3.getBucketTaggingConfiguration(bucketName);
            TagSet tagSet = bucketTaggingConfiguration.getTagSet();
            ServiceInstance serviceInstance = new ServiceInstance(instanceId, tagSet.getTag("serviceDefinitionId"),
                    tagSet.getTag("planId"), tagSet.getTag("organizationGuid"), tagSet.getTag("spaceGuid"), null);
            return serviceInstance;
        }
        return null;
    }
}
