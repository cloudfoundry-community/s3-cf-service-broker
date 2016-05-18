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
package org.cloudfoundry.community.servicebroker.s3.plan.basic;

import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.s3.model.Bucket;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.s3.plan.Plan;
import org.cloudfoundry.community.servicebroker.s3.service.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BasicPlan implements Plan {
    public static final String PLAN_ID = "s3-basic-plan";
    public static final String AMAZON_S3_HOST = "s3.amazonaws.com";
    private final BasicPlanIam iam;
    private final S3 s3;

    @Autowired
    public BasicPlan(BasicPlanIam iam, S3 s3) {
        this.iam = iam;
        this.s3 = s3;
    }

    public static org.cloudfoundry.community.servicebroker.model.Plan getPlan() {
        return new org.cloudfoundry.community.servicebroker.model.Plan(PLAN_ID, "basic", "An S3 plan providing a single bucket with unlimited storage.",
                getPlanMetadata());
    }

    private static Map<String, Object> getPlanMetadata() {
        Map<String, Object> planMetadata = new HashMap<String, Object>();
        planMetadata.put("bullets", getPlanBullets());
        return planMetadata;
    }

    private static List<String> getPlanBullets() {
        return Arrays.asList("Single S3 bucket", "Unlimited storage", "Unlimited number of objects");
    }

    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
                                                 String organizationGuid, String spaceGuid) {
        Bucket bucket = s3.createBucketForInstance(serviceInstanceId, service, planId, organizationGuid, spaceGuid);
        iam.createGroupForInstance(serviceInstanceId, bucket.getName());
        iam.applyGroupPolicyForInstance(serviceInstanceId, bucket.getName());
        return new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
    }

    public ServiceInstance deleteServiceInstance(String id) {
        ServiceInstance instance = s3.findServiceInstance(id);
        // TODO we need to make these deletes idempotent so we can handle retries on error
        iam.deleteGroupPolicyForInstance(id);
        iam.deleteGroupForInstance(id);
        s3.emptyBucket(id);
        s3.deleteBucket(id);
        return instance;
    }

    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId, String appGuid) {
        User user = iam.createUserForBinding(bindingId);
        AccessKey accessKey = iam.createAccessKey(user);
        // TODO create password and add to credentials
        iam.addUserToGroup(user, iam.getGroupNameForInstance(serviceInstance.getId()));
        String bucketName = s3.getBucketNameForInstance(serviceInstance.getId());
        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("bucket", bucketName);
        credentials.put("username", user.getUserName());
        credentials.put("access_key_id", accessKey.getAccessKeyId());
        credentials.put("secret_access_key", accessKey.getSecretAccessKey());
        credentials.put("host", AMAZON_S3_HOST);
        credentials.put("uri", this.generateUri(accessKey.getAccessKeyId(), accessKey.getSecretAccessKey(), bucketName));
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null, appGuid);
    }

    private String generateUri(String accessKeyId, String secretAccessKey, String bucketName){
        try {
            accessKeyId = URLEncoder.encode(accessKeyId, "UTF-8");
            secretAccessKey = URLEncoder.encode(secretAccessKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //let accessKeyId and secretAccessKey like they are
        }
        return String.format("s3://%s:%s@%s/%s",
                accessKeyId,
                secretAccessKey,
                AMAZON_S3_HOST,
                bucketName
        );
    }

    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId) throws ServiceBrokerException {
        // TODO make operations idempotent so we can handle retries on error
        iam.removeUserFromGroupForInstance(bindingId, serviceInstance.getId());
        iam.deleteUserAccessKeysForBinding(bindingId);
        iam.deleteUserForBinding(bindingId);
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null, null);
    }

    public List<ServiceInstance> getAllServiceInstances() {
        return s3.getAllServiceInstances();
    }

    public ServiceInstance getServiceInstance(String id) {
        return s3.findServiceInstance(id);
    }
}
