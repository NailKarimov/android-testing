| # | Bug | Result | Failing tests |
| --- | --- | --- | --- |
| M1 | tip rounded down, not half up | caught | BillSplitterTest: tip is rounded half up to a cent |
| M2 | each share rounded down: the bill is not covered | caught | BillSplitterTest: everyone pays the same, rounded up, and the bill is always covered, BillSplitterTest: the extra is always less than a cent per person |
| M3 | a 31 % tip accepted | caught | BillSplitterBoundaryTest: validates[50 / 31% / 2 → tip] |
| M4 | a zero bill accepted | caught | BillSplitterBoundaryTest: validates[0 / 10% / 2 → bill] |
| M5 | 51 people accepted | caught | BillSplitterBoundaryTest: validates[50 / 10% / 51 → people] |
| M6 | three decimals accepted | caught | BillSplitterTest: amounts parse with a dot or a comma and at most two decimals |
| M7 | a decimal comma rejected | caught | BillSplitterTest: amounts parse with a dot or a comma and at most two decimals |

All mutants caught.
