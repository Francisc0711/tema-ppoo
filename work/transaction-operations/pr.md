---
task: transaction-operations
artifact: pr
branch: transaction-operations
base: main
updated_commit: none
pr: not created
---

# feat(transaction-operations): add, update and delete transactions with balance changes

## Summary

Adds a `TransactionService` that processes deposits, withdrawals and transfers that change account balances, and can update the amount of or delete a transaction. Every successful operation is saved to the CSV files. Balances change only through `Account.deposit` and `Account.withdraw`; an overdraw throws `InsufficientFundsException`.

## Changes

- `Account`: mutable balance through `deposit` / `withdraw`, involvement check in `addToHistory` (closes TD-app-foundation-F1), `rebuildHistory`.
- `Transaction` and its three subtypes: `withAmount` returns a copy with the same id, `createdAt` and accounts.
- `BankRepository`: `findTransactionById`, `removeTransaction`, `replaceTransaction` (keeps list position).
- New `TransactionService`: `addDeposit`, `addWithdrawal`, `addTransfer` (atomic), `deleteTransaction`, `updateTransactionAmount`; unknown ids give `ValidationException`; saves via `CsvBankStorage` only after success.
- Tests: `AccountTest` (2 new), `TransactionServiceTest` (8 new). 28 tests in total.
- `Main` is unchanged; the service is wired in by the `cli-menu` task.

## How to test

```bash
mvn clean install -DskipTests
mvn test
```

Expected: 28 passed, 0 failed. The service is not reachable from `Main`; the exploratory checks (E1–E12 in `test-report.md`) called the library with temporary data and all passed.

## Quality gates

| Gate | Task file | Result |
| --- | --- | --- |
| Plan critique | `critique.md`, round 1 | approved |
| Code review | `review.md`, round 1 | approved |
| Tests | `test-report.md`, round 1 | pass |

## Tech debt

- None introduced. Known limitation: a failed `save` leaves memory ahead of disk (accepted in scope; follow-up `crash-safe-save`). `rebuildHistory` has no unit test for more than 10 matching transactions (review F2, minor; checked exploratorily).

## Follow-ups

- cli-menu: console menu that uses `TransactionService` (closes TD-app-foundation-1).
- crash-safe-save: crash-safe writes of the CSV files.
