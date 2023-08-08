
DIR=$(pwd)

cd ..
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose up -d
sleep 5

cd $DIR || exit
docker run -d --name 'auth-service' \
--network='ql' \
-e 'MYSQL_USERNAME=developer' \
-e 'MYSQL_PASSWORD=password' \
-e 'MYSQL_HOST=ql-mysql' \
-e 'MYSQL_PORT=3306' \
-e 'DB_NAME=db_ql_authentication' \
-e 'CRUD_REACTIVE_HIBERNATE_PORT=18080' \
-i --rm -p 8080:18080 quarkus-learning/authentication-service:latest