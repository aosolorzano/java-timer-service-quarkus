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

# Create a Customer Managed IAM policy to access DynamoDB.
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" ../aws/timer-service-dynamodb-policy.json
aws iam create-policy --policy-name TimerServiceDynamoDBAccessPolicy            \
    --policy-document file://../aws/timer-service-dynamodb-policy.json          \
    --profile "$aws_profile"

# Create IAM role "TimerServiceEcsDynamoDbRole".
aws iam create-role --role-name TimerServiceEcsDynamoDbRole                     \
    --assume-role-policy-document file://../aws/timer-service-trust-policy.json \
    --profile "$aws_profile"

# Attach the Customer Managed "TimerServiceDynamoDBAccessPolicy" policy to the "TimerServiceEcsDynamoDbRole" role.
aws iam attach-role-policy \
    --role-name TimerServiceEcsDynamoDbRole  \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy  \
    --profile "$aws_profile"
