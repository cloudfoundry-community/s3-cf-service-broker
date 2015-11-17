# Cloud Foundry Service Broker for Amazon S3

A Cloud Foundry Service Broker for Amazon S3 built using the [spring-boot-cf-service-broker](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker).

The broker currently publishes a single service and plan for provisioning S3 buckets.

## Design

The broker uses meta data in S3 and naming conventions to maintain the state of the services it is brokering. It does not maintain an internal database so it has no dependencies besides S3.

## Releases

Stable versions have been tagged as [releases](https://github.com/cloudfoundry-community/s3-cf-service-broker/releases).

## Running

Simply run the JAR file and provide AWS credentials via the `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` environment variables.

### Locally

```
mvn package && AWS_ACCESS_KEY=secret AWS_SECRET_KEY=secret java -jar target/s3-cf-service-broker-2.0.0-SNAPSHOT.jar
```

### In Cloud Foundry

Build s3-cf-service-broker and push it to Cloud Foundry:
```
mvn package
cf push s3-cf-service-broker -p target/s3-cf-service-broker-2.3.0-SNAPSHOT.jar --no-start
cf set-env s3-cf-service-broker AWS_ACCESS_KEY "MYAWSKEY"
cf set-env s3-cf-service-broker AWS_SECRET_KEY "MYAWSSECRET"
cf set-env s3-cf-service-broker AWS_REGION "eu-west-1" # (optional, default: US (= us-east-1))
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

## Configuration

### General Configuration

The following general configuration options are available.

Environment Variable         | Required  | Default
-----------------------------|-----------|-------------
`AWS_ACCESS_KEY`             | x         |
`AWS_SECRET_KEY`             | x         |
`AWS_REGION`                 |           | `US`
`PROXY_HOST`                 |           | none
`PROXY_PORT`                 |           | none
`PROXY_USERNAME`             |           | none
`PROXY_PASSWORD`             |           | none
`PREEMPTIVE_PROXY_BASE_AUTH` |           | `false`

### Broker Security

[spring-boot-starter-security](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-security)
is used. See the documentation here for configuration: [Spring boot security](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security)

The default password configured is "password" (see [application.properties](src/main/resources/application.properties)).

You may also configure security via environment variables as noted in the Spring Boot documentation.

Environment Variable         | Required  | Default
-----------------------------|-----------|-------------
`SECURITY_USER_NAME`         |           | `user`
`SECURITY_USER_PASSWORD`     |           | `password`

### User for Broker

An AWS user must be created for the broker. The user's accessKey and secretKey must be provided using the environments
variables `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` as noted above.

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

## Contributing

In the spirit of [free software](http://www.fsf.org/licensing/essays/free-sw.html), **everyone** is encouraged to help improve this project.
**All contributions should be done through pull requests.**

Here are some ways *you* can contribute:

* by using alpha, beta, and prerelease versions
* by reporting bugs
* by suggesting new features
* by writing or editing documentation
* by writing specifications
* by writing code (**no patch is too small**: fix typos, add comments, clean up inconsistent whitespace)
* by refactoring code
* by closing [issues](https://github.com/cf-platform-eng/rds-broker/issues)
* by reviewing patches

### Submitting an Issue

We use the [GitHub issue tracker](https://github.com/cloudfoundry-community/s3-cf-service-broker/issues) to track bugs and features. Before submitting a bug report or feature request, check to make sure it hasn't already been submitted. You can indicate support for an existing issue by voting it up. When submitting a bug report, please include a [Gist](http://gist.github.com/) that includes a stack trace and any details that may be necessary to reproduce the bug, including your Golang version and operating system. Ideally, a bug report should include a pull request with failing specs.

### Submitting a Pull Request

1. Fork the project.
2. Create a topic branch.
3. Implement your feature or bug fix.
4. Commit and push your changes.
5. Submit a pull request.