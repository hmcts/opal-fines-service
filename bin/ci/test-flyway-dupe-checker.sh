#!/usr/bin/env bash
set -euo pipefail

SCRIPT_UNDER_TEST="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/flyway-dupe-checker.sh"
TMP_ROOT="$(mktemp -d)"

cleanup() {
  rm -rf "$TMP_ROOT"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

assert_file_exists() {
  local path="$1"
  [[ -f "$path" ]] || fail "Expected file to exist: $path"
}

assert_file_missing() {
  local path="$1"
  [[ ! -e "$path" ]] || fail "Expected file to be missing: $path"
}

assert_contains() {
  local file="$1"
  local expected="$2"
  grep -Fq "$expected" "$file" || fail "Expected '$file' to contain: $expected"
}

assert_no_diff() {
  local repo="$1"
  git -C "$repo" diff --quiet || fail "Expected no working tree diff in fixture repo: $repo"
  git -C "$repo" diff --cached --quiet || fail "Expected no staged diff in fixture repo: $repo"
}

assert_staged_migration_renames() {
  local repo="$1"
  git -C "$repo" diff --quiet -- src/main/resources/db/migration \
    || fail "Expected no unstaged migration diff in fixture repo: $repo"
  if git -C "$repo" diff --cached --quiet -- src/main/resources/db/migration; then
    fail "Expected staged migration rename changes in fixture repo: $repo"
  fi
}

init_repo() {
  local name="$1"
  local repo="$TMP_ROOT/$name"
  mkdir -p "$repo/src/main/resources/db/migration/ddl"
  git -C "$repo" init --initial-branch=master >/dev/null
  git -C "$repo" config user.email "test@example.com"
  git -C "$repo" config user.name "Test User"
  printf "master 1\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__create_interface_files_and_enums.sql"
  printf "master 2\n" > "$repo/src/main/resources/db/migration/ddl/V1_2__create_business_unit_bank_account_table.sql"
  git -C "$repo" add .
  git -C "$repo" commit -m "base migrations" >/dev/null
  git -C "$repo" branch --set-upstream-to=master >/dev/null 2>&1 || true
  git -C "$repo" checkout -b feature >/dev/null
  git -C "$repo" update-ref refs/remotes/origin/master refs/heads/master
  echo "$repo"
}

commit_pr_changes() {
  local repo="$1"
  git -C "$repo" add .
  git -C "$repo" commit -m "pr migrations" >/dev/null
}

run_script() {
  local repo="$1"
  shift
  (
    cd "$repo"
    "$SCRIPT_UNDER_TEST" "$@"
  )
}

test_disable_migration_update_check_only_still_fails_on_duplicates() {
  local repo
  repo="$(init_repo disable-migration-update-check-only-duplicates)"
  printf "pr duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  commit_pr_changes "$repo"

  if run_script "$repo" --mode=check-only >"$repo/output.txt" 2>&1; then
    fail "Expected check-only mode to fail on duplicates"
  fi

  assert_contains "$repo/output.txt" "Duplicate flyway migration scripts found"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  assert_file_missing "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  assert_no_diff "$repo"
}

test_check_only_passes_on_unique_versions() {
  local repo
  repo="$(init_repo check-only-unique)"
  printf "pr unique\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=check-only
  assert_no_diff "$repo"
}

test_check_only_fails_on_numeric_equivalent_duplicate_versions() {
  local repo
  repo="$(init_repo check-only-numeric-duplicate-versions)"
  printf "pr padded duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_03__new_pr_migration.sql"
  printf "pr unpadded duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__another_pr_migration.sql"
  commit_pr_changes "$repo"

  if run_script "$repo" --mode=check-only >"$repo/output.txt" 2>&1; then
    fail "Expected check-only mode to fail on numeric-equivalent duplicate versions"
  fi

  assert_contains "$repo/output.txt" "Duplicate flyway migration scripts found"
  assert_no_diff "$repo"
}

test_check_only_fails_on_unsupported_filename() {
  local repo
  repo="$(init_repo check-only-unsupported-filename)"
  printf "pr invalid\n" > "$repo/src/main/resources/db/migration/ddl/V1_0_invalid.sql"
  printf "pr valid\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  commit_pr_changes "$repo"

  if run_script "$repo" --mode=check-only >"$repo/output.txt" 2>&1; then
    fail "Expected check-only mode to fail on unsupported migration filename"
  fi

  assert_contains "$repo/output.txt" "Unsupported Flyway migration filename"
  assert_no_diff "$repo"
}

test_auto_update_renames_added_duplicates_after_master() {
  local repo summary
  repo="$(init_repo auto-update-duplicates)"
  summary="$repo/rename-summary.md"
  printf "pr 1\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__name2.sql"
  printf "pr 2\n" > "$repo/src/main/resources/db/migration/ddl/V1_2__name2.sql"
  printf "pr 3\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__name2.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_missing "$repo/src/main/resources/db/migration/ddl/V1_1__name2.sql"
  assert_file_missing "$repo/src/main/resources/db/migration/ddl/V1_2__name2.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_3__name2.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_4__name2.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_5__name2.sql"
  assert_contains "$summary" "V1_1__name2.sql"
  assert_contains "$summary" "V1_5__name2.sql"
}

test_auto_update_leaves_staged_rename_changes_detectable() {
  local repo summary
  repo="$(init_repo auto-update-staged-renames)"
  summary="$repo/rename-summary.md"
  printf "pr duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_staged_migration_renames "$repo"
}

test_auto_update_renames_numeric_equivalent_added_versions() {
  local repo summary
  repo="$(init_repo auto-update-numeric-equivalent-added-versions)"
  summary="$repo/rename-summary.md"
  printf "pr padded version\n" > "$repo/src/main/resources/db/migration/ddl/V1_03__padded.sql"
  printf "pr unpadded version\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__unpadded.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_03__padded.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_4__unpadded.sql"
  assert_file_missing "$repo/src/main/resources/db/migration/ddl/V1_3__unpadded.sql"
  assert_staged_migration_renames "$repo"
}

test_auto_update_sorts_added_files_by_numeric_version_then_path() {
  local repo summary
  repo="$(init_repo auto-update-numeric-ordering)"
  summary="$repo/rename-summary.md"
  printf "minor 10\n" > "$repo/src/main/resources/db/migration/ddl/V1_10__later.sql"
  printf "minor 2\n" > "$repo/src/main/resources/db/migration/ddl/V1_2__earlier.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_3__earlier.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_04__later.sql"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_3__earlier.sql" "minor 2"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_04__later.sql" "minor 10"
}

test_auto_update_leaves_already_valid_added_files() {
  local repo summary
  repo="$(init_repo auto-update-valid)"
  summary="$repo/rename-summary.md"
  printf "pr 3\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  printf "pr 4\n" > "$repo/src/main/resources/db/migration/ddl/V1_4__second_pr_migration.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_4__second_pr_migration.sql"
  [[ ! -s "$summary" ]] || fail "Expected empty summary when no files are renamed"
}

test_auto_update_with_no_added_migrations_leaves_no_diff() {
  local repo summary
  repo="$(init_repo auto-update-no-added-migrations)"
  summary="$repo/rename-summary.md"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  [[ -f "$summary" && ! -s "$summary" ]] || fail "Expected an empty summary when no migrations are added"
  assert_no_diff "$repo"
}

test_auto_update_does_not_rename_master_files() {
  local repo summary
  repo="$(init_repo auto-update-master-unchanged)"
  summary="$repo/rename-summary.md"
  printf "updated master file\n" > "$repo/src/main/resources/db/migration/ddl/V1_2__create_business_unit_bank_account_table.sql"
  printf "pr duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_2__create_business_unit_bank_account_table.sql"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_2__create_business_unit_bank_account_table.sql" "updated master file"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"
  assert_contains "$summary" "V1_1__new_pr_migration.sql"
  assert_contains "$summary" "V1_3__new_pr_migration.sql"
}

test_auto_update_fails_when_target_exists_unexpectedly() {
  local repo summary
  repo="$(init_repo auto-update-target-exists)"
  summary="$repo/rename-summary.md"
  printf "pr duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  commit_pr_changes "$repo"
  printf "untracked target\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__new_pr_migration.sql"

  if run_script "$repo" --mode=auto-update --summary-file "$summary" >"$repo/output.txt" 2>&1; then
    fail "Expected auto-update mode to fail when target exists unexpectedly"
  fi

  assert_contains "$repo/output.txt" "Refusing to overwrite existing migration file"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
}

test_auto_update_handles_targets_that_are_also_added_sources() {
  local repo summary
  repo="$(init_repo auto-update-target-is-added-source)"
  summary="$repo/rename-summary.md"
  printf "first\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__first.sql"
  printf "second\n" > "$repo/src/main/resources/db/migration/ddl/V1_2__second.sql"
  printf "third\n" > "$repo/src/main/resources/db/migration/ddl/V1_3__third.sql"
  commit_pr_changes "$repo"

  run_script "$repo" --mode=auto-update --summary-file "$summary"

  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_3__first.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_4__second.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_5__third.sql"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_3__first.sql" "first"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_4__second.sql" "second"
  assert_contains "$repo/src/main/resources/db/migration/ddl/V1_5__third.sql" "third"
}

test_auto_update_validates_malformed_filenames_before_renaming() {
  local repo summary
  repo="$(init_repo auto-update-malformed-filename)"
  summary="$repo/rename-summary.md"
  printf "malformed\n" > "$repo/src/main/resources/db/migration/ddl/V1_0_malformed.sql"
  printf "pr duplicate\n" > "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  commit_pr_changes "$repo"

  if run_script "$repo" --mode=auto-update --summary-file "$summary" >"$repo/output.txt" 2>&1; then
    fail "Expected auto-update mode to fail on malformed migration filename"
  fi

  assert_contains "$repo/output.txt" "Unsupported Flyway migration filename"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_0_malformed.sql"
  assert_file_exists "$repo/src/main/resources/db/migration/ddl/V1_1__new_pr_migration.sql"
  assert_file_missing "$summary"
  assert_no_diff "$repo"
}

test_disable_migration_update_check_only_still_fails_on_duplicates
test_check_only_passes_on_unique_versions
test_check_only_fails_on_numeric_equivalent_duplicate_versions
test_check_only_fails_on_unsupported_filename
test_auto_update_renames_added_duplicates_after_master
test_auto_update_leaves_staged_rename_changes_detectable
test_auto_update_renames_numeric_equivalent_added_versions
test_auto_update_sorts_added_files_by_numeric_version_then_path
test_auto_update_leaves_already_valid_added_files
test_auto_update_with_no_added_migrations_leaves_no_diff
test_auto_update_does_not_rename_master_files
test_auto_update_fails_when_target_exists_unexpectedly
test_auto_update_handles_targets_that_are_also_added_sources
test_auto_update_validates_malformed_filenames_before_renaming

echo "All flyway-dupe-checker tests passed"