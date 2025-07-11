#!/bin/bash
set -e

echo "*** WARNING: This script will destroy your OPAL fines PR database and restore it from staging. ***"
echo "It requires \"az\" \"pg_dump\" and \"psql\", and you must also be connected to the HMCTS VPN and have a postgres database running locally."

command -v jq >/dev/null 2>&1 || { echo >&2 "I require \"jq\" but it's not installed. Aborting."; exit 1; }
command -v az >/dev/null 2>&1 || { echo >&2 "I require \"az\" but it's not installed. Aborting."; exit 1; }
command -v pg_dump >/dev/null 2>&1 || { echo >&2 "I require \"pg_dump\" but it's not installed. Aborting."; exit 1; }
command -v psql >/dev/null 2>&1 || { echo >&2 "I require \"psql\" but it's not installed. Aborting."; exit 1; }

PR_NUMBER=$CHANGE_ID

PR_HOST="opal-fines-service-dev.postgres.database.azure.com" #TODO update once setup
PR_USER="hmcts" #TODO update once setup
PR_PASSWORD="$(kubectl -n opal get secret postgres -o json | jq .data.PASSWORD -r | base64 -d)"
PR_DATABASE="pr-${PR_NUMBER}-opal"

echo "Using Database password: ***${PR_PASSWORD: -3}"
echo "Using PR_NUMBER: $PR_NUMBER"

# make the password available for psql
export PGPASSWORD="$PR_PASSWORD"

RESTORE_COUNT=$(psql -h $PR_HOST -U $PR_USER -d $PR_DATABASE -c "SELECT count(*) FROM pipeline_restore;" -t -q || echo 0 | jq -r)
if [ "$RESTORE_COUNT" -gt 0 ]; then
  echo "Database has already been restored, exiting..."
  exit 0
fi

DUMP_FILE="/tmp/opal-api-stg-dump.sql"
RESTORE_LOG_FILE="/tmp/opal-api-pr-restore.log"
RESTORE_OUTPUT="/tmp/opal-api-pr-stdout.log"

SCHEMA=$STAGING_DB_DATABASE
DATABASE=$STAGING_DB_SCHEMA

STG_HOST=$STAGING_DB_HOST
STG_USER=$STAGING_DB_USER
STG_PASSWORD=$STAGING_DB_PASS
STG_PORT=$STAGING_DB_PORT

echo "Dumping staging database..."

# make the password available for pg_dump
export PGPASSWORD="$STG_PASSWORD"
pg_dump -h $STG_HOST -p $STG_PORT -U $STG_USER -n $SCHEMA -d $DATABASE > $DUMP_FILE

echo "Dump complete, dump file: $DUMP_FILE"
echo "Restoring PR database ($PR_DATABASE)..."

# make the password available for psql
export PGPASSWORD="$PR_PASSWORD"
# drop the opal schema
psql -h $PR_HOST -U $PR_USER -d $PR_DATABASE -c "DROP SCHEMA IF EXISTS $SCHEMA CASCADE" &> /dev/null
# restore from the dump file
psql -h $PR_HOST -U $PR_USER -d $PR_DATABASE -L $RESTORE_LOG_FILE < $DUMP_FILE &> $RESTORE_OUTPUT
# set the time of database restore, in order to check it was restored on another run of this script
psql -h $PR_HOST -U $PR_USER -d $PR_DATABASE -c "CREATE TABLE IF NOT EXISTS pipeline_restore ( restore_ts timestamp with time zone NOT NULL ) TABLESPACE pg_default; INSERT INTO darts.pipeline_restore (restore_ts) VALUES (now());"

echo "Restore complete, stdout: $RESTORE_OUTPUT  log file: $RESTORE_LOG_FILE"
echo "Output: $RESTORE_OUTPUT"
echo "Log file: $RESTORE_LOG_FILE"
