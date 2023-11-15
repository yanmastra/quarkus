DIR=$(pwd)

PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) || exit
echo "project version: $PROJECT_VERSION"
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout) || exit
echo "project artifactId: $ARTIFACT_ID"
GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout) || exit
echo "project groupId: $GROUP_ID"

cd ..
source docker_env.env
cd dependencies/authorization || exit
mvn clean
mvn install -DskipTests
sleep 3

cd $DIR || exit
mvn clean package -Pnative -DskipTests \
-Dquarkus.native.additional-build-args="--report-unsupported-elements-at-runtime" \
-Dquarkus.native.container-build=true \
-Dquarkus.container-image.build=true \
-Dquarkus.container-image.group=$GROUP_ID \
-Dquarkus.container-image.name=$ARTIFACT_ID \
-Dquarkus.container-image.tag=$PROJECT_VERSION

docker tag $GROUP_ID/$ARTIFACT_ID:$PROJECT_VERSION $GROUP_ID/$ARTIFACT_ID:latest
