---
task: app-foundation
artifact: critique
round: 1
plan_version: 1
verdict: approved   # approved | changes-requested
---

# Plan critique: app-foundation, round 1

## Checklist

| Check | Result | Notes |
| --- | --- | --- |
| Every requirement in `scope.md` is covered | pass | R1 Step 1; R2 Step 3/T1; R3 Step 5/T1, T3; R4 Step 4/T5; R5 T5; R6 Step 5/T4; R7 Step 6/T7–T9; R8 Step 2/T2, T3; R9 T1–T9. R3/R8 conflict resolved by the recorded user decision (`ValidationException`) |
| One logical increment; existing behavior keeps working | pass | first code in the repository; no existing tests |
| Within the increment budget (files, lines, dependencies) | pass (note) | own estimate ~270–290 production lines (fits 300); 13 Java files, ~3 over "about 10". Accepted: the extra files are single-type declarations Java forces into separate files (2 enums, 2 exceptions, under 10 lines each) and the scope names them; splitting would leave R4/R5/R8 half done. No runtime dependency |
| Testable: essential tests mapped to requirements, commands given | pass | 9 tests, one success path per requirement plus data-protecting errors (T2, T3, T6, T9); commands given |
| Concise: under about 200 lines, snippets only for critical parts | pass | ~170 lines; snippets only for constructor validation and the circular buffer |
| Follows `docs/standards/` (rule IDs) | pass | OOP-1 (private fields, defensive copies, no `setBalance`), OOP-2 (`BankEntity`, `Transaction`, polymorphic `involves`), OOP-4 (`ArrayList`), GIT-2/GIT-3 commits |
| Consistent with `docs/ARCHITECTURE.md` | pass (note) | ARCHITECTURE still describes the Node.js stack; the plan correctly defers the rewrite to task-doc-updates |
| No over-engineering | pass | `findTransactionsByType` is justified by R4; `setOwnerName`, `getAccounts`, `getTransactions` are small and expected by later increments |
| Placeholders recorded, with what removes them | pass | `TD-app-foundation-1` (`Main`) → cli-menu |
| Snippets correct and secure | pass | buffer index arithmetic verified: when full, `end == historyStart`, oldest overwritten, start advances; copy returned oldest first. Validation runs before field assignment; transactions are immutable |
| Risks identified | pass (note) | the Maven risk is stale: `mvn` is installed (`/opt/homebrew/bin/mvn`), JDK 17.0.20 present |

## Required changes

None.

## Suggestions

- T5: filter through `BankRepository.findTransactionsByType` instead of in the test, so the public method added for R4 has a test.
- `scope.md` R8 still says a negative balance throws `InsufficientFundsException`. task-testing checks the scope's acceptance criteria, so it should read the plan's recorded decision; amending the scope (task-scoping) would avoid a false `fail`.
- `docs/standards/testing.md` is deleted in the working tree (uncommitted, not by this task). If the deletion is committed, the plan's TEST-3/4/6/7 citations dangle; the same rules are covered by `AGENTS.md` ("Increment budget"). Do not stage that deletion in task commits.
- `Main` prints an en dash; a plain `-` avoids garbled output on non-UTF-8 consoles.

## Decision

Approved: development can start.
