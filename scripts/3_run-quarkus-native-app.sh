#!/bin/bash
echo "Packaging and Running Quarkus Native App."
cd ../
mvn clean package -Dnative
./target/java-timer-service-quarkus-1.0.0-SNAPSHOT-runner