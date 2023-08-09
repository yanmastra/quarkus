#!/bin/zsh
DIR=$(pwd)
cd ..
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
export DB_NAME=$DB_NAME_03
docker compose up -d
sleep 5
cd dependencies/authorization || exit
mvn clean
mvn clean install -DskipTests
sleep 2
cd $DIR || exit

export DEBUG=15005
export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update

mvn clean
mvn quarkus:dev -Ddebug=$DEBUG