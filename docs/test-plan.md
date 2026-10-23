# Test plan: Split the bill (android-testing)

The structure follows ISO/IEC/IEEE 29119-3 (test plan), shortened for one project.

## 1. Context

- **System under test:** "Split the bill", an Android app of our own written in Kotlin with Views. The money
  rules are in `BillSplitter`, and the screen is `MainActivity`.
- **Goal:** show that the bill is always covered and fairly split, and that the screen works for a user and for
  accessibility services. Show a working test pyramid on a real emulator in CI.
- **References:** ISO/IEC 25010, Android accessibility guidelines (labels, content descriptions, live regions),
  W3C WebDriver (Appium).

## 2. Scope

| In scope | Out of scope |
| --- | --- |
| the money rules: rounding, shares, limits, parsing | currencies other than one with cents |
| the screen: input, errors, steppers, the Done key | iOS (a separate platform) |
| configuration changes: recreate, rotation | real devices and many API levels; noted in the backlog (BrowserStack) |
| accessible names of controls | a full TalkBack audit |

## 3. Quality characteristics (ISO/IEC 25010)

| Characteristic | Covered by |
| --- | --- |
| Functional correctness | JUnit examples, invariants and boundary table |
| Usability (operability, accessibility) | Espresso and Appium: labels, accessible names of the steppers, visible result |
| Reliability (recoverability) | the result survives `recreate()` and rotation |
| Portability | an emulator in CI (API 30); lint |

## 4. Approach

- **Levels:**
  - unit tests on the JVM, in milliseconds;
  - Espresso inside the app process, which synchronises with the UI thread;
  - Appium outside, against the installed APK.
- **Techniques:** boundary values (0.01 and 100 000.00, 0 to 30 %, 1 to 50 people), invariants over many
  combinations, equivalence classes for input formats.
- **Test quality:** `scripts/mutants.py` plants 7 bugs in the money rules and runs the JVM tests for each.

## 5. Risks

| Risk | Mitigation |
| --- | --- |
| Emulators are slow and flaky in CI | KVM, animations off, one API level, no snapshot |
| Appium sees only what is on screen | scroll to an element with `UiScrollable` |
| Floating-point money | whole cents (`Long`) everywhere |

## 6. Entry and exit criteria

- **Entry:** the build passes with Kotlin warnings as errors, and lint has no errors.
- **Exit:** all tests pass on the emulator, and every planted bug is caught.

## 7. Deliverables

The test suite, the reports on GitHub Pages, [`test-report.md`](test-report.md), [`android.md`](android.md) and
its visual guide.
