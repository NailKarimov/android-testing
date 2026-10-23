# Test summary report: Split the bill (android-testing)

The structure follows ISO/IEC/IEEE 29119-3 (test completion report), shortened.

- **Period:** 22.10.2026 – 23.10.2026
- **Build:** `main`, latest run in GitHub Actions on an API 30 emulator
- **Allure report:** https://nailkarimov.github.io/android-testing/

## 1. Summary

All 33 tests pass at every level. The money rules always cover the bill, and the screen works for a user and for
accessibility services. One usability issue remains open: in landscape, the result is below the fold.

## 2. Results

| Metric | Value |
| --- | --- |
| Tests run | 33: 21 JUnit, 7 Espresso, 5 Appium |
| Passed / failed / skipped | 33 / 0 / 0 |
| Device job duration | ≈ 5 min, including the emulator start |
| Mutation check | 7 of 7 planted bugs caught |

## 3. Defects

| ID | Severity | Summary | Status |
| --- | --- | --- | --- |
| AN-1 | minor (usability) | in landscape the result is below the fold; the user has to scroll after pressing Split | open; the test scrolls to it |
| AN-2 | minor (accessibility) | content descriptions on editable fields would replace what TalkBack reads | fixed before the first release |

## 4. Deviations from the plan

- One API level (30) only. Real devices are in the backlog.

## 5. Recommendations

1. Fix AN-1: in landscape, scroll the result into view after Split, or use a two-column layout.
2. Add a second API level (for example 34) to the device job when the app grows.
