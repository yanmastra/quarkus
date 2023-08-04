#!/bin/zsh
DIR=$(pwd)
cd ..
source docker_env.sh
export DB_NAME=$DB_NAME_01
docker compose up -d
sleep 5
cd authorization || exit
mvn clean
mvn install -DskipTests
sleep 2
cd $DIR || exit
mvn clean quarkus:dev