#!/bin/bash
read -r -p 'Enter the aws profile to use: [default] ' aws_profile
if [ -z "$aws_profile" ]
then
  aws_profile="default"
fi

read -r -p 'Enter the first Subnet ID: [subnet-0757f1f4cfd403c30] ' aws_first_subnet_id
if [ -z "$aws_first_subnet_id" ]
then
  aws_first_subnet_id="subnet-0757f1f4cfd403c30"
fi

read -r -p 'Enter the second Subnet ID: [subnet-042e6673123570f61] ' aws_second_subnet_id
if [ -z "$aws_second_subnet_id" ]
then
  aws_second_subnet_id="subnet-042e6673123570f61"
fi

read -r -p 'Enter the aws security group: [sg-012121d2a33ebfe56] ' aws_security_group_id
if [ -z "$aws_security_group_id" ]
then
  aws_security_group_id="sg-012121d2a33ebfe56"
fi

aws rds create-db-subnet-group                                                    \
    --db-subnet-group-name timer-service-subnet-group                             \
    --db-subnet-group-description "Subnet group for the Timer Service"            \
    --subnet-ids '['\""$aws_first_subnet_id"\"','\""$aws_second_subnet_id"\"']'   \
    --profile "$aws_profile"

aws ec2 authorize-security-group-ingress    \
    --group-id "$aws_security_group_id"     \
    --protocol tcp                          \
    --port 5432                             \
    --cidr 0.0.0.0/0                        \
    --profile "$aws_profile"

aws rds create-db-cluster                                 \
    --region us-east-1                                    \
    --engine aurora-postgresql                            \
    --engine-version 13.6                                 \
    --db-cluster-identifier timer-service-db-cluster      \
    --master-username postgres                            \
    --master-user-password postgres123                    \
    --serverless-v2-scaling-configuration MinCapacity=8,MaxCapacity=64  \
    --db-subnet-group-name timer-service-subnet-group     \
    --vpc-security-group-ids "$aws_security_group_id"     \
    --port 5432                                           \
    --database-name TimerServiceDB                        \
    --backup-retention-period 35                          \
    --no-storage-encrypted                                \
    --no-deletion-protection                              \
    --profile "$aws_profile"

sleep 5
aws rds create-db-instance                                \
    --db-instance-identifier timer-service-db-instance    \
    --db-cluster-identifier timer-service-db-cluster      \
    --engine aurora-postgresql                            \
    --db-instance-class db.serverless                     \
    --profile "$aws_profile"

sleep 5
aws rds modify-db-instance \
    --db-instance-identifier timer-service-db-instance  \
    --publicly-accessible \
    --profile "$aws_profile"