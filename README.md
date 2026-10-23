# android-testing

[![Tests](https://github.com/NailKarimov/android-testing/actions/workflows/tests.yml/badge.svg)](https://github.com/NailKarimov/android-testing/actions/workflows/tests.yml)
[![Allure report](https://img.shields.io/badge/report-Allure-orange)](https://nailkarimov.github.io/android-testing/)

Mobile tests **in Kotlin** on a small Android app of our own, "Split the bill". The tests run at three levels,
JVM unit tests, Espresso and Appium, and all of them run in GitHub Actions on an API 30 emulator.

| | |
| --- | --- |
| **System under test** | own app (`app/`): bill, tip and people in; what each person pays out, money in whole cents |
| **Language and tools** | Kotlin · JUnit 4 · Espresso · Appium 3 + UiAutomator2 (Python client) · Android lint · Gradle |
| **Types of testing** | unit, instrumented UI, black-box mobile UI, accessibility of controls, configuration changes |
| **Tests** | 33: 21 unit, 7 Espresso, 5 Appium |
| **Test quality** | 7/7 planted bugs in the money rules caught (`scripts/mutants.py`, a CI job) |
| **Reports** | [Allure](https://nailkarimov.github.io/android-testing/) · [Espresso](https://nailkarimov.github.io/android-testing/espresso/) · [Appium screenshots](https://nailkarimov.github.io/android-testing/appium/) · [unit](https://nailkarimov.github.io/android-testing/unit/) · [lint](https://nailkarimov.github.io/android-testing/lint/) · [visual guide](https://nailkarimov.github.io/android-testing/android.html) |

## What is tested

| Suite | Tool | What it proves |
| --- | --- | --- |
| `app/src/test/.../BillSplitterTest` | JUnit 4 | the money rules: half-up tip, shares rounded up so the bill is always covered, invariants over 1,750 combinations, parsing with a dot or a comma |
| `app/src/test/.../BillSplitterBoundaryTest` | JUnit 4 parameterized | one step inside and outside every input range names the right field |
| `app/src/androidTest/.../MainActivityTest` | Espresso | the screen inside the app: results, errors that clear the old result, the keyboard's Done key, steppers clamped to 1…50, the result survives `recreate()` |
| `appium/test_split_bill.py` | Appium 3 + UiAutomator2 | the installed APK from outside: the same flows, steppers found by their accessible names, visible labels, rotation |

## Findings

- **The answer is below the fold in landscape.** The first CI run failed the Appium rotation test with
  `NoSuchElement`, because UiAutomator does not report views that are off screen. Espresso did not notice, since
  `recreate()` keeps the orientation. The user has to scroll to see the result they just asked for. The test now
  scrolls with `UiScrollable`.
- **Content descriptions on text fields** would have replaced what TalkBack reads. They were removed, and Appium
  finds the fields by resource id.

## Running it

Requirements: JDK 17, Gradle 8.11, the Android SDK; for device tests an emulator or a device, Node.js 22 and
Python 3.13.

```bash
gradle testDebugUnitTest lintDebug                 # JVM, no device
gradle connectedDebugAndroidTest                   # Espresso on a running emulator
npm install -g appium@3.8.0 && appium driver install uiautomator2@8.7.0
appium &
gradle assembleDebug && pip install -r appium/requirements.txt && pytest appium -v
python3 scripts/mutants.py                         # mutation check
```

## Project structure

```text
app/src/main/                 the system under test: BillSplitter (rules), MainActivity, layout, strings
app/src/test/                 JUnit tests of the rules
app/src/androidTest/          Espresso tests
appium/                       Appium tests in Python, fixtures, requirements
scripts/device-tests.sh       what runs on the emulator: Espresso, then Appium
scripts/mutants.py            mutation check
docs/                         test plan, test report, guides
```

## CI

`.github/workflows/tests.yml` runs on push, pull request, every Monday at 07:00 UTC and on demand:
- the `unit` job runs JUnit and lint and builds both APKs;
- the `device` job starts an API 30 emulator with KVM, runs Espresso and then Appium;
- the `mutants` job plants bugs in the money rules;
- the `publish` job puts the Allure report, the Espresso, unit and lint reports, a screenshot of each Appium test,
  logcat and the logs on GitHub Pages.

## Documentation

| Document | What it is |
| --- | --- |
| [`docs/test-plan.md`](docs/test-plan.md) | test plan (ISO/IEC/IEEE 29119-3) |
| [`docs/test-report.md`](docs/test-report.md) | test summary report: latest results, findings, recommendations |
| [`docs/android.md`](docs/android.md) | guide: the three levels, Espresso vs Appium, the landscape finding |
| [`docs/android.html`](https://nailkarimov.github.io/android-testing/android.html) | the same guide, interactive: a working calculator and the test pyramid |

## Part of a portfolio

One of 13 test automation projects by [Nail Karimov](https://github.com/NailKarimov):
[all projects](https://github.com/NailKarimov) ·
[testing map](https://nailkarimov.github.io/api-test-automation/testing-map.html) ·
[toolbox](https://nailkarimov.github.io/api-test-automation/toolbox.html).
