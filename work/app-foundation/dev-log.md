---
task: app-foundation
artifact: dev-log
plan_version: 1
round: 1                 # +1 for each fix round after a review or a test report
status: done             # in-progress | done
---

# Development log: app-foundation

One entry per increment, in commit order: a plan step, or a fix in a fix round. The code review reads the increments one by one, so each entry says what the reviewer needs to know about its commit.

Before development, the user committed their own edits: `c2a2152` (testing standards rewritten for Java) and `85dd96e` (scope R8: a negative balance throws `ValidationException`, matching the plan's recorded decision).

## Increment 1: plan step "1. Build setup"

- Commit: `3c5d82b` chore(app-foundation): add maven build
- Changes: `pom.xml`: Java 17, junit-jupiter 5.10.2 (test), surefire 3.2.5, exec-maven-plugin 3.1.0 (`ebanking.Main`); `.gitignore`: `target/`; `Main.java`: prints one line, `TODO(TD-app-foundation-1)`
- Decisions: `version` `0.1.0-SNAPSHOT` (the plan gave none); private constructor on `Main` (utility class). The message uses a plain `-` instead of an en dash (critique suggestion)
- Tests: none (build only); `mvn exec:java` prints the line
- Review focus: none
- Deviation from the plan: none

## Increment 2: plan steps "2. Exceptions" and "3. BankEntity and enums"

- Commit: `2fc00d4` feat(app-foundation): add exceptions, base entity and enums
- Changes: `ValidationException.java` (checked), `InsufficientFundsException.java` (unchecked), each with a `(String message)` constructor; `BankEntity.java`: abstract, final `id` (UUID) and `createdAt`, set in a protected constructor, getters only; `AccountType.java`, `TransactionType.java`: plain enums
- Decisions: none
- Tests: none in this commit; covered by T1 (id, createdAt) and T2, T3, T6 (exceptions)
- Review focus: none
- Deviation from the plan: two plan steps in one commit (small, interdependent types; GIT-3 still holds because the build passes)

## Increment 3: plan step "4. Transaction hierarchy"

- Commit: `0b974bc` feat(app-foundation): add transaction hierarchy
- Changes: `Transaction.java`: abstract, validates `accountId` and a positive `amount` before assigning, abstract `getType()`, `involves()`; `DepositTransaction.java`, `WithdrawalTransaction.java`: call `super`, return their constant; `TransferTransaction.java`: `targetAccountId`, rejects null/blank or equal to source, overrides `involves()` to match the target too; `TransactionTest.java`: T6
- Decisions: getters `getAccountId()`, `getAmount()`, `getTargetAccountId()` added (the plan named the fields but no accessors; OOP-1)
- Tests: T6, a zero amount throws `ValidationException`
- Review focus: `TransferTransaction` checks the target after `super(...)`, so a blank source is reported before the target; `involves()` polymorphism
- Deviation from the plan: T5 moved to increment 5 because it filters through `BankRepository.findTransactionsByType` (critique suggestion), which did not exist yet

## Increment 4: plan step "5. Account"

- Commit: `b8e3a48` feat(app-foundation): add account with recent-history buffer
- Changes: `Account.java`: fields, constructor validation (accountNumber, ownerName, type, null or negative balance, all `ValidationException`), getters, `setOwnerName`, circular buffer as in the plan; `AccountTest.java`: T1–T4
- Decisions: `balance` is `final` because nothing changes it yet; `transaction-operations` drops `final` when it adds balance updates. Owner name check shared by the constructor and `setOwnerName` in a private static helper
- Tests: T1 valid account keeps its balance and has an id and creation time; T2 null account number rejected; T3 negative balance rejected; T4 after 11 additions the history has 10 entries, the oldest is the 2nd added and the newest the 11th
- Review focus: buffer index arithmetic in `addToHistory` / `getRecentHistory`; `getRecentHistory` returns a copy
- Deviation from the plan: none

## Increment 5: plan steps "6. BankRepository" and "7. Tests"

- Commit: `1dc164b` feat(app-foundation): add in-memory bank repository
- Changes: `BankRepository.java`: two `ArrayList`s, methods as in the plan's table, defensive copies via `List.copyOf` and new lists; `BankRepositoryTest.java`: T7–T9; `TransactionTest.java`: T5
- Decisions: duplicate check compares `accountNumber` with a linear scan (consistent with `findAccountById`)
- Tests: T5 each subtype reports its type and `findTransactionsByType` returns exactly one per type; T7 added account found by id, unknown id empty; T8 account's deposit and an incoming transfer returned, another account's withdrawal not; T9 duplicate account number rejected and the repository still holds one account
- Review focus: `addAccount` rejects duplicates before adding (data protection)
- Deviation from the plan: Step 7's tests were written with the steps they cover, not in one final commit (the plan allows splitting, GIT-3)

## Commands

| Command | Result |
| --- | --- |
| install: `mvn clean install -DskipTests` | success |
| test: `mvn test` | 9 passed, 0 failed |
| lint: `none` | skipped (AGENTS.md) |
| start: `mvn exec:java` | prints `E-Banking app - console menu coming in cli-menu` |

## Notes for testing

- Needs JDK 17 and Maven 3.8+; tested with OpenJDK 17.0.20.1 and Maven 3.10.0.
- No UI yet: the domain layer is exercised through the JUnit tests only; `mvn exec:java` prints a placeholder line.
- Size: 13 production Java files, 239 non-blank lines excluding package and import lines (338 raw).
