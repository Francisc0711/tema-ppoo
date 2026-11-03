---
task: file-persistence
artifact: plan
version: 1               # +1 for every revision
scope_version: 1
answers_critique_round: 0   # the critique round this version answers; 0 for the first version
---

# Plan: Save and load accounts and transactions in CSV files

## Summary

Add restore constructors (stored `id`, `createdAt`) to the entities, a new `ebanking.storage` package with `CsvBankStorage` (explicit `save`/`load` against a directory), a small `CsvCodec` for quoting and splitting records, and a checked `StorageException`. `load()` rebuilds a fresh `BankRepository`, validates every line and rebuilds each account's recent history. `Main` loads `data/` and prints the counts.

## Context

- `BankEntity`: `id` and `createdAt` generated in the only constructor; fields `private final`.
- `Account`: validation inline in the constructor; `addToHistory` keeps the last 10 (circular, D6).
- `Transaction` / subtypes: validation in constructors; `TransferTransaction` checks target ≠ source.
- `BankRepository`: `addAccount` rejects duplicate account numbers; `findAccountById`; `getAccounts()` / `getTransactions()` return copies in insertion order.
- `.gitignore` already ignores `data/` (GIT-4). `Main` has `TODO(TD-app-foundation-1)`, which stays.

## Files

| # | File | Action | Purpose |
| --- | --- | --- | --- |
| 1 | `src/main/java/ebanking/model/BankEntity.java` | modify | add restore constructor |
| 2 | `src/main/java/ebanking/model/Account.java` | modify | restore constructor; move validation to a private static method |
| 3 | `src/main/java/ebanking/model/Transaction.java` | modify | restore constructor; shared private static validation |
| 4 | `src/main/java/ebanking/model/DepositTransaction.java` | modify | restore constructor |
| 5 | `src/main/java/ebanking/model/WithdrawalTransaction.java` | modify | restore constructor |
| 6 | `src/main/java/ebanking/model/TransferTransaction.java` | modify | restore constructor; shared target validation |
| 7 | `src/main/java/ebanking/storage/StorageException.java` | create | checked exception for unreadable or invalid data |
| 8 | `src/main/java/ebanking/storage/CsvCodec.java` | create | format one record; read one record (quoted fields, embedded line breaks) |
| 9 | `src/main/java/ebanking/storage/CsvBankStorage.java` | create | `save(BankRepository)`, `load()` |
| 10 | `src/main/java/ebanking/Main.java` | modify | load `data/`, print counts |
| 11 | `src/test/java/ebanking/storage/CsvBankStorageTest.java` | create | tests T2–T8 |
| 12 | `src/test/java/ebanking/model/AccountTest.java` | modify | test T1, T9 |

## Steps

### Step 1. Restore constructors in the model (R1)

- `BankEntity`: add `protected BankEntity(String id, LocalDateTime createdAt) throws ValidationException`; blank `id` or null `createdAt` → `ValidationException`. The no-arg constructor is unchanged.
- `Account`: extract the four checks into `private static void validate(String accountNumber, String ownerName, BigDecimal balance, AccountType type)`. Add `public Account(String id, LocalDateTime createdAt, String accountNumber, String ownerName, BigDecimal balance, AccountType type)` calling `super(id, createdAt)`, then `validate(...)`, then assigning fields. The existing constructor calls `validate(...)` too; behaviour unchanged.
- `Transaction`: same pattern, `private static void validate(String accountId, BigDecimal amount)`; add `protected Transaction(String id, LocalDateTime createdAt, String accountId, BigDecimal amount)`.
- `DepositTransaction`, `WithdrawalTransaction`: add `public X(String id, LocalDateTime createdAt, String accountId, BigDecimal amount)` delegating to the new super constructor.
- `TransferTransaction`: add `public TransferTransaction(String id, LocalDateTime createdAt, String sourceAccountId, String targetAccountId, BigDecimal amount)`; move the two target checks to `private static void validateTarget(String source, String target)` used by both constructors.
- All fields stay `private` (OOP-1); no setters added. Run `mvn test` — existing tests must pass unchanged.

### Step 2. `StorageException` (R7)

`public class StorageException extends Exception` with constructors `(String message)` and `(String message, Throwable cause)`. Checked, like `ValidationException`, so `Main` must handle it.

### Step 3. `CsvCodec` (R5)

Final utility class, private constructor, package-private, two static methods:

- `String format(List<String> fields)`: a field containing `,`, `"`, `\r` or `\n` is wrapped in quotes with inner `"` doubled; fields are joined with `,`. No trailing newline.
- `List<String> readRecord(BufferedReader reader, int[] lines) throws IOException`: returns `null` at end of file. When a line ends inside an open quote, the next line is appended with `\n`. It adds the physical lines it consumed to `lines[0]`, so the caller knows each record's start line (Step 4).

```java
/** Splits one record; returns null at EOF. lines[0] is increased by the physical lines read. */
static List<String> readRecord(BufferedReader reader, int[] lines) throws IOException {
    String line = reader.readLine();
    if (line == null) return null;
    lines[0]++;
    List<String> fields = new ArrayList<>();
    StringBuilder field = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; ; i++) {
        if (i == line.length()) {
            if (!quoted) break;
            String next = reader.readLine();
            if (next == null) break;          // unterminated quote: field count check fails later
            lines[0]++; field.append('\n'); line = next; i = -1; continue;
        }
        char c = line.charAt(i);
        if (quoted && c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') { field.append('"'); i++; }
        else if (c == '"') quoted = !quoted;
        else if (c == ',' && !quoted) { fields.add(field.toString()); field.setLength(0); }
        else field.append(c);
    }
    fields.add(field.toString());
    return fields;
}
```

The developer may replace `int[] lines` with a small private counter object if preferred; the behaviour (start line of each record known to the caller) is what matters.

### Step 4. `CsvBankStorage` (R2–R7)

- Constructor `CsvBankStorage(Path directory)`; null → `IllegalArgumentException`. Constants `ACCOUNTS_FILE = "accounts.csv"`, `TRANSACTIONS_FILE = "transactions.csv"`, and the two header lists:
  - `id,createdAt,accountNumber,ownerName,balance,type`
  - `id,createdAt,type,accountId,targetAccountId,amount`
- `save(BankRepository repository) throws StorageException`: `Files.createDirectories(directory)`; for each file open `Files.newBufferedWriter(path, UTF_8)` in try-with-resources (OOP-3); write header then one record per entity, each followed by `"\n"`. Amounts with `toPlainString()`, dates with `toString()` (ISO-8601). Target id: `transaction instanceof TransferTransaction t ? t.getTargetAccountId() : ""`. `IOException` → `StorageException("cannot write <file>", e)`.
- `load() throws StorageException`: new `BankRepository`; `readAccounts(repo)` then `readTransactions(repo)`; return `repo`. A file that does not exist (`Files.notExists`) is skipped (R4). `IOException` → `StorageException("cannot read <file>", e)`.
- Each reader: open `Files.newBufferedReader(path, UTF_8)` in try-with-resources; the first record must equal the header, else error on line 1. Loop: `int start = lines[0] + 1; record = CsvCodec.readRecord(reader, lines)`; skip a record that is a single empty field (blank trailing line); else `parse(record)` and wrap every failure with the file name and `start`:

```java
private static StorageException invalid(String file, int line, String reason, Exception cause) {
    return new StorageException(file + " line " + line + ": " + reason, cause);
}
// in readTransactions, per record:
try {
    Transaction t = parseTransaction(fields, repository);
    repository.addTransaction(t);
    repository.findAccountById(t.getAccountId()).orElseThrow().addToHistory(t);
    if (t instanceof TransferTransaction transfer) {
        repository.findAccountById(transfer.getTargetAccountId()).orElseThrow().addToHistory(t);
    }
} catch (ValidationException | DateTimeParseException | IllegalArgumentException e) {
    throw invalid(TRANSACTIONS_FILE, start, e.getMessage(), e);
}
```

  `NumberFormatException` is an `IllegalArgumentException`, so it is covered; `Enum.valueOf` throws `IllegalArgumentException` for an unknown type.
- `parseAccount(fields)`: field count ≠ 6 → `ValidationException("expected 6 fields, found n")`; `new Account(id, LocalDateTime.parse(..), number, owner, new BigDecimal(..), AccountType.valueOf(..))`; `repository.addAccount` (duplicate number fails the load).
- `parseTransaction(fields, repository)`: field count ≠ 6 → `ValidationException`; `accountId` (and `targetAccountId` for TRANSFER) must resolve via `findAccountById`, else `ValidationException("unknown account: <id>")`; a non-empty `targetAccountId` on DEPOSIT/WITHDRAWAL is rejected. `switch (TransactionType.valueOf(fields.get(2)))` creates the right subtype with the restore constructor.
- History in file order (R6) follows from adding to history while reading; `Account` keeps the last 10.
- The whole load fails on the first bad record (R7); no partial repository escapes because `repo` is only returned at the end.

### Step 5. `Main` (R8)

```java
public static void main(String[] args) {
    CsvBankStorage storage = new CsvBankStorage(Path.of("data"));
    try {
        BankRepository repository = storage.load();
        System.out.println("Loaded " + repository.getAccounts().size() + " accounts and "
                + repository.getTransactions().size() + " transactions from data/");
    } catch (StorageException e) {
        System.err.println("Cannot load data: " + e.getMessage());
    }
    // TODO(TD-app-foundation-1): replace with the console menu (cli-menu)
    System.out.println("E-Banking app - console menu coming in cli-menu");
}
```

### Step 6. Tests

Write the tests below; run `mvn test`.

## Interfaces and data

```text
ebanking.storage.CsvBankStorage
  CsvBankStorage(Path directory)
  void save(BankRepository repository) throws StorageException
  BankRepository load() throws StorageException
ebanking.storage.StorageException extends Exception
New public constructors: Account(id, createdAt, accountNumber, ownerName, balance, type),
  DepositTransaction(id, createdAt, accountId, amount), WithdrawalTransaction(same),
  TransferTransaction(id, createdAt, sourceAccountId, targetAccountId, amount)
```

Files (UTF-8, `\n`, header line first): `<dir>/accounts.csv`, `<dir>/transactions.csv`. `targetAccountId` is empty for deposits and withdrawals. No new dependency, no configuration.

## Tests

All storage tests use JUnit `@TempDir` and check state directly (TEST-3, TEST-7).

| ID | File | Checks | Requirement | New / existing |
| --- | --- | --- | --- | --- |
| T1 | `AccountTest` | restore constructor keeps the given `id` and `createdAt` | R1 | new |
| T2 | `CsvBankStorageTest` | two accounts saved → `accounts.csv` has the header and two lines with the six fields | R2 | new |
| T3 | `CsvBankStorageTest` | deposit, withdrawal, transfer saved and loaded → same subtypes, ids, `createdAt`, account ids, target, amounts, same order; accounts equal in id, `createdAt`, number, owner, balance, type | R1, R3 | new |
| T4 | `CsvBankStorageTest` | empty directory → `load()` returns an empty repository | R4 | new |
| T5 | `CsvBankStorageTest` | owner `Popescu, Ion "Junior"` plus a line break round-trips unchanged | R5 | new |
| T6 | `CsvBankStorageTest` | 12 deposits on one account → loaded history has 10 entries, equal to the last 10 ids, oldest first (TEST-6) | R6 | new |
| T7 | `CsvBankStorageTest` | `transactions.csv` with `amount` = `abc` on line 3 → `StorageException` whose message contains `transactions.csv` and `line 3` | R7 | new |
| T8 | `CsvBankStorageTest` | transaction whose `accountId` is not in `accounts.csv` → `StorageException` (data integrity) | R7 | new |
| T9 | `AccountTest` | restore constructor with a blank id → `ValidationException` | R1 | new |
| — | all existing tests in `src/test/java` | unchanged behaviour of normal constructors and repository | — | existing |

Commands: `mvn test` (lint: none). Manual check for R8: with no `data/`, `mvn -q compile exec:java` prints `Loaded 0 accounts and 0 transactions from data/`; after copying the files written by T3 into `data/`, it prints `Loaded 2 accounts and 3 transactions from data/`.

## Placeholders and tech debt

| Placeholder | Why | Removed by |
| --- | --- | --- |
| None new; `Main` still prints instead of a menu (TD-app-foundation-1 stays open) | menu is a later increment | cli-menu |
| Non-atomic writes (a crash mid-save can leave a truncated file) | out of scope | crash-safe-save |

The developer records the non-atomic write as `TD-file-persistence-1` in `work/file-persistence/tech-debt.md` with a `TODO(TD-file-persistence-1)` in `save`.

## Size

- Production code: about 270 changed lines in 10 files (model ≈ 70, `CsvCodec` ≈ 50, `CsvBankStorage` ≈ 130, `StorageException` ≈ 12, `Main` ≈ 10); new runtime dependencies: none
- Increment budget: fits (at the 10-file limit)

## Risks

- Duplicated field assignment between normal and restore constructors: validation is shared in private static methods; only the assignments repeat.
- Embedded line breaks shift line numbers: the record's start line is reported, counted from physical lines.
- `LocalDateTime.toString()` drops trailing zero seconds/nanos: `LocalDateTime.parse` accepts every form it produces, so round-trip equality holds.
- History rebuilt through `addToHistory`, which does not check `involves` (TD-app-foundation-F1): storage only adds a transaction to accounts it involves.

## Changes since the previous version

Not applicable (version 1).
