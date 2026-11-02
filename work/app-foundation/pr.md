---
task: app-foundation
artifact: pr
branch: app-foundation
base: main
updated_commit: none     # HEAD after updating the branch from the base branch, if it was updated
pr: not created          # the pull request's address, once created
---

# feat(app-foundation): add maven build and core domain model

## Summary

Sets up the Maven project (Java 17, JUnit 5, exec-maven-plugin) and the core domain model: an abstract `BankEntity` base, `Account` with an array-based recent-history buffer (capacity 10), an abstract `Transaction` hierarchy (deposit, withdrawal, transfer) and an `ArrayList`-backed in-memory `BankRepository`. Later increments (persistence, transaction operations, console menu) build on this typed, tested data layer.

## Changes

- Build: `pom.xml` with Java 17, junit-jupiter 5.10.2, surefire 3.2.5, exec-maven-plugin 3.1.0 (`ebanking.Main`); `.gitignore` ignores `target/`; `Main.java` prints a placeholder line
- Exceptions: `ValidationException` (checked) and `InsufficientFundsException` (unchecked)
- Base and enums: `BankEntity` (abstract, UUID `id` and `createdAt`, getters only), `AccountType`, `TransactionType`
- Transactions: abstract `Transaction` validates the account id and a positive amount; `DepositTransaction`, `WithdrawalTransaction`, `TransferTransaction` (rejects a missing target or a transfer to self, overrides `involves()` to match the target too)
- Account: `Account` validates its fields in the constructor (null or negative balance throws `ValidationException`), exposes getters and `setOwnerName` only, and keeps a circular `Transaction[10]` history that discards the oldest entry; `getRecentHistory()` returns a copy, oldest first
- Repository: `BankRepository` with `ArrayList<Account>` and `ArrayList<Transaction>`; `addAccount` (rejects duplicate account numbers), `findAccountById`, `addTransaction`, `findTransactionsByAccount`, `findTransactionsByType`; read methods return copies
- Tests: `AccountTest`, `TransactionTest`, `BankRepositoryTest`, 9 tests in all

## How to test

```bash
mvn clean install -DskipTests
mvn test
```

All 9 tests pass. `mvn exec:java` prints `E-Banking app - console menu coming in cli-menu`. Exploratory checks (against `target/classes`, no UI yet) covered invalid account and transaction input, buffer eviction after 25 additions, defensive copies of the history and repository lists, and the checked/unchecked kinds of the exceptions.

## Quality gates

| Gate | Task file | Result |
| --- | --- | --- |
| Plan critique | `critique.md`, round 1 | approved |
| Code review | `review.md`, round 1 | approved |
| Tests | `test-report.md`, round 1 | pass |

## Tech debt

- TD-app-foundation-1: `Main.main` only prints a line instead of the console menu (removed by `cli-menu`)
- Review F1 (accepted): `Account.addToHistory` accepts a transaction that does not involve the account; to be rejected in `transaction-operations`
- Review F2 (accepted): `TransactionTest` T5 depends on `BankRepository`

## Follow-ups

- file-persistence: read and write accounts and transactions to files
- transaction-operations: apply deposits, withdrawals and transfers to balances
- cli-menu: interactive console menu
