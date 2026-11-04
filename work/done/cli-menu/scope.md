---
task: cli-menu
artifact: scope
version: 1
status: confirmed
branch: cli-menu
roadmap_item: cli-menu
---

# Scope: Console menu

## Goal

The user runs `mvn exec:java` and manages accounts and transactions from a console menu, replacing the placeholder `Main` (TD-app-foundation-1).

## Requirements

| ID | Requirement | Acceptance criterion |
| --- | --- | --- |
| R1 | `ConsoleMenu` reads from an `InputStream` and writes to a `PrintStream`; `Main` only wires storage, repository, service and menu | Given scripted input ending with the exit choice, when the menu runs, then it prints the menu and returns without error |
| R2 | `Main` loads `data/` at startup (empty repository if the files are missing) | Given no `data/` files, when the app starts, then the menu opens with zero accounts |
| R3 | Create an account (number, owner, initial balance, type), saved to disk | Given valid input, when the user creates an account, then it appears in the list and in `accounts.csv` |
| R4 | List accounts (number, owner, type, balance) | Given two accounts, when the user lists them, then both lines are printed |
| R5 | Deposit, withdraw and transfer, selecting accounts by account number | Given existing accounts, when the user deposits 50, then the balance rises by 50 and the transaction is saved |
| R6 | List the transactions of an account as a numbered list | Given an account with 3 transactions, when the user lists them, then 3 numbered lines are printed |
| R7 | Update the amount of or delete a transaction, selected by its number in that list | Given a listed transaction, when the user deletes number 1, then it is gone and the balance reversal is applied |
| R8 | Show the recent history (last 10 transactions) of an account | Given an account with 12 transactions, when the user asks for recent history, then 10 are shown |
| R9 | Errors (unknown account or number, invalid amount, insufficient funds, non-numeric input, bad menu choice, storage failure) print a short message and return to the menu | Given a withdrawal above the balance, when submitted, then an error message is printed, the balance is unchanged and the menu shows again |

## Out of scope

- Login and authentication.
- Money formatting beyond plain `BigDecimal`.
- Account deletion or editing.
- `crash-safe-save` (atomic file writes).
- Re-prompting for the same field: after an error the user returns to the main menu.

## Options considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| `ConsoleMenu(InputStream, PrintStream)` | testable with scripted input, no new dependency | slightly more wiring | chosen |
| Direct `System.in`/`System.out` | simplest | only manually testable | rejected |
| TUI library (Lanterna, JLine) | richer UI | new dependency, not needed | rejected |

Sources:

- Project code and `docs/ARCHITECTURE.md` (no web research needed: plain `java.util.Scanner`/`BufferedReader` is enough).

## Constraints and assumptions

- Java 17, Maven, JUnit 5; no new dependency.
- Accounts are chosen by account number, transactions by position in the listed order; UUIDs are never shown.
- Account creation must be saved to disk; where that lives (`TransactionService` or a new service) is left to task-planning.
- Menu code follows `docs/standards/java-oop.md`.

## Impact

- Code areas touched: `Main.java`, new `ebanking.cli.ConsoleMenu`, possibly a small account-creation method in the service layer, new tests.
- Blast radius: low (new UI layer; existing classes at most get one method).
- Increment budget: fits (about 250 lines of production code, 3 to 4 files).

## Split and placeholders

Not needed.

## Follow-ups

- crash-safe-save: already on the roadmap follow-ups.

## Open questions

- None
