---
task: app-foundation
artifact: plan
version: 1
scope_version: 1
answers_critique_round: 0
---

# Plan: App Foundation

## Summary

Create the Maven project (Java 17, JUnit 5, exec-maven-plugin) and the in-memory domain layer: abstract `BankEntity`, `Account` with a circular 10-slot `Transaction[]` history, an abstract `Transaction` with three subtypes, two enums, two custom exceptions and an `ArrayList`-backed `BankRepository`. No I/O, no balance processing.

## Context

- `scope.md`: R3 and R8 disagree on the exception for a negative initial balance. The user decided (2026-11-02): **`ValidationException`**. `InsufficientFundsException` is defined now and first thrown by `transaction-operations`.
- `docs/standards/java-oop.md`: OOP-1 (private fields, accessors), OOP-2 (abstract base classes), OOP-4 (collections in memory). OOP-3 does not apply (no I/O).
- `docs/standards/testing.md` and `docs/ARCHITECTURE.md` still describe the old Node.js stack. TEST-3, TEST-4, TEST-6, TEST-7 apply; TEST-1, TEST-2, TEST-5, TEST-8 do not (JUnit 5 is mandated by the scope). ARCHITECTURE is rewritten by task-doc-updates.
- No existing code or tests; `.gitignore` lacks `target/`.

## Files

| # | File | Action | Purpose |
| --- | --- | --- | --- |
| 1 | `pom.xml` | create | Java 17, JUnit 5, surefire, exec-maven-plugin |
| 2 | `.gitignore` | modify | add `target/` |
| 3 | `src/main/java/ebanking/Main.java` | create | entry point for `mvn exec:java` |
| 4 | `src/main/java/ebanking/model/BankEntity.java` | create | abstract base: `id`, `createdAt` (R2) |
| 5 | `src/main/java/ebanking/model/AccountType.java` | create | enum `CHECKING`, `SAVINGS` |
| 6 | `src/main/java/ebanking/model/Account.java` | create | account + recent-history buffer (R3, R6) |
| 7 | `src/main/java/ebanking/model/TransactionType.java` | create | enum `DEPOSIT`, `WITHDRAWAL`, `TRANSFER` (R5) |
| 8 | `src/main/java/ebanking/model/Transaction.java` | create | abstract transaction (R4) |
| 9 | `src/main/java/ebanking/model/DepositTransaction.java` | create | subtype (R4) |
| 10 | `src/main/java/ebanking/model/WithdrawalTransaction.java` | create | subtype (R4) |
| 11 | `src/main/java/ebanking/model/TransferTransaction.java` | create | subtype with target account (R4) |
| 12 | `src/main/java/ebanking/exception/ValidationException.java` | create | checked (R8) |
| 13 | `src/main/java/ebanking/exception/InsufficientFundsException.java` | create | unchecked (R8) |
| 14 | `src/main/java/ebanking/repository/BankRepository.java` | create | in-memory store (R7) |
| 15–17 | `src/test/java/ebanking/{model/AccountTest, model/TransactionTest, repository/BankRepositoryTest}.java` | create | tests T1–T9 |

## Steps

### Step 1. Build setup

`pom.xml`: `groupId` `ebanking`, `artifactId` `tema-ppoo`, `packaging` jar, properties `maven.compiler.release=17`, `project.build.sourceEncoding=UTF-8`. Dependency `org.junit.jupiter:junit-jupiter:5.10.2` (scope `test`). Plugins: `maven-surefire-plugin` 3.2.5 (needed to run JUnit 5), `org.codehaus.mojo:exec-maven-plugin` 3.1.0 with `<mainClass>ebanking.Main</mainClass>`. Add `target/` to `.gitignore`. `Main.main` prints one line, `E-Banking app – console menu coming in cli-menu`, marked `// TODO(TD-app-foundation-1)`. Commit `chore(app-foundation): add maven build`.

### Step 2. Exceptions

`ValidationException extends Exception` and `InsufficientFundsException extends RuntimeException`, each with a `(String message)` constructor only.

### Step 3. BankEntity and enums

`BankEntity` is `public abstract`, with `private final String id` (`UUID.randomUUID().toString()`) and `private final LocalDateTime createdAt` (`LocalDateTime.now()`), both set in a `protected` no-arg constructor; public getters only. `AccountType`, `TransactionType`: plain enums.

### Step 4. Transaction hierarchy

`Transaction extends BankEntity` (abstract): `private final String accountId`, `private final BigDecimal amount`. Protected constructor validates, then stores. `public abstract TransactionType getType()`. `public boolean involves(String accountId)` returns `this.accountId.equals(accountId)`; `TransferTransaction` overrides it to also match `targetAccountId` (polymorphism, OOP-2, used by R7).

```java
protected Transaction(String accountId, BigDecimal amount) throws ValidationException {
    if (accountId == null || accountId.isBlank()) {
        throw new ValidationException("accountId is required");
    }
    if (amount == null || amount.signum() <= 0) {
        throw new ValidationException("amount must be positive");
    }
    this.accountId = accountId;
    this.amount = amount;
}
```

`DepositTransaction(accountId, amount)` and `WithdrawalTransaction(accountId, amount)` only call `super` and return their constant from `getType()`. `TransferTransaction(sourceAccountId, targetAccountId, amount)` adds `private final String targetAccountId`; it throws `ValidationException` when the target is null/blank or equals the source.

### Step 5. Account

Fields: `accountNumber`, `ownerName`, `BigDecimal balance`, `AccountType type`, plus the buffer. `public static final int HISTORY_CAPACITY = 10`. Constructor `Account(String accountNumber, String ownerName, BigDecimal initialBalance, AccountType type) throws ValidationException`: null/blank `accountNumber` or `ownerName`, null `type`, null or negative balance → `ValidationException` (decision above). Getters for all four fields; `setOwnerName` with the same check. No `setBalance`: balance changes belong to `transaction-operations`.

Buffer (circular; returns a copy so callers cannot modify it, OOP-1):

```java
private final Transaction[] recentHistory = new Transaction[HISTORY_CAPACITY];
private int historyStart = 0;   // index of the oldest entry
private int historySize = 0;

public void addToHistory(Transaction transaction) throws ValidationException {
    if (transaction == null) throw new ValidationException("transaction is required");
    int end = (historyStart + historySize) % HISTORY_CAPACITY;
    recentHistory[end] = transaction;
    if (historySize < HISTORY_CAPACITY) historySize++;
    else historyStart = (historyStart + 1) % HISTORY_CAPACITY; // overwrote the oldest
}

public Transaction[] getRecentHistory() {               // oldest first
    Transaction[] copy = new Transaction[historySize];
    for (int i = 0; i < historySize; i++) {
        copy[i] = recentHistory[(historyStart + i) % HISTORY_CAPACITY];
    }
    return copy;
}
```

### Step 6. BankRepository

`private final List<Account> accounts = new ArrayList<>()` and `private final List<Transaction> transactions = new ArrayList<>()`.

| Method | Behavior |
| --- | --- |
| `void addAccount(Account a) throws ValidationException` | null → exception; an account with the same `accountNumber` already stored → exception (protects data) |
| `Optional<Account> findAccountById(String id)` | linear search on `getId()` |
| `void addTransaction(Transaction t) throws ValidationException` | null → exception |
| `List<Transaction> findTransactionsByAccount(String accountId)` | new list of `t.involves(accountId)` |
| `List<Transaction> findTransactionsByType(TransactionType type)` | new list of `t.getType() == type` (R4 "retrieved by type") |
| `List<Account> getAccounts()`, `List<Transaction> getTransactions()` | `List.copyOf(...)` |

The repository does not update the account history or balances; `transaction-operations` wires that.

### Step 7. Tests, then commit

Write T1–T9, run `mvn test`, commit `feat(app-foundation): add domain model and in-memory repository` (GIT-2; Steps 2–7 may be split into smaller commits, GIT-3).

## Interfaces and data

- Library-style public API as in Steps 3–6; packages `ebanking.model`, `ebanking.exception`, `ebanking.repository`.
- Money is `BigDecimal` (exact decimal arithmetic for balances).
- Account `id` (UUID) is the technical key; `accountNumber` is the business key and unique in the repository.
- New dependencies: `junit-jupiter` 5.10.2 (test only). No runtime dependency.

## Tests

| ID | File | Checks | Requirement | New / existing |
| --- | --- | --- | --- | --- |
| T1 | `AccountTest` | valid account: `getBalance()` returns the initial balance; `getId()` non-null, `getCreatedAt()` set | R3, R2 | new |
| T2 | `AccountTest` | null `accountNumber` throws `ValidationException` | R8 | new |
| T3 | `AccountTest` | negative initial balance throws `ValidationException` | R3, R8 | new |
| T4 | `AccountTest` | after 11 `addToHistory` calls, history has 10 entries, first is the 2nd added, last is the 11th | R6 | new |
| T5 | `TransactionTest` | one of each subtype in an `ArrayList<Transaction>`: each `getType()` is correct and filtering by type returns exactly one | R4, R5 | new |
| T6 | `TransactionTest` | zero amount throws `ValidationException` | R4 (data) | new |
| T7 | `BankRepositoryTest` | `addAccount` then `findAccountById` returns it; unknown id returns empty | R7 | new |
| T8 | `BankRepositoryTest` | `findTransactionsByAccount` returns the account's deposit and a transfer targeting it, not others | R7 | new |
| T9 | `BankRepositoryTest` | adding a second account with the same `accountNumber` throws `ValidationException` | R7 (data) | new |

Each test builds its own objects (TEST-4); names describe behavior, e.g. `@DisplayName("adding the 11th transaction discards the oldest")` (TEST-6).

Commands: `mvn clean install -DskipTests`, then `mvn test` (expected: 9 tests, 0 failures); `mvn exec:java` prints the line from Step 1. Lint: none.

## Placeholders and tech debt

| Placeholder | Why | Removed by |
| --- | --- | --- |
| `TD-app-foundation-1`: `Main` only prints a line | console UI is out of scope | cli-menu |

`InsufficientFundsException` is defined but not thrown yet; this is planned use, not debt (thrown by `transaction-operations`).

## Size

- Production code: about 260 lines in 13 Java files plus `pom.xml` and `.gitignore`; new runtime dependencies: none.
- Increment budget: lines fit; the file count is about 3 over the guideline of 10 because Java needs one file per public type (two enums and two exceptions are under 10 lines each). Merging them would break the scope's named types, so the plan accepts the overshoot.

## Risks

- `mvn` is not installed on the development machine (JDK 17 is): the developer installs Maven 3.8+ (`brew install maven`) before Step 1.
- `docs/standards/testing.md` is still written for Node.js: flagged for the humans who own the standards; this plan applies the stack-neutral rules only.

## Changes since the previous version

Not applicable (version 1).
