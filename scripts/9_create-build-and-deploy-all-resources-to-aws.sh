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

read -r -p 'Enter the Subnet ID: [subnet-042e6673123570f61] ' aws_subnet_id
if [ -z "$aws_subnet_id" ]
then
  aws_subnet_id="subnet-042e6673123570f61"
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

echo "CREATING DYNAMODB TABLE..."
aws dynamodb create-table --table-name Task \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --profile="$aws_profile"

echo "EXECUTING PROJECT TESTS..."
mvn clean test

echo "BUILDING DOCKER IMAGE..."
docker build -f src/main/docker/Dockerfile.aws.multistage  \
  -t aosolorzano/java-timer-service-quarkus:"$timer_service_version" .

echo "CREATING ECR REPOSITORY..."
aws ecr create-repository \
  --repository-name timer-service-repository \
  --region us-east-1 \
  --profile="$aws_profile"

echo "TAGGING DOCKER IMAGE..."
docker tag aosolorzano/java-timer-service-quarkus:"$timer_service_version" \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-repository:"$timer_service_version"

echo "GETTING ECR LOGIN..."
aws ecr get-login-password --profile="$aws_profile" | docker login --username AWS --password-stdin \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com

echo "PUSHING DOCKER IMAGE TO AWS ECR..."
docker push "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-repository:"$timer_service_version"

echo "CREATING TIMER SERVICE POLICY..."
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" aws/timer-service-dynamodb-policy.json
aws iam create-policy --policy-name TimerServiceDynamoDBAccessPolicy            \
    --policy-document file://aws/timer-service-dynamodb-policy.json          \
    --profile="$aws_profile"

echo "CREATING ECS POLICY..."
aws iam create-role --role-name TimerServiceEcsDynamoDbRole                     \
    --assume-role-policy-document file://aws/timer-service-trust-policy.json \
    --profile="$aws_profile"

echo "ATTACHING POLICY TO ROLE..."
aws iam attach-role-policy \
    --role-name TimerServiceEcsDynamoDbRole  \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy  \
    --profile="$aws_profile"

echo "CREATING ECS TRUST POLICY..."
aws iam create-role                                                     \
    --role-name ecsTaskExecutionRole                                    \
    --assume-role-policy-document file://aws/ecs-trust-policy.json      \
    --profile="$aws_profile"

echo "ATTACHING ECS POLICY TO ROLE..."
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy  \
    --profile="$aws_profile"

echo "CREATING CLUSTER..."
aws ecs create-cluster \
    --cluster-name timer-service-cluster  \
    --profile="$aws_profile"

echo "CREATING LOG GROUP..."
aws logs create-log-group                   \
    --log-group-name "/ecs/timer-service"   \
    --profile "$aws_profile"

echo "REGISTERING TASK DEFINITION..."
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" aws/timer-service-ecs-task-definition.json
sed -i'.bak' -e "s/timer_service_version/$timer_service_version/g" aws/timer-service-ecs-task-definition.json
aws ecs register-task-definition \
  --cli-input-json file://aws/timer-service-ecs-task-definition.json \
  --profile="$aws_profile"

echo "CREATING CLUSTER SERVICE..."
aws ecs create-service \
    --cluster timer-service-cluster \
    --service-name timer-service \
    --task-definition timer-service-task \
    --desired-count 1 \
    --launch-type "FARGATE" \
    --network-configuration "awsvpcConfiguration={subnets=[$aws_subnet_id],securityGroups=[$aws_security_group_id],assignPublicIp=ENABLED}" \
    --profile="$aws_profile"