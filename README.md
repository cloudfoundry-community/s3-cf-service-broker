# Cloud Foundry Service Broker for Amazon S3

In progress...

## Design

The application is based on [spring-boot-cf-service-broker](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker).  

Does not maintain an internal database so it has no dependencies besides S3.

## Running


### Locally

```
mvn package && AWS_ACCESS_KEY=secret AWS_SECRET_KEY=secret java -jar target//s3-cf-service-broker-0.0.1-SNAPSHOT.jar
```

### In Cloud Foundry



## Broker Security

[spring-boot-starter-security](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-security) is used.  
See the documentation here for configuration: [Spring boot security](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security)

The default password configured is "password"

## Creation and Naming of AWS Resources 

A service provisioning call will create an S3 bucket, an IAM group, and an IAM Policy to provide access controls on the bucket. A binding call will create an IAM user, generate access keys, ad 
add it to the bucket's group. Unbinding and deprovisioning calls will delete all resources created.

The following names are used and can be customized with a prefix:

Resource         | Name is based on     | Custom Prefix Environment Variable  | Default Prefix    | Example Name  
-----------------|----------------------|-------------------------------------|-------------------|---------------
S3 Buckets       | service instance ID  | BUCKET_NAME_PREFIX                  | cloud-foundry-    | cloud-foundry-2eac2d52-bfc9-4d0f-af28-c02187689d72
IAM Group Names  | service instance ID  | GROUP_NAME_PREFIX                   | cloud-foundry-s3- | cloud-foundry-s3-2eac2d52-bfc9-4d0f-af28-c02187689d72
IAM Policy Names | service instance ID  | POLICY_NAME_PREFIX                  | cloud-foundry-s3- | cloud-foundry-s3-2eac2d52-bfc9-4d0f-af28-c02187689d72
IAM User Names   | binding ID           | USER_NAME_PREFIX                    | cloud-foundry-s3- | cloud-foundry-s3-e9bea699-aa68-4464-bb8f-0c8622884b43

Also the following paths are used for IAM resources and can be customized with a prefix:

Resource    | Custom Path Environment Variable  | Default Path 
------------|-----------------------------------|---------------
IAM User    | USER_PATH                         | /cloud-foundry/s3/ 
IAM Group   | GROUP_PATH                        | /cloud-foundry/s3/ 


## User for Broker

An AWS user must be created for the broker. The user's accessKey and secretKey must be provided using the environments variables `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`.

An example user policy for the broker user is provided in [broker-user-iam-policy.json](https://github.com/davidehringer/s3-cf-service-broker/blob/master/src/main/resources/broker-user-iam-policy.json). 
If desired, you can further limit user and group resources in this policy based on prefixes defined above.

Note: The S3 policies could be more limited based on what is actually used. 

## Bucket Policy

The group policy applied to all buckets created is provided in [default-bucket-policy.json](https://github.com/davidehringer/s3-cf-service-broker/blob/master/src/main/resources/default-bucket-policy.json).

## Bucket Tagging

All buckets are tagged with the following values:
* serviceInstanceId
* serviceDefinitionId
* planId
* organizationGuid
* spaceGuid

- Custom tags?

## Registering a Broker with the Cloud Controller

See [Managing Service Brokers](http://docs.cloudfoundry.org/services/managing-service-brokers.html).