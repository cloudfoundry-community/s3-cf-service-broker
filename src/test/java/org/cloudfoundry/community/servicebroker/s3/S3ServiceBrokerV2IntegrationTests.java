package org.cloudfoundry.community.servicebroker.s3;

import org.cloudfoundry.community.servicebroker.ServiceBrokerV2IntegrationTestBase;
import org.cloudfoundry.community.servicebroker.s3.config.Application;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;

@SpringApplicationConfiguration(classes = Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class S3ServiceBrokerV2IntegrationTests extends ServiceBrokerV2IntegrationTestBase {
}
