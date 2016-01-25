package org.cloudfoundry.community.servicebroker.s3.config;

import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.s3.plan.basic.BasicPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.*;

/**
 * Created by jcarter on 22/01/16.
 */
@Configuration
public class ServiceConfiguration {

    @Value("${SERVICE_ID:s3}")
    private String serviceId;

    @Value("${SERVICE_NAME:amazon-s3}")
    private String serviceName;

    @Value("${PLAN_ID:s3-basic-plan}")
    private String basicPlanId;

    @Value("${PLAN_NAME:basic}")
    private String basicPlanName;

    @Bean
    public ServiceDefinition serviceDefinition() throws IOException {
        return new ServiceDefinition(serviceId, serviceName,
                "Amazon S3 is storage for the Internet.", true, false, getPlans(), getTags(), getServiceDefinitionMetadata(),
                Arrays.asList("syslog_drain"), null);
    }

    private List<String> getTags() {
        return Arrays.asList("s3", "object-storage");
    }

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<String, Object>();
        sdMetadata.put("displayName", "Amazon S3");
        sdMetadata.put("imageUrl", "http://a1.awsstatic.com/images/logos/aws_logo.png");
        sdMetadata.put("longDescription", "Amazon S3 Service");
        sdMetadata.put("providerDisplayName", "Amazon");
        sdMetadata.put("documentationUrl", "http://aws.amazon.com/s3");
        sdMetadata.put("supportUrl", "http://aws.amazon.com/s3");
        return sdMetadata;
    }

    private List<Plan> getPlans() {
        List<Plan> myPlans = new ArrayList<Plan>();
        myPlans.add(BasicPlan.getPlan(basicPlanId, basicPlanName));
        return myPlans;
    }
}
