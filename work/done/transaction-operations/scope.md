---
task: transaction-operations
artifact: scope
version: 1
status: confirmed        # draft | confirmed (only after the user confirms)
branch: transaction-operations
roadmap_item: transaction-operations
---

# Scope: Add, update and delete transactions with balance changes

## Goal

A `TransactionService` processes deposits, withdrawals and transfers that change account balances, and can update or delete a transaction; every successful operation is saved to the CSV files.

## Requirements

| ID | Requirement | Acceptance criterion |
| --- | --- | --- |
| R1 | `Account` balance can change only through `deposit(amount)` and `withdraw(amount)`; `withdraw` below zero throws `InsufficientFundsException` | Given balance 50, when `withdraw(80)` runs, then `InsufficientFundsException` is thrown and the balance stays 50. |
| R2 | `addDeposit` / `addWithdrawal` apply the amount to the account, store the transaction in the repository and in the account's recent history | Given balance 100, when a withdrawal of 30 is added, then balance is 70, the repository holds the transaction and it is the newest history entry. |
| R3 | `addTransfer` moves the amount from source to target atomically; on any failure neither balance nor repository changes | Given source 20, when a transfer of 50 is added, then `InsufficientFundsException` is thrown and both balances and the repository are unchanged. |
| R4 | Unknown account ids are rejected with `ValidationException` | Given an unknown account id, when a deposit is added, then `ValidationException` is thrown and nothing changes. |
| R5 | `deleteTransaction(id)` removes it from the repository and the histories of its accounts and reverses its balance effect; if the reversal would make a balance negative, it throws `InsufficientFundsException` and nothing changes | Given a deposit of 40 on balance 40 followed by a withdrawal of 40, when the deposit is deleted, then `InsufficientFundsException` is thrown; when the withdrawal is deleted first, balance returns to 40 and the history no longer holds it. |
| R6 | `updateTransactionAmount(id, newAmount)` keeps id, type, accounts and `createdAt` and position, reverses the old amount and applies the new one, atomically | Given a deposit of 40 on balance 100, when updated to 10, then balance is 70 and the stored transaction has amount 10 with the same id. |
| R7 | Delete or update of an unknown transaction id throws `ValidationException` | Given an unknown id, when `deleteTransaction` runs, then `ValidationException` is thrown. |
| R8 | After each successful add, update or delete the service calls `CsvBankStorage.save`; a failed operation does not save | Given a temp directory, when a deposit is added, then `load()` returns a repository with the new balance and transaction. |
| R9 | `Account.addToHistory` rejects a transaction that does not involve the account (TD-app-foundation-F1) | Given a transaction of another account, when `addToHistory` runs, then `ValidationException` is thrown. |

## Out of scope

- Console menu and user input (`cli-menu`); `Main` is unchanged (TD-app-foundation-1 stays open).
- Changing the type, accounts or `createdAt` of a transaction; editing account data.
- Transfer fees, limits, currencies, concurrency, transaction rollback beyond the checks above.
- Crash-safe writes (crash-safe-save).

## Options considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| `TransactionService` using `BankRepository` and `CsvBankStorage` | single place for rules, repository stays free of I/O, testable with `@TempDir` | one more class | chosen (user) |
| Logic inside `BankRepository` | fewer classes | mixes storage, rules and I/O | rejected |
| Delete reverses balance; update = reverse old + apply new, `Transaction` stays immutable | consistent balances, keeps immutability | reversal can fail on a negative balance | chosen (user) |
| Mutable `Transaction.amount` | simple setter | breaks immutability | rejected |
| Delete only removes the record | simplest | balances become inconsistent with history | rejected (user) |

Sources: none needed; plain Java 17, no new technology.

## Constraints and assumptions

- Java 17, no new dependency; existing exceptions: `ValidationException` (checked), `InsufficientFundsException` (unchecked).
- Update replaces the stored object with a restore-constructor copy (same id, `createdAt`); the repository gets replace/remove methods.
- After delete or update, the recent history of the affected accounts is rebuilt from the repository transactions (last 10, as on load).
- Atomicity: validate and compute first, then mutate; if saving fails, the in-memory change stays and `StorageException` is reported to the caller (no rollback of memory).

## Impact

- Code areas touched: `Account` (mutable balance, history check/rebuild), `BankRepository` (remove, replace, find by id), new `TransactionService`, tests.
- Blast radius: low to medium; `Account` constructors stay, `balance` becomes mutable only through two methods.
- Increment budget: fits (about 200–250 production lines, about 5 files).

## Split and placeholders

Not needed.

## Follow-ups

- None new (crash-safe-save is already listed).

## Open questions

- None
