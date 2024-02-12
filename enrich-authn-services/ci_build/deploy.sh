cd ..

# Update the deployment file with the new image name



DEPLOYMENT_FILE="enrich-authn-deployment.yaml"
#NEW_IMAGE_NAME="$REPOSITORY_NAME:$DOCKER_IMAGE_NAME-$IMAGE_TAG"
BASE_FOLDER="kube"
TEMP_FOLDER="temp"

mkdir -p "$TEMP_FOLDER"

# Copy all YAML files from the base folder to the temporary folder

pwd
#cp "$BASE_FOLDER" "$TEMP_FOLDER/"

cp -r "$BASE_FOLDER"/* "$TEMP_FOLDER"/

# Use sed to replace the placeholder with the new image name in the deployment file in the temporary folder
sed -i "s|{{IMAGE_NAME}}|$IMG|g" "$TEMP_FOLDER/deployments/base/$DEPLOYMENT_FILE"

# Use sed to replace the placeholder with the new VERSION name in the deployment file in the temporary folder
sed -i "s|{{VERSION}}|$VERSION|g" "$TEMP_FOLDER/deployments/base/$DEPLOYMENT_FILE"

# Apply the updated deployment to Kubernetes
kubectl apply -k temp/deployments/$ENVIRONMENT

# After applying, delete the temporary folder
rm -rf "$TEMP_FOLDER"

echo "Updated Kubernetes deployment file with new image name."
