#!/bin/bash

# GETTING THE AWS PROFILE NAME FROM ENV VARS
if [ -z "${AWS_PROFILE}" ]
then
  aws_profile="default"
else
  aws_profile="${AWS_PROFILE}"
fi

# GETTING THE AWS SECURITY GROUP ID
if [ -z "${AWS_SECURITY_GROUP_ID}" ]
then
  aws_security_group_id="sg-012121d2a33ebfe56"
else
  aws_security_group_id="${AWS_SECURITY_GROUP_ID}"
fi

# Create an Ingress in security group
aws ec2 authorize-security-group-ingress    \
    --group-id "$aws_security_group_id"     \
    --protocol tcp                          \
    --port 8080                             \
    --cidr 0.0.0.0/0                        \
    --profile "$aws_profile"

# Create ECS Cluster
aws ecs create-cluster                        \
    --cluster-name timer-service-ecs-cluster  \
    --profile "$aws_profile"

# Create Logging Group on CloudWatch
aws logs create-log-group                     \
    --log-group-name "/ecs/timer-service"     \
    --profile "$aws_profile"