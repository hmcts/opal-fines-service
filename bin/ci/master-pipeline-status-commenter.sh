#!/usr/bin/env bash
set -euo pipefail

MARKER="<!-- master-pipeline-status -->"
BOT_LOGIN="github-actions[bot]"
BASE_BRANCH="${BASE_BRANCH:-master}"

usage() {
  cat >&2 <<'USAGE'
Usage:
  master-pipeline-status-commenter.sh --classify-rollup <graphql-response-json>
  master-pipeline-status-commenter.sh --comment-body <building|passing|failing> <sha> [updated-at]
  master-pipeline-status-commenter.sh --find-marker-comment <comments-json>
  master-pipeline-status-commenter.sh --run <owner/repo>
USAGE
}

classify_rollup() {
  local file="$1"
  local sha
  local state

  sha="$(jq -r '.data.repository.ref.target.oid // empty' "$file")"
  state="$(jq -r '.data.repository.ref.target.statusCheckRollup.state // empty' "$file")"

  [[ -n "$sha" ]] || {
    echo "Could not resolve ${BASE_BRANCH} SHA from GitHub response" >&2
    exit 1
  }

  case "$state" in
    SUCCESS)
      echo "passing $sha"
      ;;
    FAILURE | ERROR)
      echo "failing $sha"
      ;;
    *)
      echo "building $sha"
      ;;
  esac
}

write_comment_body() {
  local state="$1"
  local sha="$2"
  local updated_at="${3:-}"

  if [[ -z "$updated_at" ]]; then
    updated_at="$(date -u '+%Y-%m-%d %H:%M:%S UTC')"
  fi

  echo "$MARKER"
  case "$state" in
    building)
      echo "![master building](https://img.shields.io/badge/master-building-yellow)"
      echo
      echo "${BASE_BRANCH} is building. Checks are still queued, pending, or in progress."
      ;;
    passing)
      echo "![master passing](https://img.shields.io/badge/master-passing-green)"
      echo
      echo "${BASE_BRANCH} is passing. Checks are currently green."
      ;;
    failing)
      echo "![master failing](https://img.shields.io/badge/master-failing-red)"
      echo
      echo "${BASE_BRANCH} is failing. Please fix this before merging this PR."
      ;;
    *)
      echo "Unsupported comment state: $state" >&2
      exit 2
      ;;
  esac
  echo
  echo "Checked \`${BASE_BRANCH}\` at \`${sha}\`."
  echo "Last updated: \`${updated_at}\`."
}

find_marker_comment() {
  local file="$1"
  jq -r --arg marker "$MARKER" --arg bot "$BOT_LOGIN" '
    map(select(.user.login == $bot and (.body // "" | contains($marker))))
    | first
    | .id // empty
  ' "$file"
}

require_gh_token() {
  [[ -n "${GH_TOKEN:-}" ]] || {
    echo "GH_TOKEN is required" >&2
    exit 1
  }
}

fetch_master_rollup() {
  local repository="$1"
  local owner="${repository%%/*}"
  local repo="${repository#*/}"

  gh api graphql \
    -f owner="$owner" \
    -f name="$repo" \
    -f branch="refs/heads/${BASE_BRANCH}" \
    -f query='
      query($owner: String!, $name: String!, $branch: String!) {
        repository(owner: $owner, name: $name) {
          ref(qualifiedName: $branch) {
            target {
              ... on Commit {
                oid
                statusCheckRollup {
                  state
                }
              }
            }
          }
        }
      }
    '
}

open_pr_numbers() {
  local repository="$1"
  gh api "repos/${repository}/pulls" --method GET --paginate -f state=open -f base="$BASE_BRANCH" --jq '.[].number'
}

issue_comments() {
  local repository="$1"
  local pr_number="$2"
  gh api "repos/${repository}/issues/${pr_number}/comments" --paginate
}

upsert_pr_comment() {
  local repository="$1"
  local pr_number="$2"
  local state="$3"
  local sha="$4"
  local comments_file="$5"
  local body_file="$6"
  local comment_id

  issue_comments "$repository" "$pr_number" > "$comments_file"
  comment_id="$(find_marker_comment "$comments_file")"
  write_comment_body "$state" "$sha" > "$body_file"
  jq -n --rawfile body "$body_file" '{body: $body}' > "${body_file}.json"

  if [[ -n "$comment_id" ]]; then
    gh api "repos/${repository}/issues/comments/${comment_id}" \
      -X PATCH \
      --input "${body_file}.json" >/dev/null
  else
    gh api "repos/${repository}/issues/${pr_number}/comments" \
      -X POST \
      --input "${body_file}.json" >/dev/null
  fi
}

run_commenter() {
  local repository="$1"
  local tmp_dir
  tmp_dir="$(mktemp -d)"
  RUN_TMP_DIR="$tmp_dir"
  trap 'rm -rf "${RUN_TMP_DIR:-}"' EXIT

  require_gh_token
  fetch_master_rollup "$repository" > "$tmp_dir/rollup.json"

  local classification
  local state
  local sha
  classification="$(classify_rollup "$tmp_dir/rollup.json")"
  state="${classification%% *}"
  sha="${classification#* }"

  local pr_number
  while IFS= read -r pr_number; do
    [[ -n "$pr_number" ]] || continue
    upsert_pr_comment "$repository" "$pr_number" "$state" "$sha" "$tmp_dir/comments-${pr_number}.json" "$tmp_dir/body-${pr_number}.md"
  done < <(open_pr_numbers "$repository")
}

case "${1:-}" in
  --classify-rollup)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    classify_rollup "$2"
    ;;
  --comment-body)
    [[ $# -eq 3 || $# -eq 4 ]] || { usage; exit 2; }
    write_comment_body "$2" "$3" "${4:-}"
    ;;
  --find-marker-comment)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    find_marker_comment "$2"
    ;;
  --run)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    run_commenter "$2"
    ;;
  *)
    usage
    exit 2
    ;;
esac
