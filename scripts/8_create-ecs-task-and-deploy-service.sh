#!/bin/bash

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

# GETTING THE AWS SUBNET ONE
if [ -z "${AWS_SUBNET_ID_ONE}" ]
then
  aws_subnet_id_one="subnet-042e6673123570f61"
else
  aws_subnet_id_one="${AWS_SUBNET_ID_ONE}"
fi

# GETTING THE AWS SECURITY GROUP ID
if [ -z "${AWS_SECURITY_GROUP_ID}" ]
then
  aws_security_group_id="sg-012121d2a33ebfe56"
else
  aws_security_group_id="${AWS_SECURITY_GROUP_ID}"
fi

# GETTING THE TIMER SERVICE VERSION
if [ -z "${TIMER_SERVICE_VERSION}" ]
then
  timer_service_version="1.0.0"
else
  timer_service_version="${TIMER_SERVICE_VERSION}"
fi

# Register ECS Task Definition
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" ../aws/timer-service-ecs-task-definition.json
sed -i'.bak' -e "s/timer_service_version/$timer_service_version/g" ../aws/timer-service-ecs-task-definition.json
aws ecs register-task-definition \
  --cli-input-json file://../aws/timer-service-ecs-task-definition.json \
  --profile "$aws_profile"

# Create ECS Service
aws ecs create-service                        \
    --cluster timer-service-ecs-cluster       \
    --service-name timer-service-ecs-service  \
    --task-definition timer-service-ecs-task  \
    --desired-count 1                         \
    --launch-type "FARGATE"                   \
    --network-configuration "awsvpcConfiguration={subnets=[$aws_subnet_id_one],securityGroups=[$aws_security_group_id],assignPublicIp=ENABLED}" \
    --profile "$aws_profile"
