# Tech debt

Open shortcuts and placeholders. Each item comes from the `tech-debt.md` of a task or from a finding its code review accepted as debt; placeholders carry the same ID in a `TODO(<id>)` comment.

| ID | Location | Description | Removed by | Status |
| --- | --- | --- | --- | --- |
| TD-app-foundation-1 | `src/main/java/ebanking/Main.java` | `main` only prints a line instead of the console menu | cli-menu | resolved in cli-menu |
| TD-app-foundation-F1 | `src/main/java/ebanking/model/Account.java` | `addToHistory` accepts a transaction that does not involve the account; reject it with `ValidationException` and a test | transaction-operations | resolved in transaction-operations |
| TD-app-foundation-F2 | `src/test/java/ebanking/model/TransactionTest.java` | test T5 depends on `BankRepository`; move the type-filter part to `BankRepositoryTest` | any later task touching these tests | open |
| TD-file-persistence-1 | `src/main/java/ebanking/storage/CsvBankStorage.java` (`save`) | Files are written in place; a crash mid-save can leave a truncated file | crash-safe-save | resolved in crash-safe-save |
| TD-file-persistence-2 | `src/main/java/ebanking/storage/CsvBankStorage.java` | Duplicate account or transaction ids are not rejected on load; a lone `\r` inside a field comes back as `\n` | later hardening | open |
| TD-file-persistence-F1 | `src/main/java/ebanking/storage/CsvCodec.java` | An unterminated quote at end of file ends the record silently; reject it with a format error and a test | later hardening | open |
| TD-file-persistence-F2 | `src/main/java/ebanking/model/Account.java` | The restore constructor says "initial balance" for a stored balance; use neutral wording | any later task touching `Account` | open |
| TD-crash-safe-save-1 | `src/main/java/ebanking/storage/CsvBankStorage.java` (`save`) | A crash between the two moves can leave `accounts.csv` and `transactions.csv` out of sync | atomic-pair-save | open |
| TD-crash-safe-save-F1 | `src/test/java/ebanking/storage/CsvBankStorageTest.java` | Fully qualified names instead of imports in T1 to T3 | any later task touching this test | open |
| TD-crash-safe-save-F2 | `src/main/java/ebanking/storage/CsvBankStorage.java` | Package-private constructor does not null-check `mover` | any later task touching `CsvBankStorage` | open |
