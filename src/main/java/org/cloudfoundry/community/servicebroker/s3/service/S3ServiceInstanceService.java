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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.s3.plan.Plan;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author David Ehringer
 */
@Service
public class S3ServiceInstanceService implements ServiceInstanceService {
    private final Plan plan;

    @Autowired
    public S3ServiceInstanceService(Plan plan) {
        this.plan = plan;
    }

    @Override
    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
            String organizationGuid, String spaceGuid) throws ServiceInstanceExistsException, ServiceBrokerException {
        return plan.createServiceInstance(service, serviceInstanceId, planId, organizationGuid, spaceGuid);
    }

    @Override
    public ServiceInstance deleteServiceInstance(String id, String serviceId, String planId)
            throws ServiceBrokerException {
        return plan.deleteServiceInstance(id);
    }

    @Override
    public List<ServiceInstance> getAllServiceInstances() {
        return plan.getAllServiceInstances();
    }

    @Override
    public ServiceInstance getServiceInstance(String id) {
        return plan.getServiceInstance(id);
    }
}
