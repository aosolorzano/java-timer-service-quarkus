#!/bin/bash

read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

# Create a Cluster
aws ecs create-cluster                      \
    --cluster-name timer-service-cluster    \
    --profile "$aws_profile"

aws logs create-log-group                   \
    --log-group-name "/ecs/timer-service"   \
    --profile "$aws_profile"