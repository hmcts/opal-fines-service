#!/usr/bin/env bash
# Keep JDBC assertions on the Jenkins agent connected to the PR database.
set -euo pipefail
state_dir="${WORKSPACE:-$PWD}/.functional-test-db-tunnel"

case "${1:-}" in
  supervise)
    namespace="${2:?namespace required}"
    pod="${3:?database pod required}"
    local_port=''
    child_pid=''
    cleanup() {
      if [[ -n "$child_pid" ]]; then
        kill "$child_pid" 2>/dev/null || true
        wait "$child_pid" 2>/dev/null || true
      fi
    }
    trap cleanup EXIT
    trap 'exit 0' TERM INT
    while true; do
      echo "$(date -u +%FT%TZ) Starting database tunnel (${local_port:-automatic}:5432)" >> "$state_dir/log"
      kubectl --namespace "$namespace" port-forward --address 127.0.0.1 \
        "pod/$pod" "${local_port}:5432" > "$state_dir/attempt.log" 2>&1 &
      child_pid=$!
      listening=false
      for ((attempt = 0; attempt < 60; attempt++)); do
        port=$(sed -n 's/^Forwarding from 127\.0\.0\.1:\([0-9]*\) -> 5432$/\1/p' \
          "$state_dir/attempt.log" | head -1)
        if [[ -n "$port" ]] && kill -0 "$child_pid" 2>/dev/null; then
          local_port="$port"
          printf '%s\n' "$local_port" > "$state_dir/port.tmp"
          mv "$state_dir/port.tmp" "$state_dir/port"
          echo "$(date -u +%FT%TZ) Database tunnel listening on 127.0.0.1:$local_port" >> "$state_dir/log"
          listening=true
          break
        fi
        if ! kill -0 "$child_pid" 2>/dev/null; then
          break
        fi
        sleep 1
      done
      if [[ "$listening" != true ]]; then
        kill "$child_pid" 2>/dev/null || true
      fi
      status=0
      wait "$child_pid" || status=$?
      child_pid=''
      cat "$state_dir/attempt.log" >> "$state_dir/log"
      echo "$(date -u +%FT%TZ) Database tunnel exited with status $status; restarting on the same port" \
        >> "$state_dir/log"
      # Record pod readiness/restarts without dumping environment variables or credentials.
      kubectl --namespace "$namespace" get "pod/$pod" --request-timeout=10s \
        -o 'custom-columns=POD:.metadata.name,PHASE:.status.phase,RESTARTS:.status.containerStatuses[*].restartCount' \
        >> "$state_dir/log" 2>&1 || true
      sleep 1
    done
    ;;
  start)
    namespace="${2:?namespace required}"
    pod="${3:?database pod required}"
    mkdir -p "$state_dir"
    if [[ -f "$state_dir/pid" ]]; then
      echo "Database tunnel state already exists; refusing to start a second tunnel" >&2
      exit 1
    fi
    kubectl --namespace "$namespace" wait --for=condition=Ready "pod/$pod" --timeout=120s >&2
    rm -f "$state_dir/port" "$state_dir/port.tmp"
    : > "$state_dir/log"
    # Survive Jenkins sh-step cleanup; bound the supervisor and all reconnect attempts.
    JENKINS_NODE_COOKIE="${BUILD_TAG:-functional}-db-tunnel" \
      nohup timeout --kill-after=10s 45m bash "$0" supervise "$namespace" "$pod" \
      > "$state_dir/supervisor.log" 2>&1 < /dev/null &
    tunnel_pid=$!
    echo "$tunnel_pid" > "$state_dir/pid"
    ready=false
    trap 'if [[ "$ready" != true ]]; then kill "$tunnel_pid" 2>/dev/null || true; rm -f "$state_dir/pid"; fi' EXIT
    for ((attempt = 0; attempt < 60; attempt++)); do
      if [[ -s "$state_dir/port" ]] && kill -0 "$tunnel_pid" 2>/dev/null; then
        ready=true
        cat "$state_dir/port"
        exit 0
      fi
      if ! kill -0 "$tunnel_pid" 2>/dev/null; then
        cat "$state_dir/log" "$state_dir/supervisor.log" >&2
        exit 1
      fi
      sleep 1
    done
    cat "$state_dir/log" "$state_dir/supervisor.log" >&2
    echo "Timed out waiting for the functional-test database tunnel" >&2
    exit 1
    ;;
  stop)
    if [[ -f "$state_dir/pid" ]]; then
      read -r tunnel_pid < "$state_dir/pid"
      kill "$tunnel_pid" 2>/dev/null || true
      rm -f "$state_dir/pid"
    fi
    ;;
  *) echo "Usage: $0 start <namespace> <database-pod> | stop" >&2; exit 2 ;;
esac
