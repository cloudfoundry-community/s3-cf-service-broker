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
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

/**
 * @author David Ehringer
 * @author Justin Carter
 */
@Service
public class S3ServiceInstanceService implements ServiceInstanceService {
    private final Plan plan;

    @Autowired
    public S3ServiceInstanceService(Plan plan) {
        this.plan = plan;
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request)
            throws ServiceInstanceExistsException, ServiceBrokerException {
         plan.createServiceInstance(request);
        return new CreateServiceInstanceResponse(false);
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request)
            throws ServiceBrokerException {
        plan.deleteServiceInstance(request);
        return new DeleteServiceInstanceResponse(false);
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request)
            throws ServiceBrokerException, ServiceInstanceUpdateNotSupportedException {

        throw new ServiceInstanceUpdateNotSupportedException("Updating this Service Instance is not supported");
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
        return null;
    }
}
