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

import org.cloudfoundry.community.servicebroker.s3.policy.BucketGroupPolicy;
import org.cloudfoundry.community.servicebroker.s3.service.Iam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.Group;
import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author David Ehringer
 */
@Component
public class BasicPlanIam extends Iam {
    private static final Logger logger = LoggerFactory.getLogger(BasicPlanIam.class);

    @Autowired
    public BasicPlanIam(AmazonIdentityManagement iam, BucketGroupPolicy bucketGroupPolicy,
                        @Value("${GROUP_PATH:/cloud-foundry/s3/}") String groupPath,
                        @Value("${GROUP_NAME_PREFIX:cloud-foundry-s3-}") String groupNamePrefix,
                        @Value("${POLICY_NAME_PREFIX:cloud-foundry-s3-}") String policyNamePrefix,
                        @Value("${USER_PATH:/cloud-foundry/s3/}") String userPath,
                        @Value("${USER_NAME_PREFIX:cloud-foundry-s3-}") String userNamePrefix) {
        super(iam, bucketGroupPolicy, groupPath, groupNamePrefix, policyNamePrefix, userPath, userNamePrefix);
    }

    public Group createGroupForInstance(String instanceId, String bucketName) {
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Creating group '{}' for bucket '{}'", groupName, bucketName);
        return createGroup(groupName);
    }

    public void applyGroupPolicyForInstance(String instanceId, String bucketName) {
        String groupName = getGroupNameForInstance(instanceId);
        String policyName = getPolicyNameForInstance(instanceId);
        applyGroupPolicy(groupName, policyName, bucketName);
    }

    public void deleteGroupPolicyForInstance(String instanceId) {
        String groupName = getGroupNameForInstance(instanceId);
        String policyName = getPolicyNameForInstance(instanceId);
        deleteGroupPolicy(groupName, policyName);
    }

    public void deleteGroupForInstance(String instanceId) {
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Deleting group '{}' for instance '{}'", groupName, instanceId);
        deleteGroup(groupName);
    }

    public String getGroupNameForInstance(String instanceId) {
        return getGroupNamePrefix() + instanceId;
    }

    private String getPolicyNameForInstance(String instanceId) {
        return getPolicyNamePrefix() + instanceId;
    }

    public User createUserForBinding(String bindingId) {
        String userName = getUserNameForBinding(bindingId);
        logger.info("Creating user '{}' for service binding '{}'", userName, bindingId);
        return createUser(userName);
    }

    public String getUserNameForBinding(String bindingId) {
        return getUserNamePrefix() + bindingId;
    }

    /**
     * The user must not be a member of any groups or have any access keys.
     *
     * @param bindingId
     */
    public void deleteUserForBinding(String bindingId) {
        String userName = getUserNameForBinding(bindingId);
        logger.info("Deleting user '{}' from service binding '{}'", userName, bindingId);
        deleteUser(userName);
    }

    public void removeUserFromGroupForInstance(String bindingId, String instanceId) {
        String userName = getUserNameForBinding(bindingId);
        String groupName = getGroupNameForInstance(instanceId);
        removeUserFromGroup(userName, groupName);
    }

    public void deleteUserAccessKeysForBinding(String bindingId) {
        String userName = getUserNameForBinding(bindingId);
        deleteUserAccessKeys(userName);
    }
}
