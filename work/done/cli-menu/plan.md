---
task: cli-menu
artifact: plan
version: 1
scope_version: 1
answers_critique_round: 0
---

# Plan: Console menu

## Summary

Add `ebanking.cli.ConsoleMenu`, a text menu over an `InputStream`/`PrintStream`, that calls the existing `TransactionService` and `BankRepository`. Add `TransactionService.createAccount` so account creation is validated and saved in the same place as the other operations. `Main` only loads `data/`, wires the objects and runs the menu (removes TD-app-foundation-1).

## Context

- `TransactionService`: all transaction rules and `save`; takes account **ids**, so the menu resolves account numbers to ids itself. Throws `ValidationException`, `StorageException` (checked) and `InsufficientFundsException` (unchecked).
- `BankRepository`: `getAccounts()`, `addAccount` (rejects duplicate numbers), `findTransactionsByAccount(id)` (file order, used as the numbered list).
- `Account.getRecentHistory()`: copy, oldest first, at most 10 (the circular buffer already holds the last 10).
- `CsvBankStorage.load()` returns an empty repository when files are missing (R2 already holds); an invalid file throws `StorageException`.
- `docs/standards/java-oop.md` OOP-1 (private fields), OOP-3 (try-with-resources for files; the menu does not own `System.in`, so it is not closed), `testing.md` TEST-3, TEST-4, TEST-7 (no mocks).

## Files

| # | File | Action | Purpose |
| --- | --- | --- | --- |
| 1 | `src/main/java/ebanking/service/TransactionService.java` | modify | add `createAccount(number, owner, balance, type)`: build `Account`, `repository.addAccount`, `storage.save` |
| 2 | `src/main/java/ebanking/cli/ConsoleMenu.java` | create | menu loop, input parsing, output, error handling |
| 3 | `src/main/java/ebanking/Main.java` | modify | wire storage, repository, service, menu; remove the placeholder |
| 4 | `src/test/java/ebanking/cli/ConsoleMenuTest.java` | create | scripted-input tests |
| 5 | `src/test/java/ebanking/service/TransactionServiceTest.java` | modify | one test for `createAccount` |

## Steps

### Step 1. `TransactionService.createAccount`

```java
public Account createAccount(String number, String owner, BigDecimal balance, AccountType type)
        throws ValidationException, StorageException {
    Account account = new Account(number, owner, balance, type); // validates input
    repository.addAccount(account);                               // rejects duplicate number
    storage.save(repository);
    return account;
}
```

### Step 2. `ConsoleMenu`

Constructor `ConsoleMenu(InputStream in, PrintStream out, BankRepository repository, TransactionService service)`; all fields private, null arguments throw `IllegalArgumentException` (as in `TransactionService`). Input is a `BufferedReader` over `InputStreamReader(in, UTF_8)`, not closed (OOP-3 does not apply to streams the caller owns).

`public void run()` loops: print the menu, read a choice, dispatch, until `0` or end of input (`readLine() == null` is treated as exit, so a closed stdin cannot loop forever).

| Choice | Action | Service / repository call |
| --- | --- | --- |
| 1 | create account: number, owner, initial balance, type (`CHECKING`/`SAVINGS`, case-insensitive) | `service.createAccount` |
| 2 | list accounts: `number, owner, type, balance` | `repository.getAccounts()` |
| 3 / 4 | deposit / withdraw: account number, amount | `addDeposit` / `addWithdrawal` |
| 5 | transfer: source number, target number, amount | `addTransfer` |
| 6 | list transactions of an account, numbered from 1 | `findTransactionsByAccount` |
| 7 | update amount: account number, transaction number, new amount | `updateTransactionAmount` |
| 8 | delete: account number, transaction number | `deleteTransaction` |
| 9 | recent history, newest first, at most 10 | `account.getRecentHistory()` |
| 0 | exit | |

Private helpers, each throwing `ValidationException` with a short message so one `catch` handles every error:

- `readAccount(prompt)`: reads a number, finds it in `getAccounts()`, else `"unknown account: X"`.
- `readAmount(prompt)`: `new BigDecimal(line.trim())`; a `NumberFormatException` becomes `ValidationException("not a number: X")`. Positive-amount rules stay in the model.
- `readTransaction(account)`: lists the account's transactions, reads an integer, checks `1..size`, else `"unknown transaction number"`. Returns the `Transaction` at that position (the same list used for printing, so numbers match).
- `readType(prompt)`: `AccountType.valueOf(upper)`; `IllegalArgumentException` becomes `ValidationException("unknown account type: X")`.
- `describe(Transaction)`: `type amount` plus, for a transfer, `from <number> to <number>` (numbers via `repository.findAccountById`, never ids); also the date.

Error handling (R9): the dispatch is wrapped once.

```java
try {
    handle(choice);
} catch (ValidationException | InsufficientFundsException e) {
    out.println("Error: " + e.getMessage());
} catch (StorageException e) {
    out.println("Storage error: " + e.getMessage());
}
```

A bad menu choice prints `Unknown choice` and shows the menu again. No re-prompting for a field: after any error the user returns to the main menu (scope).

Success messages are one line (`Deposited 50 on RO01. Balance: 150`). UUIDs are never printed.

### Step 3. `Main`

```java
CsvBankStorage storage = new CsvBankStorage(Path.of("data"));
BankRepository repository;
try {
    repository = storage.load();   // empty when the files are missing
} catch (StorageException e) {
    System.err.println("Cannot load data: " + e.getMessage());
    return;                        // do not run on a broken file: the first save would overwrite it
}
new ConsoleMenu(System.in, System.out, repository, new TransactionService(repository, storage)).run();
```

Remove the `TODO(TD-app-foundation-1)` comment and the placeholder prints.

### Step 4. Tests (Tests section), then run `mvn test`.

## Interfaces and data

- CLI: menu choices as in the table in Step 2. Run with `mvn exec:java`; reads `data/accounts.csv` and `data/transactions.csv`, writes them after every change.
- Service: `createAccount(String, String, BigDecimal, AccountType) -> Account`.
- No data model, file format, configuration or dependency changes.

## Tests

All `ConsoleMenuTest` tests use a `@TempDir`, a real `BankRepository`/`CsvBankStorage`/`TransactionService`, scripted input joined with `\n` and ending with `0`, and read the output from a `ByteArrayOutputStream` (TEST-7: no mocks).

| ID | File | Checks | Requirement | New / existing |
| --- | --- | --- | --- | --- |
| T1 | `ConsoleMenuTest` | input `0`: menu text printed, `run()` returns | R1 | new |
| T2 | `ConsoleMenuTest` | empty repository from a missing directory: list shows no accounts | R2 | new |
| T3 | `ConsoleMenuTest` | create an account: it is in the repository and in `accounts.csv` | R3 | new |
| T4 | `ConsoleMenuTest` | two accounts: both lines printed with number, owner, type, balance | R4 | new |
| T5 | `ConsoleMenuTest` | deposit 50 raises balance by 50 and `transactions.csv` has the line; withdraw and transfer change balances | R5 | new |
| T6 | `ConsoleMenuTest` | account with 3 transactions prints lines numbered 1 to 3 | R6 | new |
| T7 | `ConsoleMenuTest` | delete number 1 removes it and reverses the balance; update number 1 changes the amount and balance | R7 | new |
| T8 | `ConsoleMenuTest` | account with 12 transactions: recent history prints 10 | R8 | new |
| T9 | `ConsoleMenuTest` | withdrawal above balance: `Error:` printed, balance unchanged, menu printed again | R9 | new |
| T10 | `ConsoleMenuTest` | non-numeric amount, unknown account number, unknown transaction number, bad menu choice: message printed, menu shown again, no state change | R9 | new |
| T11 | `TransactionServiceTest` | `createAccount` stores and saves; a duplicate number throws `ValidationException` and leaves one account | R3 | new |

Existing tests to run again: all of `mvn test` (`AccountTest`, `TransactionTest`, `BankRepositoryTest`, `CsvBankStorageTest`, `TransactionServiceTest`).

Commands: `mvn test`; manual check without a test (R2, `Main` wiring): in a copy of the project without `data/`, `printf '2\n0\n' | mvn -q exec:java` must print the menu and an empty account list and exit.

## Placeholders and tech debt

| Placeholder | Why | Removed by |
| --- | --- | --- |
| none | `TD-app-foundation-1` is resolved by this task | |

## Size

- Production code: about 230 changed lines in 3 files (`ConsoleMenu` ~190, `Main` ~20, service ~10); new runtime dependencies: none
- Increment budget: fits

## Risks

- A storage failure after a successful in-memory change leaves memory ahead of disk (KI-transaction-operations-2): the menu prints the storage error; fixed later by `crash-safe-save`.
- `Main` stops on a corrupt data file instead of starting empty: protects data, user must fix or move the file.
- Output wording is tested loosely (`contains`), so wording changes do not break tests.
