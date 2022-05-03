#!/bin/bash
cd scripts/ || { echo "Error moving to the 'scripts' directory."; exit 1; }
POC_WORKING_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)

echo "Running all scripts in the $POC_WORKING_DIR directory."
sh ./1_create-dynamodb-table.sh

cd "$POC_WORKING_DIR" || { echo "Error moving to the working directory."; exit 1; }
sh ./2_compile-java-project.sh
