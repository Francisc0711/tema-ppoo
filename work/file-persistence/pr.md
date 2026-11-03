---
task: file-persistence
artifact: pr
branch: file-persistence
base: main
updated_commit: none
pr: not created
---

# feat(storage): save and load accounts and transactions in CSV files

## Summary

The bank data survives a restart: `CsvBankStorage` writes all accounts and transactions of a `BankRepository` to `accounts.csv` and `transactions.csv` and reads them back into a new repository with the same ids, values and recent histories. `Main` loads `data/` at start and prints the counts.

## Changes

- Restore constructors for `Account` and the transaction classes keep a stored `id` and `createdAt`; normal constructors are unchanged.
- New `storage` package: `CsvBankStorage` (`save`, `load`), `CsvCodec` (quoting and parsing), `StorageException` (names file and line).
- Invalid data (field count, number, date, type, domain value, unknown account) fails the whole load.
- Account history is rebuilt from the loaded transactions (last 10).
- `Main` loads `data/` and prints the number of accounts and transactions.
- Tests: `AccountTest` and storage tests (round trip, special characters, history, invalid data).

## How to test

```bash
mvn clean install -DskipTests
mvn test
```

Manual: put valid `accounts.csv` and `transactions.csv` in `data/`, run `mvn exec:java`; it prints `Loaded 2 accounts and 3 transactions from data/`. Without `data/` it prints zero counts; an invalid line fails with file name and line number.

## Quality gates

| Gate | Task file | Result |
| --- | --- | --- |
| Plan critique | `critique.md`, round 1 | approved |
| Code review | `review.md`, round 1 | approved |
| Tests | `test-report.md`, round 1 | pass |

## Tech debt

- TD-file-persistence-1: files are written in place; a crash mid-save can truncate a file.
- TD-file-persistence-2: duplicate ids are not rejected on load; a lone `\r` in a field returns as `\n`.

## Follow-ups

- crash-safe-save: write to a temp file and move atomically.
