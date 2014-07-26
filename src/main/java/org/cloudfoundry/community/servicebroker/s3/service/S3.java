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

import java.util.List;

import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.TagSet;
import com.amazonaws.services.s3.model.VersionListing;
import com.google.common.collect.Lists;

/**
 * @author David Ehringer
 */
@Component
public class S3 {

    private static final Logger logger = LoggerFactory.getLogger(S3.class);

    private final AmazonS3 s3;
    private final String bucketNamePrefix;

    @Autowired
    public S3(AmazonS3 s3, @Value("${BUCKET_NAME_PREFIX:cloud-foundry-}") String bucketNamePrefix) {
        this.s3 = s3;
        this.bucketNamePrefix = bucketNamePrefix;
    }

    public Bucket createBucketForInstance(String instanceId, ServiceDefinition service, String planId,
            String organizationGuid, String spaceGuid) {
        String bucketName = getBucketNameForInstance(instanceId);
        logger.info("Creating bucket '{}' for serviceInstanceId '{}'", bucketName, instanceId);
        Bucket bucket = s3.createBucket(bucketName);

        // TODO allow for additional, custom tagging options
        BucketTaggingConfiguration bucketTaggingConfiguration = new BucketTaggingConfiguration();
        TagSet tagSet = new TagSet();
        tagSet.setTag("serviceInstanceId", instanceId);
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

    /**
     * Deletes all objects and all object versions in the bucket.
     * 
     * @param id
     */
    public void emptyBucket(String id) {
        String bucketName = getBucketNameForInstance(id);
        deleteAllObjects(bucketName);
        deleteAllVersions(bucketName);
    }

    private void deleteAllObjects(String bucketName) {
        logger.info("Deleting all objects from bucket '{}'", bucketName);
        ObjectListing objectList = s3.listObjects(bucketName);
        delete(objectList);
        while (objectList.isTruncated()) {
            objectList = s3.listNextBatchOfObjects(objectList);
            delete(objectList);
        }
    }

    private void delete(ObjectListing objectList) {
        for (S3ObjectSummary objectSummary : objectList.getObjectSummaries()) {
            s3.deleteObject(objectSummary.getBucketName(), objectSummary.getKey());
        }
    }

    private void deleteAllVersions(String bucketName) {
        logger.info("Deleting all object versions from bucket '{}'", bucketName);
        VersionListing versionListing = s3.listVersions(bucketName, null);
        delete(versionListing);
        while (versionListing.isTruncated()) {
            versionListing = s3.listNextBatchOfVersions(versionListing);
            delete(versionListing);
        }
    }

    private void delete(VersionListing versionListing) {
        for (S3VersionSummary versionSummary : versionListing.getVersionSummaries()) {
            s3.deleteVersion(versionSummary.getBucketName(), versionSummary.getKey(), versionSummary.getVersionId());
        }
    }

    public String getBucketNameForInstance(String instanceId) {
        return bucketNamePrefix + instanceId;
    }

    public ServiceInstance findServiceInstance(String instanceId) {
        String bucketName = getBucketNameForInstance(instanceId);
        if (s3.doesBucketExist(bucketName)) {
            BucketTaggingConfiguration taggingConfiguration = s3.getBucketTaggingConfiguration(bucketName);
            return createServiceInstance(taggingConfiguration);
        }
        return null;
    }

    public List<ServiceInstance> getAllServiceInstances() {
        List<ServiceInstance> serviceInstances = Lists.newArrayList();
        for (Bucket bucket : s3.listBuckets()) {
            BucketTaggingConfiguration taggingConfiguration = s3.getBucketTaggingConfiguration(bucket.getName());
            ServiceInstance serviceInstance = createServiceInstance(taggingConfiguration);
            serviceInstances.add(serviceInstance);
        }
        return serviceInstances;
    }

    private ServiceInstance createServiceInstance(BucketTaggingConfiguration taggingConfiguration) {
        // While the Java API has multiple TagSets, it would appear from
        // http://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTtagging.html
        // that only one TagSet is supported.
        TagSet tagSet = taggingConfiguration.getTagSet();
        String serviceInstanceId = tagSet.getTag("serviceInstanceId");
        if (serviceInstanceId == null) {
            // could occur if someone used this broker AWS ID to a bucket
            // outside of the broker process
            return null;
        }
        String serviceDefinitionId = tagSet.getTag("serviceDefinitionId");
        String planId = tagSet.getTag("planId");
        String organizationGuid = tagSet.getTag("organizationGuid");
        String spaceGuid = tagSet.getTag("spaceGuid");
        ServiceInstance serviceInstance = new ServiceInstance(serviceInstanceId, serviceDefinitionId, planId,
                organizationGuid, spaceGuid, null);
        return serviceInstance;
    }
}
