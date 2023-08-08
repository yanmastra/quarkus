DIR=$(pwd)
cd ..
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit
echo "project version: $PROJECT_VERSION"
source docker_env.env
cd dependencies/authorization || exit
mvn clean
mvn install -DskipTests
sleep 3

cd $DIR || exit
mvn clean package -Pnative -DskipTests \
-Dquarkus.native.container-build=true \
-Dquarkus.container-image.build=true \
-Dquarkus.container-image.group='quarkus-learning' \
-Dquarkus.container-image.name='authentication-service' \
-Dquarkus.container-image.tag=$PROJECT_VERSION

docker tag quarkus-learning/authentication-service:$$PROJECT_VERSION quarkus-learning/authentication-service:latest