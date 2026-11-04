---
task: crash-safe-save
artifact: dev-log
plan_version: 1
round: 1
status: done
---

# Development log: crash-safe-save

## Increment 1: plan step "1. Split writing from moving in save"

- Commit: `806684b` fix(storage): write csv files to temporary files before replacing them
- Changes: `CsvBankStorage.java`: `save` writes `*.csv.tmp` first, then moves both; new `writeAccounts`, `writeTransactions`, `move`, `deleteQuietly`; `open` and the TODO removed. `CsvBankStorageTest.java`: T1, T2.
- Decisions: this commit moves with `Files.move` directly (ATOMIC_MOVE, fallback REPLACE_EXISTING); the seam comes in increment 2.
- Tests: T1 second save replaces data, only the two CSV files remain; T2 blocked temp file makes save fail, both CSVs byte-identical, no `accounts.csv.tmp`.
- Review focus: `finally` cleanup; error messages still name the failing file.
- Deviation from the plan: none (step order split so each commit compiles and passes).

## Increment 2: plan step "2. Atomic move with fallback, behind a tiny seam"

- Commit: `46fae8b` refactor(storage): add FileMover seam and atomic move fallback test
- Changes: `CsvBankStorage.java`: package-private `FileMover`, constructor `(Path, FileMover)`, public constructor delegates with `Files::move`. `CsvBankStorageTest.java`: T3.
- Decisions: none beyond the plan.
- Tests: T3 a mover rejecting ATOMIC_MOVE; save succeeds twice and the data is correct.
- Review focus: seam stays package-private; null check on `directory` kept.
- Deviation from the plan: none.

## Commands

| Command | Result |
| --- | --- |
| test: `mvn test` | 43 passed, 0 failed |
| lint: none | skipped |

## Notes for testing

- Failure of the second move (pair gap) is accepted and not tested; see TD-crash-safe-save-1.
