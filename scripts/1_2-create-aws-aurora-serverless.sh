#!/bin/bash
read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

read -r -p 'Enter the aws security group: [sg-012121d2a33ebfe56] ' aws_security_group_id
if [ -z "$aws_security_group_id" ]
then
  aws_profile="sg-012121d2a33ebfe56"
fi

aws rds create-db-cluster                                   \
    --region us-east-1                                      \
    --engine aurora-postgresql                              \
    --engine-version 13.6                                   \
    --db-cluster-identifier timer-service-cluster           \
    --master-username postgres                              \
    --master-user-password admin123                         \
    --db-subnet-group-name mysubnetgroup                    \
    --vpc-security-group-ids "$aws_security_group_id"       \
    --profile "$aws_profile"                                \
    --serverless-v2-scaling-configuration MinCapacity=2,MaxCapacity=16 \

aws rds create-db-instance                                  \
    --db-instance-identifier timer-service-instance         \
    --db-cluster-identifier timer-service-cluster           \
    --engine aurora-postgresql                              \
    --db-instance-class db.r4.large