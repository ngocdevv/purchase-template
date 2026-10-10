#!/bin/zsh
set -euo pipefail
project_root="${0:A:h:h}"
destination="${1:-generic/platform=iOS Simulator}"
xcodebuild \
  -project "$project_root/PurchaseTemplate.xcodeproj" \
  -scheme PurchaseTemplate \
  -configuration Debug \
  -destination "$destination" \
  -derivedDataPath "$project_root/build" \
  CODE_SIGNING_ALLOWED=NO \
  build
