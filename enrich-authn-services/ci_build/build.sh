#!/bin/bash

cd ..
# for building jar

mvn clean package

echo "jar was builded"

# Replace these variables with your actual values
DOCKER_IMAGE_NAME="enrich-authn-services"

DOCKERFILE_PATH="ci_build/Dockerfile"
DESTINATION_PATH="."

# Copy Dockerfile
cp "$DOCKERFILE_PATH" "$DESTINATION_PATH"

REPO_TAG=$DOCKER_IMAGE_NAME:$IMAGE_TAG
docker build -t $DOCKER_IMAGE_NAME:$IMAGE_TAG .

docker tag $DOCKER_IMAGE_NAME:$IMAGE_TAG $REPOSITORY_NAME:$DOCKER_IMAGE_NAME-$IMAGE_TAG

# Push the Docker image to Docker Hub
docker push $REPOSITORY_NAME:$DOCKER_IMAGE_NAME-$IMAGE_TAG

# Clean up - remove the Dockerfile and the project source code if needed.
rm Dockerfile
# rm -rf your_project_directory

echo "Docker image $REPOSITORY_NAME:$DOCKER_IMAGE_NAME:$IMAGE_TAG has been built and pushed to Docker Hub."




