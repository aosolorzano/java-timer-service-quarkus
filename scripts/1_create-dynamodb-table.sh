#!/bin/bash

# GETTING THE AWS PROFILE NAME FROM ENV VARS
if [ -z "${AWS_PROFILE}" ]
then
  aws_profile="default"
else
  aws_profile="${AWS_PROFILE}"
fi

aws dynamodb create-table --table-name Task                   \
    --attribute-definitions AttributeName=id,AttributeType=S  \
    --key-schema AttributeName=id,KeyType=HASH                \
    --billing-mode PAY_PER_REQUEST                            \
    --profile "$aws_profile"