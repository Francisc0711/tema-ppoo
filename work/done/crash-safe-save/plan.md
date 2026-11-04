---
task: crash-safe-save
artifact: plan
version: 1
scope_version: 1
answers_critique_round: 0
---

# Plan: Crash-safe save

## Summary

`CsvBankStorage.save` writes both CSV files to temporary files in the data directory first, and only when both are complete moves them over the targets (`ATOMIC_MOVE`, fallback `REPLACE_EXISTING`). A write error leaves both targets untouched and deletes the temporary files. File format, `load` and the public API stay unchanged.

## Context

- `src/main/java/ebanking/storage/CsvBankStorage.java`: `save` opens each target with `Files.newBufferedWriter`, which truncates it at once (TD-file-persistence-1, carries a `TODO`).
- `docs/ARCHITECTURE.md` D16: a failed save leaves memory ahead of disk; stays as is (out of scope).
- `docs/standards/testing.md` TEST-4, TEST-7: a test per requirement, no complex mocks, check state directly.
- `CsvBankStorageTest` uses `@TempDir`; `TransactionServiceTest` and `ConsoleMenuTest` save through this class.

## Files

| # | File | Action | Purpose |
| --- | --- | --- | --- |
| 1 | `src/main/java/ebanking/storage/CsvBankStorage.java` | modify | temp-file write, then move; small `FileMover` seam |
| 2 | `src/test/java/ebanking/storage/CsvBankStorageTest.java` | modify | three new tests (T1 to T3) |

## Steps

### Step 1. Split writing from moving in `save`

Replace the two in-place `try` blocks with: create the directory (unchanged), write `accounts.csv.tmp` and `transactions.csv.tmp`, then move both. Extract two private methods, `writeAccounts(Path, BankRepository)` and `writeTransactions(Path, BankRepository)`, holding the existing loops unchanged. The temp names are fixed (`<file>.tmp`, constants `TEMP_SUFFIX`); an old leftover from a crash is simply overwritten by the next save. Writing both temporaries before moving either keeps both targets unchanged when any write fails.

Remove the `TODO(TD-file-persistence-1)` comment.

```java
public void save(BankRepository repository) throws StorageException {
    // ... createDirectories unchanged ...
    Path accountsTmp = directory.resolve(ACCOUNTS_FILE + TEMP_SUFFIX);
    Path transactionsTmp = directory.resolve(TRANSACTIONS_FILE + TEMP_SUFFIX);
    try {
        writeAccounts(accountsTmp, repository);
        writeTransactions(transactionsTmp, repository);
        move(accountsTmp, directory.resolve(ACCOUNTS_FILE));
        move(transactionsTmp, directory.resolve(TRANSACTIONS_FILE));
    } catch (IOException e) {
        throw new StorageException("cannot write " + directory, e);
    } finally {
        deleteQuietly(accountsTmp);       // Files.deleteIfExists, IOException ignored
        deleteQuietly(transactionsTmp);
    }
}
```

The error message keeps naming the failing file: `writeAccounts`/`writeTransactions` catch their own `IOException` and rethrow `StorageException("cannot write accounts.csv", e)` as today (so `save` also declares `StorageException` from them; only the move step uses the generic message).

### Step 2. Atomic move with fallback, behind a tiny seam

```java
/** Moves a file; replaceable in tests to simulate a file system without atomic move. */
interface FileMover {
    void move(Path source, Path target, CopyOption... options) throws IOException;
}

private void move(Path tmp, Path target) throws IOException {
    try {
        mover.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
        mover.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
```

- Field `private final FileMover mover`; the public constructor delegates with `Files::move`; a new package-private constructor `CsvBankStorage(Path, FileMover)` is used by the tests only. Both keep the null check on `directory`.
- Other `IOException`s from the move propagate to `save` and become `StorageException`. After the first move succeeded and the second fails, the second temporary file is removed by `finally`; the pair-consistency gap is accepted (scope, out of scope).

## Interfaces and data

- Public API unchanged. New package-private `FileMover` and constructor `CsvBankStorage(Path, FileMover)`.
- CSV format, columns and encoding unchanged. No new dependency, configuration or data change.
- Temporary files `accounts.csv.tmp`, `transactions.csv.tmp` exist only during `save`.

## Tests

| ID | File | Checks | Requirement | New / existing |
| --- | --- | --- | --- | --- |
| T1 | `CsvBankStorageTest` | save twice with changed data; the files hold the new data (load returns it) and the directory lists only the two CSV files | R1 | new |
| T2 | `CsvBankStorageTest` | after a first save, create a non-empty directory named `transactions.csv.tmp` so the write fails; `save` throws `StorageException`, both CSV files keep their old content byte for byte, and no `accounts.csv.tmp` remains | R2 | new |
| T3 | `CsvBankStorageTest` | with a `FileMover` that throws `AtomicMoveNotSupportedException` when `ATOMIC_MOVE` is passed and otherwise calls `Files.move`, `save` does not throw and the files hold the new data | R3 | new |
| T4 | all existing storage, service and menu tests | pass unchanged | R4 | existing |

Commands: `mvn test`. No other checks (lint is `none`).

## Placeholders and tech debt

| Placeholder | Why | Removed by |
| --- | --- | --- |
| none in code | the two-file gap needs a format or commit-marker change | atomic-pair-save |

Entries for `work/crash-safe-save/tech-debt.md` (written by task-development): new `TD-crash-safe-save-1` (a crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync, removed by atomic-pair-save); `TD-file-persistence-1` resolved. task-doc-updates later rewords KI-transaction-operations-2 and updates D16.

## Size

- Production code: about 40 changed lines in 1 file; new runtime dependencies: none
- Increment budget: fits

## Risks

- Fixed temp names collide with a user file of that name: acceptable, the directory is application-owned.
- Some file systems (for example network shares) reject `ATOMIC_MOVE` with another `IOException` than `AtomicMoveNotSupportedException`: they surface as `StorageException`, as the scope defines.
- The test seam adds a package-private type: kept minimal, documented, and avoids mocking libraries (TEST-7).
