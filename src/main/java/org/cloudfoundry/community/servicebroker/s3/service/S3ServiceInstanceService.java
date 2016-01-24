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

import com.amazonaws.services.dynamodbv2.model.transform.DeleteRequestJsonUnmarshaller;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.cloudfoundry.community.servicebroker.model.*;
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
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
            throws ServiceInstanceExistsException, ServiceBrokerException {
        return plan.createServiceInstance(request);
    }

    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request)
            throws ServiceBrokerException {
        return plan.deleteServiceInstance(request);
    }

    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
            throws ServiceBrokerException, ServiceInstanceUpdateNotSupportedException {

        throw new ServiceInstanceUpdateNotSupportedException("Updating this Service Instance is not supported");
    }

    @Override
    public ServiceInstance getServiceInstance(String id) {
        return plan.getServiceInstance(id);
    }
}
