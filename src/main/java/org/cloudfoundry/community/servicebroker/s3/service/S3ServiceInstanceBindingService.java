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

import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author David Ehringer
 */
@Service
public class S3ServiceInstanceBindingService implements ServiceInstanceBindingService {

    private final S3 s3;
    private final Iam iam;

    @Autowired
    public S3ServiceInstanceBindingService(S3 s3, Iam iam) {
        this.s3 = s3;
        this.iam = iam;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
            String serviceId, String planId, String appGuid) throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {
        User user = iam.createUserForBinding(bindingId);
        AccessKey accessKey = iam.createAccessKey(user);
        // TODO create password
        iam.addUserToGroup(user, iam.getGroupNameForInstance(serviceInstance.getId()));

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("bucket", s3.getBucketNameForInstance(serviceInstance.getId()));
        credentials.put("username", user.getUserName());
        credentials.put("access_key_id", accessKey.getAccessKeyId());
        credentials.put("secret_access_key", accessKey.getSecretAccessKey());
        // TODO add password (make password optional?)
        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), credentials, null, appGuid);
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(String id) throws ServiceBrokerException {
        iam.deleteUserForBinding(id);
        // TODO submit pull request to have the deleteServiceInstanceBinding
        // method signature changed so you don't have to return what you just
        // deleted.
        return new ServiceInstanceBinding(id, null, null, null, null);
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        // TODO submit pull request to have this removed
        throw new IllegalStateException("Not implemented");
    }

}
