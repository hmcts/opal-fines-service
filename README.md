# Opal Fines Service
[![API Docs](https://img.shields.io/badge/API%20Docs-Fines_Services-e140ad.svg)](https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/opal-fines-service.json)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts%3Aopal-fines-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts%3Aopal-fines-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts%3Aopal-fines-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts%3Aopal-fines-service)

## Getting Started

### Prerequisites
- [JDK 21](https://java.com)
- [Docker](https://docker.com)

## Building and deploying the application

### Running the application

#### Environment variables

The following environment variables are required to run the service.

```bash / zsh
AAD_CLIENT_ID= <Ask Team Memebers>
AAD_CLIENT_SECRET=<Ask Team Memebers>
AAD_TENANT_ID=<Ask Team Memebers>
OPAL_TEST_USER_PASSWORD=<Ask Team Memebers>

LAUNCH_DARKLY_SDK_KEY=<Ask Team Memebers>
```

#### Caching

Redis has been configured as the default caching provider. When running docker-compose with the local configuration a Redis container will be started.

If starting the opal-fines-service from Intellij or the command line you have the following options:
Follow instructions under 'Running the application locally'

In local env by default opal-fines-service uses simple cache instead of Redis cache. This can be enabled by setting this env variable:
```bash / zsh
OPAL_REDIS_ENABLED=true
```

Alternatively the opal-fines-service can be run using a simple in-memory cache by starting the application with the profile in-memory-caching.

To view the cache - when running against local Redis - Intellij has a free plugin called Redis Helper.
However, if you want to view the cache in staging the plugin doesn't support SSL. Instead, install:

```bash
brew install --cask another-redis-desktop-manager
sudo xattr -rd com.apple.quarantine /Applications/Another\ Redis\ Desktop\ Manager.app
```

You can also run redis container in local docker:

**Bash**:
```bash
  docker-compose up redis
```
**Zsh**:
```zsh
  docker compose up redis
```

#### Approach 1: Dev Application (No existing dependencies)

The simplest way to run the application is using the `bootTestRun` Gradle task:

```bash / zsh
  ./gradlew bootTestRun
```

This task has no dependencies and starts up a Postgres database in Docker using [Testcontainers](https://testcontainers.com).
The database is available on `jdbc:postgresql://localhost:5432/opal-fines-db` with username and password `opal-fines`.

To persist the database between application restarts set the environment variable `TESTCONTAINERS_REUSE_ENABLE` to `true`.
Note this does **not** persist data if the Docker container is manually stopped, or through laptop restarts).

#### Approach 2: Dev Application (With existing dependencies)

Use the standard Spring Boot `run` Gradle task:

```bash / zsh
  ./gradlew run
```

This approach can be used if a database is already running and may be preferred if the lack of long-term data persistence
from the previous approach is an issue for development.

#### Approach 3: Docker

Create the image of the application by executing the following command:

```bash / zsh
  ./gradlew assemble
```

Create docker image:

**Bash**:
```bash
  docker-compose build
```
**Zsh**:
```zsh 
  docker compose build
```

Run the distribution (created in `build/install/opal-fines-service` directory)
by executing the following command:

**Bash**:
```bash
  docker-compose up
```
**Zsh**:
```zsh
  docker compose up
```

To skip all the setting up and building with Docker, just execute the following command:

```bash / zsh
./bin/run-in-docker.sh
```

For more information:

```bash / zsh
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

**Bash**:
```bash
docker-compose rm
```
**Zsh**:
```zsh
docker compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash / zsh
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

### Verifying application startup

Regardless of approach followed for starting the application, in order to test if the application is up, you can call its health endpoint:

```bash / zsh
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash / zsh
  ./gradlew build
```
## Manual api testing (Postman)

Within the project's postman directory is an importable script to set up api tests in the Postman app.
Current tests cover the following apis:

PUT http://localhost:4550/api/defendant-account
Create a new or update an existing Defendant Account in OPAL

GET http://localhost:4550/api/defendant-account?businessUnitId=${Short}&accountNumber=${String}
Get an existing Defendant Account by business Unit ID and Account Number.

## OpenAPI

The OpenAPI specification is available publicly (see badge at top of README) and when running the application
at `/swagger-ui/index.html`. When running locally this is available at http://localhost:4550/swagger-ui/index.html.


## Style rules
This project we use a common set of styles rules to ensure all changes follow the same structure.
These rules are outlined in the project's style file located at .idea/codeStyles/Project.xml

To ensure we are following the same styles you will need to enable this project style on your IDE to do this following the below instructions.

**Step 1: To to your InteliJ Settings**

![img.png](readme_images/intelij_settings.png)

**Step 2: Go to the 'Code Styles' tab**

![img.png](readme_images/intelij_settings_codestyle.png)

**Step 3: Ensure the global scheme is set to 'Project' under the 'Stored in Project' heading.**

![img.png](readme_images/intelij_settings_codestyle_project.png)
## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
