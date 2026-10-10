#!/usr/bin/env bash
set -euo pipefail
task_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$task_root"
if [[ -z "${JAVA_HOME:-}" && "$(uname -s)" == "Darwin" ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
fi
task_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$task_sdk" && -d "$HOME/Library/Android/sdk" ]]; then
  task_sdk="$HOME/Library/Android/sdk"
fi
if [[ -n "$task_sdk" && ! -f local.properties ]]; then
  printf 'sdk.dir=%s\n' "$task_sdk" > local.properties
fi
exec ./gradlew :app:assembleDebug :app:lintDebug "$@"
