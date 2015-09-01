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

import org.cloudfoundry.community.servicebroker.s3.policy.BucketGroupPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;

public abstract class Iam {
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

    public BucketGroupPolicy getBucketGroupPolicy() {
        return bucketGroupPolicy;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public String getGroupNamePrefix() {
        return groupNamePrefix;
    }

    public String getPolicyNamePrefix() {
        return policyNamePrefix;
    }

    public String getUserPath() {
        return userPath;
    }

    public String getUserNamePrefix() {
        return userNamePrefix;
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

    public Group createGroup(String groupName) {
        CreateGroupRequest request = new CreateGroupRequest(groupName);
        request.setPath(groupPath);
        CreateGroupResult result = iam.createGroup(request);
        return result.getGroup();
    }

    public void applyGroupPolicy(String groupName, String policyName, String bucketName) {
        // https://forums.aws.amazon.com/message.jspa?messageID=356160
        PutGroupPolicyRequest request = new PutGroupPolicyRequest();
        logger.info("Putting policy document on group '{}': {}", groupName,
                bucketGroupPolicy.policyDocumentForBucket(bucketName));
        request.setGroupName(groupName);
        request.setPolicyName(policyName);
        request.setPolicyDocument(bucketGroupPolicy.policyDocumentForBucket(bucketName));
        iam.putGroupPolicy(request);
    }

    public void deleteGroupPolicy(String groupName, String policyName) {
        logger.info("Deleting policy document for group '{}'", groupName);
        DeleteGroupPolicyRequest request = new DeleteGroupPolicyRequest(groupName, policyName);
        iam.deleteGroupPolicy(request);
    }

    public void deleteGroup(String groupName) {
        DeleteGroupRequest request = new DeleteGroupRequest(groupName);
        iam.deleteGroup(request);
    }

    public User createUser(String userName) {
        CreateUserRequest request = new CreateUserRequest(userName).withPath(userPath);
        CreateUserResult result = iam.createUser(request);
        return result.getUser();
    }

    /**
     * The user must not be a member of any groups or have any access keys.
     *
     * @param userName
     */
    public void deleteUser(String userName) {
        DeleteUserRequest request = new DeleteUserRequest(userName);
        iam.deleteUser(request);
    }

    public void removeUserFromGroup(String userName, String groupName) {
        logger.info("Removing user '{}' from group '{}'", userName, groupName);
        // iam.listGroupsForUser(listGroupsForUserRequest)
        RemoveUserFromGroupRequest removeUserFromGroupRequest = new RemoveUserFromGroupRequest(groupName, userName);
        iam.removeUserFromGroup(removeUserFromGroupRequest);
    }

    public void deleteUserAccessKeys(String userName) {
        logger.info("Deleting all access keys for user '{}'", userName);
        ListAccessKeysRequest accessKeysRequest = new ListAccessKeysRequest();
        accessKeysRequest.setUserName(userName);
        ListAccessKeysResult accessKeysResult = iam.listAccessKeys(accessKeysRequest);
        for (AccessKeyMetadata keyMeta : accessKeysResult.getAccessKeyMetadata()) {
            DeleteAccessKeyRequest request = new DeleteAccessKeyRequest(userName, keyMeta.getAccessKeyId());
            iam.deleteAccessKey(request);
        }
        // ListAccessKeysResult has truncation in it but there doesn't seem to
        // be a way to use it
    }
}
