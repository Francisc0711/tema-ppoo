# Tech debt

Open shortcuts and placeholders. Each item comes from the `tech-debt.md` of a task or from a finding its code review accepted as debt; placeholders carry the same ID in a `TODO(<id>)` comment.

| ID | Location | Description | Removed by | Status |
| --- | --- | --- | --- | --- |
| TD-app-foundation-1 | `src/main/java/ebanking/Main.java` | `main` only prints a line instead of the console menu | cli-menu | open |
| TD-app-foundation-F1 | `src/main/java/ebanking/model/Account.java` | `addToHistory` accepts a transaction that does not involve the account; reject it with `ValidationException` and a test | transaction-operations | open |
| TD-app-foundation-F2 | `src/test/java/ebanking/model/TransactionTest.java` | test T5 depends on `BankRepository`; move the type-filter part to `BankRepositoryTest` | any later task touching these tests | open |
