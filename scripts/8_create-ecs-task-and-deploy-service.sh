#!/bin/bash

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

read -r -p 'Enter the Subnet ID: [subnet-0757f1f4cfd403c30] ' aws_subnet_id
if [ -z "$aws_subnet_id" ]
then
  aws_subnet_id="subnet-0757f1f4cfd403c30"
fi

read -r -p 'Enter the Security Group ID: [sg-012121d2a33ebfe56] ' aws_security_group_id
if [ -z "$aws_security_group_id" ]
then
  aws_security_group_id="sg-012121d2a33ebfe56"
fi

read -r -p 'Enter the project version to TAG: [1.0.0-SNAPSHOT] ' timer_service_version
if [ -z "$timer_service_version" ]
then
  timer_service_version="1.0.0-SNAPSHOT"
fi

# Register a Linux Task Definition
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" ../aws/timer-service-ecs-task-definition.json
sed -i'.bak' -e "s/timer_service_version/$timer_service_version/g" ../aws/timer-service-ecs-task-definition.json
aws ecs register-task-definition \
  --cli-input-json file://../aws/timer-service-ecs-task-definition.json \
  --profile "$aws_profile"

# Create a Service
aws ecs create-service \
    --cluster timer-service-cluster \
    --service-name timer-service \
    --task-definition timer-service-task \
    --desired-count 1 \
    --launch-type "FARGATE" \
    --network-configuration "awsvpcConfiguration={subnets=[$aws_subnet_id],securityGroups=[$aws_security_group_id],assignPublicIp=ENABLED}" \
    --profile "$aws_profile"
