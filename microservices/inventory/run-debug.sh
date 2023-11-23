#!/bin/zsh
DIR=$(pwd)
cd ../../
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose up -d
sleep 5
cd dependencies || exit
for dir in $(ls -1)
do
  cd $dir || continue
  if [ -d "target" ]; then
    cd ..
    continue
  fi
  mvn clean
  mvn clean install -DskipTests
  cd ..
done

sleep 3
cd $DIR || exit

export DEBUG=15006
export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update
export AUTHORIZATION_DEFAULT_REDIRECT="http://localhost:10001/web/v1/auth"
export AUTHORIZATION_PUBLIC_PATH="/*"
export AUTHORIZATION_SERVICE_URL="http://localhost:10001/api/v1/auth/authorize"

mvn clean quarkus:dev -Ddebug=$DEBUG