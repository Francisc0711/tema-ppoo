# E-Banking (PPOO)

A console e-banking application in Java 17, built to practise object-oriented programming (encapsulation, inheritance, polymorphism). Accounts and transactions are kept in memory and can be saved to and loaded from CSV files; a service processes transactions that change balances; a console menu lets the user manage them.

Status: the domain layer, CSV persistence, transaction processing and the console menu exist. The increments are listed in `docs/ROADMAP.md`.

## Features

| Feature | Interface | Since |
| --- | --- | --- |
| Accounts with validation and a recent-history buffer (last 10 transactions) | `ebanking.model.Account` (Java API) | app-foundation |
| Deposit, withdrawal and transfer transactions | `ebanking.model.Transaction` and subtypes (Java API) | app-foundation |
| In-memory store: add and find accounts and transactions | `ebanking.repository.BankRepository` (Java API) | app-foundation |
| Add deposits, withdrawals and transfers that change balances (`InsufficientFundsException` on overdraw), update the amount of or delete a transaction (balance effect reversed); every success is saved to the CSV files | `ebanking.service.TransactionService` (Java API, used by the console menu); `createAccount` validates, rejects duplicate numbers and saves | transaction-operations |
| Console menu: create and list accounts; deposit, withdraw, transfer; list (numbered), update and delete transactions of an account; show the last 10 transactions; errors print one line and return to the menu | `ebanking.cli.ConsoleMenu` (started by `Main`) | cli-menu |
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

The data directory is `data/` (git-ignored), fixed in `Main`. It holds `accounts.csv` and `transactions.csv`; missing files give an empty repository.

## Run

```bash
mvn compile exec:java
```

This loads `data/` (empty repository if the files are missing) and opens the console menu. Accounts are chosen by account number and transactions by their number in the listed order; every change is saved to the CSV files immediately. A corrupt data file stops the program with a message and is not overwritten.

## Test

```bash
mvn test
```

JUnit 5 tests are in `src/test/java/ebanking/`.

## Development workflow

Every change is a task that goes through eight phases, each one a skill in `.agents/skills/`; the rules are in `AGENTS.md`. A task starts with `/task-scoping`, which takes the next roadmap item when it gets no request. The later phases (`/task-planning`, `/task-plan-critique`, `/task-development`, `/task-code-review`, `/task-testing`, `/task-gitops`, `/task-doc-updates`) find the task from the current branch. The files of a task are in `work/<slug>/` while it is open and in `work/done/<slug>/` after it is finished.
