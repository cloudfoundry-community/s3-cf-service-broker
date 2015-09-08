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
package org.cloudfoundry.community.servicebroker;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;

/**
 * Abstract test base class for the Service Broker V2 API.
 *
 * Usage:
 * Annotate the implementing test class with the following implementation-specific annotation:
 *
 *      @SpringApplicationConfiguration(classes = Application.class)
 *
 * If you would want to test the actual creation/deletion of resources, you might also want this annotation:
 *
 *      @FixMethodOrder(MethodSorters.NAME_ASCENDING)
 *
 * This would cause JUnit to run the methods in name-ascending order, causing the cases to run in order.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class ServiceBrokerV2IntegrationTestBase {

    @Value("${local.server.port}")
    protected int port;

    @Value("${security.user.password}")
    protected String password;

    @Value("${service_id}")
    protected String serviceId;

    @Value("${plan_id}")
    protected String planId;

    protected final String username = "user";

    protected final String organizationGuid = "system";

    protected final String spaceGuid = "thespace";

    protected static String instanceId;

    protected static String appGuid;

    protected final String fetchCatalogPath = "/v2/catalog";

    protected final String provisionOrRemoveInstanceBasePath = "/v2/service_instances/%s";

    protected final String createOrRemoveBindingBasePath = "/v2/service_instances/%s/service_bindings/%s";

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @BeforeClass
    public static void generateUniqueIds() {
        instanceId = UUID.randomUUID().toString();
        appGuid = UUID.randomUUID().toString();
    }

    /**
     * cf marketplace
     * cf create-service-broker
     * <p>
     * Fetch Catalog (GET /v2/catalog)
     */

    @Test
    public void case1_fetchCatalogFailsWithoutCredentials() throws Exception {
        given().auth().none().when().get(fetchCatalogPath).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void case1_fetchCatalogSucceedsWithCredentials() throws Exception {
        given().auth().basic(username, password).when().get(fetchCatalogPath).then().statusCode(HttpStatus.SC_OK);
    }

    /**
     * cf create-service
     * <p>
     * Provision Instance (PUT /v2/service_instances/:id)
     */

    @Test
    public void case2_provisionInstanceFailsWithoutCredentials() throws Exception {
        String provisionInstancePath = String.format(provisionOrRemoveInstanceBasePath, instanceId);
        given().auth().none().when().put(provisionInstancePath).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void case2_provisionInstanceSucceedsWithCredentials() throws Exception {
        String provisionInstancePath = String.format(provisionOrRemoveInstanceBasePath, instanceId);
        String request_body = "{\n" +
                "  \"service_id\":        \"" + serviceId + "\",\n" +
                "  \"plan_id\":           \"" + planId + "\",\n" +
                "  \"organization_guid\": \"" + organizationGuid + "\",\n" +
                "  \"space_guid\":        \"" + spaceGuid + "\"\n" +
                "}";

        given().auth().basic(username, password).request().contentType(ContentType.JSON).body(request_body).when().put(provisionInstancePath).then().statusCode(HttpStatus.SC_CREATED);
    }

    /**
     * cf bind-service
     * <p>
     * Create Binding (PUT /v2/service_instances/:instance_id/service_bindings/:id)
     */

    @Test
    public void case3_createBindingFailsWithoutCredentials() throws Exception {
        String createBindingPath = String.format(createOrRemoveBindingBasePath, instanceId, serviceId);
        given().auth().none().when().put(createBindingPath).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void case3_createBindingSucceedsWithCredentials() throws Exception {
        String createBindingPath = String.format(createOrRemoveBindingBasePath, instanceId, serviceId);
        String request_body = "{\n" +
                "  \"plan_id\":      \"" + planId + "\",\n" +
                "  \"service_id\":   \"" + serviceId + "\",\n" +
                "  \"app_guid\":     \"" + appGuid + "\"\n" +
                "}";

        given().auth().basic(username, password).request().contentType(ContentType.JSON).body(request_body).when().put(createBindingPath).then().statusCode(HttpStatus.SC_CREATED);
    }

    /**
     * cf unbind-service
     * <p>
     * Remove Binding (DELETE /v2/service_instances/:instance_id/service_bindings/:id)
     */

    @Test
    public void case4_removeBindingFailsWithoutCredentials() throws Exception {
        String removeBindingPath = String.format(createOrRemoveBindingBasePath, instanceId, serviceId) + "?service_id=" + serviceId + "&plan_id=" + planId;
        given().auth().none().when().delete(removeBindingPath).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void case4_removeBindingSucceedsWithCredentials() throws Exception {
        String removeBindingPath = String.format(createOrRemoveBindingBasePath, instanceId, serviceId) + "?service_id=" + serviceId + "&plan_id=" + planId;
        given().auth().basic(username, password).when().delete(removeBindingPath).then().statusCode(HttpStatus.SC_OK);
    }

    /**
     * cf delete-service
     * <p>
     * Remove Instance (DELETE /v2/service_instances/:id)
     */

    @Test
    public void case5_removeInstanceFailsWithoutCredentials() throws Exception {
        String removeInstancePath = String.format(provisionOrRemoveInstanceBasePath, instanceId) + "?service_id=" + serviceId + "&plan_id=" + planId;
        given().auth().none().when().delete(removeInstancePath).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void case5_removeInstanceSucceedsWithCredentials() throws Exception {
        String removeInstancePath = String.format(provisionOrRemoveInstanceBasePath, instanceId) + "?service_id=" + serviceId + "&plan_id=" + planId;
        given().auth().basic(username, password).when().delete(removeInstancePath).then().statusCode(HttpStatus.SC_OK);
    }
}