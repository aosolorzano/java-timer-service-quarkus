#!/bin/bash

cd scripts/ || {
  echo "Error moving to the 'scripts' directory."
  exit 1
}

function create_dynamodb_table() {
  echo ""
  sh ./1_create-dynamodb-table.sh
  echo "DONE!"
}

function create_aurora_postgres_db() {
  echo ""
  sh ./2_create-aws-aurora-postgres-db.sh
  echo "DONE!"
}

function quarkus_native_container_image() {
  echo ""
  sh ./3_build-and-run-quarkus-local-native-container.sh
  echo "DONE!"
}

function push_quarkus_native_image_to_ecr() {
  echo ""
  sh ./4_push-quarkus-native-image-to-ecr.sh
  echo "DONE!"
}

function timer_services_required_iam_resources() {
  echo ""
  sh ./5_create-timer-service-iam-roles-and-policies.sh
  echo "DONE!"
}

function ecs_required_iam_resources() {
  echo ""
  sh ./6_create-ecs-task-iam-roles-and-policies.sh
  echo "DONE!"
}

function create-ecs-cluster-and-log-group() {
  echo ""
  sh ./7_create-ecs-cluster-and-log-group.sh
  echo "DONE!"
}

function create-ecs-task-and-deploy-service() {
  echo ""
  sh ./8_create-ecs-task-and-deploy-service.sh
  echo "DONE!"
}

function deploy_all_resources() {
  echo ""
  sh ./9_deploy-all-resources-to-aws.sh
}

function delete_all_resources() {
  echo ""
  sh ./10_delete-all-resources-from-aws.sh
}

# Main Menu
menu() {
  echo -ne "
  *********************
  ***** Main Menu *****
  *********************
  1) Create Task table on DynamoDB.
  2) Create Postgres database on Aurora Serverless.
  3) Build and run Timer Service as Quarkus native image container.
  4) Push the Timer Service image to AWS ECR.
  5) Create Timer Service required IAM policies and roles.
  6) Create ECS Service required IAM policies and roles.
  7) Create ECS Cluster and Log Group.
  8) Create ECS Task and deploy the Timer Service.
  9) Deploy ALL resources on AWS ECS.
  d) DELETE all resources from AWS.
  q) Quit/Exit.
  "
  read -r -p 'Choose an option: ' option
  case $option in
  1)
    create_dynamodb_table
    clear
    menu
    ;;
  2)
    create_aurora_postgres_db
    clear
    menu
    ;;
  3)
    quarkus_native_container_image
    clear
    menu
    ;;
  4)
    push_quarkus_native_image_to_ecr
    clear
    menu
    ;;
  5)
    timer_services_required_iam_resources
    clear
    menu
    ;;
  6)
    ecs_required_iam_resources
    clear
    menu
    ;;
  7)
    create-ecs-cluster-and-log-group
    clear
    menu
    ;;
  8)
    create-ecs-task-and-deploy-service
    clear
    menu
    ;;
  9)
    deploy_all_resources
    clear
    menu
    ;;
  d)
    delete_all_resources
    clear
    menu
    ;;
  q)
    exit 0 ;;
  *)
    echo -e 'Wrong option.'
    clear
    menu
    ;;
  esac
}

# Call the menu function
menu
