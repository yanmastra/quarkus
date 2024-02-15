#!/bin/zsh
sudo chmod -R g+rw "$HOME/.docker"

DIR=$(pwd)
cd ..
ROOT_PRJ=$(pwd)

export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
export DB_NAME=db_authentication
docker compose -f docker-compose.yml up mysql -d
sleep 5
cd dependencies/authorization || exit

if [ -d "target" ]; then
    echo "Already build"
else
  mvn clean
  mvn clean install -DskipTests
  sleep 3
fi

cd $DIR || exit

export DEBUG=15005
export QUARKUS_LOG_LEVEL=INFO
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update
export AUTHORIZATION_DEFAULT_REDIRECT="/web/v1/auth"
export AUTHORIZATION_PUBLIC_PATH="/api/v1/auth/*,/web/v1/auth,/web/v1/auth/*,/api/v1/register,/favicon.ico,/"
export AUTHORIZATION_SERVICE_URL="http://localhost:10001/api/v1/auth/authorize"
export JWT_SIGN_KEY_LOCATION="${ROOT_PRJ}/privatekey.pem"
export JWT_SIGN_PUBLIC_LOCATION="${ROOT_PRJ}/publickey.pub"

mvn clean quarkus:dev -Ddebug=$DEBUG