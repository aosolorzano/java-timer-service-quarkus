#!/bin/bash
echo "Packaging and Running Quarkus App."
cd ../
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar