---
task: file-persistence
artifact: dev-log
plan_version: 1
round: 1
status: done
---

# Development log: file-persistence

## Increment 1: plan step 1. Restore constructors in the model

- Commit: `0d45e7a` feat(model): add restore constructors for stored entities
- Changes: `BankEntity`, `Account`, `Transaction`, `DepositTransaction`, `WithdrawalTransaction`, `TransferTransaction`: restore constructors, validation moved to private static methods; `AccountTest`: T1, T9
- Decisions: none
- Tests: T1 restore keeps id and createdAt; T9 blank id rejected
- Review focus: normal constructors keep identical checks
- Deviation from the plan: none

## Increment 2: plan steps 2-4. Storage package

- Commit: `8509dad` feat(storage): add CSV storage for accounts and transactions
- Changes: `StorageException`, `CsvCodec`, `CsvBankStorage` created
- Decisions: shared `readFile` with a private `RecordHandler` interface for both files; empty (0 byte) file is treated like a missing file; wrong header fails on line 1; `TD-file-persistence-1` marked in `save`
- Tests: see increment 4
- Review focus: `CsvCodec.readRecord` quote handling; error wrapping; account resolution before history update
- Deviation from the plan: `readFile`/`RecordHandler` replace two separate reader methods, to keep the class short

## Increment 3: plan step 5. Main

- Commit: `bdf0a2b` feat(app): load stored data at startup
- Changes: `Main` loads `data/` and prints counts; error goes to stderr
- Decisions: none
- Tests: manual R8 check (see Notes)
- Review focus: `TODO(TD-app-foundation-1)` kept
- Deviation from the plan: none

## Increment 4: plan step 6. Tests

- Commit: `75bfcd5` test(storage): cover CSV round trip and invalid data
- Changes: `CsvBankStorageTest` with T2-T8
- Decisions: `@TempDir`; T7 edits the saved file
- Tests: T2 header and six fields; T3 full round trip; T4 empty directory; T5 special characters; T6 last 10 history; T7 file and line in the error; T8 unknown account
- Review focus: T5 uses `\n` only
- Deviation from the plan: none

## Commands

| Command | Result |
| --- | --- |
| test: `mvn test` | 18 passed, 0 failed |
| lint: none | skipped |

## Notes for testing

- `mvn -q compile exec:java` without `data/` prints `Loaded 0 accounts and 0 transactions from data/` (verified).
- For the 2/3 case, save a repository with a small scratch snippet or write the two CSV files by hand into `data/`.
