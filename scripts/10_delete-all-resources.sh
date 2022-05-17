#!/bin/bash

read -r -p 'Enter the AWS profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

read -r -p 'Enter your AWS Account ID: ' aws_account_id
if [ -z "$aws_account_id" ]
then
  echo 'Your Account ID is required to delete resources.'
  exit 0;
fi

read -r -p 'Enter the project version: [1.0.0-SNAPSHOT] ' timer_service_version
if [ -z "$timer_service_version" ]
then
  timer_service_version="1.0.0-SNAPSHOT"
fi

read -r -p 'Enter the task definition number: ' task_definition_number
if [ -z "$task_definition_number" ]
then
  echo 'The task definition number is required to deregister the task from cluster.'
  exit 0;
fi

echo "DELETING CLUSTER SERVICE ..."
aws ecs delete-service \
    --cluster timer-service-cluster \
    --service timer-service \
    --force \
    --profile="$aws_profile"

echo "DE-REGISTERING TASK DEFINITION..."
aws ecs deregister-task-definition \
    --task-definition timer-service-task:"$task_definition_number" \
    --profile="$aws_profile"

echo "DELETING CLUSTER..."
sleep 5
aws ecs delete-cluster \
    --cluster timer-service-cluster \
    --profile="$aws_profile"

echo "DELETING LOG GROUP..."
aws logs delete-log-group \
    --log-group-name "/ecs/timer-service"  \
    --profile="$aws_profile"

echo "DELETING ECR REPOSITORY..."
aws ecr delete-repository \
    --repository-name timer-service-repository \
    --force \
    --profile="$aws_profile"

echo "DETACHING IAM POLICIES..."
aws iam detach-role-policy \
    --role-name TimerServiceEcsDynamoDbRole \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy \
    --profile="$aws_profile"
aws iam detach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy \
    --profile="$aws_profile"

echo "DELETING IAM ROLES..."
aws iam delete-role                         \
    --role-name TimerServiceEcsDynamoDbRole \
    --profile="$aws_profile"
aws iam delete-role                         \
    --role-name ecsTaskExecutionRole        \
    --profile="$aws_profile"

echo "DELETING IAM POLICY VERSION..."
aws iam delete-policy-version \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy \
    --version-id v1 \
    --profile="$aws_profile"

echo "DELETING IAM POLICY..."
aws iam delete-policy \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy \
    --profile="$aws_profile"

echo "DELETING DYNAMODB TABLE..."
aws dynamodb delete-table \
    --table-name Task \
    --profile="$aws_profile"

echo "DELETING TIMER SERVICE DOCKER IMAGES..."
docker rm "$(docker ps --filter status=exited -q)"
docker container prune
docker volume prune

# Remove Timer Service Image
docker rmi aosolorzano/java-timer-service-quarkus:"$timer_service_version"

# Remove Timer Service image for ECR
docker rmi "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-repository:"$timer_service_version"