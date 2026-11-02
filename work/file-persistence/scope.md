---
task: file-persistence
artifact: scope
version: 1
status: confirmed        # draft | confirmed (only after the user confirms)
branch: file-persistence
roadmap_item: file-persistence
---

# Scope: Save and load accounts and transactions in CSV files

## Goal

The bank data survives a restart: a storage class writes all accounts and transactions of a `BankRepository` to CSV text files and reads them back into a new repository with the same ids, values and recent histories.

## Requirements

| ID | Requirement | Acceptance criterion |
| --- | --- | --- |
| R1 | Entities can be recreated with a stored `id` and `createdAt` (restore constructors, normal constructors unchanged) | Given a saved account and transaction, when they are loaded, then `getId()` and `getCreatedAt()` equal the saved values. |
| R2 | `save(repository)` writes every account to `accounts.csv` (id, createdAt, accountNumber, ownerName, balance, type) in the storage directory | Given a repository with two accounts, when it is saved, then `accounts.csv` has a header and two lines with these fields. |
| R3 | `save` writes every transaction to `transactions.csv` (id, createdAt, type, accountId, targetAccountId or empty, amount), in repository order | Given one deposit, one withdrawal and one transfer, when saved and loaded, then three transactions of the right subtypes with equal fields come back in the same order. |
| R4 | `load()` returns a new `BankRepository` with all saved accounts and transactions; missing files give an empty repository | Given no data files, when `load()` runs, then it returns an empty repository without an error. |
| R5 | Text fields containing commas, quotes or line breaks are quoted so they round-trip | Given owner name `Popescu, Ion "Junior"`, when saved and loaded, then the name is identical. |
| R6 | After loading, each account's recent history is rebuilt from the loaded transactions in file order (last 10) | Given 12 transactions on one account, when loaded, then its history holds the last 10, oldest first. |
| R7 | An invalid line (wrong field count, bad number or date, unknown type, invalid domain value, transaction on an unknown account) makes the whole load fail with a checked exception naming the file and line number | Given `transactions.csv` with `amount` = `abc` on line 3, when `load()` runs, then the exception message names `transactions.csv` and line 3 and no repository is returned. |
| R8 | `Main` loads the data from `data/` at start and prints the number of accounts and transactions | Given saved files in `data/`, when `mvn exec:java` runs, then it prints the two counts. |

## Out of scope

- Saving automatically after each change (wired by `transaction-operations` / `cli-menu`).
- Updating or deleting records, balance changes from transactions (`transaction-operations`).
- Binary files, JSON, databases (OOP-4), file locking, backups, encryption.
- Crash-safe writes (temp file and atomic move); see Follow-ups.

## Options considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| CSV text with `BufferedReader`/`BufferedWriter` (`java.nio.file.Files`) | readable, easy to inspect in tests, no dependency, fits OOP-4 | own parsing and quoting | chosen |
| Binary, `ObjectOutputStream` (Serializable) | least code, keeps object graph | opaque files, breaks on class changes, deserialization risks | rejected |
| Binary, `DataOutputStream` | compact, fixed layout | more code, not readable, fragile string handling | rejected |
| JSON library (Gson/Jackson) | mature parsing | new runtime dependency for a simple flat format | rejected |
| Fail whole load on a bad line | protects data integrity, simple | one bad line blocks startup | chosen (user) |
| Explicit `save`/`load` API | repository stays free of I/O, easy to test with a temp directory | callers must remember to save | chosen (user) |

Sources:

- https://www.happycoders.eu/?p=9751 (DataOutputStream / DataInputStream)
- https://kiwix.lema.org/stackoverflow.com_en_all_2023-11/questions/38023660/how-efficient-is-java-serialization
- https://gist.github.com/3187135 (serialization size comparison)
- https://www.w3docs.com/learn-java/java-nio-files (Files API, temp file and ATOMIC_MOVE)
- https://kiwix.lema.org/stackoverflow.com_en_all_2023-11/questions/25238284/how-to-guarantee-atomic-move-or-exception-of-a-file-in-java

## Constraints and assumptions

- Java 17, `java.nio.file`, UTF-8, try-with-resources for every file (OOP-3); no new dependency.
- The storage directory is a constructor argument (tests use JUnit `@TempDir`); `Main` uses `data/`, already in `.gitignore`.
- Amounts are written with `BigDecimal.toPlainString()`; dates in ISO-8601.
- Rebuilding the history uses `Account.addToHistory`; TD-app-foundation-F1 (unrelated transactions accepted) stays open.

## Impact

- Code areas touched: `BankEntity`, `Account`, `Transaction` and three subtypes (restore constructors), new storage class and exception in a new `ebanking.storage` package, `Main`.
- Blast radius: low; existing constructors and behaviour are unchanged, only constructors are added.
- Increment budget: fits (about 200–250 production lines, about 10 files).

## Split and placeholders

Not needed. `Main` still prints instead of a menu (TD-app-foundation-1 stays open until `cli-menu`).

## Follow-ups

- crash-safe-save: write to a temp file in the same directory, then move it over the old file atomically.

## Open questions

- None
