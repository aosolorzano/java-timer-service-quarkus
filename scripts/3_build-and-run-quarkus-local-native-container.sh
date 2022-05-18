#!/bin/bash
cd ../

read -r -p 'Enter your AWS_ACCESS_KEY_ID: [default] ' access_key_id
if [ -z "$access_key_id" ]
then
  access_key_id=$AWS_ACCESS_KEY_ID
fi

read -r -p 'Enter your AWS_SECRET_ACCESS_KEY: [default] ' secret_access_key_id
if [ -z "$secret_access_key_id" ]
then
  secret_access_key_id=$AWS_SECRET_ACCESS_KEY
fi

read -r -p 'Enter the project version to TAG: [1.0.0-SNAPSHOT] ' time_service_version
if [ -z "$time_service_version" ]
then
  time_service_version="1.0.0-SNAPSHOT"
fi

# Build the image
docker build -f src/main/docker/Dockerfile.multistage \
    -t aosolorzano/java-timer-service-quarkus:"$time_service_version" .

# Run all containers and scale to 2 instance of Timer Service.
docker-compose up --scale tasks=2

