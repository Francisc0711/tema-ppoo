---
task: transaction-operations
artifact: dev-log
plan_version: 1
round: 1
status: done
---

# Development log: transaction-operations

## Increment 1: plan step "1. Account"

- Commit: `62c7e8d` feat(transaction-operations): add account deposit, withdraw and history rebuild
- Changes: `Account`: mutable balance, `deposit`/`withdraw`, involvement check in `addToHistory`, `rebuildHistory`; `AccountTest`: two new tests
- Decisions: `Arrays.fill` clears the ring (critique suggestion); `InsufficientFundsException` is unchecked, so `withdraw` declares only `ValidationException`
- Tests: T1 (deposit/withdraw, overdraw keeps balance), T2 (foreign transaction rejected)
- Review focus: `withdraw` checks funds before mutating
- Deviation from the plan: none

## Increment 2: plan step "2. Transaction copy with a new amount"

- Commit: `29e0af4` feat(transaction-operations): add withAmount to transactions
- Changes: `Transaction`: abstract `withAmount`; `DepositTransaction`, `WithdrawalTransaction`, `TransferTransaction`: implementations using the restore constructor (covariant return types)
- Decisions: none
- Tests: covered by T8 in increment 4
- Review focus: id, `createdAt` and accounts are kept
- Deviation from the plan: none

## Increment 3: plan step "3. Repository"

- Commit: `6e37599` feat(transaction-operations): add transaction find, remove and replace to repository
- Changes: `BankRepository`: `findTransactionById`, `removeTransaction`, `replaceTransaction`
- Decisions: none
- Tests: covered through the service tests (T7–T9)
- Review focus: `replaceTransaction` keeps the list position
- Deviation from the plan: none

## Increment 4: plan step "4. TransactionService"

- Commit: see `git log` (feat(transaction-operations): add transaction service with balance checks)
- Changes: `TransactionService`: add deposit/withdrawal/transfer, delete, update amount; `TransactionServiceTest`: 8 tests
- Decisions: `add` resolves all accounts before `apply` so unknown ids fail first; histories are rebuilt for the accounts of the changed transaction
- Tests: T3–T10 (one test each; T7 and T10 combine the plan's two checks)
- Review focus: delta computation in `updateTransactionAmount`, check-before-mutate in `apply`, save only after success
- Deviation from the plan: none

## Commands

| Command | Result |
| --- | --- |
| test: `mvn test` | 28 passed, 0 failed |
| lint: none | skipped (AGENTS.md) |

## Notes for testing

- The service is not wired into `Main` (`cli-menu` follows); exercise it through the tests.
- A failed `save` leaves memory ahead of disk (accepted in the scope).
