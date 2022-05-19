#!/bin/bash
cd ../

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

# GETTING THE AWS SUBNET TWO
if [ -z "${AWS_SUBNET_ID_TWO}" ]
then
  aws_subnet_id_two="subnet-042e6673123570f61"
else
  aws_subnet_id_two="${AWS_SUBNET_ID_TWO}"
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

echo "CREATING DYNAMODB TABLE..."
aws dynamodb create-table --table-name Task                   \
    --attribute-definitions AttributeName=id,AttributeType=S  \
    --key-schema AttributeName=id,KeyType=HASH                \
    --billing-mode PAY_PER_REQUEST                            \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING AURORA POSTGRES SUBNET GROUP..."
aws rds create-db-subnet-group                                              \
    --db-subnet-group-name timer-service-db-subnet-group                    \
    --db-subnet-group-description "Subnet group for the Timer Service"      \
    --subnet-ids '['\""$aws_subnet_id_one"\"','\""$aws_subnet_id_two"\"']'   \
    --profile "$aws_profile"
echo "DONE!"

echo "CREATING AURORA POSTGRES CLUSTER..."
aws rds create-db-cluster                                               \
    --region us-east-1                                                  \
    --engine aurora-postgresql                                          \
    --engine-version 13.6                                               \
    --db-cluster-identifier timer-service-db-cluster                    \
    --master-username postgres                                          \
    --master-user-password postgres123                                  \
    --serverless-v2-scaling-configuration MinCapacity=8,MaxCapacity=64  \
    --db-subnet-group-name timer-service-db-subnet-group                \
    --vpc-security-group-ids "$aws_security_group_id"                   \
    --port 5432                                                         \
    --database-name TimerServiceDB                                      \
    --backup-retention-period 35                                        \
    --no-storage-encrypted                                              \
    --no-deletion-protection                                            \
    --profile "$aws_profile"
echo "DONE!"

echo "CREATING AURORA POSTGRES INSTANCE..."
aws rds create-db-instance                                \
    --db-instance-identifier timer-service-db-instance    \
    --db-cluster-identifier timer-service-db-cluster      \
    --engine aurora-postgresql                            \
    --db-instance-class db.serverless                     \
    --profile "$aws_profile"
echo "DONE!"

echo "BUILDING TIMER SERVICE DOCKER IMAGE..."
docker build -f src/main/docker/Dockerfile.multistage  \
  -t aosolorzano/java-timer-service-quarkus:"$timer_service_version" .
echo "DONE!"
echo ""

echo "CREATING ECR REPOSITORY..."
aws ecr create-repository \
  --repository-name timer-service-ecr-repository \
  --region us-east-1 \
  --profile="$aws_profile"
echo "DONE!"

echo "TAGGING TIMER SERVICE DOCKER IMAGE..."
docker tag aosolorzano/java-timer-service-quarkus:"$timer_service_version" \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-ecr-repository:"$timer_service_version"
echo "DONE!"

echo "GETTING ECR LOGIN..."
aws ecr get-login-password --profile="$aws_profile" | docker login --username AWS --password-stdin \
  "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com
echo "DONE!"

echo "PUSHING TIMER SERVICE DOCKER IMAGE TO ECR..."
docker push "$aws_account_id".dkr.ecr.us-east-1.amazonaws.com/timer-service-ecr-repository:"$timer_service_version"
echo "DONE!"
echo ""

echo "CREATING CUSTOM TIMER SERVICE POLICY..."
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" aws/timer-service-dynamodb-access-policy.json
aws iam create-policy --policy-name TimerServiceDynamoDBAccessPolicy        \
    --policy-document file://aws/timer-service-dynamodb-access-policy.json  \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING TIMER SERVICE CUSTOM ROLE..."
aws iam create-role \
    --role-name TimerServiceEcsRole \
    --assume-role-policy-document file://aws/timer-service-trust-policy.json \
    --profile="$aws_profile"
echo "DONE!"

echo "ATTACHING POLICY TO TIMER SERVICE CUSTOM ROLE..."
aws iam attach-role-policy \
    --role-name TimerServiceEcsRole  \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy  \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING ECS TASK EXECUTION ROLE..."
aws iam create-role                                                 \
    --role-name ecsTaskExecutionRole                                \
    --assume-role-policy-document file://aws/ecs-trust-policy.json  \
    --profile="$aws_profile"
echo "DONE!"

echo "ATTACHING ECS POLICY TO TASK EXECUTION ROLE..."
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy  \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING ECS CLUSTER..."
aws ecs create-cluster                        \
    --cluster-name timer-service-ecs-cluster  \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING LOG GROUP ON CLOUD_WATCH..."
aws logs create-log-group                   \
    --log-group-name "/ecs/timer-service"   \
    --profile "$aws_profile"
echo "DONE!"

echo ""
echo "The following command shows you the created database information."
echo "Please, copy the corresponding Endpoint Address value and then paste it in the next step."
echo "[Press enter to continue...]"
read -r
aws rds describe-db-instances \
    --db-instance-identifier timer-service-db-instance \
    --profile "$aws_profile"

read -r -p 'Enter the Database Endpoint Address: ' rds_endpoint_address
if [ -z "$rds_endpoint_address" ]
then
  echo 'Not Task Definition entered to deregister.'
  exit 0;
fi

echo ""
echo "REGISTERING ECS TASK DEFINITION..."
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" aws/timer-service-ecs-task-definition.json
sed -i'.bak' -e "s/rds_endpoint_address/$rds_endpoint_address/g" aws/timer-service-ecs-task-definition.json
sed -i'.bak' -e "s/timer_service_version/$timer_service_version/g" aws/timer-service-ecs-task-definition.json
aws ecs register-task-definition \
  --cli-input-json file://aws/timer-service-ecs-task-definition.json \
  --profile="$aws_profile"
echo "DONE!"

echo "CREATING ECS CLUSTER SERVICE..."
aws ecs create-service                          \
    --cluster timer-service-ecs-cluster         \
    --service-name timer-service-ecs-service    \
    --task-definition timer-service-ecs-task    \
    --desired-count 1                           \
    --launch-type "FARGATE"                     \
    --network-configuration "awsvpcConfiguration={subnets=[$aws_subnet_id_one],securityGroups=[$aws_security_group_id],assignPublicIp=ENABLED}" \
    --profile="$aws_profile"
echo "DONE!"

echo "CREATING ECS CLUSTER SECURITY GROUP INGRESS..."
aws ec2 authorize-security-group-ingress    \
    --group-id "$aws_security_group_id"     \
    --protocol tcp                          \
    --port 8080                             \
    --cidr 0.0.0.0/0                        \
    --profile "$aws_profile"
echo "DONE!"