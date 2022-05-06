## What is it?
This project uses Quarkus Framework to generate CRUD operations for Quartz jobs that are stored as Tasks records on AWS DynamoDB.

## Detailed project architecture and components
You can find more detail of the configurations and components coded in this project in the following posts:
[Reactive Timer Microservice with Java Quartz, DynamoDB and Quarkus](https://aosolorzano.medium.com/reactive-timer-microservice-with-java-quartz-dynamodb-and-quarkus-bb4cf6e0dc23).
[Deploying Quarkus Native Container Image on AWS ECS]().

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Requirements
1. An AWS account.
2. [Git](https://git-scm.com/downloads).
3. [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html).
4. GraalVM with OpenJDK 17. You can use [SDK Man](https://sdkman.io/install).
5. [Maven](https://maven.apache.org/download.cgi).

## Running the application in dev mode
You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```
> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application
The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```
The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable
You can create a native executable using: 
```shell script
./mvnw package -Pnative
```
Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```
You can then execute your native executable with: 
```
./target/java-timer-service-quarkus-1.0.0-SNAPSHOT-runner
```
If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Creating a native container image
You can create a native container image as follows:
```shell script
docker build -f src/main/docker/Dockerfile.multistage     \
             -t quarkus/java-timer-service-quarkus .
```
Before execute our docker container, export your AWS credential before to pass them to the "docker run" command:
```shell script
docker run --rm --name java-timer-service -p 8080:8080    \
    -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID               \
    -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY       \
    -e AWS_PROFILE="default"                              \
    -e AWS_DEFAULT_REGION="us-east-1"                     \
    -it quarkus/java-timer-service-quarkus
```
To interact with our timer service located on the generated image, you need to know th IP of your Docker daemon. 
In the case that you are using Minikube like me, you can get your internal docker IP with the following command:
```
minikube ip
```
With this result, you can use the Postman tool to send HTTP requests to our Timer Service:
```
http://192.168.64.3:8080/tasks
```

## Related Guides
- Amazon DynamoDB ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/amazon-dynamodb.html)): Connect to Amazon DynamoDB datastore
- Quartz ([guide](https://quarkus.io/guides/quartz)): Schedule clustered tasks with Quartz

## RESTEasy Reactive
Easily start your Reactive RESTful Web Services
[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
