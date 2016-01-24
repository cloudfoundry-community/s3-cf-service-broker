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
import org.cloudfoundry.community.servicebroker.model.*;
import org.cloudfoundry.community.servicebroker.s3.plan.Plan;
import org.cloudfoundry.community.servicebroker.s3.service.S3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BasicPlan implements Plan {
    private final BasicPlanIam iam;
    private final S3 s3;

    @Autowired
    public BasicPlan(BasicPlanIam iam, S3 s3) {
        this.iam = iam;
        this.s3 = s3;
    }

    public static org.cloudfoundry.community.servicebroker.model.Plan getPlan(String planId, String planName) {
        return new org.cloudfoundry.community.servicebroker.model.Plan(planId, planName, "An S3 plan providing a single bucket with unlimited storage.",
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

    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) {
        Bucket bucket = s3.createBucketForInstance(request.getServiceInstanceId(),
                request.getServiceDefinitionId(), request.getPlanId(), request.getOrganizationGuid(), request.getSpaceGuid());
        iam.createGroupForInstance(request.getServiceInstanceId(), bucket.getName());
        iam.applyGroupPolicyForInstance(request.getServiceInstanceId(), bucket.getName());

        return new ServiceInstance(request);
    }

    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) {
        String id = request.getServiceInstanceId();
        ServiceInstance instance = s3.findServiceInstance(id);
        // TODO we need to make these deletes idempotent so we can handle retries on error
        iam.deleteGroupPolicyForInstance(id);
        iam.deleteGroupForInstance(id);
        s3.emptyBucket(id);
        s3.deleteBucket(id);
        return instance;
    }

    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        User user = iam.createUserForBinding(request.getBindingId());
        AccessKey accessKey = iam.createAccessKey(user);
        // TODO create password and add to credentials
        iam.addUserToGroup(user, iam.getGroupNameForInstance(request.getServiceInstanceId()));

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("bucket", s3.getBucketNameForInstance(request.getServiceInstanceId()));
        credentials.put("username", user.getUserName());
        credentials.put("access_key_id", accessKey.getAccessKeyId());
        credentials.put("secret_access_key", accessKey.getSecretAccessKey());
        return new ServiceInstanceBinding(
                request.getBindingId(), request.getServiceInstanceId(), credentials, null, request.getAppGuid());
    }

    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
            throws ServiceBrokerException {
        // TODO make operations idempotent so we can handle retries on error
        iam.removeUserFromGroupForInstance(request.getBindingId(), request.getInstance().getServiceInstanceId());
        iam.deleteUserAccessKeysForBinding(request.getBindingId());
        iam.deleteUserForBinding(request.getBindingId());
        return new ServiceInstanceBinding(request.getBindingId(), request.getInstance().getServiceInstanceId(), null, null, null);
    }

    public ServiceInstance getServiceInstance(String id) {
        return s3.findServiceInstance(id);
    }
}
