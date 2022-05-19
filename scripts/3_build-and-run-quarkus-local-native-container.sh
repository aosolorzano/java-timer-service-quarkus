#!/bin/bash
cd ../

# GETTING THE TIMER SERVICE VERSION
if [ -z "${TIMER_SERVICE_VERSION}" ]
then
  timer_service_version="1.0.0-SNAPSHOT"
else
  timer_service_version="${TIMER_SERVICE_VERSION}"
fi

# Build the Timer Service image
docker build -f src/main/docker/Dockerfile.multistage \
    -t aosolorzano/java-timer-service-quarkus:"$timer_service_version" .

# Run all containers and scale to 2 instance of Timer Service.
docker-compose up --scale tasks=2

