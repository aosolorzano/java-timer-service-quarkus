#!/bin/bash
cd ../
mvn clean package -Pnative
./target/java-timer-service-quarkus-1.0.0-SNAPSHOT-runner