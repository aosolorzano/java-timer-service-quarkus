#!/bin/bash
cd ../

# GETTING THE AWS ACCOUNT ID FROM ENV VARS
if [ -z "${AWS_ACCOUNT_ID}" ]
then
  echo 'Your Account ID is required to deploy required resources on AWS.'
  exit 0;
else
  aws_account_id="${AWS_ACCOUNT_ID}"
fi

# GETTING THE AWS PROFILE NAME FROM ENV VARS
if [ -z "${AWS_PROFILE}" ]
then
  aws_profile="default"
else
  aws_profile="${AWS_PROFILE}"
fi

# GETTING THE TIMER SERVICE VERSION
if [ -z "${TIMER_SERVICE_VERSION}" ]
then
  timer_service_version="1.0.0"
else
  timer_service_version="${TIMER_SERVICE_VERSION}"
fi

# Create ECR repository
aws ecr create-repository                         \
  --repository-name timer-service-ecr-repository  \
  --region us-east-1                              \
  --profile "$aws_profile"

# TAG the docker image
docker tag aosolorzano/java-timer-service-quarkus:"$timer_service_version" \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-ecr-repository:"$timer_service_version"

# Login against the ECR service
aws ecr get-login-password --profile "$aws_profile" | docker login --username AWS --password-stdin \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com

# Push the Timer Service image on ECR
docker push "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-ecr-repository:"$timer_service_version"
