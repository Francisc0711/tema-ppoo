# Architecture

## Overview

A Java 17 console application built with Maven. The domain layer (`ebanking.model`) is a class hierarchy with private fields and validation in constructors; `ebanking.repository` keeps all objects in memory in `ArrayList`s; `ebanking.storage` saves and loads them as CSV files. `ebanking.service` applies transactions to balances; the console menu is added by `cli-menu` (`docs/ROADMAP.md`).

## Components

| Component | Location | Responsibility |
| --- | --- | --- |
| Entry point | `src/main/java/ebanking/Main.java` | started by `mvn exec:java`; loads `data/` and prints the counts until `cli-menu` |
| Domain model | `src/main/java/ebanking/model/` | `BankEntity`, `Account`, `Transaction` and subtypes, enums `AccountType`, `TransactionType` |
| Exceptions | `src/main/java/ebanking/exception/` | `ValidationException` (checked, invalid input), `InsufficientFundsException` (unchecked, thrown by `Account.withdraw`) |
| Repository | `src/main/java/ebanking/repository/BankRepository.java` | in-memory store of accounts and transactions; rejects duplicate account numbers; read methods return copies |
| Service | `src/main/java/ebanking/service/TransactionService.java` | `addDeposit`, `addWithdrawal`, `addTransfer`, `deleteTransaction`, `updateTransactionAmount`; validates, mutates, then saves through `CsvBankStorage` |
| Storage | `src/main/java/ebanking/storage/` | `CsvBankStorage` (`save`, `load`) writes and reads `accounts.csv` and `transactions.csv` in a directory; `CsvCodec` quotes and parses fields; `StorageException` (checked) names file and line |

## Data model

| Entity | Fields | Stored in |
| --- | --- | --- |
| `BankEntity` (abstract) | `id` (UUID string), `createdAt` | base of every entity |
| `Account` | `accountNumber` (unique), `ownerName`, `balance` (`BigDecimal`, not negative), `type` (`CHECKING`, `SAVINGS`), `recentHistory` (`Transaction[10]`, circular, oldest discarded); balance changes only via `deposit`/`withdraw`; `addToHistory` rejects foreign transactions | `BankRepository` (memory) |
| `Transaction` (abstract) | `accountId`, `amount` (`BigDecimal`, positive), `getType()` | `BankRepository` (memory) |
| `DepositTransaction`, `WithdrawalTransaction` | as `Transaction` | `BankRepository` (memory) |
| `TransferTransaction` | as `Transaction` plus `targetAccountId` (not the source) | `BankRepository` (memory) |

## Files

| File | Columns |
| --- | --- |
| `accounts.csv` | id, createdAt, accountNumber, ownerName, balance, type |
| `transactions.csv` | id, createdAt, type, accountId, targetAccountId (empty unless transfer), amount |

UTF-8, header line, ISO-8601 dates, `BigDecimal.toPlainString()` amounts, fields with commas, quotes or line breaks quoted.

## Request flow

No user interaction yet; `cli-menu` adds the flow from the console. Service flow: resolve accounts (unknown id gives `ValidationException`), check funds, mutate balances, update repository and histories, then `CsvBankStorage.save`.

## Decisions

| ID | Decision | Reason | Task |
| --- | --- | --- | --- |
| D1 | Java 17, Maven, JUnit 5, exec-maven-plugin | course requirement; standard Java build and test tools | app-foundation |
| D2 | Abstract `BankEntity` and `Transaction` with subtypes; `TransferTransaction` overrides `involves()` | inheritance and polymorphism (OOP-2) instead of a flat class with a type flag | app-foundation |
| D3 | Money as `BigDecimal` | exact decimal arithmetic for balances | app-foundation |
| D4 | A negative or missing initial balance throws `ValidationException` | invalid input, not a failed withdrawal; `InsufficientFundsException` is reserved for `transaction-operations` | app-foundation |
| D5 | `ArrayList`-backed `BankRepository`, linear search | course requirement (OOP-4); a map can be added later if lookups need it | app-foundation |
| D6 | Recent history as a circular `Transaction[]` of capacity 10, returned as a copy | fixed-size array required by the course; copies keep it encapsulated (OOP-1) | app-foundation |
| D7 | CSV text files via `java.nio.file.Files`, no library | readable, no new dependency, no database (OOP-4) | file-persistence |
| D8 | Explicit `save`/`load` on `CsvBankStorage`; `BankRepository` has no I/O | repository stays simple and testable with a temp directory | file-persistence |
| D9 | Restore constructors keep stored `id` and `createdAt`; normal constructors unchanged | loaded objects equal the saved ones | file-persistence |
| D10 | An invalid line fails the whole load with `StorageException` (file, line) | protects data integrity; no partial repository | file-persistence |
| D11 | Account history rebuilt on load from the transactions in file order (last 10) | history is not stored separately | file-persistence |
| D12 | Rules live in `TransactionService`; `BankRepository` stays free of rules and I/O | one place for business rules, testable with a temp directory | transaction-operations |
| D13 | `Transaction` stays immutable; update replaces it with `withAmount` (same id, `createdAt`, list position) | keeps immutability (OOP-1) | transaction-operations |
| D14 | Delete reverses the balance effect; reversal that would make a balance negative throws `InsufficientFundsException` | balances stay consistent with history | transaction-operations |
| D15 | Funds are checked before any mutation; histories of affected accounts are rebuilt from the repository (last 10) after delete or update | atomic operations without rollback code | transaction-operations |
| D16 | `save` runs only after success; a failed save leaves memory ahead of disk | accepted in scope; see `crash-safe-save` | transaction-operations |
