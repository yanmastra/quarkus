
DIR=$(pwd)

PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit
echo "project version: $PROJECT_VERSION"
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout) || exit
echo "project artifactId: $ARTIFACT_ID"
GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout) || exit
echo "project groupId: $GROUP_ID"

cd ..
export $(grep -v "^$" docker_env.env | grep -v "^#" | xargs)
docker compose up -d
sleep 5

cd $DIR || exit

#docker container stop $ARTIFACT_ID
#docker container rm $ARTIFACT_ID

docker run -d --name "$ARTIFACT_ID" \
--network='ql' \
--volume='./:/var/lib/authentication-service:delegated' \
-e 'MYSQL_USERNAME=developer' \
-e 'MYSQL_PASSWORD=password' \
-e 'MYSQL_HOST=ql-mysql' \
-e 'MYSQL_PORT=3306' \
-e 'DB_NAME=db_ql_authentication' \
-e 'QUARKUS_HTTP_PORT=18080' \
-e 'AUTH_SERVICE_CACHE_LOCATION=/var/lib/authentication-service' \
-e 'AUTH_SERVICE_ACCESS_TOKEN_EXPIRED_IN=60' \
-e 'AUTH_SERVICE_REFRESH_TOKEN_EXPIRED_IN=10080' \
-i --rm -p 10001:18080 $GROUP_ID/$ARTIFACT_ID:latest