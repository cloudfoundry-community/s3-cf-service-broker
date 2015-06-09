package org.cloudfoundry.community.servicebroker.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.cloudfoundry.community.servicebroker.ServiceBrokerV2IntegrationTestBase;
import org.cloudfoundry.community.servicebroker.s3.config.Application;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringApplicationConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class S3ServiceBrokerV2IntegrationTests extends ServiceBrokerV2IntegrationTestBase {

    private AmazonS3 s3;

    @Value("${BUCKET_NAME_PREFIX:cloud-foundry-}")
    private String  bucketNamePrefix;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        s3 = new AmazonS3Client();
    }

    @Override
    public void case2_provisionInstanceSucceedsWithCredentials() throws Exception {
        super.case2_provisionInstanceSucceedsWithCredentials();
        assertTrue(s3.doesBucketExist(bucketNamePrefix + instanceId));
    }

    @Override
    public void case5_removeInstanceSucceedsWithCredentials() throws Exception {
        super.case5_removeInstanceSucceedsWithCredentials();
        assertFalse(s3.doesBucketExist(bucketNamePrefix + instanceId));
    }
}
