---
task: cli-menu
artifact: dev-log
plan_version: 1
round: 1
status: done
---

# Development log: cli-menu

## Increment 1: 1. TransactionService.createAccount

- Commit: `110bfed` feat(cli-menu): add createAccount to transaction service
- Changes: `TransactionService.java`: `createAccount` builds the `Account`, adds it to the repository, saves; `TransactionServiceTest.java`: T11
- Decisions: none
- Tests: T11 creates an account (in memory and in the saved file) and checks a duplicate number throws `ValidationException` and leaves 3 accounts
- Review focus: validation order (Account constructor, then duplicate check, then save)
- Deviation from the plan: none

## Increment 2: 2-4. ConsoleMenu, Main, menu tests

- Commit: `307b730` feat(cli-menu): add console menu and wire it in Main
- Changes: `ConsoleMenu.java`: menu loop, helpers, single catch for errors; `Main.java`: wiring, stops on a corrupt data file, placeholder and TD-app-foundation-1 removed; `ConsoleMenuTest.java`: T1-T10
- Decisions: end of input in a field prompt counts as an empty answer (gives a normal validation error); a non-integer transaction number gives "unknown transaction number" (critique suggestion); `describe` prints `getCreatedAt()`; the menu text ends with `Choice: ` and is printed with `print`
- Tests: T1 menu and EOF exit; T2 empty list; T3 create is saved; T4 list; T5 deposit/withdraw/transfer + CSV lines + no id printed; T6 numbered list; T7 update/delete; T8 history 10, newest first; T9 insufficient funds; T10 invalid inputs (one assertion per case)
- Review focus: `readLine` wraps `IOException` in `UncheckedIOException`; balances printed after the service call read the live `Account`; stdin is not closed
- Deviation from the plan: none

## Commands

| Command | Result |
| --- | --- |
| test: `mvn test` | 40 passed, 0 failed |
| lint: none | skipped (AGENTS.md) |

## Notes for testing

- Start: `mvn compile exec:java` (the plan's `mvn exec:java` needs a prior compile); data is read from and written to `data/` in the working directory.
- Manual R2 check done in a copy without `data/`: `printf '2\n0\n' | mvn -q compile exec:java` prints the menu, "No accounts." and exits.
