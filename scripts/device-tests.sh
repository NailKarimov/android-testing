#!/usr/bin/env bash
# Runs on the emulator inside reactivecircus/android-emulator-runner: Espresso first, then Appium.
set -uo pipefail
mkdir -p out/diag appium-results
status=0

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

echo "::group::Espresso"
gradle --no-daemon connectedDebugAndroidTest 2>&1 | tee out/diag/espresso.log
[ "${PIPESTATUS[0]}" -eq 0 ] || status=1
echo "::endgroup::"

echo "::group::Appium"
appium --log out/diag/appium-server.log --log-level warn &
APPIUM_PID=$!
for i in $(seq 1 60); do curl -sf http://127.0.0.1:4723/status >/dev/null && break; sleep 1; done
python3 -m pytest appium -v --junitxml=appium-results/junit.xml 2>&1 | tee out/diag/appium.log
[ "${PIPESTATUS[0]}" -eq 0 ] || status=1
kill "$APPIUM_PID" || true
echo "::endgroup::"

adb logcat -d > out/diag/logcat.txt || true
exit $status
