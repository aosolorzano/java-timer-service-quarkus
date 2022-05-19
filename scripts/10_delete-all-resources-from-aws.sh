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
  timer_service_version="1.0.0-SNAPSHOT"
else
  timer_service_version="${TIMER_SERVICE_VERSION}"
fi

echo "REVOKING ECS SECURITY GROUP INGRESS..."
aws ec2 revoke-security-group-ingress       \
    --group-id "$aws_security_group_id"     \
    --protocol tcp                          \
    --port 8080                             \
    --cidr 0.0.0.0/0                        \
    --profile="$aws_profile"

echo ""
echo "The following command shows you the running Task ID of your cluster."
echo "Copy it and then entered in the next step of the program to stop it."
echo "[Press enter to continue...]"
read -r
aws ecs list-tasks                      \
    --cluster timer-service-ecs-cluster \
    --profile="$aws_profile"

read -r -p 'Enter the running Task ID from your cluster: ' aws_ecs_task_id
if [ -z "$aws_ecs_task_id" ]
then
  echo 'No Task ID entered to stop in the cluster.'
  echo ""
else
  echo "STOP RUNNING TASK..."
  aws ecs stop-task                       \
      --cluster timer-service-ecs-cluster \
      --task "$aws_ecs_task_id"           \
      --profile="$aws_profile"
  echo "DONE!"
fi

echo "DELETING CLUSTER SERVICE ..."
aws ecs delete-service                    \
    --cluster timer-service-ecs-cluster   \
    --service timer-service-ecs-service   \
    --force                               \
    --profile="$aws_profile"
echo "DONE!"

echo ""
echo "The following command shows you the active Task Definition of your cluster."
echo "Copy the corresponding task number and then entered in the next step of the program to deregister it."
echo "[Press enter to continue...]"
read -r
aws ecs list-task-definitions   \
    --status ACTIVE             \
    --profile="$aws_profile"

read -r -p 'Enter the task definition number: ' task_definition_number
if [ -z "$task_definition_number" ]
then
  echo 'Not Task Definition entered to deregister in the cluster.'
  echo ""
else
  echo "DE-REGISTERING TASK DEFINITION..."
  aws ecs deregister-task-definition \
      --task-definition timer-service-ecs-task:"$task_definition_number" \
      --profile="$aws_profile"
      echo "DONE!"
fi

echo "DELETING CLUSTER..."
aws ecs delete-cluster                      \
    --cluster timer-service-ecs-cluster     \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING LOG GROUP..."
aws logs delete-log-group                   \
    --log-group-name "/ecs/timer-service"   \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING ECR REPOSITORY..."
aws ecr delete-repository                           \
    --repository-name timer-service-ecr-repository  \
    --force                                         \
    --profile="$aws_profile"
echo "DONE!"

echo "DETACHING IAM POLICIES..."
aws iam detach-role-policy \
    --role-name TimerServiceEcsRole \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy \
    --profile="$aws_profile"
aws iam detach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING CUSTOM IAM ROLES..."
aws iam delete-role                         \
    --role-name TimerServiceEcsRole         \
    --profile="$aws_profile"
aws iam delete-role                         \
    --role-name ecsTaskExecutionRole        \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING CUSTOM IAM POLICY..."
aws iam delete-policy \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING DYNAMODB TABLE..."
aws dynamodb delete-table     \
    --table-name Task         \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING RDS INSTANCE..."
aws rds delete-db-instance                              \
    --db-instance-identifier timer-service-db-instance  \
    --skip-final-snapshot                               \
    --delete-automated-backups                          \
    --profile="$aws_profile"
sleep 10
echo "DONE!"

echo "DELETING RDS CLUSTER..."
aws rds delete-db-cluster                               \
    --db-cluster-identifier timer-service-db-cluster    \
    --skip-final-snapshot                               \
    --profile="$aws_profile"
sleep 10
echo "DONE!"

echo "DELETING RDS SERVER GROUP..."
aws rds delete-db-subnet-group                            \
    --db-subnet-group-name timer-service-db-subnet-group  \
    --profile="$aws_profile"
echo "DONE!"

echo "DELETING TIMER SERVICE DOCKER IMAGES..."
docker rm "$(docker ps --filter status=exited -q)"
docker rmi aosolorzano/java-timer-service-quarkus:"$timer_service_version"
docker rmi "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-ecr-repository:"$timer_service_version"

docker container prune
docker volume prune
docker system prune
echo "DONE!"