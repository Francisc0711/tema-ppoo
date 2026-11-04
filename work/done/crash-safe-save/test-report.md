---
task: crash-safe-save
artifact: test-report
round: 1
tested_commit: cc73291
verdict: pass
---

# Test report: crash-safe-save, round 1

## Environment

- OpenJDK 17.0.20.1, Maven 3.10.0, macOS (Darwin 25.5.0), local file system; temporary data directory outside the repository, deleted afterwards
- `git diff fb31fae HEAD -- . ':!work'` is empty (no code changed since the review)

## Automated tests

- `mvn clean install -DskipTests` then `mvn test`: 43 passed, 0 failed, 0 skipped

| Plan test | Exists | Result |
| --- | --- | --- |
| T1 | yes | pass |
| T2 | yes | pass |
| T3 | yes | pass |
| T4 (existing storage, service, menu tests) | yes | pass |

## Acceptance criteria

| Requirement | Verified by | Result |
| --- | --- | --- |
| R1 | T1, E1, E2 | pass |
| R2 | T2, E3 | pass |
| R3 | T3 | pass |
| R4 | T4, E1, E2 | pass |

## Exploratory checks

Run through `ebanking.Main` with piped menu input in a temporary working directory.

| ID | Action | Expected | Actual | Result |
| --- | --- | --- | --- | --- |
| E1 | Create two accounts, deposit 100, list | Both CSV files written, no `.tmp` left | Only `accounts.csv` and `transactions.csv`; balance 150 saved | pass |
| E2 | New run: withdraw 30, transfer 20, overdraw 9999 | Data loaded and updated, overdraw rejected, no `.tmp` left | Balances 100 / 20 saved, `insufficient funds` error, no `.tmp` | pass |
| E3 | Put a non-empty directory at `transactions.csv.tmp`, deposit 5 | `Storage error`, CSV files unchanged | `Storage error: cannot write transactions.csv`; both CSVs identical to the backup; no `accounts.csv.tmp` | pass |

## Failures

| ID | Description | Steps to reproduce | Severity |
| --- | --- | --- | --- |
| None | | | |

## Known issues to record

| Description | Severity |
| --- | --- |
| A crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync (TD-crash-safe-save-1, already recorded) | minor |
| After a failed save, memory stays ahead of disk (KI-transaction-operations-2): in E3 the deposit stays in memory of the running session | minor |
| A failed save leaves the blocked temp path in place (only in the artificial E3 setup); not caused by the task | trivial |

## Decision

pass: continue with task-gitops.
