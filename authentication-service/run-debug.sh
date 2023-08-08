#!/bin/zsh
DIR=$(pwd)
cd ..
source docker_env.sh
export DB_NAME=$DB_NAME_03
docker compose up -d
sleep 5
cd dependencies/authorization || exit
mvn clean
mvn install -DskipTests
sleep 2
cd $DIR || exit

export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=drop-and-create

mvn clean
mvn quarkus:dev