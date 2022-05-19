#!/bin/bash

# GETTING THE AWS PROFILE NAME FROM ENV VARS
if [ -z "${AWS_PROFILE}" ]
then
  aws_profile="default"
else
  aws_profile="${AWS_PROFILE}"
fi

# Create IAM role "ecsTaskExecutionRole".
aws iam create-role                                                     \
    --role-name ecsTaskExecutionRole                                    \
    --assume-role-policy-document file://../aws/ecs-trust-policy.json   \
    --profile "$aws_profile"

# Attach the AWS Managed "AmazonECSTaskExecutionRolePolicy"
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy  \
    --profile "$aws_profile"