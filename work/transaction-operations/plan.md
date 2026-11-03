---
task: transaction-operations
artifact: plan
version: 1
scope_version: 1
answers_critique_round: 0
---

# Plan: Add, update and delete transactions with balance changes

## Summary

A new `TransactionService` (package `ebanking.service`) adds deposits, withdrawals and transfers, deletes transactions and updates their amount. It changes balances only through `Account.deposit` / `withdraw`, keeps repository and account histories consistent, and calls `CsvBankStorage.save` after each successful operation. Every operation first computes the balance change of each account (a delta map), checks that no balance goes negative, and only then mutates, so a failure leaves nothing changed.

## Context

- `Account`: `balance` is `final`; `addToHistory` accepts any transaction (TD-app-foundation-F1); history is a circular `Transaction[10]`.
- `Transaction` is immutable; subtypes have a restore constructor `(id, createdAt, ...)`, reused for update.
- `BankRepository`: `ArrayList`s, no remove/replace/find-by-id for transactions.
- `CsvBankStorage.load` already rebuilds histories in file order (last 10); the same rule is reused by `Account.rebuildHistory`.
- Standards: OOP-1 (private fields), OOP-2 (polymorphism: `withAmount` per subtype), TEST-3/4/7 (JUnit 5, state checks, no mocks).

## Files

| # | File | Action | Purpose |
| --- | --- | --- | --- |
| 1 | `src/main/java/ebanking/model/Account.java` | modify | mutable balance with `deposit`/`withdraw`; `addToHistory` involvement check; `rebuildHistory` |
| 2 | `src/main/java/ebanking/model/Transaction.java` | modify | abstract `withAmount(BigDecimal)` |
| 3 | `src/main/java/ebanking/model/DepositTransaction.java`, `WithdrawalTransaction.java`, `TransferTransaction.java` | modify | implement `withAmount` with the restore constructor |
| 4 | `src/main/java/ebanking/repository/BankRepository.java` | modify | `findTransactionById`, `removeTransaction`, `replaceTransaction` |
| 5 | `src/main/java/ebanking/service/TransactionService.java` | create | add / delete / update with checks, balance deltas and saving |
| 6 | `src/test/java/ebanking/model/AccountTest.java` | modify | tests for R1, R9 |
| 7 | `src/test/java/ebanking/service/TransactionServiceTest.java` | create | tests for R2–R8 |
| 8 | `docs/` | none | updated later by task-doc-updates (TD-app-foundation-F1 closes) |

## Steps

### Step 1. Account

- Remove `final` from `balance`. Add `deposit(BigDecimal)` and `withdraw(BigDecimal)`: both throw `ValidationException` if the amount is null or not positive; `withdraw` throws `InsufficientFundsException` if `balance < amount`, before changing anything.
- `addToHistory`: after the null check, `if (!transaction.involves(getId())) throw new ValidationException("transaction does not involve this account")` (closes TD-app-foundation-F1).
- `rebuildHistory(List<Transaction> all)`: reset `historyStart` and `historySize` to 0, null the array, then `addToHistory` every transaction of `all` that `involves(getId())`, in list order (the ring keeps the last 10). Throws `ValidationException` only through `addToHistory`, which cannot fail for filtered entries.
- Existing `AccountTest.historyDiscardsOldestWhenFull` uses deposits with `account.getId()`, so it keeps passing.

### Step 2. Transaction copy with a new amount

`public abstract Transaction withAmount(BigDecimal amount) throws ValidationException;` in `Transaction`. Each subtype returns `new XTransaction(getId(), getCreatedAt(), <accounts>, amount)`. Id, `createdAt`, type and accounts are kept (R6).

### Step 3. Repository

- `Optional<Transaction> findTransactionById(String id)`: linear search.
- `boolean removeTransaction(String id)`: `transactions.removeIf(t -> t.getId().equals(id))`.
- `void replaceTransaction(Transaction t) throws ValidationException`: `set` at the index of the same id (keeps position); `ValidationException` if not found.

### Step 4. TransactionService

Constructor `(BankRepository repository, CsvBankStorage storage)`, both required (`IllegalArgumentException`, as `CsvBankStorage`). Public methods, all `throws ValidationException, StorageException`:

| Method | Flow |
| --- | --- |
| `addDeposit(accountId, amount)` | `add(new DepositTransaction(...))` |
| `addWithdrawal(accountId, amount)` | `add(new WithdrawalTransaction(...))` |
| `addTransfer(sourceId, targetId, amount)` | `add(new TransferTransaction(...))` |
| `deleteTransaction(id)` | find or `ValidationException` (R7); `apply(negate(effect(old)))`; remove; rebuild histories; save |
| `updateTransactionAmount(id, newAmount)` | find or `ValidationException`; `updated = old.withAmount(newAmount)`; delta = `effect(updated) - effect(old)`; apply; replace; rebuild histories; save |

`add(tx)`: resolve every account of `tx` (unknown id gives `ValidationException`, R4, before anything else); `apply(effect(tx))`; `repository.addTransaction(tx)`; `addToHistory` on each account; save. Return the stored `Transaction`.

Critical part, balance effect and atomic apply (R3, R5, R6):

```java
/** Signed balance change per account id caused by this transaction. */
private static Map<String, BigDecimal> effect(Transaction t) {
    Map<String, BigDecimal> m = new LinkedHashMap<>();
    switch (t.getType()) {
        case DEPOSIT -> m.put(t.getAccountId(), t.getAmount());
        case WITHDRAWAL -> m.put(t.getAccountId(), t.getAmount().negate());
        default -> {
            m.put(t.getAccountId(), t.getAmount().negate());
            m.put(((TransferTransaction) t).getTargetAccountId(), t.getAmount());
        }
    }
    return m;
}

/** Checks every resulting balance first, then changes the accounts. */
private void apply(Map<String, BigDecimal> delta) throws ValidationException {
    for (var e : delta.entrySet()) {
        Account a = account(e.getKey());
        if (a.getBalance().add(e.getValue()).signum() < 0) {
            throw new InsufficientFundsException("insufficient funds on " + a.getAccountNumber());
        }
    }
    for (var e : delta.entrySet()) {
        if (e.getValue().signum() > 0) account(e.getKey()).deposit(e.getValue());
        else if (e.getValue().signum() < 0) account(e.getKey()).withdraw(e.getValue().negate());
    }
}
```

Update delta uses `Map.merge(id, value, BigDecimal::add)` to combine `effect(updated)` with `effect(old).negate()`; delete uses the negated old effect. `account(id)` returns the account or throws `ValidationException("unknown account: " + id)`. `rebuildHistories(tx)` calls `account.rebuildHistory(repository.getTransactions())` for each account the transaction involves (source, and target for transfers). Because validation and the funds check precede all mutations (the repository mutation comes after `apply`, and `replace` cannot fail for an id that was just found), a failed operation changes nothing and does not save. If `save` fails, memory stays changed and `StorageException` propagates (scope assumption).

## Interfaces and data

New public API: `Account.deposit/withdraw/rebuildHistory`, `Transaction.withAmount`, `BankRepository.findTransactionById/removeTransaction/replaceTransaction`, `TransactionService` (methods above). No file format change, no new dependency. `Main` is unchanged.

## Tests

| ID | File | Checks | Requirement | New / existing |
| --- | --- | --- | --- | --- |
| T1 | `AccountTest` | `withdraw(80)` on 50 throws `InsufficientFundsException`, balance stays 50; `deposit`/`withdraw` change the balance | R1 | new |
| T2 | `AccountTest` | `addToHistory` of a transaction of another account throws `ValidationException` | R9 | new |
| T3 | `TransactionServiceTest` | withdrawal 30 on 100: balance 70, repository holds it, newest history entry | R2 | new |
| T4 | `TransactionServiceTest` | transfer 50 from balance 20 throws `InsufficientFundsException`; both balances and repository unchanged | R3 | new |
| T5 | `TransactionServiceTest` | successful transfer moves the amount and appears in both histories | R3 | new |
| T6 | `TransactionServiceTest` | deposit on unknown account id throws `ValidationException`, nothing changes | R4 | new |
| T7 | `TransactionServiceTest` | deposit 40 then withdrawal 40 on balance 40: deleting the deposit throws `InsufficientFundsException` and changes nothing; deleting the withdrawal restores 40 and removes it from the history | R5 | new |
| T8 | `TransactionServiceTest` | update deposit 40 to 10 on balance 100 gives 70; same id, `createdAt`, position; history shows the new object | R6 | new |
| T9 | `TransactionServiceTest` | delete and update of an unknown id throw `ValidationException` | R7 | new |
| T10 | `TransactionServiceTest` | with `@TempDir`: after a deposit `storage.load()` returns the new balance and transaction; after a failed withdrawal the files are unchanged | R8 | new |
| T11 | existing suite | all earlier tests still pass | all | existing |

Commands: `mvn test` (must pass, all tests); `mvn compile`. No lint (`AGENTS.md`).

## Placeholders and tech debt

None. TD-app-foundation-F1 is closed by this task (task-doc-updates records it). TD-app-foundation-1 (Main) stays open for `cli-menu`.

## Size

- Production code: about 200 changed lines in 7 files (Account, Transaction, 3 subtypes, BankRepository, TransactionService); new runtime dependencies: none
- Increment budget: fits (at most 300 lines, 10 files)

## Risks

- Update of a transfer to a lower amount can still fail on the target (money already spent): the combined delta check covers both accounts and throws before any change.
- Mutating balances before `save` and a failed save leaves memory ahead of disk: accepted in the scope; `crash-safe-save` is the related follow-up.
- `rebuildHistory` is O(n) per call on a full transaction list: acceptable for a console application (OOP-4, ArrayList).

## Changes since the previous version

First version.
