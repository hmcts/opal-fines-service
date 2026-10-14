#!/usr/bin/env bash
set -euo pipefail

SCRIPT_UNDER_TEST="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/master-pipeline-status-commenter.sh"
TMP_ROOT="$(mktemp -d)"

cleanup() {
  rm -rf "$TMP_ROOT"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

assert_equals() {
  local expected="$1"
  local actual="$2"
  [[ "$actual" == "$expected" ]] || fail "Expected '$expected', got '$actual'"
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq -- "$expected" "$file" || fail "Expected '$file' to contain: $expected"
}

write_rollup_fixture() {
  local file="$1"
  local state="$2"
  cat > "$file" <<JSON
{
  "data": {
    "repository": {
      "ref": {
        "target": {
          "oid": "abc123456789",
          "statusCheckRollup": {
            "state": "$state"
          }
        }
      }
    }
  }
}
JSON
}

write_missing_rollup_fixture() {
  local file="$1"
  cat > "$file" <<'JSON'
{
  "data": {
    "repository": {
      "ref": {
        "target": {
          "oid": "abc123456789",
          "statusCheckRollup": null
        }
      }
    }
  }
}
JSON
}

test_success_rollup_classifies_passing() {
  local fixture="$TMP_ROOT/success.json"
  write_rollup_fixture "$fixture" "SUCCESS"

  local result
  result="$("$SCRIPT_UNDER_TEST" --classify-rollup "$fixture")"

  assert_equals "passing abc123456789" "$result"
}

test_failure_rollup_classifies_failing() {
  local fixture="$TMP_ROOT/failure.json"
  write_rollup_fixture "$fixture" "FAILURE"

  local result
  result="$("$SCRIPT_UNDER_TEST" --classify-rollup "$fixture")"

  assert_equals "failing abc123456789" "$result"
}

test_error_rollup_classifies_failing() {
  local fixture="$TMP_ROOT/error.json"
  write_rollup_fixture "$fixture" "ERROR"

  local result
  result="$("$SCRIPT_UNDER_TEST" --classify-rollup "$fixture")"

  assert_equals "failing abc123456789" "$result"
}

test_pending_rollup_classifies_building() {
  local fixture="$TMP_ROOT/pending.json"
  write_rollup_fixture "$fixture" "PENDING"

  local result
  result="$("$SCRIPT_UNDER_TEST" --classify-rollup "$fixture")"

  assert_equals "building abc123456789" "$result"
}

test_missing_rollup_classifies_building() {
  local fixture="$TMP_ROOT/missing.json"
  write_missing_rollup_fixture "$fixture"

  local result
  result="$("$SCRIPT_UNDER_TEST" --classify-rollup "$fixture")"

  assert_equals "building abc123456789" "$result"
}

test_failing_comment_body_contains_red_badge_marker_and_sha() {
  local output="$TMP_ROOT/failing-comment.md"

  "$SCRIPT_UNDER_TEST" --comment-body failing abc123456789 "2026-10-14 12:34:56 UTC" > "$output"

  assert_contains "$output" "<!-- master-pipeline-status -->"
  assert_contains "$output" "![master failing](https://img.shields.io/badge/master-failing-red)"
  assert_contains "$output" "master is failing"
  assert_contains "$output" "abc123456789"
  assert_contains "$output" "Last updated: \`2026-10-14 12:34:56 UTC\`."
}

test_building_comment_body_contains_yellow_badge_marker_and_sha() {
  local output="$TMP_ROOT/building-comment.md"

  "$SCRIPT_UNDER_TEST" --comment-body building abc123456789 "2026-10-14 12:34:56 UTC" > "$output"

  assert_contains "$output" "<!-- master-pipeline-status -->"
  assert_contains "$output" "![master building](https://img.shields.io/badge/master-building-yellow)"
  assert_contains "$output" "master is building"
  assert_contains "$output" "abc123456789"
  assert_contains "$output" "Last updated: \`2026-10-14 12:34:56 UTC\`."
}

test_passing_comment_body_contains_green_badge_marker_and_sha() {
  local output="$TMP_ROOT/passing-comment.md"

  "$SCRIPT_UNDER_TEST" --comment-body passing abc123456789 "2026-10-14 12:34:56 UTC" > "$output"

  assert_contains "$output" "<!-- master-pipeline-status -->"
  assert_contains "$output" "![master passing](https://img.shields.io/badge/master-passing-green)"
  assert_contains "$output" "master is passing"
  assert_contains "$output" "abc123456789"
  assert_contains "$output" "Last updated: \`2026-10-14 12:34:56 UTC\`."
}

test_existing_marker_comment_is_found() {
  local comments="$TMP_ROOT/comments.json"
  cat > "$comments" <<'JSON'
[
  {
    "id": 101,
    "user": {
      "login": "someone"
    },
    "body": "unrelated"
  },
  {
    "id": 202,
    "user": {
      "login": "github-actions[bot]"
    },
    "body": "<!-- master-pipeline-status -->\nold body"
  }
]
JSON

  local result
  result="$("$SCRIPT_UNDER_TEST" --find-marker-comment "$comments")"

  assert_equals "202" "$result"
}

test_missing_marker_comment_returns_empty() {
  local comments="$TMP_ROOT/no-marker-comments.json"
  cat > "$comments" <<'JSON'
[
  {
    "id": 101,
    "user": {
      "login": "github-actions[bot]"
    },
    "body": "unrelated"
  }
]
JSON

  local result
  result="$("$SCRIPT_UNDER_TEST" --find-marker-comment "$comments")"

  assert_equals "" "$result"
}

test_run_mode_updates_existing_and_missing_comments_for_building() {
  local work="$TMP_ROOT/run-building"
  mkdir -p "$work/bin"
  local log="$work/gh.log"
  local rollup="$work/pending.json"
  write_rollup_fixture "$rollup" "PENDING"

  cat > "$work/comments-11.json" <<'JSON'
[
  {
    "id": 202,
    "user": {
      "login": "github-actions[bot]"
    },
    "body": "<!-- master-pipeline-status -->\nold body"
  }
]
JSON

  cat > "$work/comments-12.json" <<'JSON'
[]
JSON

  cat > "$work/bin/gh" <<GH
#!/usr/bin/env bash
set -euo pipefail
echo "\$*" >> "$log"
if [[ "\$1 \$2" == "api graphql" ]]; then
  cat "$rollup"
  exit 0
fi
if [[ "\$*" == "api repos/hmcts/opal-file-handler-service/pulls --method GET --paginate -f state=open -f base=master --jq .[].number" ]]; then
  printf "11\n12\n"
  exit 0
fi
if [[ "\$*" == "api repos/hmcts/opal-file-handler-service/issues/11/comments --paginate" ]]; then
  cat "$work/comments-11.json"
  exit 0
fi
if [[ "\$*" == "api repos/hmcts/opal-file-handler-service/issues/12/comments --paginate" ]]; then
  cat "$work/comments-12.json"
  exit 0
fi
if [[ "\$*" == api\ repos/hmcts/opal-file-handler-service/issues/comments/202* ]]; then
  exit 0
fi
if [[ "\$*" == api\ repos/hmcts/opal-file-handler-service/issues/12/comments* ]]; then
  exit 0
fi
echo "unexpected gh call: \$*" >&2
exit 1
GH
  chmod +x "$work/bin/gh"

  PATH="$work/bin:$PATH" GH_TOKEN=test-token "$SCRIPT_UNDER_TEST" --run hmcts/opal-file-handler-service

  grep -Fq -- "issues/comments/202" "$log" || fail "Expected existing PR comment to be updated"
  grep -Fq -- "-X PATCH" "$log" || fail "Expected existing PR comment to use PATCH"
  grep -Fq -- "issues/12/comments" "$log" || fail "Expected missing PR comment to be created"
  grep -Fq -- "-X POST" "$log" || fail "Expected missing PR comment to use POST"
}

test_success_rollup_classifies_passing
test_failure_rollup_classifies_failing
test_error_rollup_classifies_failing
test_pending_rollup_classifies_building
test_missing_rollup_classifies_building
test_failing_comment_body_contains_red_badge_marker_and_sha
test_building_comment_body_contains_yellow_badge_marker_and_sha
test_passing_comment_body_contains_green_badge_marker_and_sha
test_existing_marker_comment_is_found
test_missing_marker_comment_returns_empty
test_run_mode_updates_existing_and_missing_comments_for_building

echo "All master-pipeline-status-commenter tests passed"
