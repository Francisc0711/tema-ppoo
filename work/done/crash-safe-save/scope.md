---
task: crash-safe-save
artifact: scope
version: 1
status: confirmed
branch: crash-safe-save
roadmap_item: crash-safe-save
---

# Scope: Crash-safe save

## Goal

A crash or write error during `CsvBankStorage.save` can no longer truncate or corrupt an existing data file: each file is either the old complete version or the new complete version.

## Requirements

| ID | Requirement | Acceptance criterion |
| --- | --- | --- |
| R1 | `save` writes each CSV to a temporary file in the same directory, then moves it over the target | Given a saved repository, when `save` runs again, then the files hold the new data and no temporary file is left in the directory |
| R2 | If writing a file fails, the existing target file is left unchanged | Given existing data files and a write that fails, when `save` runs, then it throws `StorageException` and the files still hold the old content and no temporary file remains |
| R3 | The move is atomic when the file system supports it, otherwise it falls back to a normal replace | Given a file system without atomic move, when `save` runs, then the data is still replaced and no error is thrown |
| R4 | Save and load behavior and file format stay unchanged | Given the existing `CsvBankStorageTest` and service tests, when run, then all pass unchanged |

## Out of scope

- Atomic update of both files together; a crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync (recorded as tech debt).
- Rolling back memory when a save fails (KI-transaction-operations-2 stays open, reworded).
- `fsync` of file contents or the directory.
- Load-side hardening (TD-file-persistence-2, -F1).

## Options considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| Temp file + `Files.move` (`ATOMIC_MOVE`, fallback `REPLACE_EXISTING`) | JDK only, no new dependency, small | two files still not updated together | chosen |
| Single combined file or commit marker | closes the two-file gap | changes the file format, larger task | rejected |
| Keep in-place write | no change | truncation risk | rejected |

Sources:

- [Stack Overflow: how to guarantee atomic move or exception of a file in java (mirror)](https://kiwix.lema.org/stackoverflow.com_en_all_2023-11/questions/25238284/how-to-guarantee-atomic-move-or-exception-of-a-file-in-java)
- [HowToDoInJava: Rename or move a file in Java](https://howtodoinjava.com/java/io/rename-move-file-directory/)

## Constraints and assumptions

- Java 17, `java.nio.file` only; no new dependency.
- The temporary file is created in the data directory, so it is on the same file system as the target.
- `AtomicMoveNotSupportedException` triggers the non-atomic fallback; other `IOException`s become `StorageException`.

## Impact

- Code areas touched: `CsvBankStorage.save` (about 30 lines), `CsvBankStorageTest`; docs: `docs/TECH_DEBT.md`, `docs/KNOWN_ISSUES.md`, `docs/ARCHITECTURE.md` (written later by task-doc-updates).
- Blast radius: low; every service operation saves through this method, but the format and API do not change.
- Increment budget: fits.

## Split and placeholders

Not needed.

## Follow-ups

- atomic-pair-save: update `accounts.csv` and `transactions.csv` as one unit.
- save-failure-rollback: restore memory when a save fails (KI-transaction-operations-2).

## Open questions

- None
