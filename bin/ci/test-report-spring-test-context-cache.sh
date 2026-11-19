#!/usr/bin/env bash
set -euo pipefail

SCRIPT_UNDER_TEST="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/report-spring-test-context-cache.sh"
TMP_ROOT=""

cleanup() {
  rm -rf "$TMP_ROOT"
}

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq -- "$expected" "$file" || fail "Expected '$file' to contain: $expected"
}

create_fixture() {
  local fixture="$1"
  mkdir -p "$fixture/html/classes" "$fixture/xml"
}

write_junit_summary() {
  local fixture="$1"
  cat > "$fixture/xml/TEST-example.xml" <<'XML'
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="example" tests="7" skipped="2" failures="1" errors="1" timestamp="2026-11-03T10:00:00" hostname="localhost" time="12.345">
</testsuite>
XML
}

run_report() {
  local fixture="$1"
  GIT_COMMIT="abc123" BUILD_TAG="jenkins-opal-42" "$SCRIPT_UNDER_TEST" \
    --task integration \
    --html-directory "$fixture/html" \
    --xml-directory "$fixture/xml" \
    --output-file "$fixture/output/spring-test-context-cache.json" \
    > "$fixture/output.txt"
}

test_selects_snapshot_with_greatest_activity() {
  local fixture="$TMP_ROOT/multiple"
  create_fixture "$fixture"
  write_junit_summary "$fixture"
  cat > "$fixture/html/classes/example.html" <<'HTML'
Spring test ApplicationContext cache statistics: [DefaultContextCache@1 size = 2, maxSize = 4, contextUsageCount = 1, parentContextCount = 0, hitCount = 20, missCount = 3, failureCount = 0]
Spring test ApplicationContext cache statistics: [DefaultContextCache@1 size = 4, maxSize = 4, contextUsageCount = 1, parentContextCount = 0, hitCount = 120, missCount = 15, failureCount = 0]
Spring test ApplicationContext cache statistics: [DefaultContextCache@1 size = 3, maxSize = 4, contextUsageCount = 1, parentContextCount = 0, hitCount = 80, missCount = 9, failureCount = 0]
HTML

  run_report "$fixture"

  assert_contains "$fixture/output.txt" "Spring Test Context Cache [integration]: missCount=15, hitCount=120, failureCount=0, maxSize=4, size=4"
  assert_contains "$fixture/output/spring-test-context-cache.json" '"status": "found"'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"hitCount": 120'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"missCount": 15'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"validSnapshots": 3'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"executed": 5'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"failed": 2'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"skipped": 2'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"runtimeSeconds": 12.345'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"hardThresholdEnforced": false'
}

test_reports_non_zero_failure_count() {
  local fixture="$TMP_ROOT/failure-count"
  create_fixture "$fixture"
  cat > "$fixture/html/cache.html" <<'HTML'
Spring test ApplicationContext cache statistics: [DefaultContextCache@2 size = 1, maxSize = 4, contextUsageCount = 1, parentContextCount = 0, hitCount = 5, missCount = 2, failureCount = 3]
HTML

  run_report "$fixture"

  assert_contains "$fixture/output.txt" "failureCount=3"
  assert_contains "$fixture/output/spring-test-context-cache.json" '"failureCount": 3'
}

test_missing_statistics_warns_without_failing() {
  local fixture="$TMP_ROOT/missing"
  create_fixture "$fixture"
  cat > "$fixture/html/index.html" <<'HTML'
<html><body>No Spring cache output</body></html>
HTML

  run_report "$fixture"

  assert_contains "$fixture/output.txt" "WARNING: Spring Test Context Cache [integration]: no valid cache-statistics snapshots"
  assert_contains "$fixture/output/spring-test-context-cache.json" '"status": "missing"'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"missCount": null'
}

test_malformed_statistics_warns_without_failing() {
  local fixture="$TMP_ROOT/malformed"
  create_fixture "$fixture"
  cat > "$fixture/html/index.html" <<'HTML'
Spring test ApplicationContext cache statistics: [unexpected format]
HTML

  run_report "$fixture"

  assert_contains "$fixture/output.txt" "ignored 1 malformed cache-statistics snapshot"
  assert_contains "$fixture/output/spring-test-context-cache.json" '"status": "missing"'
  assert_contains "$fixture/output/spring-test-context-cache.json" '"malformedSnapshots": 1'
}

main() {
  TMP_ROOT="$(mktemp -d)"
  trap cleanup EXIT

  test_selects_snapshot_with_greatest_activity
  test_reports_non_zero_failure_count
  test_missing_statistics_warns_without_failing
  test_malformed_statistics_warns_without_failing

  echo "All Spring Test Context cache report tests passed"
}

main "$@"
