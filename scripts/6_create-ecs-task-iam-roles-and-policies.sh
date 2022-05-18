#!/bin/bash

read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
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