#!/bin/bash

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

aws ec2 authorize-security-group-ingress    \
    --group-id "$aws_security_group_id"     \
    --protocol tcp                          \
    --port 5432                             \
    --cidr 0.0.0.0/0                        \
    --profile "$aws_profile"

aws rds create-db-subnet-group                                              \
    --db-subnet-group-name timer-service-db-subnet-group                    \
    --db-subnet-group-description "Subnet group for the Timer Service"      \
    --subnet-ids '['\""$aws_subnet_id_one"\"','\""$aws_subnet_id_two"\"']'  \
    --profile "$aws_profile"

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

aws rds create-db-instance                                \
    --db-instance-identifier timer-service-db-instance    \
    --db-cluster-identifier timer-service-db-cluster      \
    --engine aurora-postgresql                            \
    --db-instance-class db.serverless                     \
    --profile "$aws_profile"

aws rds modify-db-instance                              \
    --db-instance-identifier timer-service-db-instance  \
    --publicly-accessible                               \
    --profile "$aws_profile"