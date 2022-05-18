#!/bin/bash
read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

aws dynamodb create-table --table-name Task \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --profile "$aws_profile"