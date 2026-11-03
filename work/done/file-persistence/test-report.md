---
task: file-persistence
artifact: test-report
round: 1
tested_commit: ff51461
verdict: pass
---

# Test report: file-persistence, round 1

## Environment

- OpenJDK 17.0.20.1, Maven 3.10.0, macOS (Darwin 25.5.0), local CSV files in a temporary `data/` directory (deleted afterwards)

## Automated tests

- `mvn clean install -DskipTests` then `mvn test`: 18 passed, 0 failed, 0 skipped
- No code changed since the reviewed commit `441400a` (only `work/` files)

| Plan test | Exists | Result |
| --- | --- | --- |
| T1, T9 (`AccountTest`) | yes | pass |
| T2 `saveWritesAccountsFile` | yes | pass |
| T3 `roundTrip` | yes | pass |
| T4 `emptyDirectory` | yes | pass |
| T5 `specialCharacters` | yes | pass |
| T6 `historyKeepsLastTen` | yes | pass |
| T7 `invalidAmountReportsLocation` | yes | pass |
| T8 `unknownAccountFails` | yes | pass |
| existing tests | yes | pass |

## Acceptance criteria

| Requirement | Verified by | Result |
| --- | --- | --- |
| R1 | T1, T3, T9 | pass |
| R2 | T2, E1 | pass |
| R3 | T3, E1 | pass |
| R4 | T4, E7 | pass |
| R5 | T5, E1 (quoted owner name loads) | pass |
| R6 | T6 | pass |
| R7 | T7, T8, E2-E6 | pass |
| R8 | E1, E7 | pass |

## Exploratory checks

| ID | Action | Expected | Actual | Result |
| --- | --- | --- | --- | --- |
| E1 | Hand-written `data/` (2 accounts incl. quoted name, 3 transactions), `mvn -q compile exec:java` | `Loaded 2 accounts and 3 transactions from data/` | as expected | pass |
| E2 | Amount `abc` on `transactions.csv` line 3 | error naming file and line 3 | `transactions.csv line 3: ...` | pass |
| E3 | Wrong header | error on line 1 | `transactions.csv line 1: unexpected header` | pass |
| E4 | Transaction for unknown account | error with line | `line 4: unknown account: zz` | pass |
| E5 | Unknown transaction type / extra field | error with line | `line 3: No enum constant ...BOGUS` / `expected 6 fields, found 7` | pass |
| E6 | Negative balance in `accounts.csv` | error naming file and line | `accounts.csv line 3: initial balance must be zero or positive` | pass |
| E7 | No `data/` directory | `Loaded 0 accounts and 0 transactions` | as expected | pass |

## Failures

| ID | Description | Steps to reproduce | Severity |
| --- | --- | --- | --- |
| None | | | |

## Known issues to record

| Description | Severity |
| --- | --- |
| Review F1: unterminated quote at EOF ends the record silently; a truncated file can load a wrong value | minor |
| Review F2: restore constructor reports "initial balance" wording for a stored balance | nit |
| TD-file-persistence-2: duplicate ids not rejected on load; lone `\r` becomes `\n` | minor |
| Error text for a bad number is the raw `BigDecimal` message (not user friendly) | nit |

## Decision

pass: continue with task-gitops.
