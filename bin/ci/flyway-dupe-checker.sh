#!/usr/bin/env bash
set -euo pipefail

MIGRATION_DIR="./src/main/resources/db/migration"
BASE_REF="origin/master"
MODE="check-only"
SUMMARY_FILE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode=check-only)
      MODE="check-only"
      shift
      ;;
    --mode=auto-update)
      MODE="auto-update"
      shift
      ;;
    --summary-file)
      SUMMARY_FILE="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

migration_version_from_path() {
  local path="$1"
  parse_migration_filename "$path"
  printf 'V%s_%s\n' "$(numeric_value "$MIGRATION_MAJOR")" "$(numeric_value "$MIGRATION_MINOR")"
}

find_migration_files() {
  find "$MIGRATION_DIR" -type f -name 'V*.sql' | sort
}

validate_migration_filenames() {
  local path

  while IFS= read -r path; do
    [[ -n "$path" ]] || continue
    migration_version_from_path "$path" >/dev/null
  done < <(find_migration_files)
}

check_for_duplicate_versions() {
  local duplicate_versions
  validate_migration_filenames
  duplicate_versions="$(
    while IFS= read -r path; do
      [[ -n "$path" ]] || continue
      migration_version_from_path "$path"
    done < <(find_migration_files) | sort | uniq -d | wc -l | tr -d ' '
  )"

  if [[ "$duplicate_versions" -gt 0 ]]; then
    echo "Duplicate flyway migration scripts found. Automatic migration updates may be disabled by the disable_migration_update label; resolve duplicate versions manually."
    return 1
  fi

  echo "No duplicate flyway migration scripts found"
}

parse_migration_filename() {
  local path="$1"
  local filename
  filename="$(basename "$path")"

  if [[ "$filename" =~ ^V([0-9]+)_([0-9]+)__(.+\.sql)$ ]]; then
    MIGRATION_MAJOR="${BASH_REMATCH[1]}"
    MIGRATION_MINOR="${BASH_REMATCH[2]}"
    MIGRATION_DESCRIPTION="${BASH_REMATCH[3]}"
    return 0
  fi

  echo "Unsupported Flyway migration filename: $path" >&2
  return 1
}

numeric_value() {
  local value="$1"
  echo "$((10#$value))"
}

format_minor_version() {
  local number="$1"
  local width="$2"
  printf "%0${width}d" "$number"
}

ensure_base_ref_available() {
  git rev-parse --verify "$BASE_REF" >/dev/null 2>&1 || {
    echo "Base ref is unavailable: $BASE_REF" >&2
    exit 1
  }
}

base_migration_files() {
  git ls-tree -r --name-only "$BASE_REF" -- "$MIGRATION_DIR" | grep -E '/V[0-9]+_[0-9]+__.+\.sql$' | sort || true
}

highest_base_version() {
  local highest_major=""
  local highest_minor=0
  local path

  while IFS= read -r path; do
    [[ -n "$path" ]] || continue
    parse_migration_filename "$path"
    local major_number
    local minor_number
    major_number="$(numeric_value "$MIGRATION_MAJOR")"
    minor_number="$(numeric_value "$MIGRATION_MINOR")"

    if [[ -z "$highest_major" ]]; then
      highest_major="$major_number"
    elif [[ "$highest_major" != "$major_number" ]]; then
      echo "Multiple Flyway major versions are not supported: V${highest_major} and V${major_number}" >&2
      exit 1
    fi

    if [[ "$minor_number" -gt "$highest_minor" ]]; then
      highest_minor="$minor_number"
    fi
  done < <(base_migration_files)

  [[ -n "$highest_major" ]] || {
    echo "No base Flyway migration files found under $MIGRATION_DIR" >&2
    exit 1
  }

  BASE_MAJOR="$highest_major"
  BASE_HIGHEST_MINOR="$highest_minor"
}

added_migration_files() {
  local path
  local filename

  while IFS= read -r path; do
    [[ -n "$path" ]] || continue
    filename="$(basename "$path")"
    [[ "$filename" == V*.sql ]] || continue
    parse_migration_filename "$path"
    printf '%s\t%s\t%s\n' "$(numeric_value "$MIGRATION_MAJOR")" "$(numeric_value "$MIGRATION_MINOR")" "$path"
  done < <(git diff --name-only --diff-filter=A "$BASE_REF"...HEAD -- "$MIGRATION_DIR") \
    | sort -n -k1,1 -k2,2 -k3,3 \
    | cut -f3-
}

array_contains() {
  local needle="$1"
  shift

  local item
  for item in "$@"; do
    [[ "$item" == "$needle" ]] && return 0
  done

  return 1
}

temporary_rename_path() {
  local path="$1"
  echo "${path}.flyway-dupe-checker-tmp"
}

auto_update_added_migrations() {
  ensure_base_ref_available
  validate_migration_filenames
  highest_base_version

  [[ -n "$SUMMARY_FILE" ]] && : > "$SUMMARY_FILE"

  local added_files=()
  local path
  while IFS= read -r path; do
    [[ -n "$path" ]] && added_files+=("$path")
  done < <(added_migration_files)

  if [[ "${#added_files[@]}" -eq 0 ]]; then
    echo "No PR-added Flyway migration scripts found"
    check_for_duplicate_versions
    return
  fi

  local needs_update=false
  local seen_versions=""

  for path in "${added_files[@]}"; do
    parse_migration_filename "$path"
    local major_number
    local minor_number
    local version
    major_number="$(numeric_value "$MIGRATION_MAJOR")"
    minor_number="$(numeric_value "$MIGRATION_MINOR")"
    version="V${major_number}_${minor_number}"
    [[ "$major_number" == "$BASE_MAJOR" ]] || {
      echo "Added migration major V${major_number} does not match base major V${BASE_MAJOR}: $path" >&2
      exit 1
    }

    if [[ "$minor_number" -le "$BASE_HIGHEST_MINOR" ]]; then
      needs_update=true
    fi

    if grep -Fxq "$version" <<< "$seen_versions"; then
      needs_update=true
    fi
    seen_versions="${seen_versions}"$'\n'"$version"
  done

  if [[ "$needs_update" == false ]]; then
    echo "PR-added Flyway migration scripts already follow master"
    check_for_duplicate_versions
    return
  fi

  local next_minor="$((BASE_HIGHEST_MINOR + 1))"
  local sources=()
  local targets=()
  local temporaries=()

  for path in "${added_files[@]}"; do
    parse_migration_filename "$path"
    local width="${#MIGRATION_MINOR}"
    local new_minor
    new_minor="$(format_minor_version "$next_minor" "$width")"
    local target
    target="$(dirname "$path")/V${BASE_MAJOR}_${new_minor}__${MIGRATION_DESCRIPTION}"

    if [[ "$target" != "$path" && ( -e "$target" || -L "$target" ) ]] && ! array_contains "$target" "${added_files[@]}"; then
      echo "Refusing to overwrite existing migration file: $target" >&2
      exit 1
    fi

    if [[ "$target" != "$path" ]]; then
      local temporary
      temporary="$(temporary_rename_path "$path")"
      [[ ! -e "$temporary" && ! -L "$temporary" ]] || {
        echo "Temporary migration path already exists: $temporary" >&2
        exit 1
      }
      sources+=("$path")
      targets+=("$target")
      temporaries+=("$temporary")
    fi

    next_minor="$((next_minor + 1))"
  done

  local index
  for index in "${!sources[@]}"; do
    git mv "${sources[$index]}" "${temporaries[$index]}"
  done

  for index in "${!sources[@]}"; do
    mkdir -p "$(dirname "${targets[$index]}")"
    git mv "${temporaries[$index]}" "${targets[$index]}"
    if [[ -n "$SUMMARY_FILE" ]]; then
      printf -- '- `%s` -> `%s`\n' "${sources[$index]}" "${targets[$index]}" >> "$SUMMARY_FILE"
    fi
    echo "Renamed ${sources[$index]} -> ${targets[$index]}"
  done

  check_for_duplicate_versions
}

case "$MODE" in
  check-only)
    check_for_duplicate_versions
    ;;
  auto-update)
    auto_update_added_migrations
    ;;
esac