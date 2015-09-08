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
package org.cloudfoundry.community.servicebroker.s3.plan;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;

import java.util.List;

public interface Plan {
    // static org.cloudfoundry.community.servicebroker.model.Plan getPlan() should also be present, but is static.

    ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId, String planId,
                                          String organizationGuid, String spaceGuid);

    ServiceInstance deleteServiceInstance(String id);

    ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId, String appGuid);

    ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId) throws ServiceBrokerException;

    List<ServiceInstance> getAllServiceInstances();

    ServiceInstance getServiceInstance(String id);
}