---
task: cli-menu
artifact: test-report
round: 1
tested_commit: f97ac12
verdict: pass
---

# Test report: cli-menu, round 1

## Environment

- OpenJDK 17.0.20.1, Maven 3.10.0, macOS (Darwin 25.5.0); CSV files in a temporary copy of the project (`data/` removed afterwards, repository `data/` untouched)

## Automated tests

- `mvn clean install -DskipTests` then `mvn test`: 40 passed, 0 failed, 0 skipped (ConsoleMenuTest 11, TransactionServiceTest 9, AccountTest 8, CsvBankStorageTest 7, BankRepositoryTest 3, TransactionTest 2)
- No code changed since the reviewed commit `5940433` (`git diff 5940433 HEAD -- . ':!work'` is empty)

| Plan test | Exists | Result |
| --- | --- | --- |
| T1-T10 (`ConsoleMenuTest`) | yes | pass |
| T11 (`TransactionServiceTest`) | yes | pass |

## Acceptance criteria

| Requirement | Verified by | Result |
| --- | --- | --- |
| R1 | T1, E1 | pass |
| R2 | T2, E1 | pass |
| R3 | T3, T11, E2 | pass |
| R4 | T4, E2 | pass |
| R5 | T5, E2 | pass |
| R6 | T6, E2 | pass |
| R7 | T7, E2 | pass |
| R8 | T8, E2 | pass |
| R9 | T9, T10, E3, E4 | pass |

## Exploratory checks

Run with `mvn exec:java` and piped input in a temporary copy.

| ID | Action | Expected | Actual | Result |
| --- | --- | --- | --- | --- |
| E1 | No `data/`; choose 2, then 0 | menu, "No accounts.", exit | as expected | pass |
| E2 | Create RO01 (100) and RO02 (20); list; deposit 50; withdraw 500; withdraw 30; transfer 10; list transactions; update tx 1 to 60; delete tx 2; history; list | balances 150 / 30 at the end, CSVs match, no UUIDs printed, numbered lists, history newest first | as expected; `accounts.csv` balance 150 / 30, `transactions.csv` holds the remaining deposit 60 and the transfer | pass |
| E3 | Unknown account, non-numeric amount, negative amount, bad type, duplicate number, unknown transaction number (99 and "zz"), bad menu choice | one-line message, back to the menu | `Error: unknown account`, `not a number`, `amount must be positive`, `unknown account type`, `account number already exists`, `unknown transaction number`, `Unknown choice`; process kept running | pass |
| E4 | Corrupt `accounts.csv` at start | message, exit, file not overwritten | `Cannot load data: accounts.csv line 1: unexpected header`; file unchanged | pass |
| E5 | End of input in the main loop | exit | not run from the CLI (no `timeout` command); covered by T1 / `endOfInputExits` | not run (covered by test) |

## Failures

| ID | Description | Steps to reproduce | Severity |
| --- | --- | --- | --- |
| None | | | |

## Known issues to record

| Description | Severity |
| --- | --- |
| A broken stdin (`UncheckedIOException`) ends the program with a stack trace (review F3) | minor |
| Transaction listing prints the raw ISO timestamp with microseconds | nit |

## Decision

pass: every test passes and every acceptance criterion is met. Continue with task-gitops.
