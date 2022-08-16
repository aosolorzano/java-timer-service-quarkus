## What is it?
This project uses the Quarkus Framework to generate CRUD operations over Tasks records stored on AWS DynamoDB.

## Detailed project architecture and components
You can find more detail of the configurations and components coded in this project in the following posts:
- [Reactive Timer Microservice with Java Quartz, DynamoDB and Quarkus](https://aosolorzano.medium.com/reactive-timer-microservice-with-java-quartz-dynamodb-and-quarkus-bb4cf6e0dc23).
- [Deploying Quarkus Native Image Container on AWS Fargate ECS](https://aosolorzano.medium.com/deploying-a-container-image-with-a-quarkus-native-application-on-aws-fargate-ecs-b09141fe7ff4).

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Requirements
1. An AWS account.
2. [Git](https://git-scm.com/downloads).
3. [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html).
4. GraalVM with OpenJDK 17. You can use [SDKMAN](https://sdkman.io/install).
5. [Maven](https://maven.apache.org/download.cgi).
6. Docker and Docker Compose.

## Live coding with Quarkus
The Maven Quarkus plugin provides a development mode that supports live coding. To try this out:
```
mvn clean compile quarkus:dev
```
In this mode you can make changes to the code and have the changes immediately applied, by just refreshing your browser.

## Running the application in dev mode
You can run your application in dev mode that enables live coding using:
```
mvn clean compile quarkus:dev
```
> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application
The application can be packaged using:
```
mvn clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```
mvn package -Dquarkus.package.type=uber-jar
```
The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable
You can create a native executable using: 
```
mvn package -Pnative
```
Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```
mvn package -Pnative -Dquarkus.native.container-build=true
```
You can then execute your native executable with: 
```
./target/java-timer-service-quarkus-1.0.0-runner
```
If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Creating a native container image for local environment
You can create a native container image as follows:
```
docker-compose build
docker-compose up
```
This starts 2 instances of the Timer Service container alongside 1 instance of the rest of the containers.

## Timer Service interaction
You need to know th IP of your Docker daemon. In the case that you are using Minikube (like me), you can get your internal docker IP with the following command:
```
minikube ip
```
With this IP, you can use the Postman tool to send HTTP requests to our Timer Service.

## Deploying ALL resources to AWS
Execute the following script located at project's root folder:
> **_IMPORTANT:_**  Before running bash scripts, you must modify and export the environment variables located in the file `./scripts/0_export-required-env-variables`. Then, you only need to execute:
> `$ source scripts/0_export-required-env-variables`.
```
./run-scripts
```
This script will show you an option's menu where you can select the tasks to deploy the Timer Service on your AWS account.

## Commands to get the public IP address of your Timer Service on ECS
```
aws ecs describe-services \
    --cluster timer-service-cluster \
    --services timer-service
```
```
aws ecs describe-tasks \
  --cluster timer-service-cluster \
  --tasks <task_id>
```
```
aws ec2 describe-network-interfaces \
  --network-interface-id <eni-id>
```

## Install ECS CLI to interact with the ECS Cluster 
You can follow the instruction shown in this [tutorial](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_CLI_installation.html) to install the "ecs-cli" command.
Also, you need to follow the instructions shown in this [tutorial](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_CLI_Configuration.html) to configure the "ecs-cli" command.

## Related Guides
- Amazon DynamoDB ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-dynamodb.html)): Connect to Amazon DynamoDB datastore.
- Quartz ([guide](https://quarkus.io/guides/quartz)): Schedule clustered tasks with Quartz.

## RESTEasy Reactive
Easily start your Reactive RESTful Web Services
[Related guide section.](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
