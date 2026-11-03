# E-Banking (PPOO)

A console e-banking application in Java 17, built to practise object-oriented programming (encapsulation, inheritance, polymorphism). Accounts and transactions are kept in memory and can be saved to and loaded from CSV files; a service processes transactions that change balances; the console menu follows in a later increment.

Status: the domain layer, CSV persistence and transaction processing exist; there is no user interface yet. The increments are listed in `docs/ROADMAP.md`.

## Features

| Feature | Interface | Since |
| --- | --- | --- |
| Accounts with validation and a recent-history buffer (last 10 transactions) | `ebanking.model.Account` (Java API) | app-foundation |
| Deposit, withdrawal and transfer transactions | `ebanking.model.Transaction` and subtypes (Java API) | app-foundation |
| In-memory store: add and find accounts and transactions | `ebanking.repository.BankRepository` (Java API) | app-foundation |
| Add deposits, withdrawals and transfers that change balances (`InsufficientFundsException` on overdraw), update the amount of or delete a transaction (balance effect reversed); every success is saved to the CSV files | `ebanking.service.TransactionService` (Java API, not yet reachable from `Main`) | transaction-operations |
| Save and load all accounts and transactions as `accounts.csv` and `transactions.csv` (invalid data fails the load with file name and line number) | `ebanking.storage.CsvBankStorage` (Java API) | file-persistence |

## Requirements

- JDK 17
- Maven 3.8 or newer
- Git, and GitHub CLI (`gh`) for pull requests
- A coding agent that reads skills, for example Antigravity CLI (`agy`)

## Setup

```bash
mvn clean install -DskipTests
```

## Configuration

The data directory is `data/` (git-ignored), fixed in `Main` for now. It holds `accounts.csv` and `transactions.csv`; missing files give an empty repository.

## Run

```bash
mvn exec:java
```

For now this loads `data/` and prints the number of accounts and transactions (for example `Loaded 2 accounts and 3 transactions from data/`); nothing is saved automatically yet. The console menu that uses `TransactionService` arrives with `cli-menu`.

## Test

```bash
mvn test
```

JUnit 5 tests are in `src/test/java/ebanking/`.

## Development workflow

Every change is a task that goes through eight phases, each one a skill in `.agents/skills/`; the rules are in `AGENTS.md`. A task starts with `/task-scoping`, which takes the next roadmap item when it gets no request. The later phases (`/task-planning`, `/task-plan-critique`, `/task-development`, `/task-code-review`, `/task-testing`, `/task-gitops`, `/task-doc-updates`) find the task from the current branch. The files of a task are in `work/<slug>/` while it is open and in `work/done/<slug>/` after it is finished.
