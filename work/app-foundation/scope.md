---
task: app-foundation
artifact: scope
version: 1
status: confirmed
branch: app-foundation
roadmap_item: app-foundation
---

# Scope: App Foundation

## Goal

Set up the Maven project and define the core domain model — `Account`, `Transaction`, and their class hierarchy — with strict OOP encapsulation, an array-based recent-transaction buffer, and an `ArrayList`-backed in-memory store, so every subsequent increment has a typed, testable data layer to build on.

## Requirements

| ID | Requirement | Acceptance criterion |
| --- | --- | --- |
| R1 | Maven project with Java 17, JUnit 5, and exec-maven-plugin | `mvn compile` and `mvn test` complete with no errors on a clean checkout |
| R2 | Abstract `BankEntity` base class with `id` (UUID) and `createdAt` | Every domain object extends `BankEntity` and inherits `getId()` / `getCreatedAt()` |
| R3 | `Account` class (encapsulated: `accountNumber`, `ownerName`, `balance`, `type`) | Given a valid `Account`, `getBalance()` returns the initial balance; a negative balance throws `ValidationException` |
| R4 | `Transaction` abstract class with subtypes `DepositTransaction`, `WithdrawalTransaction`, `TransferTransaction` | Each subtype can be instantiated, stored in an `ArrayList<Transaction>`, and retrieved by type |
| R5 | `TransactionType` enum (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`) | `transaction.getType()` returns the correct enum constant for each subtype |
| R6 | Fixed-size `Transaction[]` array as a "recent history" buffer (capacity 10) in `Account` | Adding the 11th transaction to an account's history discards the oldest entry |
| R7 | `BankRepository` holding `ArrayList<Account>` and `ArrayList<Transaction>` in memory | `addAccount()` / `findAccountById()` / `addTransaction()` / `findTransactionsByAccount()` work correctly |
| R8 | `ValidationException` (checked) and `InsufficientFundsException` (unchecked) custom exceptions | Constructing an `Account` with a null `accountNumber` throws `ValidationException`; constructing with `balance < 0` throws `InsufficientFundsException` |
| R9 | JUnit 5 tests covering R3 success path, R3 error cases (R8), R4 subtype instantiation, R6 buffer eviction, R7 CRUD | All tests pass via `mvn test` |

## Out of scope

- File I/O and persistence (task `file-persistence`)
- Console menu and user interaction (task `cli-menu`)
- Transaction processing logic: applying deposits/withdrawals to account balance (task `transaction-operations`)
- Authentication, multi-user sessions, or any network layer

## Options considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| Maven | Specified in AGENTS.md; standard in Java courses | Verbose pom.xml | chosen |
| Flat `Transaction` class | Simpler | Misses OOP-2 polymorphism requirement | rejected |
| Abstract `Transaction` + subtypes | Satisfies OOP-2; enables `instanceof` / visitor pattern later | Slightly more files | chosen |
| `HashMap<String, Account>` as main store | O(1) lookup by id | Harder to iterate in order | rejected for now; `ArrayList` used per R7, HashMap can be added in `transaction-operations` |

Sources:

- Java 17 release notes: https://openjdk.org/projects/jdk/17/
- JUnit 5 user guide: https://junit.org/junit5/docs/current/user-guide/
- exec-maven-plugin: https://www.mojohaus.org/exec-maven-plugin/

## Constraints and assumptions

- Java 17 (LTS), Maven 3.8+
- JUnit 5.10 (junit-jupiter) for tests
- All field access through getters/setters (OOP-1)
- `id` generated as `UUID.randomUUID().toString()` at construction time
- `Transaction[]` buffer capacity is a constant `HISTORY_CAPACITY = 10`
- No file I/O in this increment; `BankRepository` is purely in-memory

## Impact

- Code areas touched: `pom.xml` (new), `src/main/java/ebanking/` (new package), `src/test/java/ebanking/` (new tests)
- Blast radius: low — no existing code; this is the first increment
- Increment budget: fits (~8 production files, ~200 lines of production code)

## Split and placeholders

Not needed — the increment fits within the budget.

## Follow-ups

- file-persistence: read/write `Account` and `Transaction` objects to text/binary files
- transaction-operations: apply deposits, withdrawals, transfers to account balances
- cli-menu: interactive console menu

## Open questions

- None
