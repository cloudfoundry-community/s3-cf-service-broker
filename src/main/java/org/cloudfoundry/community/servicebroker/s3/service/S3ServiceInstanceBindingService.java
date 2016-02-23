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

import org.cloudfoundry.community.servicebroker.s3.plan.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.ServiceInstanceBinding;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

/**
 * @author David Ehringer
 * @author Justin Carter
 */
@Service
public class S3ServiceInstanceBindingService implements ServiceInstanceBindingService {
    private final Plan plan;

    @Autowired
    public S3ServiceInstanceBindingService(Plan plan) {
        this.plan = plan;
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        ServiceInstanceBinding binding = plan.createServiceInstanceBinding(request);
        return new CreateServiceInstanceBindingResponse(binding.getCredentials());
    }

    @Override
    public void deleteServiceInstanceBinding(
            DeleteServiceInstanceBindingRequest request) throws ServiceBrokerException {
         plan.deleteServiceInstanceBinding(request);
        return;
    }
}
