---
task: cli-menu
artifact: review
round: 1
plan_version: 1
reviewed_commit: 5940433
verdict: approved
---

# Code review: cli-menu, round 1

## Reviewed

- `git diff main...5940433`: 5 source/test files (+472 / -5 lines, ConsoleMenu 246 incl. blank lines)
- build: `mvn test` compiles, BUILD SUCCESS; test: 40 passed, 0 failed; lint: none (AGENTS.md)

## Increments

| Increment | Commit | Plan step or finding | Notes |
| --- | --- | --- | --- |
| 1 | `110bfed` | 1. TransactionService.createAccount | conforms; order is constructor validation, duplicate check, save |
| 2 | `307b730` | 2-4. ConsoleMenu, Main, menu tests | conforms; deviations none, decisions justified (EOF in a prompt = empty answer; `mvn compile exec:java`) |

## Findings

| ID | Severity | Location | Finding | Rule | Required change |
| --- | --- | --- | --- | --- | --- |
| F1 | minor | `ConsoleMenuTest.java:73` | `endOfInputExits` only asserts that some output exists; it passes even if the loop printed the menu twice, but a hang is the real risk and is covered by the test returning. Acceptable, assertion is weak. | TEST-4 | Optional: assert the menu appears exactly once. |
| F2 | nit | `ConsoleMenuTest.java:116-123` | `headerLines()` helper guesses the CSV header at runtime; a fixed expectation is simpler. | | Optional. |
| F3 | minor | `ConsoleMenu.java:60` | `UncheckedIOException` from `readLine` is not caught, so a broken stdin ends the program with a stack trace. Rare, noted in dev-log. | | Optional: accepted as is. |

## Previous findings

Round 1: none.

## Tech debt

- Every placeholder has a `TODO(<id>)` and a row in `tech-debt.md`: yes (no placeholders; TD-app-foundation-1 and its TODO are removed from `Main`)
- Accepted as debt in this review: none (F1-F3 are optional, not recorded)

## Decision

approved: no blocker or major finding. Requirements R1-R9 map to T1-T11, no ids are printed, errors are handled in one place, no leftovers. Continue with task-testing.
