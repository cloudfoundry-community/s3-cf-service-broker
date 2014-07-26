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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.amazonaws.services.identitymanagement.model.CreateGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateGroupResult;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserResult;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteGroupRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest;
import com.amazonaws.services.identitymanagement.model.Group;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest;
import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author David Ehringer
 */
@Component
public class Iam {

    private static final Logger logger = LoggerFactory.getLogger(Iam.class);

    private final AmazonIdentityManagement iam;
    private final BucketGroupPolicy bucketGroupPolicy;

    private final String groupPath;
    private final String groupNamePrefix;

    private final String policyNamePrefix;

    private final String userPath;
    private final String userNamePrefix;

    @Autowired
    public Iam(AmazonIdentityManagement iam, BucketGroupPolicy bucketGroupPolicy,
            @Value("${GROUP_PATH:/cloud-foundry/s3/}") String groupPath,
            @Value("${GROUP_NAME_PREFIX:cloud-foundry-s3-}") String groupNamePrefix,
            @Value("${POLICY_NAME_PREFIX:cloud-foundry-s3-}") String policyNamePrefix,
            @Value("${USER_PATH:/cloud-foundry/s3/}") String userPath,
            @Value("${USER_NAME_PREFIX:cloud-foundry-s3-}") String userNamePrefix) {
        this.iam = iam;
        this.bucketGroupPolicy = bucketGroupPolicy;
        this.groupPath = groupPath;
        this.groupNamePrefix = groupNamePrefix;
        this.policyNamePrefix = policyNamePrefix;
        this.userPath = userPath;
        this.userNamePrefix = userNamePrefix;
    }

    public Group createGroupForBucket(String instanceId, String bucketName) {
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Creating group '{}' for bucket '{}'", groupName, bucketName);

        CreateGroupRequest request = new CreateGroupRequest(groupName);
        request.setPath(groupPath);
        CreateGroupResult result = iam.createGroup(request);
        return result.getGroup();
    }

    public void applyGroupPolicyForBucket(String instanceId, String bucketName) {
        String groupName = getGroupNameForInstance(instanceId);

        // https://forums.aws.amazon.com/message.jspa?messageID=356160
        PutGroupPolicyRequest request = new PutGroupPolicyRequest();
        logger.info("Putting policy document on group '{}': {}", groupName,
                bucketGroupPolicy.policyDocumentForBucket(bucketName));
        request.setGroupName(groupName);
        request.setPolicyName(getPolicyNameForInstance(instanceId));
        request.setPolicyDocument(bucketGroupPolicy.policyDocumentForBucket(bucketName));
        iam.putGroupPolicy(request);
    }

    public void deleteGroupPolicy(String instanceId) {
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Deleting policy document for group '{}'", groupName);

        DeleteGroupPolicyRequest request = new DeleteGroupPolicyRequest(groupName, getPolicyNameForInstance(instanceId));
        iam.deleteGroupPolicy(request);
    }

    public void deleteGroupForInstance(String instanceId) {
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Deleting group '{}' for instance '{}'", groupName, instanceId);
        DeleteGroupRequest request = new DeleteGroupRequest(groupName);
        iam.deleteGroup(request);
    }

    public String getGroupNameForInstance(String instanceId) {
        return groupNamePrefix + instanceId;
    }

    private String getPolicyNameForInstance(String instanceId) {
        return policyNamePrefix + instanceId;
    }

    public User createUserForBinding(String bindingId) {
        String username = getUserNameForBinding(bindingId);
        logger.info("Creating user '{}' for service binding '{}'", username, bindingId);
        CreateUserRequest request = new CreateUserRequest(username).withPath(userPath);
        CreateUserResult result = iam.createUser(request);
        return result.getUser();
    }

    public String getUserNameForBinding(String bindingId) {
        return userNamePrefix + bindingId;
    }

    public AccessKey createAccessKey(User user) {
        CreateAccessKeyRequest request = new CreateAccessKeyRequest().withUserName(user.getUserName());
        CreateAccessKeyResult result = iam.createAccessKey(request);
        return result.getAccessKey();
    }

    public void addUserToGroup(User user, String groupName) {
        logger.info("Adding user '{}' to group '{}'", user.getUserName(), groupName);
        AddUserToGroupRequest request = new AddUserToGroupRequest();
        request.setGroupName(groupName);
        request.setUserName(user.getUserName());
        iam.addUserToGroup(request);
    }

    /**
     * The user must not be a member of any groups or have any access keys.
     * 
     * @param bindingId
     */
    public void deleteUserForBinding(String bindingId) {
        String username = getUserNameForBinding(bindingId);
        logger.info("Deleting user '{}' from service binding '{}'", username, bindingId);

        DeleteUserRequest request = new DeleteUserRequest(username);
        iam.deleteUser(request);
    }

    public void removeUserFromGroup(String bindingId, String instanceId) {
        String userName = getUserNameForBinding(bindingId);
        String groupName = getGroupNameForInstance(instanceId);
        logger.info("Removing user '{}' from group '{}'", userName, groupName);
        // iam.listGroupsForUser(listGroupsForUserRequest)
        RemoveUserFromGroupRequest removeUserFromGroupRequest = new RemoveUserFromGroupRequest(groupName, userName);
        iam.removeUserFromGroup(removeUserFromGroupRequest);
    }

    public void deleteUserAccessKeys(String bindingId) {
        String userName = getUserNameForBinding(bindingId);
        logger.info("Deleting all access keys for user '{}'", userName);
        ListAccessKeysRequest accessKeysRequest = new ListAccessKeysRequest();
        accessKeysRequest.setUserName(userName);
        ListAccessKeysResult accessKeysResult = iam.listAccessKeys(accessKeysRequest);
        for (AccessKeyMetadata keyMeta : accessKeysResult.getAccessKeyMetadata()) {
            DeleteAccessKeyRequest request = new DeleteAccessKeyRequest(keyMeta.getAccessKeyId());
            request.setUserName(userName);
            iam.deleteAccessKey(request);
        }
        // ListAccessKeysResult has truncation in it but there doesn't seem to
        // be a way to use it
    }
}
