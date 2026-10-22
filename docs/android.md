# Testing on Android: unit, Espresso, Appium

The app is ours: **Split the bill**, written in Kotlin with Views (`app/src/main`). You enter the bill, the tip and
the number of people, and it shows what each person pays. Money is kept in whole cents (`Long`), never in floating
point.

- The tip is rounded half up to a cent.
- Everyone pays the same share, rounded **up** to a cent, so the bill is always covered. The screen shows the
  extra this collects, which is always less than a cent per person.
- Allowed inputs: a bill from 0.01 to 100 000.00, a tip from 0 to 30 %, from 1 to 50 people. A decimal comma
  works too.

The tests run at three levels, from fastest to most realistic:

| Level | Tool | Runs on | Where | Tests |
| --- | --- | --- | --- | ---: |
| Unit | JUnit 4, parameterized | the JVM, no device | `app/src/test` | 21 |
| Instrumented UI | Espresso | an API 30 emulator, inside the app process | `app/src/androidTest` | 7 |
| Black-box UI | Appium 3 + UiAutomator2, Python client | the same emulator, from outside, against the installed APK | `appium/` | 5 |

Everything runs in GitHub Actions (`.github/workflows/tests.yml`):
- the `unit` job runs JUnit and Android lint and builds both APKs;
- the `device` job starts an emulator with `reactivecircus/android-emulator-runner` (KVM enabled), runs Espresso,
  then starts the Appium server and runs pytest;
- the `mutants` job plants bugs in the money rules;
- `publish` puts the Allure report, the Espresso, unit and lint reports, an Appium screenshot of each test, logcat
  and the logs on GitHub Pages.

## 1. Unit tests: the rules, in milliseconds

The money rules are a plain Kotlin `object`, so they are tested on the JVM:
- worked examples: 100.00 split between 3 is 33.34 each, with 0.02 extra;
- a 12.5-cent tip rounds to 13 cents;
- invariants checked over 5 × 7 × 50 combinations: the extra is always between 0 and people − 1 cents, and
  `total + extra = share × people`;
- a boundary table for every input: one step inside passes, one step outside names the right field.

## 2. Espresso: inside the app

```kotlin
fill("84.50", "10", "3")
onView(withId(R.id.split)).perform(click())
onView(withId(R.id.result)).check(matches(withText(containsString("Each pays 30.99"))))
```

Espresso runs in the app's process and waits for the UI thread to go idle before every action and check, so
there are no sleeps. The tests cover:
- the worked example;
- an even split, which shows no "extra" line;
- the keyboard's Done key;
- a bad tip, which also clears the old result;
- an empty bill;
- the − / + steppers clamped to 1…50;
- **`scenario.recreate()`**, which is what a rotation or a dark-mode switch does: the result survives because
  the activity saves it in `onSaveInstanceState`.

## 3. Appium: outside the app

Appium knows only what the screen exposes to UiAutomator: resource ids, text and content descriptions. That is
also what accessibility services see. The tests find the steppers by their accessible names ("More people",
"Fewer people") and check that every input has a visible label.

**What the first CI run found.** The rotation test failed with `NoSuchElement`. In landscape the result is below
the fold of the `ScrollView`, and UiAutomator does not report views that are off screen. Espresso did not notice,
because `recreate()` keeps the orientation. The test now scrolls to the result with `UiScrollable`. It is also a
small usability finding: in landscape the user has to scroll to see the answer they just asked for.

## 4. Espresso or Appium?

| | Espresso | Appium |
| --- | --- | --- |
| Where it runs | inside the app process | outside the app, over the W3C WebDriver protocol |
| Synchronisation | automatic (idle UI thread) | explicit waits |
| Finds views by | ids, matchers on the view tree | what UiAutomator exposes: ids, text, accessibility |
| Language | Kotlin / Java, in the app's project | any client: Python here |
| Speed | fast | slower (server, driver, adb) |
| Best for | developers' UI tests in the same repo | black-box, cross-app or cross-platform suites |

## 5. Mutation check

`python3 scripts/mutants.py` plants each bug in `BillSplitter.kt` and runs the JVM tests. In CI it is a separate
job, and the result is on Pages (`mutants/result.md`).

| # | Bug |
| --- | --- |
| M1 | the tip is rounded down instead of half up |
| M2 | each share is rounded down, so the bill is not covered |
| M3 | a 31 % tip is accepted |
| M4 | a zero bill is accepted |
| M5 | 51 people are accepted |
| M6 | three decimals are accepted |
| M7 | a decimal comma is rejected |

## Running it

```bash
gradle testDebugUnitTest lintDebug                 # JVM, no device
gradle connectedDebugAndroidTest                   # Espresso; needs a running emulator or device
npm install -g appium@3.8.0 && appium driver install uiautomator2@8.7.0
appium &                                           # then, with the debug APK built:
pip install -r appium/requirements.txt && pytest appium -v
python3 scripts/mutants.py
```
