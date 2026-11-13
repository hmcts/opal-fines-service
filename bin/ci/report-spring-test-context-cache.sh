#!/usr/bin/env bash
set -euo pipefail

TASK_NAME=""
HTML_DIRECTORY=""
XML_DIRECTORY=""
OUTPUT_FILE=""

usage() {
  cat >&2 <<'USAGE'
Usage: report-spring-test-context-cache.sh \
  --task <task-name> \
  --html-directory <gradle-test-html-directory> \
  --xml-directory <gradle-test-xml-directory> \
  --output-file <report-json-path>
USAGE
}

parse_arguments() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --task)
        [[ $# -ge 2 ]] || { echo "Missing value for $1" >&2; usage; exit 2; }
        TASK_NAME="$2"
        shift 2
        ;;
      --html-directory)
        [[ $# -ge 2 ]] || { echo "Missing value for $1" >&2; usage; exit 2; }
        HTML_DIRECTORY="$2"
        shift 2
        ;;
      --xml-directory)
        [[ $# -ge 2 ]] || { echo "Missing value for $1" >&2; usage; exit 2; }
        XML_DIRECTORY="$2"
        shift 2
        ;;
      --output-file)
        [[ $# -ge 2 ]] || { echo "Missing value for $1" >&2; usage; exit 2; }
        OUTPUT_FILE="$2"
        shift 2
        ;;
      *)
        echo "Unknown argument: $1" >&2
        usage
        exit 2
        ;;
    esac
  done

  if [[ -z "$TASK_NAME" || -z "$HTML_DIRECTORY" || -z "$XML_DIRECTORY" || -z "$OUTPUT_FILE" ]]; then
    usage
    exit 2
  fi
}

json_escape() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\t'/\\t}"
  value="${value//$'\r'/\\r}"
  value="${value//$'\n'/\\n}"
  printf '%s' "$value"
}

read_numeric_attribute() {
  local line="$1"
  local attribute="$2"
  local pattern="${attribute}=\"([0-9]+([.][0-9]+)?)\""

  if [[ $line =~ $pattern ]]; then
    printf '%s' "${BASH_REMATCH[1]}"
  else
    printf '0'
  fi
}

collect_cache_statistics() {
  local cache_pattern cache_line size max_size hit_count miss_count failure_count activity
  cache_pattern='size[[:space:]]*=[[:space:]]*([0-9]+),[[:space:]]*maxSize[[:space:]]*=[[:space:]]*([0-9]+),.*'
  cache_pattern+='hitCount[[:space:]]*=[[:space:]]*([0-9]+),[[:space:]]*missCount[[:space:]]*=[[:space:]]*([0-9]+),'
  cache_pattern+='[[:space:]]*failureCount[[:space:]]*=[[:space:]]*([0-9]+)'

  selected_size=0
  selected_max_size=0
  selected_hit_count=0
  selected_miss_count=0
  selected_failure_count=0
  selected_activity=-1
  valid_entry_count=0
  malformed_entry_count=0

  while IFS= read -r cache_line; do
    [[ -n "$cache_line" ]] || continue

    if [[ $cache_line =~ $cache_pattern ]]; then
      size="${BASH_REMATCH[1]}"
      max_size="${BASH_REMATCH[2]}"
      hit_count="${BASH_REMATCH[3]}"
      miss_count="${BASH_REMATCH[4]}"
      failure_count="${BASH_REMATCH[5]}"
      activity=$((hit_count + miss_count + failure_count))
      valid_entry_count=$((valid_entry_count + 1))

      # HTML file ordering is not chronological, so select the greatest cumulative activity.
      if ((activity > selected_activity)); then
        selected_size="$size"
        selected_max_size="$max_size"
        selected_hit_count="$hit_count"
        selected_miss_count="$miss_count"
        selected_failure_count="$failure_count"
        selected_activity="$activity"
      fi
    else
      malformed_entry_count=$((malformed_entry_count + 1))
    fi
  done < <(
    # Missing reports and no matches are warnings rather than parser failures.
    find "$HTML_DIRECTORY" -type f -name '*.html' \
      -exec grep -hF 'Spring test ApplicationContext cache statistics:' {} + 2>/dev/null || true
  )
}

collect_test_summary() {
  local testsuite_pattern='<testsuite[[:space:]]'
  local xml_file xml_line suite_tests suite_skipped suite_failures suite_errors suite_runtime

  tests_found=0
  skipped_tests=0
  failed_tests=0
  runtime_seconds="0.000"

  while IFS= read -r -d '' xml_file; do
    while IFS= read -r xml_line; do
      [[ $xml_line =~ $testsuite_pattern ]] || continue

      suite_tests="$(read_numeric_attribute "$xml_line" tests)"
      suite_skipped="$(read_numeric_attribute "$xml_line" skipped)"
      suite_failures="$(read_numeric_attribute "$xml_line" failures)"
      suite_errors="$(read_numeric_attribute "$xml_line" errors)"
      suite_runtime="$(read_numeric_attribute "$xml_line" time)"

      tests_found=$((tests_found + suite_tests))
      skipped_tests=$((skipped_tests + suite_skipped))
      failed_tests=$((failed_tests + suite_failures + suite_errors))
      runtime_seconds="$(
        awk -v total="$runtime_seconds" -v suite="$suite_runtime" 'BEGIN { printf "%.3f", total + suite }'
      )"
    done < "$xml_file"
  done < <(find "$XML_DIRECTORY" -maxdepth 1 -type f -name 'TEST-*.xml' -print0 2>/dev/null || true)

  executed_tests=$((tests_found - skipped_tests))
  if ((executed_tests < 0)); then
    executed_tests=0
  fi
}

resolve_build_metadata() {
  commit_identifier="${GIT_COMMIT:-}"
  if [[ -z "$commit_identifier" ]] && command -v git >/dev/null 2>&1; then
    commit_identifier="$(git rev-parse HEAD 2>/dev/null || true)"
  fi
  commit_identifier="${commit_identifier:-unknown}"
  build_identifier="${BUILD_TAG:-${BUILD_ID:-local}}"
  generated_at="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
}

prepare_report_values() {
  if ((valid_entry_count > 0)); then
    report_status="found"
    metric_size="$selected_size"
    metric_max_size="$selected_max_size"
    metric_hit_count="$selected_hit_count"
    metric_miss_count="$selected_miss_count"
    metric_failure_count="$selected_failure_count"
  else
    report_status="missing"
    metric_size="null"
    metric_max_size="null"
    metric_hit_count="null"
    metric_miss_count="null"
    metric_failure_count="null"
  fi
}

write_report() {
  mkdir -p "$(dirname "$OUTPUT_FILE")"

  cat > "$OUTPUT_FILE" <<EOF
{
  "schemaVersion": 1,
  "status": "$report_status",
  "task": "$(json_escape "$TASK_NAME")",
  "buildIdentifier": "$(json_escape "$build_identifier")",
  "commitIdentifier": "$(json_escape "$commit_identifier")",
  "generatedAt": "$generated_at",
  "selection": "snapshot with the greatest hitCount + missCount + failureCount",
  "cache": {
    "size": $metric_size,
    "maxSize": $metric_max_size,
    "hitCount": $metric_hit_count,
    "missCount": $metric_miss_count,
    "failureCount": $metric_failure_count,
    "validSnapshots": $valid_entry_count,
    "malformedSnapshots": $malformed_entry_count
  },
  "tests": {
    "found": $tests_found,
    "executed": $executed_tests,
    "failed": $failed_tests,
    "skipped": $skipped_tests,
    "runtimeSeconds": $runtime_seconds
  },
  "hardThresholdEnforced": false
}
EOF
}

print_summary() {
  if ((valid_entry_count > 0)); then
    echo "Spring Test Context Cache [${TASK_NAME}]: missCount=${selected_miss_count}," \
      "hitCount=${selected_hit_count}, failureCount=${selected_failure_count}," \
      "maxSize=${selected_max_size}, size=${selected_size}"
  else
    echo "WARNING: Spring Test Context Cache [${TASK_NAME}]:" \
      "no valid cache-statistics snapshots were found under ${HTML_DIRECTORY}."
  fi

  if ((malformed_entry_count > 0)); then
    echo "WARNING: Spring Test Context Cache [${TASK_NAME}]:" \
      "ignored ${malformed_entry_count} malformed cache-statistics snapshot(s)."
  fi

  echo "Integration Tests [${TASK_NAME}]: executed=${executed_tests}, failed=${failed_tests}," \
    "skipped=${skipped_tests}, runtimeSeconds=${runtime_seconds}"
  echo "Spring Test Context Cache report [${TASK_NAME}]: ${OUTPUT_FILE}"
}

main() {
  parse_arguments "$@"
  collect_cache_statistics
  collect_test_summary
  resolve_build_metadata
  prepare_report_values
  write_report
  print_summary
}

main "$@"
