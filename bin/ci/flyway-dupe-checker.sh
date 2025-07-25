#!/usr/bin/env bash
set -e

flyway_script_count=$(find ./src/main/resources/db/migration/allEnvs -type f | wc -l | jq -r)
echo "Flyway migration script count: $flyway_script_count"

readarray -t unique_numbers < <(find ./src/main/resources/db/migration/allEnvs -type f | awk -F '__' '{print $1}' | sort | uniq -c)
echo "Unique flyway migration script count: ${#unique_numbers[@]}"

if [ "$flyway_script_count" -ne "${#unique_numbers[@]}" ]; then
  echo "Duplicate flyway migration scripts found"
  exit 1
fi