#!/bin/zsh
DIR=$(pwd)
cd ..
source docker_env.sh
docker compose up -d
sleep 3
cd $DIR || exit
mvn clean quarkus:dev