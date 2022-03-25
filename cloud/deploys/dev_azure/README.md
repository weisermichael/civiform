# Dev Azure
This is a deploy directory that allows individuals to create a deploy of the 
app. We intentionally don't use a shared state with this terraform directory
to allow for everyone to have their own app. 

The process to deploy is similar
to the staging process but run from the cloud/deploys/dev_azure folder. 

# Running the dev deploy
## Copy the civiform_config.example.sh 
Copy the civiform_config.example.sh into civiform_config.sh and
 change the required variables. 

## Setup Login Radius For Local Development
Go to [Login Radius Dashboard](https://dashboard.loginradius.com/) and click
configure a civiform integration. Choose the outbound SSO Saml.

From there add an app with Sp initiated login and pick a name (this gets put
into the config as `LOGIN_RADIUS_SAML_APP_NAME`).

To generate the private key for the form run, cat the file and put into dashboard.
```
openssl genrsa -out private.key 2048
```

For generating the cert run, cat the file and put into dashboard.
```
openssl req -new -x509 -key private.key -out certificate.cert -days 365 -subj /CN=civiform-staging.hub.loginradius.com
```

We need to copy the details from a previous working setup in login radius 
once we set up the certs so look back at the staging one to fill out.

## Run
After that you can start the setup by running and following the instructions:

```
cloud/deploys/dev_azure/bin/setup  
```

# Local Docker Build to Remote Azure Deploy
If you want to do local onto terraform we build/tag/deploy the docker image 
and then update the azure app service to point to the local image. 

## 1. Build, Tag and Push the Docker Image
This should take like 30 minutes (the push takes the longest).

```
docker build -f prod.Dockerfile -t <IMAGE_TAG> --cache-from docker.io/civiform/civiform-browser-test:latest --build-arg BUILDKIT_INLINE_CACHE=1 .
docker tag <IMAGE_TAG> <DOCKER_USERNAME>/<DOCKER_REPO_NAME>:<IMAGE_TAG>
docker push <DOCKER_USERNAME>/<DOCKER_REPO_NAME>:<IMAGE_TAG>
```

## 2. Update the image name/tag for your remote azure deploy

Within the app service resource, you can select Deployment Center, and within
the registry settings change the 'Full Image Name and Tag' to be 
`<DOCKER_USERNAME>/<DOCKER_REPO_NAME>:<IMAGE_TAG>`
