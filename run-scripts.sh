#!/bin/bash

cd scripts/ || {
  echo "Error moving to the 'scripts' directory."
  exit 1
}

function dynamodb_table() {
  echo ""
  sh ./1_create-dynamodb-table.sh
}

function quarkus_app() {
  echo ""
  sh ./2_build-and-run-quarkus-app.sh
}

function quarkus_native_app() {
  echo ""
  sh ./3_build-and-run-quarkus-native-app.sh
}

function quarkus_native_container_image() {
  echo ""
  sh ./4_build-and-run-quarkus-local-native-container.sh
}

function build_and_push_quarkus_native_image_to_ecr() {
  echo ""
  sh ./5_build-and-push-quarkus-native-image-to-ecr.sh
}

function timer_services_required_iam_resources() {
  echo ""
  sh ./6_create-timer-service-iam-roles-and-policies.sh
}

function ecs_required_iam_resources() {
  echo ""
  sh ./7_create-ecs-task-iam-roles-and-policies.sh
}

function create_ecs_cluster_and_deploy_timer_service() {
  echo ""
  sh ./8_1-create-ecs-cluster-and-log-group.sh
  sh ./8_2-create-ecs-task-and-deploy-service.sh
}

function create_and_deploy_all_resources() {
  echo ""
  sh ./9_create-build-and-deploy-all-resources-to-aws.sh
}

function delete_resources() {
  echo ""
  sh ./10_delete-all-resources.sh
}

# Main Menu
menu() {
  echo -ne "
  *********************
  ***** Main Menu *****
  *********************
  1) Create Task table on DynamoDB.
  2) Build and run Timer Service as Quarkus app.
  3) Build and run Timer Service as Quarkus native app.
  4) Build and run Timer Service as Quarkus native image container.
  5) Build and Push Timer Service native image to AWS ECR.
  6) Create Timer Service required IAM policies and roles.
  7) Create ECS Service required IAM policies and roles.
  8) Create ECS Cluster and deploy the Timer Service.
  9) Create, build and deploy Timer Service on AWS ECS.
  d) Delete all created resources on AWS.
  e) Exit.
  "
  read -r -p 'Choose an option: ' a
  case $a in
  1)
    dynamodb_table
    menu
    ;;
  2)
    quarkus_app
    menu
    ;;
  3)
    quarkus_native_app
    menu
    ;;
  4)
    quarkus_native_container_image
    menu
    ;;
  5)
    build_and_push_quarkus_native_image_to_ecr
    menu
    ;;
  6)
    timer_services_required_iam_resources
    menu
    ;;
  7)
    ecs_required_iam_resources
    menu
    ;;
  8)
    create_ecs_cluster_and_deploy_timer_service
    menu
    ;;
  9)
    create_and_deploy_all_resources
    menu
    ;;
  d)
    delete_resources
    menu
    ;;
  e) exit 0 ;;
  *)
    echo -e 'Wrong option.'
    menu
    ;;
  esac
}

# Call the menu function
menu
