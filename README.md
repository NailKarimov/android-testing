# android-testing

[![Tests](https://github.com/NailKarimov/android-testing/actions/workflows/tests.yml/badge.svg)](https://github.com/NailKarimov/android-testing/actions/workflows/tests.yml)
[![Allure report](https://img.shields.io/badge/report-Allure-orange)](https://nailkarimov.github.io/android-testing/)

A small Android app of our own in **Kotlin** (split the bill) with three levels of tests, all in GitHub Actions
on an API 30 emulator:

| Level | Tool | What it proves |
| --- | --- | --- |
| Unit (JVM) | JUnit 4, parameterized | the money rules in cents: half-up tip, shares rounded up so the bill is always covered, invariants over 1,750 combinations, a boundary table for every input |
| Instrumented UI | **Espresso** | the screen inside the app: results, errors that clear the old result, the Done key, steppers clamped to 1…50, the result survives `recreate()` |
| Black-box UI | **Appium 3** + UiAutomator2 (Python) | the installed APK from outside: the same flows, steppers found by their accessible names, visible labels, rotation |

33 tests. The first CI run found that in landscape the result is below the fold. UiAutomator does not see it,
and the user has to scroll. **Mutation check:** 7 planted bugs in the money rules, all caught by the JVM tests.
Details: [`docs/android.md`](docs/android.md) · visual guide:
[`docs/android.html`](https://nailkarimov.github.io/android-testing/android.html). Reports on Pages:
[Allure](https://nailkarimov.github.io/android-testing/),
[Espresso](https://nailkarimov.github.io/android-testing/espresso/),
[Appium screenshots](https://nailkarimov.github.io/android-testing/appium/),
[unit](https://nailkarimov.github.io/android-testing/unit/),
[lint](https://nailkarimov.github.io/android-testing/lint/).

Part of a QA portfolio: [github.com/NailKarimov](https://github.com/NailKarimov).
