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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.s3.plan.Plan;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author David Ehringer
 */
@Service
public class S3ServiceInstanceBindingService implements ServiceInstanceBindingService {
    private final Plan plan;

    @Autowired
    public S3ServiceInstanceBindingService(Plan plan) {
        this.plan = plan;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
            String serviceId, String planId, String appGuid) throws ServiceInstanceBindingExistsException,
            ServiceBrokerException {
        return plan.createServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId, appGuid);
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
            String serviceId, String planId) throws ServiceBrokerException {
        return plan.deleteServiceInstanceBinding(bindingId, serviceInstance, serviceId, planId);
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        throw new IllegalStateException("Not implemented");
    }
}
