# Architecture

## Overview

A Java 17 console application built with Maven. The domain layer (`ebanking.model`) is a class hierarchy with private fields and validation in constructors; `ebanking.repository` keeps all objects in memory in `ArrayList`s. File persistence, transaction processing and the console menu are added by the increments in `docs/ROADMAP.md`.

## Components

| Component | Location | Responsibility |
| --- | --- | --- |
| Entry point | `src/main/java/ebanking/Main.java` | started by `mvn exec:java`; prints a placeholder line until `cli-menu` |
| Domain model | `src/main/java/ebanking/model/` | `BankEntity`, `Account`, `Transaction` and subtypes, enums `AccountType`, `TransactionType` |
| Exceptions | `src/main/java/ebanking/exception/` | `ValidationException` (checked, invalid input), `InsufficientFundsException` (unchecked, thrown from `transaction-operations`) |
| Repository | `src/main/java/ebanking/repository/BankRepository.java` | in-memory store of accounts and transactions; rejects duplicate account numbers; read methods return copies |

## Data model

| Entity | Fields | Stored in |
| --- | --- | --- |
| `BankEntity` (abstract) | `id` (UUID string), `createdAt` | base of every entity |
| `Account` | `accountNumber` (unique), `ownerName`, `balance` (`BigDecimal`, not negative), `type` (`CHECKING`, `SAVINGS`), `recentHistory` (`Transaction[10]`, circular, oldest discarded) | `BankRepository` (memory) |
| `Transaction` (abstract) | `accountId`, `amount` (`BigDecimal`, positive), `getType()` | `BankRepository` (memory) |
| `DepositTransaction`, `WithdrawalTransaction` | as `Transaction` | `BankRepository` (memory) |
| `TransferTransaction` | as `Transaction` plus `targetAccountId` (not the source) | `BankRepository` (memory) |

## Request flow

No user interaction yet; `cli-menu` adds the flow from the console to the repository.

## Decisions

| ID | Decision | Reason | Task |
| --- | --- | --- | --- |
| D1 | Java 17, Maven, JUnit 5, exec-maven-plugin | course requirement; standard Java build and test tools | app-foundation |
| D2 | Abstract `BankEntity` and `Transaction` with subtypes; `TransferTransaction` overrides `involves()` | inheritance and polymorphism (OOP-2) instead of a flat class with a type flag | app-foundation |
| D3 | Money as `BigDecimal` | exact decimal arithmetic for balances | app-foundation |
| D4 | A negative or missing initial balance throws `ValidationException` | invalid input, not a failed withdrawal; `InsufficientFundsException` is reserved for `transaction-operations` | app-foundation |
| D5 | `ArrayList`-backed `BankRepository`, linear search | course requirement (OOP-4); a map can be added later if lookups need it | app-foundation |
| D6 | Recent history as a circular `Transaction[]` of capacity 10, returned as a copy | fixed-size array required by the course; copies keep it encapsulated (OOP-1) | app-foundation |
