#!/bin/zsh
DIR=$(pwd)
cd ../../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
export DB_NAME=$DB_NAME_03
docker compose up mysql -d
sleep 5
cd dependencies/authorization || exit
mvn clean
sleep 1
mvn install -DskipTests
sleep 2
cd $DIR || exit

export DEBUG=15004
export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update
export AUTHORIZATION_PUBLIC_PATH="/auth"
export AUTHORIZATION_SERVICE_URL="http://localhost:10001/api/v1/auth/authorize"

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG