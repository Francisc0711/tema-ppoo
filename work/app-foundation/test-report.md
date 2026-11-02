---
task: app-foundation
artifact: test-report
round: 1
tested_commit: 9d4a3aa
verdict: pass            # pass | fail
---

# Test report: app-foundation, round 1

## Environment

- `java`: OpenJDK 17.0.20.1 (Temurin); Maven 3.10.0 running on Homebrew OpenJDK 27.0.1, compiling with `maven.compiler.release` 17; macOS 26.5; no storage or external services (in-memory only)

## Automated tests

- `mvn clean install -DskipTests` then `mvn test`: 9 passed, 0 failed, 0 skipped (BUILD SUCCESS)

| Plan test | Exists | Result |
| --- | --- | --- |
| T1 | yes, `AccountTest.validAccountIsCreated` | pass |
| T2 | yes, `AccountTest.nullAccountNumberIsRejected` | pass |
| T3 | yes, `AccountTest.negativeBalanceIsRejected` | pass |
| T4 | yes, `AccountTest.historyDiscardsOldestWhenFull` | pass |
| T5 | yes, `TransactionTest.subtypesReportTheirType` | pass |
| T6 | yes, `TransactionTest.zeroAmountIsRejected` | pass |
| T7 | yes, `BankRepositoryTest.accountIsFoundById` | pass |
| T8 | yes, `BankRepositoryTest.transactionsAreFoundByAccount` | pass |
| T9 | yes, `BankRepositoryTest.duplicateAccountNumberIsRejected` | pass |

## Acceptance criteria

| Requirement | Verified by | Result |
| --- | --- | --- |
| R1 | clean install, `mvn test`, E1 | pass |
| R2 | T1, E5 | pass |
| R3 | T1, T3, E2, E3–E3e | pass |
| R4 | T5, T6, E6–E7b | pass |
| R5 | T5, E16 | pass |
| R6 | T4, E8–E10 | pass |
| R7 | T7, T8, T9, E12–E16 | pass |
| R8 | T2, T3, E17 | pass |
| R9 | `mvn test`: 9/9 | pass |

## Exploratory checks

Run from a temporary Java script against `target/classes` (no UI exists yet).

| ID | Action | Expected | Actual | Result |
| --- | --- | --- | --- | --- |
| E1 | `mvn exec:java` | prints placeholder line | `E-Banking app - console menu coming in cli-menu` | pass |
| E2 | `Account` with balance 0 | created, balance 0 | balance 0 | pass |
| E3 | `Account` with null / blank owner, blank account number, null type, null balance | `ValidationException` each | `ValidationException` each | pass |
| E4 | `setOwnerName("")` | rejected, old name kept | rejected, owner unchanged | pass |
| E5 | ids of two accounts; `Account` and `DepositTransaction` `instanceof BankEntity` | unique, true | unique, true, true | pass |
| E6 | deposit with -1, withdrawal with null amount | `ValidationException` | `ValidationException` | pass |
| E7 | transfer to self; transfer with null target | `ValidationException` | `ValidationException` | pass |
| E8 | `getRecentHistory()` on new account | empty array | length 0 | pass |
| E9 | 25 additions to history (amounts 1–25) | 10 entries, 16 … 25 | 10, first 16, last 25 | pass |
| E10 | modify returned history array | internal history unchanged | unchanged | pass |
| E11 | `addToHistory(null)` | `ValidationException` | `ValidationException` | pass |
| E12 | `addAccount(null)` | `ValidationException` | `ValidationException` | pass |
| E13 | `addTransaction(null)` | `ValidationException` | `ValidationException` | pass |
| E14 | `getAccounts().clear()` | repository not mutated | `UnsupportedOperationException` | pass |
| E15 | `findTransactionsByAccount("nope")`, `findAccountById(null)` | empty, empty | 0, `Optional.empty` | pass |
| E16 | `findTransactionsByType` after one transfer | TRANSFER 1, DEPOSIT 0 | 1, 0 | pass |
| E17 | exception kinds | `InsufficientFundsException` unchecked, `ValidationException` checked | as expected | pass |

## Failures

| ID | Description | Steps to reproduce | Severity |
| --- | --- | --- | --- |
| None | | | |

## Known issues to record

| Description | Severity |
| --- | --- |
| None | |

## Decision

Pass: continue with task-gitops.
