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

# Create a Customer Managed IAM policy to access DynamoDB.
sed -i'.bak' -e "s/aws_account_id/$aws_account_id/g" ../aws/timer-service-dynamodb-access-policy.json
aws iam create-policy --policy-name TimerServiceDynamoDBAccessPolicy            \
    --policy-document file://../aws/timer-service-dynamodb-access-policy.json   \
    --profile "$aws_profile"

# Create IAM role "TimerServiceEcsRole".
aws iam create-role --role-name TimerServiceEcsRole                             \
    --assume-role-policy-document file://../aws/timer-service-trust-policy.json \
    --profile "$aws_profile"

# Attach the Customer Managed "TimerServiceDynamoDBAccessPolicy" policy to the "TimerServiceEcsRole" role.
aws iam attach-role-policy \
    --role-name TimerServiceEcsRole  \
    --policy-arn arn:aws:iam::"$aws_account_id":policy/TimerServiceDynamoDBAccessPolicy  \
    --profile "$aws_profile"
