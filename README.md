# Cloud Foundry Service Broker for Amazon S3

A Cloud Foundry Service Broker for Amazon S3 built using the [spring-boot-cf-service-broker](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker).

The broker currently publishes a single service and plan for provisioning S3 buckets.

## Design

The broker uses meta data in S3 and naming conventions to maintain the state of the services it is brokering. It does not maintain an internal database so it has no dependencies besides S3.

Capability with the Cloud Foundry service broker API is indicated by the project version number. For example, version 2.3.0 is based off the 2.3 version of the broker API.

## Running

Simply run the JAR file and provide AWS credentials via the `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` environment variables.

### Locally

```
mvn package && AWS_ACCESS_KEY=secret AWS_SECRET_KEY=secret java -jar target/s3-cf-service-broker-2.3.0-SNAPSHOT.jar
```

### In Cloud Foundry

Build s3-cf-service-broker and push it to Cloud Foundry:
```
mvn package
cf push s3-cf-service-broker -p target/s3-cf-service-broker-2.3.0-SNAPSHOT.jar --no-start
cf set-env s3-cf-service-broker AWS_ACCESS_KEY "MYAWSKEY"
cf set-env s3-cf-service-broker AWS_SECRET_KEY "MYAWSSECRET"
cf set-env s3-cf-service-broker JAVA_OPTS "-Dsecurity.user.password=mysecret"
```

Start the service broker:
```
cf start s3-cf-service-broker
```

Create Cloud Foundry service broker:
```
cf create-service-broker s3-cf-service-broker user mysecret http://s3-cf-service-broker.cfapps.io
```

Add service broker to Cloud Foundry Marketplace:
```
cf enable-service-access amazon-s3 -o ORG
```

## Using the services in your application

### Format of Credentials

The credentials provided in a bind call have the following format:

```
"credentials":{
	"username":"cloud-foundry-s3-c5271ba4-6d2f-4163-843c-6a5fdceb7a1a",
	"access_key_id":"secret",
	"bucket":"cloud-foundry-2eac2d52-bfc9-4d0f-af28-c02187689d72",
	"secret_access_key":"secret"
}
```

### Java Applications - Spring Cloud

For Java applications, you may consider using [Spring Cloud](https://github.com/spring-projects/spring-cloud) and the [spring-cloud-s3-service-connector](https://github.com/cloudfoundry-community/spring-cloud-s3-service-connector).

## Broker Security

[spring-boot-starter-security](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-security) is used. See the documentation here for configuration: [Spring boot security](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security)

The default password configured is "password"

## Creation and Naming of AWS Resources

### User for Broker

An AWS user must be created for the broker. The user's accessKey and secretKey must be provided using the environments variables `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`.

Resource          | Environment Variable | Default
------------------|----------------------|-------------
Broker Access Key | AWS_ACCESS_KEY       | - (required)
Broker Secret Key | AWS_SECRET_KEY       | - (required)

An example user policy for the broker user is provided in [broker-user-iam-policy.json](src/main/resources/broker-user-iam-policy.json). If desired, you can further limit user and group resources in this policy based on prefixes defined above.

Note: The S3 policies could be more limited based on what is actually used.

### Basic Plan

A service provisioning call will create an S3 bucket, an IAM group, and an IAM Policy to provide access controls on the bucket. A binding call will create an IAM user, generate access keys, and add it to the bucket's group. Unbinding and deprovisioning calls will delete all resources created.

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

#### Bucket Policy

The group policy applied to all buckets created is provided in [default-bucket-policy.json](src/main/resources/default-bucket-policy.json).

#### Bucket Tagging

All buckets are tagged with the following values:
* serviceInstanceId
* serviceDefinitionId
* planId
* organizationGuid
* spaceGuid

The ability to apply additional custom tags is in the works.

## Registering a Broker with the Cloud Controller

See [Managing Service Brokers](http://docs.cloudfoundry.org/services/managing-service-brokers.html).

## Testing

Export AWS credentials environment variables:
```
export AWS_ACCESS_KEY="YOUR_AWS_ACCESS_KEY"
export AWS_SECRET_KEY="YOUR_AWS_SECRET_KEY"
```

and execute tests with maven:
```
mvn test
```
