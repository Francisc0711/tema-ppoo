---
task: crash-safe-save
artifact: pr
branch: crash-safe-save
base: main
updated_commit: none
pr: not created
---

# fix(storage): make CSV save crash-safe

## Summary

A crash or write error during `CsvBankStorage.save` can no longer truncate or corrupt an existing data file. Each file is either the old complete version or the new complete version.

## Changes

- `CsvBankStorage.save` writes each CSV to a `*.csv.tmp` file in the data directory, then moves it over the target (`ATOMIC_MOVE`, fallback `REPLACE_EXISTING`). Temporary files are removed on failure.
- Package-private `FileMover` seam so the fallback can be tested; the public API and the file format are unchanged.
- Tests in `CsvBankStorageTest`: second save replaces data and leaves no temp file, failed write keeps old files, atomic move fallback.

## How to test

```bash
mvn clean install -DskipTests
mvn test
```

Manual checks (see `test-report.md`): save through the menu leaves only `accounts.csv` and `transactions.csv`; a blocked `transactions.csv.tmp` makes save fail with `Storage error` and the CSV files stay unchanged.

## Quality gates

| Gate | Task file | Result |
| --- | --- | --- |
| Plan critique | `critique.md`, round 1 | approved |
| Code review | `review.md`, round 1 | approved |
| Tests | `test-report.md`, round 1 | pass (43 passed) |

## Tech debt

- TD-crash-safe-save-1: a crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync.
- TD-file-persistence-1: resolved by this task.

## Follow-ups

- atomic-pair-save: update `accounts.csv` and `transactions.csv` as one unit.
- save-failure-rollback: restore memory when a save fails (KI-transaction-operations-2).
