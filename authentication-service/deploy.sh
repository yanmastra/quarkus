#!/bin/zsh

PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit
echo "project version: $PROJECT_VERSION"
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout) || exit
echo "project artifactId: $ARTIFACT_ID"
GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout) || exit
echo "project groupId: $GROUP_ID"

PROD_DIR="../../prod-$GROUP_ID-$ARTIFACT_ID"

if [ -d "$PROD_DIR" ]; then
  echo "$PROD_DIR exists"
else
  echo "creating directory $PROD_DIR"
  mkdir "$PROD_DIR"
fi

if [ -d "$PROD_DIR/target" ]; then
  echo "$PROD_DIR/target exists"
else
  echo "creating directory $PROD_DIR/target"
  mkdir "$PROD_DIR/target"
fi

#mvn clean install -DskipTests -Pnative
cp -rf target/quarkus-app "$PROD_DIR/target/"
cp -rf target/*-runner "$PROD_DIR/"
cp src/main/docker/Dockerfile.jvm "$PROD_DIR/"

echo "DB_HOST=\"Please fill\"" > "$PROD_DIR/docker_env.env"
echo "DB_PORT=\"Please fill\"" >> "$PROD_DIR/docker_env.env"
echo "DB_NAME=\"Please fill\"" >> "$PROD_DIR/docker_env.env"
echo "QUARKUS_DATASOURCE_USERNAME=\"Please fill\"" >> "$PROD_DIR/docker_env.env"
echo "QUARKUS_DATASOURCE_PASSWORD=\"Please fill\"" >> "$PROD_DIR/docker_env.env"
echo "QUARKUS_DATASOURCE_DB_KIND=\"Please fill mysql / postgres\"" >> "$PROD_DIR/docker_env.env"
echo "QUARKUS_DATASOURCE_REACTIVE_URL=\"vertx-reactive:\${QUARKUS_DATASOURCE_DB_KIND}://\${DB_HOST}:\${DB_PORT}/\${DB_NAME}?serverTimezone=UTC&timezone=UTC\"" >> "$PROD_DIR/docker_env.env"
echo "QUARKUS_HTTP_PORT=10001" >> "$PROD_DIR/docker_env.env"
echo "AUTH_SERVICE_CACHE_LOCATION=/var/lib/authentication-service" >> "$PROD_DIR/docker_env.env"
echo "AUTH_SERVICE_ACCESS_TOKEN_EXPIRED_IN=60" >> "$PROD_DIR/docker_env.env"
echo "AUTH_SERVICE_REFRESH_TOKEN_EXPIRED_IN=10080" >> "$PROD_DIR/docker_env.env"
echo "DOCKER_NETWORK_NAME=\"Please fill\"" >> "$PROD_DIR/docker_env.env"

echo "export \$(grep -v \"^\$\" docker_env.env | grep -v \"^#\" | xargs)" > "$PROD_DIR/run-native.sh"

echo "./${ARTIFACT_ID}-${PROJECT_VERSION}-runner" >> "$PROD_DIR/run-native.sh"
chmod +x "$PROD_DIR/run-native.sh"