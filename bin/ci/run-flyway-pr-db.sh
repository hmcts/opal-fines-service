#!/bin/bash
set -e

PR_NUMBER=$CHANGE_ID

PR_HOST="opal-fines-service-dev.postgres.database.azure.com"  #TODO update once setup
PR_USER="hmcts"  #TODO update once setup
PR_PASSWORD="$(kubectl -n opal get secret postgres -o json | jq .data.PASSWORD -r | base64 -d)"
PR_DATABASE="pr-${PR_NUMBER}-opal"

echo "Using Database password: ***${PR_PASSWORD: -3}"
echo "Using PR_NUMBER: $PR_NUMBER"

export FLYWAY_URL="jdbc:postgresql://${PR_HOST}:5432/${PR_DATABASE}"
export FLYWAY_USER=$PR_USER
export FLYWAY_PASSWORD=$PR_PASSWORD

./gradlew --no-daemon --init-script init.gradle assemble migratePostgresDatabase
