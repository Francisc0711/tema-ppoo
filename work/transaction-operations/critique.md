---
task: transaction-operations
artifact: critique
round: 1
plan_version: 1
verdict: approved   # approved | changes-requested
---

# Plan critique: transaction-operations, round 1

## Checklist

| Check | Result | Notes |
| --- | --- | --- |
| Every requirement in `scope.md` is covered | pass | R1–R9 map to steps 1–4 and tests T1–T10. |
| One logical increment; existing behavior keeps working | pass | Existing `AccountTest.historyDiscardsOldestWhenFull` uses `account.getId()`; `CsvBankStorage.load` adds histories only to involved accounts (lines 161–163), so the new `addToHistory` check does not break them. |
| Within the increment budget (files, lines, dependencies) | pass | 7 production files, about 200 lines, no dependency. Estimate agrees (about 150–220 lines). |
| Testable: essential tests mapped to requirements, commands given | pass | One to two tests per requirement, state checks only, `mvn test` and `mvn compile` given. |
| Concise: under about 200 lines, snippets only for critical parts | pass | About 130 lines; snippet only for the delta/atomic-apply logic. |
| Follows `docs/standards/` (rule IDs) | pass | OOP-1 (balance stays private, two mutators), OOP-2 (`withAmount` per subtype), OOP-3/4 respected, TEST-3/4/6/7. |
| Consistent with `docs/ARCHITECTURE.md` | pass | New `service` layer over repository and storage; repository stays free of I/O. |
| No over-engineering | pass | No interfaces or options beyond the requirements; delta map is the minimal way to get atomicity. |
| Placeholders recorded, with what removes them | pass | None needed; TD-app-foundation-F1 closes (checked in `docs/TECH_DEBT.md`). |
| Snippets correct and secure | pass | Funds check runs for all accounts before any mutation; `signum` handles zero deltas (update to same amount); `effect` default branch is only reached for TRANSFER; checked exceptions are declared. |
| Risks identified | pass | Failed save leaves memory ahead of disk (accepted in scope); transfer-update target check covered. |

## Required changes

| ID | Plan section | Problem | Required change |
| --- | --- | --- | --- |
| - | - | None | - |

## Suggestions

- T10 could also assert that the repository is unchanged after a failed operation; the file check already covers it.
- Consider also asserting in T8 that the `createdAt` of the stored transaction is unchanged (the plan lists it; make sure the test does).
- Consider `Account.rebuildHistory` clearing the array with `Arrays.fill` for brevity.

## Decision

Approved: development can start.
