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
package org.cloudfoundry.community.servicebroker.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.Group;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringUtils;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.cloudfoundry.community.servicebroker.ServiceBrokerV2IntegrationTestBase;
import org.cloudfoundry.community.servicebroker.s3.config.Application;
import org.cloudfoundry.community.servicebroker.s3.config.AwsClientConfiguration;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringApplicationConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class S3ServiceBrokerV2IntegrationTests extends ServiceBrokerV2IntegrationTestBase {

    @Autowired
    private AmazonS3 s3;
    @Autowired
    private AmazonIdentityManagementClient iam;
    @Autowired
    private AwsClientConfiguration awsClientConfiguration;

    @Value("${BUCKET_NAME_PREFIX:cloud-foundry-}")
    private String bucketNamePrefix;

    @Value("${GROUP_NAME_PREFIX:cloud-foundry-s3-}")
    private String groupNamePrefix;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    private boolean doesGroupExist(String groupName) {
        for (Group g: iam.listGroups().getGroups()) {
            if (g.getGroupName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private void testBucketOperations(String accessKey, String secretKey, String bucketName) throws IOException {
        AmazonS3Client instanceS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey), awsClientConfiguration.toClientConfiguration());
        assertTrue(instanceS3.doesBucketExist(bucketName));
        String objectName = "testObject";
        String objectContent = "Hello World!";

        //set object content
        ByteArrayInputStream data = new ByteArrayInputStream(objectContent.getBytes());
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectName, data, new ObjectMetadata());
        instanceS3.putObject(putRequest);

        //get object content
        GetObjectRequest getRequest = new GetObjectRequest(bucketName, objectName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(instanceS3.getObject(getRequest).getObjectContent()));
        assertTrue(reader.readLine().equals(objectContent));

        //delete object
        instanceS3.deleteObject(bucketName, objectName);
    }

    @Override
    public void case2_provisionInstanceSucceedsWithCredentials() throws Exception {
        super.case2_provisionInstanceSucceedsWithCredentials();
        assertTrue(s3.doesBucketExist(bucketNamePrefix + instanceId));
        assertTrue(doesGroupExist(groupNamePrefix + instanceId));
    }

    @Override
    public void case3_createBindingSucceedsWithCredentials() throws Exception {
        String createBindingPath = String.format(createOrRemoveBindingBasePath, instanceId, serviceId);
        String request_body = "{\n" +
                "  \"plan_id\":      \"" + planId + "\",\n" +
                "  \"service_id\":   \"" + serviceId + "\",\n" +
                "  \"app_guid\":     \"" + appGuid + "\"\n" +
                "}";


        ValidatableResponse response = given().header(apiHeader).auth().basic(username, password).request().contentType(ContentType.JSON).body(request_body).when().put(createBindingPath).then().statusCode(HttpStatus.SC_CREATED);
        String accessKey = response.extract().path("credentials.access_key_id");
        String secretKey = response.extract().path("credentials.secret_access_key");

        //wait for AWS to do its user creation magic
        Thread.sleep(1000 * 5);
        testBucketOperations(accessKey, secretKey, bucketNamePrefix + instanceId);
    }

    @Override
    public void case5_removeInstanceSucceedsWithCredentials() throws Exception {
        super.case5_removeInstanceSucceedsWithCredentials();
        assertFalse(s3.doesBucketExist(bucketNamePrefix + instanceId));
    }
}
