"""Mutation check for the money rules: plants one bug at a time in BillSplitter.kt and runs the JVM tests.

The screen is covered by Espresso and Appium on the emulator; these mutants are about the rules, where the JVM
tests answer in seconds.

python3 scripts/mutants.py           # all
python3 scripts/mutants.py M2 M6     # some
"""

import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILE = ROOT / "app/src/main/java/dev/nailkarimov/splitbill/BillSplitter.kt"
MUTANTS = [
    ("M1", "(billCents * tipPercent + 50) / 100", "(billCents * tipPercent) / 100", "tip rounded down, not half up"),
    ("M2", "(total + people - 1) / people", "total / people", "each share rounded down: the bill is not covered"),
    ("M3", "tip > MAX_TIP_PERCENT", "tip > MAX_TIP_PERCENT + 1", "a 31 % tip accepted"),
    ("M4", "bill < 1 ||", "bill < 0 ||", "a zero bill accepted"),
    ("M5", "people > MAX_PEOPLE", "people > MAX_PEOPLE + 1", "51 people accepted"),
    ("M6", r"(?:\.(\d{1,2}))?", r"(?:\.(\d{1,3}))?", "three decimals accepted"),
    ("M7", ".replace(',', '.')", "", "a decimal comma rejected"),
]


def main(selected: set[str]) -> int:
    original = FILE.read_text()
    survived = []
    for mid, old, new, what in MUTANTS:
        if selected and mid not in selected:
            continue
        if original.count(old) != 1:
            raise SystemExit(f"{mid}: pattern must occur exactly once")
        FILE.write_text(original.replace(old, new))
        try:
            r = subprocess.run(["gradle", "--no-daemon", "-q", "testDebugUnitTest"], cwd=ROOT,
                               capture_output=True, text=True, check=False)
        finally:
            FILE.write_text(original)
        caught = r.returncode != 0
        out = r.stdout + r.stderr
        failed = sorted(set(re.findall(r"^(\w+) > (.+?) FAILED", out, re.M)))[:2]
        names = ", ".join(f"{c}: {t}" for c, t in failed)
        print(f"| {mid} | {what} | {'caught' if caught else '**SURVIVED**'} | {names} |", flush=True)
        if not caught:
            survived.append(mid)
    print(f"\n{len(survived)} survived: {' '.join(survived)}" if survived else "\nAll mutants caught.")
    return 1 if survived else 0


if __name__ == "__main__":
    print("| # | Bug | Result | Failing tests |\n| --- | --- | --- | --- |")
    sys.exit(main(set(sys.argv[1:])))
