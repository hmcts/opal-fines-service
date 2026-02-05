# Opal Logging Service - Docker Development Environment

Start a development environment for Opal Services.

## Prerequisites

- Docker and Docker Compose installed on your machine.
- Clone the following repositories to the same parent directory:
    - [opal-fines-service](https://github.com/hmcts/opal-fines-service) (this repository)
    - [opal-user-service](https://github.com/hmcts/opal-user-service)
    - [opal-logging-service](https://github.com/hmcts/opal-logging-service) 
    - [opal-shared-infrastructure](https://github.com/hmcts/opal-shared-infrastructure)
- Ensure you have '.env.shared' file in 'opal-shared-infrastructure/docker-files/' directory. (You
  will need to create this file as it is git ignored by default). It is recommended to include the
  following environment variables:
    - AAD_CLIENT_ID
    - AAD_CLIENT_SECRET
    - AAD_TENANT_ID
    - LAUNCH_DARKLY_SDK_KEY
    - OPAL_TEST_USER_PASSWORD

## Starting the Services

To start the Opal Fines Service along with the Opal User Service and Opal Logging Service, use the
following commands:

Docker using local version of fines (Based on your local changes)
(All other services will use the images from sdshmctspublic)
```bash / zsh
 docker compose -p opal-fines-local \
  -f docker-compose.base.yml \
  -f docker-compose.local.yml \
  -f ../opal-user-service/docker-compose.base.yml \
  -f ../opal-user-service/docker-compose.master.yml \
  -f ../opal-logging-service/docker-compose.base.yml \
  -f ../opal-logging-service/docker-compose.master.yml \
  up --build -d
```
Docker using master version of fines (Based on the code in master)
```bash / zsh
 docker compose -p opal-fines-master \
  -f docker-compose.base.yml \
  -f docker-compose.master.yml \
  -f ../opal-user-service/docker-compose.base.yml \
  -f ../opal-user-service/docker-compose.master.yml \
  -f ../opal-logging-service/docker-compose.base.yml \
  -f ../opal-logging-service/docker-compose.master.yml \
  up --build -d
```
## Alternatively
install and used the scripts described in [the Scripts readme](scripts/scripts-readme.md) to start the services.
