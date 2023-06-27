#!/bin/zsh
DIR=$(pwd)
source docker_env.sh
docker compose up -d
sleep 3
mvn clean quarkus:dev