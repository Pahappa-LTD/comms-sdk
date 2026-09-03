#!/usr/bin/env bash
# Runs every SDK's test suite in one go.
#
# Sandbox-only live tests (single/multiple send, balance, >1000-number
# rejection) opt in automatically if COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY
# are set (via a .env file at the repo root, or already exported). Without
# them, those tests skip cleanly and only the mocked + wrong-credentials
# tests run. See .env.example.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if [ -f "$ROOT_DIR/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
fi

if [ -z "${COMMS_SANDBOX_USERNAME:-}" ] || [ -z "${COMMS_SANDBOX_API_KEY:-}" ]; then
  echo "COMMS_SANDBOX_USERNAME/COMMS_SANDBOX_API_KEY not set — live-sandbox tests will skip. See .env.example."
fi
echo

if [ -d "$ROOT_DIR/go" ] && [ -z "$(ls -A "$ROOT_DIR/go" 2>/dev/null)" ]; then
  echo "Initializing go/ submodule..."
  git submodule update --init --recursive -- go
fi
if [ -d "$ROOT_DIR/php" ] && [ -z "$(ls -A "$ROOT_DIR/php" 2>/dev/null)" ]; then
  echo "Initializing php/ submodule..."
  git submodule update --init --recursive -- php
fi

declare -A RESULTS

run_lang() {
  local name="$1"
  local dir="$2"
  shift 2
  echo "=== $name ==="
  if [ ! -d "$ROOT_DIR/$dir" ]; then
    echo "  (skipping — $dir/ not found)"
    RESULTS["$name"]="SKIP"
    return
  fi
  (
    cd "$ROOT_DIR/$dir" || exit 1
    "$@"
  )
  local status=$?
  if [ $status -eq 0 ]; then
    RESULTS["$name"]="PASS"
  else
    RESULTS["$name"]="FAIL"
  fi
  echo
}

run_lang "Java"   "java"   mvn -q test
run_lang "Rust"   "rust"   cargo test
run_lang "Kotlin" "kotlin" ./gradlew test --console=plain
run_lang "JS/TS"  "js"     pnpm test
run_lang "Python" "python" python -m pytest
run_lang "Ruby"   "ruby"   bundle exec rspec
run_lang "PHP"    "php"    ./vendor/bin/phpunit
run_lang "Dart"   "dart"   dart test
run_lang "C#"     "c#"     dotnet test "CommsSdk.sln"
run_lang "Go"     "go"     go test -v ./...

echo "==================== Summary ===================="
overall=0
for name in Java Rust Kotlin JS/TS Python Ruby PHP Dart C# Go; do
  status="${RESULTS[$name]:-SKIP}"
  printf "  %-8s %s\n" "$name" "$status"
  if [ "$status" = "FAIL" ]; then
    overall=1
  fi
done
echo "==================================================="

exit $overall
