---
task: transaction-operations
artifact: test-report
round: 1
tested_commit: 7328a34
verdict: pass
---

# Test report: transaction-operations, round 1

## Environment

- OpenJDK 17.0.20.1, Maven 3.10.0, macOS (Darwin 25.5.0), CSV files in a temporary directory (deleted afterwards); no external services
- No code changed since the reviewed commit `57b6858` (`git diff 57b6858 HEAD -- . ':!work'` is empty)

## Automated tests

- `mvn clean install -DskipTests` then `mvn test`: 28 passed, 0 failed, 0 skipped (BankRepositoryTest 3, CsvBankStorageTest 7, TransactionTest 2, AccountTest 8, TransactionServiceTest 8)

| Plan test | Exists | Result |
| --- | --- | --- |
| T1 | yes | pass |
| T2 | yes | pass |
| T3 | yes | pass |
| T4 | yes | pass |
| T5 | yes | pass |
| T6 | yes | pass |
| T7 | yes | pass |
| T8 | yes | pass |
| T9 | yes | pass |
| T10 | yes | pass |
| T11 | yes (earlier 18 tests) | pass |

## Acceptance criteria

| Requirement | Verified by | Result |
| --- | --- | --- |
| R1 | T1 | pass |
| R2 | T3, E1 | pass |
| R3 | T4, T5, E2, E3 | pass |
| R4 | T6, E4 | pass |
| R5 | T7, E9, E10 | pass |
| R6 | T8, E6, E7 | pass |
| R7 | T9, E8, E8b | pass |
| R8 | T10, E11 | pass |
| R9 | T2 | pass |

## Exploratory checks

The service is not wired into `Main` (`cli-menu` follows), so a short Java script called the library with temporary data (accounts x = 100, y = 20).

| ID | Action | Expected | Actual | Result |
| --- | --- | --- | --- | --- |
| E1 | Withdraw 30 from x | balance 70, 1 transaction in repository, newest history entry | as expected | pass |
| E2 | Transfer 50 from y (20) to x | `InsufficientFundsException`, balances and repository unchanged | y=20, x=70, 1 transaction | pass |
| E3 | Transfer 25 from x to y | x=45, y=45 | as expected | pass |
| E4 | Deposit on unknown account id | `ValidationException` | thrown ("unknown account") | pass |
| E5 | Deposit of -5 and of null | `ValidationException` | thrown for both | pass |
| E6 | Update the transfer of 25 to 10 | x=60, y=30, same id, amount 10 | as expected | pass |
| E7 | Update the same transfer to 500 | `InsufficientFundsException`, nothing changes | y=30, amount 10 | pass |
| E8 | Delete and update of unknown id | `ValidationException` | thrown for both | pass |
| E9 | Deposit 40 and withdrawal 65 on y, delete the deposit | `InsufficientFundsException`, nothing changes | y=5 | pass |
| E10 | Delete the withdrawal, then the transfer | balances reversed, removed from history | x=70, y=60, not in history | pass |
| E11 | `storage.load()` after the operations | same balances and transaction count as memory | x=70, y=60, 2 transactions (both match) | pass |
| E12 | 13 deposits (history wraps), then delete the newest | history still holds 10 entries | 10 | pass |

## Failures

| ID | Description | Steps to reproduce | Severity |
| --- | --- | --- | --- |
| None | | | |

## Known issues to record

| Description | Severity |
| --- | --- |
| `rebuildHistory` has no unit test for more than 10 matching transactions (review F2); E12 covered it exploratorily and it works | minor |
| A failed `save` leaves memory ahead of disk (accepted in scope; `crash-safe-save` is the follow-up) | minor |

## Decision

pass: continue with task-gitops.
