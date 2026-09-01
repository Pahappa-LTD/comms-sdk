#!/usr/bin/env bash
# Builds/packages every SDK — but only after run-all-tests.sh passes cleanly.
# Refuses to package anything if a single language's tests fail.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "Running the full test suite first (run-all-tests.sh)..."
if ! "$ROOT_DIR/run-all-tests.sh"; then
  echo
  echo "One or more languages failed their tests — aborting, nothing was built or packaged." >&2
  exit 1
fi

echo
echo "All tests passed. Building/packaging every SDK..."
echo

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

run_lang "Java"   "java"   mvn -q package
run_lang "Rust"   "rust"   cargo package --allow-dirty
run_lang "Kotlin" "kotlin" ./gradlew build --console=plain
run_lang "JS/TS"  "js"     bash -c "pnpm run build && npm pack"
run_lang "Python" "python" python -m build
run_lang "Ruby"   "ruby"   bash -c "rm -f *.gem && gem build comms_sdk.gemspec"

echo "=== PHP ==="
echo "  (skipped — Composer libraries need no build step)"
RESULTS["PHP"]="SKIP"
echo

run_lang "Dart"   "dart"   dart pub publish --dry-run
run_lang "C#"     "c#"     dotnet pack "src/CommsSdk.csproj" -c Release
run_lang "Go"     "go"     go build ./...

echo "================ Build/Package Summary ================"
overall=0
for name in Java Rust Kotlin JS/TS Python Ruby PHP Dart C# Go; do
  status="${RESULTS[$name]:-SKIP}"
  printf "  %-8s %s\n" "$name" "$status"
  if [ "$status" = "FAIL" ]; then
    overall=1
  fi
done
echo "========================================================="

exit $overall
