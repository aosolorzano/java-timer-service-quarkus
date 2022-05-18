#!/bin/bash
cd ../

read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

read -r -p 'Enter your account ID: ' aws_account_id
if [ -z "$aws_account_id" ]
then
  echo 'Your Account ID is required to deploy the image to AWS ECR.'
  exit 0;
fi

read -r -p 'Enter the project version to TAG: [1.0.0-SNAPSHOT] ' time_service_version
if [ -z "$time_service_version" ]
then
  time_service_version="1.0.0-SNAPSHOT"
fi

# Create ECR repository
aws ecr create-repository \
  --repository-name timer-service-repository \
  --region us-east-1 \
  --profile "$aws_profile"

# TAG the docker image
docker tag aosolorzano/java-timer-service-quarkus:"$time_service_version" \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-repository:"$time_service_version"

# Login against the ECR service
aws ecr get-login-password --profile "$aws_profile" | docker login --username AWS --password-stdin \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com

# Push the Timer Service image on ECR
docker push "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-repository:"$time_service_version"
